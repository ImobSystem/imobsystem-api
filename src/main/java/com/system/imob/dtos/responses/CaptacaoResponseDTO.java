package com.system.imob.dtos.responses;

public record CaptacaoResponseDTO(
        Long corretorId,
        String nomeCorretor,
        Long totalCaptacoes
) {}