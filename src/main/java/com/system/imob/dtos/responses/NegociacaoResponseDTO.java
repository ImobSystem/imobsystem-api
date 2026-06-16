package com.system.imob.dtos.responses;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusNegocio;

import java.time.LocalDate;

public record NegociacaoResponseDTO(
        Long id,
        Finalidade finalidade,
        StatusNegocio statusNegocio,
        LocalDate dataInicio,
        LocalDate dataFim,
        Double valor,
        String motivoPerda,
        LocalDate dataUltimaInteracao,
        Long imovelId,
        Long clienteId,
        Long corretorId
) {}
