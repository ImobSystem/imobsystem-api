package com.system.imob.dtos.responses;

import com.system.imob.enums.PerfilUsuario;

public record CorretorResponseDTO(
        Long id,
        String nome,
        String email,
        String creci,
        PerfilUsuario perfil,
        Long imobiliariaId
) {
}
