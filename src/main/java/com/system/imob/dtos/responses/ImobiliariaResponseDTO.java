package com.system.imob.dtos.responses;

import com.system.imob.enums.StatusPlano;
import com.system.imob.enums.TipoPlano;

import java.time.LocalDate;

public record ImobiliariaResponseDTO(
        Long id,
        String nome,
        String cnpj,
        String email,
        String telefone,
        StatusPlano statusPlano,
        TipoPlano plano,
        LocalDate dataVencimento,
        String logoBase64
) {}