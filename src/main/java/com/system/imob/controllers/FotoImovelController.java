package com.system.imob.controllers;

import com.system.imob.dtos.requests.FotoImovelRequestDTO;
import com.system.imob.dtos.responses.FotoImovelResponseDTO;
import com.system.imob.services.FotoImovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/imoveis/{imovelId}/fotos")
public class FotoImovelController {

    @Autowired
    private FotoImovelService fotoImovelService;

    @PostMapping
    public ResponseEntity adicionarFoto(@PathVariable Long imovelId,
                                        @RequestBody FotoImovelRequestDTO dto) {
        FotoImovelResponseDTO response = fotoImovelService.adicionarFoto(imovelId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity listarFotos(@PathVariable Long imovelId) {
        List<FotoImovelResponseDTO> response = fotoImovelService.listarFotos(imovelId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{fotoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removerFoto(@PathVariable Long imovelId, @PathVariable Long fotoId) {
        fotoImovelService.removerFoto(imovelId, fotoId);
    }
}