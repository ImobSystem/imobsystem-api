package com.system.imob.controllers;

import com.system.imob.dtos.requests.CorretorRequestDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.dtos.responses.CorretorMetricasDTO;
import com.system.imob.dtos.responses.CorretorResponseDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.services.CorretorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<List<CorretorResponseDTO>> listarCorretores(){
        return ResponseEntity.ok(corretorService.listarCorretores());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CorretorResponseDTO> buscarPorId(@PathVariable Long id){
        return ResponseEntity.ok(corretorService.buscarCorretorPorId(id));
    }

    @GetMapping("/{id}/imoveis")
    public ResponseEntity<List<ImovelResponseDTO>> listarImoveisDoCorretor(@PathVariable Long id){
        return ResponseEntity.ok(corretorService.listarImoveisDoCorretor(id));
    }

    @GetMapping("/{id}/clientes")
    public ResponseEntity<List<ClienteResponseDTO>> listarClientesDoCorretor(@PathVariable Long id){
        return ResponseEntity.ok(corretorService.listarClientesDoCorretor(id));
    }

    @GetMapping("/{id}/metricas")
    public ResponseEntity<CorretorMetricasDTO> buscarMetricasDoCorretor(@PathVariable Long id){
        return ResponseEntity.ok(corretorService.buscarMetricasDoCorretor(id));
    }

    @GetMapping("/captacoes")
    public ResponseEntity listarCaptacoes() {
        return ResponseEntity.ok(corretorService.listarCaptacoes());
    }
}
