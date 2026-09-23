package com.system.imob.dtos.responses;

public record AssinarPlanoResponseDTO(
        String plano,
        String status,
        String linkPagamento, // URL do Asaas onde o cliente paga
        String proximoVencimento
) {
}
