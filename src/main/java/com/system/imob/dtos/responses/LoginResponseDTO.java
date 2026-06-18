package com.system.imob.dtos.responses;

import com.system.imob.enums.PerfilUsuario;

public record LoginResponseDTO(
        String token,
        String email,
        PerfilUsuario perfil
) {
}
