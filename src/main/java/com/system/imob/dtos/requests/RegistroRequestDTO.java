package com.system.imob.dtos.requests;

public record RegistroRequestDTO(
        // dados da imobiliária
        String nomeImobiliaria,
        String cnpj,
        String emailImobiliaria,
        String telefone,
        // dados do admin
        String nomeAdmin,
        String emailAdmin,
        String senha,
        String creci
) {
}
