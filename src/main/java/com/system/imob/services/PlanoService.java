package com.system.imob.services;

import com.system.imob.config.AuthUtil;
import com.system.imob.dtos.requests.AssinarPlanoRequestDTO;
import com.system.imob.dtos.responses.AssinarPlanoResponseDTO;
import com.system.imob.dtos.responses.StatusPlanoResponseDTO;
import com.system.imob.enums.PerfilUsuario;
import com.system.imob.enums.StatusPlano;
import com.system.imob.enums.TipoPlano;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imobiliaria;
import com.system.imob.repositories.ImobiliariaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class PlanoService {

    private static final Logger log = LoggerFactory.getLogger(PlanoService.class);

    private static final int DIAS_CICLO = 30;

    @Value("${asaas.webhook.token}")
    private String webhookToken;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    @Autowired
    private AsaasService asaasService;

    public AssinarPlanoResponseDTO assinarPlano(AssinarPlanoRequestDTO dto) {
        Corretor logado = authUtil.getCorretorLogado();
        if (logado.getPerfil() != PerfilUsuario.ADMIN) {
            throw new AccessDeniedException("Apenas ADMIN pode gerenciar o plano");
        }

        TipoPlano plano = lerPlano(dto.plano());

        Imobiliaria imob = imobiliariaRepository.findById(logado.getImobiliaria().getId())
                .orElseThrow(() -> new RuntimeException("Imobiliária não encontrada"));

        // Troca de plano: cancela a assinatura anterior antes de abrir a nova, senão a
        // imobiliária ficaria com duas cobranças mensais rodando em paralelo no Asaas
        if (imob.getAsaasSubscriptionId() != null) {
            asaasService.cancelarAssinatura(imob.getAsaasSubscriptionId());
        }

        if (imob.getAsaasCustomerId() == null) {
            imob.setAsaasCustomerId(asaasService.criarClienteAsaas(imob));
        }

        Map<String, Object> assinatura = asaasService.criarAssinatura(imob, plano);

        String subscriptionId = (String) assinatura.get("id");
        if (subscriptionId == null) {
            throw new RuntimeException("O Asaas não retornou o id da assinatura");
        }

        imob.setAsaasSubscriptionId(subscriptionId);
        imob.setPlano(plano);
        imob.setStatusPlano(StatusPlano.ATIVO);
        imob.setDataVencimento(LocalDate.now().plusDays(DIAS_CICLO));
        imobiliariaRepository.save(imob);

        String linkPagamento = asaasService.buscarLinkPagamento(subscriptionId);

        return new AssinarPlanoResponseDTO(
                plano.name(),
                StatusPlano.ATIVO.name(),
                linkPagamento,
                imob.getDataVencimento().toString());
    }

    public StatusPlanoResponseDTO buscarStatusPlano() {
        Corretor logado = authUtil.getCorretorLogado();
        Imobiliaria imob = imobiliariaRepository.findById(logado.getImobiliaria().getId())
                .orElseThrow(() -> new RuntimeException("Imobiliária não encontrada"));

        LocalDate vencimento = imob.getDataVencimento();
        long diasRestantes = vencimento == null
                ? 0
                : Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), vencimento));

        return new StatusPlanoResponseDTO(
                imob.getPlano() != null ? imob.getPlano().name() : null,
                imob.getStatusPlano() != null ? imob.getStatusPlano().name() : null,
                vencimento != null ? vencimento.toString() : null,
                diasRestantes);
    }

    // Chamado pela rota pública /webhooks/asaas — por isso valida o token antes de mexer em qualquer plano
    public void processarWebhook(String tokenRecebido, Map<String, Object> payload) {
        validarTokenWebhook(tokenRecebido);

        if (payload == null) {
            return;
        }

        String evento = (String) payload.get("event");

        @SuppressWarnings("unchecked")
        Map<String, Object> payment = (Map<String, Object>) payload.get("payment");

        if (evento == null || payment == null) {
            return;
        }

        String asaasCustomerId = (String) payment.get("customer");
        if (asaasCustomerId == null) {
            return;
        }

        Imobiliaria imob = imobiliariaRepository.findByAsaasCustomerId(asaasCustomerId).orElse(null);
        if (imob == null) {
            log.warn("Webhook Asaas ignorado: nenhuma imobiliária com asaasCustomerId {}", asaasCustomerId);
            return;
        }

        switch (evento) {
            case "PAYMENT_CONFIRMED", "PAYMENT_RECEIVED" -> {
                imob.setStatusPlano(StatusPlano.ATIVO);
                imob.setDataVencimento(LocalDate.now().plusDays(DIAS_CICLO));
            }
            case "PAYMENT_OVERDUE" -> imob.setStatusPlano(StatusPlano.INADIMPLENTE);
            default -> {
                // PAYMENT_DELETED, PAYMENT_REFUNDED e os demais eventos não mudam o status por enquanto
                log.info("Webhook Asaas recebido sem tratamento: {}", evento);
                return;
            }
        }

        imobiliariaRepository.save(imob);
        log.info("Webhook Asaas {} aplicado à imobiliária {}", evento, imob.getId());
    }

    // O Asaas manda o token configurado no painel dele no header de cada webhook.
    // Sem essa checagem, qualquer um consegue renovar o próprio plano mandando um POST na rota.
    private void validarTokenWebhook(String tokenRecebido) {
        if (webhookToken == null || webhookToken.isBlank()) {
            log.warn("asaas.webhook.token não configurado — webhook aceito sem validação. NÃO USE ASSIM EM PRODUÇÃO.");
            return;
        }
        if (!webhookToken.equals(tokenRecebido)) {
            throw new AccessDeniedException("Token do webhook inválido");
        }
    }

    private TipoPlano lerPlano(String plano) {
        if (plano == null || plano.isBlank()) {
            throw new RuntimeException("Informe o plano: BASICO, PROFISSIONAL ou PREMIUM");
        }
        try {
            return TipoPlano.valueOf(plano.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Plano inválido: " + plano + ". Use BASICO, PROFISSIONAL ou PREMIUM");
        }
    }
}
