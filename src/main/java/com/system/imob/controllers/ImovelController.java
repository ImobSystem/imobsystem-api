package com.system.imob.controllers;

import com.system.imob.dtos.requests.ImovelRequestDTO;
import com.system.imob.dtos.responses.ImovelResponseDTO;
import com.system.imob.enums.Finalidade;
import com.system.imob.enums.StatusImovel;
import com.system.imob.enums.TipoImovel;
import com.system.imob.services.ImovelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/imoveis")
public class ImovelController {

    @Autowired
    private ImovelService imovelService;

    @PostMapping
    public ResponseEntity<ImovelResponseDTO> cadastrar(@RequestBody ImovelRequestDTO dto) {
        ImovelResponseDTO response = imovelService.cadastrarImovel(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Os quatro filtros são opcionais e podem ser combinados
    @GetMapping
    public ResponseEntity<List<ImovelResponseDTO>> listar(
            @RequestParam(required = false) String endereco,
            @RequestParam(required = false) StatusImovel status,
            @RequestParam(required = false) Finalidade finalidade,
            @RequestParam(required = false) TipoImovel tipo) {
        return ResponseEntity.ok(imovelService.listar(endereco, status, finalidade, tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> atualizarImovel(@PathVariable Long id, @RequestBody ImovelRequestDTO dto) {
        ImovelResponseDTO response = imovelService.atualizarImovel(id, dto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImovelResponseDTO> buscarImovelPorId(@PathVariable Long id) {
        ImovelResponseDTO response = imovelService.buscarImovelPorId(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarImovelPorId(@PathVariable Long id) {
        imovelService.deletarImovelPorId(id);
    }
}