package com.system.imob.controllers;

import com.system.imob.dtos.requests.NegociacaoRequestDTO;
import com.system.imob.dtos.requests.NegociacaoStatusRequestDTO;
import com.system.imob.dtos.responses.NegociacaoResponseDTO;
import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusNegocio;
import com.system.imob.services.NegociacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/negociacoes")
public class NegociacaoController {
    @Autowired
    private NegociacaoService negociacaoService;

    @PostMapping
    public ResponseEntity<NegociacaoResponseDTO> criarNegociacao(@RequestBody NegociacaoRequestDTO dto){
        NegociacaoResponseDTO response = negociacaoService.criarNegociacao(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity listarNegociacoes(
            @RequestParam(required = false) StatusNegocio status,
            @RequestParam(required = false) Finalidade finalidade) {
        return ResponseEntity.ok(negociacaoService.listarNegociacoes(status, finalidade));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NegociacaoResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(negociacaoService.buscarNegociacaoPorId(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<NegociacaoResponseDTO> atualizarStatus(@PathVariable Long id, @RequestBody NegociacaoStatusRequestDTO dto){
        return ResponseEntity.ok(negociacaoService.atualizarStatus(id, dto));
    }
}
