package com.system.imob.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Devolve os erros como {"message": "..."} em vez do corpo padrão do Spring
 * (que vem com status 500 e stack trace), pra o frontend conseguir mostrar
 * a mensagem real do backend na tela.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // AccessDeniedException é RuntimeException — sem este handler ela cairia no
    // de baixo e um erro de permissão viraria 400 em vez de 403.
    // O código ACESSO_NEGADO distingue "logado mas sem permissão" do 403 que o
    // Spring Security devolve quando o token expirou — o frontend desloga só no segundo.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException e) {
        Map<String, String> corpo = new HashMap<>(corpo(e));
        corpo.put("error", "ACESSO_NEGADO");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(corpo);
    }

    // A mensagem original expõe a assinatura do método do controller
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleCorpoInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", "Corpo da requisição inválido ou ausente."));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        log.warn("Requisição falhou: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(corpo(e));
    }

    // Map.of não aceita valor nulo, e getMessage() pode ser null
    private Map<String, String> corpo(Exception e) {
        String mensagem = e.getMessage() != null && !e.getMessage().isBlank()
                ? e.getMessage()
                : "Não foi possível processar a requisição.";
        return Map.of("message", mensagem);
    }
}
