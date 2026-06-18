package com.system.imob.dtos.requests;

import com.system.imob.enums.StatusPlano;
import com.system.imob.enums.TipoPlano;

import java.time.LocalDate;

public record ImobiliariaRequestDTO(
        String nome,
        String cnpj,
        String email,
        String telefone,
        StatusPlano statusPlano,
        TipoPlano plano,
        LocalDate dataVencimento
) {
}
