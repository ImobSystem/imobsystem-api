package com.system.imob.config;

import com.system.imob.models.Corretor;
import com.system.imob.repositories.CorretorRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CorretorRepository corretorRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Se não tem header ou não começa com "Bearer ", segue sem autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Remove o prefixo "Bearer " e fica só com o token
        String token = authHeader.substring(7);

        if (jwtUtil.tokenValido(token)) {
            String email = jwtUtil.extrairEmail(token);
            Corretor corretor = corretorRepository.findByEmail(email).orElse(null);

            if (corretor != null) {
                // Cria a autenticação com o perfil do corretor
                var authority = new SimpleGrantedAuthority("ROLE_" + corretor.getPerfil().name());
                var auth = new UsernamePasswordAuthenticationToken(
                        corretor, null, List.of(authority));

                // Registra o corretor como "logado" para esta requisição
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}