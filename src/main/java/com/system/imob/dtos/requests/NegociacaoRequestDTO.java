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
        Long imobiliariaId  // TODO: após JWT, remover — virá do token
) {}
