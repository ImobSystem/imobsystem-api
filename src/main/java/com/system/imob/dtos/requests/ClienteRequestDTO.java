package com.system.imob.dtos.requests;

import com.system.imob.enums.TipoCliente;

public record ClienteRequestDTO(
        String nome,
        String cpf,
        String email,
        String telefone,
        TipoCliente tipoCliente
) {
}
