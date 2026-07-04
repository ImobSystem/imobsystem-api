package com.system.imob.dtos.requests;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusNegocio;

import java.time.LocalDate;

public record NegociacaoRequestDTO(
        Finalidade finalidade,
        StatusNegocio statusNegocio,
        LocalDate dataInicio,
        LocalDate dataFim,
        Double valor,
        Long imovelId,
        Long clienteId,
        Long corretorId, // ignorado — o corretor vem do token
        String motivoPerda, // opcional
        Long imobiliariaId  // ignorado — a imobiliária vem do imóvel
) {}
