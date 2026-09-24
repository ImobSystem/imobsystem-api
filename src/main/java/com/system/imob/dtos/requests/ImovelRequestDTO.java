package com.system.imob.dtos.requests;

import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import com.system.imob.enums.TipoImovel;

public record ImovelRequestDTO(
        String endereco,
        String CEP,
        Double area_m2,
        Finalidade finalidade,
        StatusImovel statusImovel,
        // campos opcionais, usados principalmente pelo feed dos portais
        TipoImovel tipoImovel,
        Double valor,
        Integer quartos,
        Integer banheiros,
        Integer vagasGaragem,
        String bairro,
        String cidade,
        String estado,
        String descricao,
        Boolean publicarPortais
) {
}
