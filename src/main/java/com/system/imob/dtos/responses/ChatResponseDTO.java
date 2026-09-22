package com.system.imob.dtos.responses;

public record ChatResponseDTO(
        String resposta,
        String acao,        // null se só respondeu texto; "IMOVEL_CRIADO" ou "CLIENTE_CRIADO" se criou algo
        Object dadosCriados // null ou o DTO do que foi criado (ImovelResponseDTO, ClienteResponseDTO)
) {
}
