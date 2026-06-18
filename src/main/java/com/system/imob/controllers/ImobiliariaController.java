package com.system.imob.controllers;

import com.system.imob.dtos.requests.ImobiliariaRequestDTO;
import com.system.imob.dtos.responses.ImobiliariaResponseDTO;
import com.system.imob.services.ImobiliariaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/imobiliarias")
public class ImobiliariaController {

    @Autowired
    private ImobiliariaService imobiliariaService;

    @PostMapping("/cadastrar")
    public ResponseEntity cadastrarImobiliaria(@RequestBody ImobiliariaRequestDTO dto) {
        ImobiliariaResponseDTO response = imobiliariaService.cadastrarImobiliaria(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(imobiliariaService.buscarPorId(id));
    }

    @GetMapping
    public ResponseEntity listarImobiliarias() {
        return ResponseEntity.ok(imobiliariaService.listarImobiliarias());
    }
}