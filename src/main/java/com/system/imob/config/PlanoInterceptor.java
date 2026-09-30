package com.system.imob.config;

import com.system.imob.enums.StatusPlano;
import com.system.imob.models.Corretor;
import com.system.imob.models.Imobiliaria;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;

@Component
public class PlanoInterceptor implements HandlerInterceptor {

    // 402 e não 403: o frontend precisa distinguir "plano expirado" (renovar)
    // de "sem permissão" (AccessDeniedException), que pedem telas diferentes
    private static final String CORPO_PLANO_EXPIRADO =
            "{\"error\":\"PLANO_EXPIRADO\",\"message\":\"Seu plano expirou. Renove para continuar usando o sistema.\"}";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (rotaLiberada(request)) {
            return true;
        }

        // O JwtAuthFilter já carregou o Corretor do banco e o colocou como principal,
        // então não precisa de uma segunda consulta aqui
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Corretor corretor)) {
            return true; // anônimo ou principal inesperado — quem barra é o Spring Security
        }

        Imobiliaria imobiliaria = corretor.getImobiliaria();
        boolean planoValido = imobiliaria != null
                && imobiliaria.getStatusPlano() == StatusPlano.ATIVO
                && imobiliaria.getDataVencimento() != null
                && !imobiliaria.getDataVencimento().isBefore(LocalDate.now());

        if (!planoValido) {
            response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(CORPO_PLANO_EXPIRADO);
            return false;
        }

        return true;
    }

    // Rotas que passam mesmo com o plano vencido
    private boolean rotaLiberada(HttpServletRequest request) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true; // preflight do CORS
        }

        String path = request.getRequestURI();

        return path.startsWith("/auth/")              // login e registro, senão ninguém entra
                || path.startsWith("/webhooks/")      // o Asaas precisa avisar sobre pagamentos
                || path.startsWith("/imobiliarias/plano") // ver e assinar plano tem que funcionar expirado
                || path.equals("/imobiliarias/minha")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/api-docs")       // caminho real configurado em springdoc.api-docs.path
                || path.equals("/error");
    }
}
