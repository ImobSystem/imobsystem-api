package com.system.imob.config;

import com.system.imob.models.Corretor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    // Retorna o corretor autenticado na requisição atual
    public Corretor getCorretorLogado() {
        return (Corretor) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    // Atalho para pegar direto o id da imobiliária dele
    public Long getImobiliariaId() {
        return getCorretorLogado().getImobiliaria().getId();
    }
}