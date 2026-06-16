package com.system.imob.config;

import com.system.imob.enums.StatusPlano;
import com.system.imob.models.Imobiliaria;
import com.system.imob.repositories.ImobiliariaRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;

@Component
public class PlanoInterceptor implements HandlerInterceptor {
    @Autowired
    private ImobiliariaRepository imobiliariaRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // TODO: após JWT, ler imobiliariaId do token em vez do header
        String header = request.getHeader("X-Imobiliaria-Id");

        if (header == null || header.isBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Header X-Imobiliaria-Id é obrigatório");
            return false;
        }

        Long imobiliariaId = Long.parseLong(header);

        Imobiliaria imobiliaria = imobiliariaRepository.findById(imobiliariaId).orElse(null);
        if (imobiliaria == null) {
            response.sendError(HttpStatus.NOT_FOUND.value(), "Imobiliária não encontrada");
            return false;
        }

        boolean planoInativo = imobiliaria.getStatusPlano() != StatusPlano.ATIVO;
        boolean planoVencido = imobiliaria.getDataVencimento() == null
                || imobiliaria.getDataVencimento().isBefore(LocalDate.now());

        if (planoInativo || planoVencido) {
            response.sendError(HttpStatus.PAYMENT_REQUIRED.value(),
                    "Acesso bloqueado: o plano da imobiliária está vencido ou inativo");
            return false;
        }

        return true;
    }
}
