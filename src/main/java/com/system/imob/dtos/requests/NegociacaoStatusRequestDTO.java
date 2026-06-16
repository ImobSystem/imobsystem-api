package com.system.imob.dtos.requests;

import com.system.imob.enums.StatusNegocio;

public record NegociacaoStatusRequestDTO(
        StatusNegocio statusNegocio,
        String motivoPerda // opcional — informado quando o status for PERDIDO
) {}
