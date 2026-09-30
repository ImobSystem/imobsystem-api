package com.system.imob.controllers;

import com.system.imob.dtos.requests.AssinarPlanoRequestDTO;
import com.system.imob.dtos.responses.AssinarPlanoResponseDTO;
import com.system.imob.dtos.responses.StatusPlanoResponseDTO;
import com.system.imob.services.PlanoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/imobiliarias/plano")
public class PlanoController {

    @Autowired
    private PlanoService planoService;

    @PostMapping
    public ResponseEntity<AssinarPlanoResponseDTO> assinarPlano(@RequestBody AssinarPlanoRequestDTO dto) {
        return ResponseEntity.ok(planoService.assinarPlano(dto));
    }

    @GetMapping
    public ResponseEntity<StatusPlanoResponseDTO> statusPlano() {
        return ResponseEntity.ok(planoService.buscarStatusPlano());
    }
}
