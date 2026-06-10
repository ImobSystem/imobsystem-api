package com.system.imob.dtos.requests;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;

public record ImovelRequestDTO(
        String endereco,
        String CEP,
        Double area_m2,
        Finalidade finalidade,
        StatusImovel statusImovel,
        Long imobiliariaId
) {
}
