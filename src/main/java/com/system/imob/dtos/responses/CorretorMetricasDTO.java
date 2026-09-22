package com.system.imob.dtos.responses;

public record CorretorMetricasDTO(
        Long corretorId,
        String nomeCorretor,
        Long totalImoveis,
        Long totalClientes,
        Long totalNegociacoes,
        Long negociacoesGanhas
) {}
