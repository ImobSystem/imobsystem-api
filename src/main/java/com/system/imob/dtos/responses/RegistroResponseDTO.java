package com.system.imob.dtos.responses;

public record RegistroResponseDTO(
        String token,
        String email,
        String perfil,
        Long imobiliariaId
) {
}
