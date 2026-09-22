package com.system.imob.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.imob.enums.TipoPlano;
import com.system.imob.models.Imobiliaria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AsaasService {

    private static final Logger log = LoggerFactory.getLogger(AsaasService.class);

    // Chamada de rede dentro do fluxo de registro (que é @Transactional): sem timeout,
    // uma indisponibilidade do Asaas seguraria a conexão do banco indefinidamente.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(15);

    private static final int DIAS_TRIAL = 30;

    @Value("${asaas.api.key}")
    private String apiKey;

    @Value("${asaas.api.url}")
    private String asaasUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AsaasService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(CONNECT_TIMEOUT);
        factory.setReadTimeout(READ_TIMEOUT);
        this.restTemplate = new RestTemplate(factory);
    }

    // Cria no Asaas o cliente correspondente à imobiliária e devolve o id dele (ex: "cus_xxxxxxxxxxxx")
    public String criarClienteAsaas(Imobiliaria imobiliaria) {
        if (imobiliaria.getCnpj() == null || imobiliaria.getCnpj().isBlank()) {
            throw new RuntimeException("A imobiliária precisa de um CNPJ para ser cadastrada no Asaas");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("name", imobiliaria.getNome());
        body.put("cpfCnpj", apenasDigitos(imobiliaria.getCnpj()));
        body.put("email", imobiliaria.getEmail());
        body.put("phone", apenasDigitos(imobiliaria.getTelefone()));
        body.put("externalReference", imobiliaria.getId().toString());

        Map<String, Object> resposta = chamar(HttpMethod.POST, "/customers", body);

        String customerId = (String) resposta.get("id");
        if (customerId == null) {
            throw new RuntimeException("O Asaas não retornou o id do cliente");
        }
        return customerId;
    }

    // Cria a assinatura recorrente. A primeira cobrança só cai em +30 dias — é assim que o trial funciona.
    public Map<String, Object> criarAssinatura(Imobiliaria imobiliaria, TipoPlano plano) {
        Map<String, Object> body = new HashMap<>();
        body.put("customer", imobiliaria.getAsaasCustomerId());
        body.put("billingType", "UNDEFINED"); // deixa o cliente escolher PIX, boleto ou cartão
        body.put("value", valorDoPlano(plano));
        body.put("nextDueDate", LocalDate.now().plusDays(DIAS_TRIAL).toString());
        body.put("cycle", "MONTHLY");
        body.put("description", "Assinatura Kaza System - Plano " + plano.name());
        body.put("externalReference", imobiliaria.getId().toString());

        return chamar(HttpMethod.POST, "/subscriptions", body);
    }

    // O invoiceUrl fica na cobrança, não na assinatura — o objeto subscription não tem esse campo
    public String buscarLinkPagamento(String assinaturaId) {
        Map<String, Object> resposta = chamar(HttpMethod.GET,
                "/subscriptions/" + assinaturaId + "/payments", null);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> pagamentos = (List<Map<String, Object>>) resposta.get("data");

        String link = pagamentos == null || pagamentos.isEmpty()
                ? null
                : (String) pagamentos.get(0).get("invoiceUrl");

        if (link == null || link.isBlank()) {
            // Repetir a assinatura é seguro: assinarPlano cancela a anterior antes de criar outra
            throw new RuntimeException("A assinatura foi criada, mas o Asaas ainda não gerou o link "
                    + "de pagamento. Tente novamente em instantes.");
        }
        return link;
    }

    // Usado na troca de plano. Uma assinatura que já não existe no Asaas não é erro —
    // mas qualquer outra falha precisa subir, senão a imobiliária fica com duas cobranças mensais.
    public void cancelarAssinatura(String assinaturaId) {
        try {
            executar(HttpMethod.DELETE, "/subscriptions/" + assinaturaId, null);
            log.info("Assinatura {} cancelada no Asaas", assinaturaId);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Assinatura {} não existe mais no Asaas; seguindo com a troca de plano", assinaturaId);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Não foi possível cancelar a assinatura anterior no Asaas: "
                    + extrairMensagemDeErro(e));
        } catch (RestClientException e) {
            throw new RuntimeException("Não foi possível cancelar a assinatura anterior no Asaas: "
                    + e.getMessage());
        }
    }

    public double valorDoPlano(TipoPlano plano) {
        return switch (plano) {
            case BASICO -> 99.90;
            case PROFISSIONAL -> 199.90;
            case PREMIUM -> 299.90;
        };
    }

    // Todas as chamadas pro Asaas passam por aqui: traduz o erro numa mensagem legível
    private Map<String, Object> chamar(HttpMethod metodo, String caminho, Map<String, Object> body) {
        Map<String, Object> resposta;
        try {
            resposta = executar(metodo, caminho, body);
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("Erro no Asaas: " + extrairMensagemDeErro(e));
        } catch (RestClientException e) {
            throw new RuntimeException("Erro ao comunicar com o Asaas: " + e.getMessage());
        }

        if (resposta == null) {
            throw new RuntimeException("Erro ao comunicar com o Asaas: resposta vazia");
        }
        return resposta;
    }

    // Chamada crua, com header de auth e timeout. Deixa a exceção do RestTemplate subir
    // pra quem chama poder distinguir os status (o cancelamento trata 404 de forma diferente).
    private Map<String, Object> executar(HttpMethod metodo, String caminho, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("access_token", apiKey);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        return restTemplate.exchange(asaasUrl + caminho, metodo, request,
                new ParameterizedTypeReference<Map<String, Object>>() {
                }).getBody();
    }

    // O Asaas devolve {"errors":[{"code":"...","description":"..."}]} — sem isso o usuário só veria "400 Bad Request"
    private String extrairMensagemDeErro(HttpStatusCodeException e) {
        try {
            JsonNode erros = objectMapper.readTree(e.getResponseBodyAsString()).get("errors");
            if (erros != null && erros.isArray() && !erros.isEmpty()) {
                List<String> descricoes = erros.findValuesAsText("description");
                if (!descricoes.isEmpty()) {
                    return String.join("; ", descricoes);
                }
            }
        } catch (Exception ignorado) {
            // corpo não é o JSON de erro esperado — cai no fallback abaixo
        }
        return e.getStatusCode().toString();
    }

    private String apenasDigitos(String valor) {
        return valor == null ? null : valor.replaceAll("[^0-9]", "");
    }
}
