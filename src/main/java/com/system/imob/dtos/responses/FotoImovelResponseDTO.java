package com.system.imob.dtos.responses;

public record FotoImovelResponseDTO(
        Long id,
        String url,
        Long imovelId
) {}