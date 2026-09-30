package com.system.imob.controllers;

import com.system.imob.services.PlanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private PlanoService planoService;

    // Rota pública (sem JWT) — o Asaas chama de servidor pra servidor.
    // A autenticidade é validada pelo header asaas-access-token dentro do PlanoService.
    @PostMapping("/asaas")
    public ResponseEntity<Void> receberWebhookAsaas(
            @RequestHeader(value = "asaas-access-token", required = false) String token,
            @RequestBody Map<String, Object> payload) {
        planoService.processarWebhook(token, payload);
        return ResponseEntity.ok().build();
    }
}
