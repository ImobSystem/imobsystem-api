package com.system.imob.dtos.responses;

import com.system.imob.enums.TipoCliente;

public record ClienteResponseDTO(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        TipoCliente tipoCliente) {
}
