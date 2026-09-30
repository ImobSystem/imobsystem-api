package com.system.imob.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.ClienteRequestDTO;
import com.system.imob.dtos.requests.ImovelRequestDTO;
import com.system.imob.dtos.responses.ChatResponseDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.enums.Finalidade;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.StatusImovel;
import com.system.imob.enums.TipoCliente;
import com.system.imob.models.Cliente;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imovel;
import com.system.imob.models.Negociacao;
import com.system.imob.repositories.ClienteRepository;
import com.system.imob.repositories.ImovelRepository;
import com.system.imob.repositories.NegociacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ChatService {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.model}")
    private String model;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ImovelRepository imovelRepository;

    @Autowired
    private ImovelService imovelService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private NegociacaoRepository negociacaoRepository;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponseDTO processarMensagem(String mensagem) {
        if (mensagem == null || mensagem.isBlank()) {
            throw new RuntimeException("A mensagem não pode ser vazia");
        }

        Corretor logado = authUtil.getCorretorLogado();

        String contexto = montarContexto(logado);
        String systemPrompt = montarSystemPrompt(logado, contexto);
        String respostaIA = chamarClaude(systemPrompt, mensagem);

        return interpretarResposta(respostaIA);
    }

    // Monta um resumo dos dados reais que o corretor logado enxerga, pra IA responder com base neles
    private String montarContexto(Corretor logado) {
        List<Imovel> imoveis;
        List<Cliente> clientes;
        List<Negociacao> negociacoes;

        if (logado.getPerfil() == PerfilUsuario.ADMIN) {
            Long imobId = logado.getImobiliaria().getId();
            imoveis = imovelRepository.findByImobiliariaId(imobId);
            clientes = clienteRepository.findByImobiliariaId(imobId);
            negociacoes = negociacaoRepository.findByImovelImobiliariaId(imobId);
        } else {
            imoveis = imovelRepository.findByCorretorId(logado.getId());
            clientes = clienteRepository.findByCorretorId(logado.getId());
            negociacoes = negociacaoRepository.findByCorretorId(logado.getId());
        }

        StringBuilder sb = new StringBuilder();

        sb.append("IMÓVEIS (").append(imoveis.size()).append("):\n");
        for (Imovel i : imoveis) {
            sb.append("- ID:").append(i.getId())
                    .append(" | ").append(i.getEndereco())
                    .append(" | ").append(i.getArea_m2()).append("m²")
                    .append(" | ").append(i.getFinalidade())
                    .append(" | ").append(i.getStatusImovel())
                    .append(" | Corretor:").append(nomeDoCorretor(i.getCorretor()))
                    .append("\n");
        }

        sb.append("\nCLIENTES (").append(clientes.size()).append("):\n");
        for (Cliente c : clientes) {
            sb.append("- ID:").append(c.getId())
                    .append(" | ").append(c.getNome())
                    .append(" | ").append(c.getEmail())
                    .append(" | ").append(c.getTipoCliente())
                    .append(" | Corretor:").append(nomeDoCorretor(c.getCorretor()))
                    .append("\n");
        }

        sb.append("\nNEGOCIAÇÕES (").append(negociacoes.size()).append("):\n");
        for (Negociacao n : negociacoes) {
            sb.append("- ID:").append(n.getId())
                    .append(" | Imóvel:").append(n.getImovel() != null ? n.getImovel().getEndereco() : "-")
                    .append(" | Cliente:").append(n.getCliente() != null ? n.getCliente().getNome() : "-")
                    .append(" | ").append(n.getStatusNegocio())
                    .append(" | R$").append(n.getValor())
                    .append(" | Corretor:").append(nomeDoCorretor(n.getCorretor()))
                    .append("\n");
        }

        return sb.toString();
    }

    private String nomeDoCorretor(Corretor corretor) {
        return corretor != null ? corretor.getNome() : "-";
    }

    private String montarSystemPrompt(Corretor logado, String contexto) {
        String perfil = logado.getPerfil().name();
        String nome = logado.getNome();

        return """
                Você é o Imo, o assistente inteligente do ImobSystem.
                Você está conversando com %s, que é %s da imobiliária.

                DADOS ATUAIS DO SISTEMA:
                %s

                SUAS CAPACIDADES:
                1. CADASTRAR IMÓVEL: quando o usuário pedir pra cadastrar um imóvel, extraia os dados da mensagem e responda EXATAMENTE neste formato JSON (e NADA mais):
                   {"acao": "CADASTRAR_IMOVEL", "dados": {"endereco": "...", "CEP": "...", "area_m2": 0.0, "finalidade": "VENDA ou ALUGUEL", "statusImovel": "DISPONIVEL"}}

                2. CADASTRAR CLIENTE: quando pedir pra cadastrar um cliente:
                   {"acao": "CADASTRAR_CLIENTE", "dados": {"nome": "...", "cpf": "...", "email": "...", "telefone": "...", "tipoCliente": "COMPRADOR ou LOCATARIO ou PROPRIETARIO"}}

                3. RESPONDER PERGUNTAS: sobre os dados do sistema (quantos imóveis, quais clientes, status das negociações, etc.) — responda em linguagem natural com base nos dados acima.

                4. INSIGHTS (se o usuário for ADMIN): analise os dados e ofereça sugestões estratégicas quando perguntado (ex: corretores com melhor performance, imóveis parados, tendências).

                REGRAS:
                - Se o usuário pedir pra cadastrar algo, SEMPRE responda com o JSON da ação. Não misture texto com JSON e não use blocos de código.
                - Se faltar um dado obrigatório pra cadastrar (ex: endereço sem CEP), PERGUNTE o que falta em vez de inventar.
                - Campos obrigatórios pra imóvel: endereco, CEP, area_m2, finalidade (VENDA/ALUGUEL), statusImovel (DISPONIVEL/NEGOCIANDO/FECHADO)
                - Campos obrigatórios pra cliente: nome, cpf, email, telefone, tipoCliente (COMPRADOR/LOCATARIO/PROPRIETARIO)
                - Se não for um cadastro nem pergunta sobre dados, responda normalmente como assistente prestativo.
                - Responda sempre em português do Brasil, tom profissional mas amigável.
                - Seja conciso — respostas curtas e diretas.
                """.formatted(nome, perfil, contexto);
    }

    private String chamarClaude(String systemPrompt, String mensagemUsuario) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", ANTHROPIC_VERSION);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("max_tokens", 1024);
        body.put("system", systemPrompt);
        body.put("messages", List.of(Map.of("role", "user", "content", mensagemUsuario)));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        Map<String, Object> resposta;
        try {
            resposta = restTemplate.exchange(API_URL, HttpMethod.POST, request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();
        } catch (RestClientException e) {
            throw new RuntimeException("Erro ao comunicar com a IA: " + e.getMessage());
        }

        if (resposta == null) {
            throw new RuntimeException("Erro ao comunicar com a IA: resposta vazia");
        }

        // A resposta vem numa lista de blocos; pega o primeiro bloco de texto
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) resposta.get("content");
        if (content == null) {
            throw new RuntimeException("Erro ao comunicar com a IA: formato de resposta inesperado");
        }

        return content.stream()
                .filter(bloco -> "text".equals(bloco.get("type")))
                .map(bloco -> (String) bloco.get("text"))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("A IA não retornou nenhum texto"));
    }

    // Se a IA devolveu o JSON de uma ação, executa o cadastro; senão devolve o texto puro
    private ChatResponseDTO interpretarResposta(String respostaIA) {
        Map<String, Object> acaoJson = lerAcao(respostaIA);

        if (acaoJson == null) {
            return new ChatResponseDTO(respostaIA, null, null);
        }

        String acao = (String) acaoJson.get("acao");

        @SuppressWarnings("unchecked")
        Map<String, Object> dados = (Map<String, Object>) acaoJson.get("dados");

        if ("CADASTRAR_IMOVEL".equals(acao)) {
            ImovelRequestDTO dto = new ImovelRequestDTO(
                    texto(dados, "endereco"),
                    texto(dados, "CEP"),
                    numero(dados, "area_m2"),
                    valorDeEnum(Finalidade.class, dados, "finalidade"),
                    valorDeEnum(StatusImovel.class, dados, "statusImovel"),
                    // o prompt da IA só coleta os cinco campos básicos; tipo, valor,
                    // quartos, localização e publicação ficam pra edição do imóvel
                    null, null, null, null, null,
                    null, null, null, null, null
            );
            ImovelResponseDTO criado = imovelService.cadastrarImovel(dto);
            return new ChatResponseDTO(
                    "Imóvel cadastrado com sucesso! Endereço: " + criado.endereco(),
                    "IMOVEL_CRIADO",
                    criado
            );
        }

        if ("CADASTRAR_CLIENTE".equals(acao)) {
            ClienteRequestDTO dto = new ClienteRequestDTO(
                    texto(dados, "nome"),
                    texto(dados, "cpf"),
                    texto(dados, "email"),
                    texto(dados, "telefone"),
                    valorDeEnum(TipoCliente.class, dados, "tipoCliente")
            );
            ClienteResponseDTO criado = clienteService.cadastrarCliente(dto);
            return new ChatResponseDTO(
                    "Cliente cadastrado com sucesso! Nome: " + criado.nome(),
                    "CLIENTE_CRIADO",
                    criado
            );
        }

        // JSON válido mas ação desconhecida — trata como texto normal
        return new ChatResponseDTO(respostaIA, null, null);
    }

    // Retorna o JSON da ação, ou null se a resposta for texto comum
    private Map<String, Object> lerAcao(String respostaIA) {
        String limpa = removerBlocoDeCodigo(respostaIA.trim());

        if (!limpa.startsWith("{")) {
            return null;
        }

        try {
            Map<String, Object> json = objectMapper.readValue(limpa, new TypeReference<Map<String, Object>>() {
            });
            boolean temAcao = json.get("acao") instanceof String && json.get("dados") instanceof Map;
            return temAcao ? json : null;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // Às vezes a IA embrulha o JSON em ```json ... ```
    private String removerBlocoDeCodigo(String resposta) {
        if (!resposta.startsWith("```")) {
            return resposta;
        }
        int inicio = resposta.indexOf('\n');
        int fim = resposta.lastIndexOf("```");
        if (inicio < 0 || fim <= inicio) {
            return resposta;
        }
        return resposta.substring(inicio + 1, fim).trim();
    }

    private String texto(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        if (valor == null || valor.toString().isBlank()) {
            throw new RuntimeException("A IA não informou o campo obrigatório: " + campo);
        }
        return valor.toString().trim();
    }

    private Double numero(Map<String, Object> dados, String campo) {
        Object valor = dados.get(campo);
        if (valor instanceof Number numero) {
            return numero.doubleValue();
        }
        if (valor != null) {
            try {
                return Double.parseDouble(valor.toString().trim().replace(",", "."));
            } catch (NumberFormatException ignorado) {
                // cai no erro abaixo
            }
        }
        throw new RuntimeException("A IA não informou um número válido para o campo: " + campo);
    }

    private <E extends Enum<E>> E valorDeEnum(Class<E> tipo, Map<String, Object> dados, String campo) {
        String valor = texto(dados, campo).toUpperCase();
        try {
            return Enum.valueOf(tipo, valor);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Valor inválido para o campo " + campo + ": " + valor);
        }
    }
}
