package com.system.imob.dtos.responses;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        Integer telefone
) {
}
