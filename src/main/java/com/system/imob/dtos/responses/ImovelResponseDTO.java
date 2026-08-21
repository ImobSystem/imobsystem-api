package com.system.imob.dtos.responses;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;

import java.util.List;

public record ImovelResponseDTO(
        Long id,
        String endereco,
        String CEP,
        Double area_m2,
        Finalidade finalidade,
        StatusImovel statusImovel,
        Long imobiliariaId,
        List<String>fotos
) {
}
