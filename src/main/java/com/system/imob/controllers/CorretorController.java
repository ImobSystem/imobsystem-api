package com.system.imob.controllers;

import com.system.imob.dtos.requests.CorretorRequestDTO;
import com.system.imob.dtos.responses.CorretorResponseDTO;
import com.system.imob.services.CorretorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/corretores")
public class CorretorController {
    @Autowired
    private CorretorService corretorService;

    @PostMapping("/cadastrar")
    public ResponseEntity<CorretorResponseDTO> cadastrarCorretor (@RequestBody CorretorRequestDTO dto){
        CorretorResponseDTO response = corretorService.cadastrarCorretor(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
