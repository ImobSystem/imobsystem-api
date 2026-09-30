package com.system.imob.dtos.responses;

public record StatusPlanoResponseDTO(
        String plano,
        String statusPlano,
        String dataVencimento,
        long diasRestantes
) {
}
