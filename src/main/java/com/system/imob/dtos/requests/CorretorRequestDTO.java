package com.system.imob.dtos.requests;

import com.system.imob.enums.PerfilUsuario;

public record CorretorRequestDTO(
        String nome,
        String email,
        String senha,
        String creci,
        PerfilUsuario perfil,
        Long imobiliariaId
) {
}
