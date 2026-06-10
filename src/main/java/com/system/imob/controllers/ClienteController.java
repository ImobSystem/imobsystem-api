package com.system.imob.controllers;

import com.system.imob.dtos.requests.ClienteRequestDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.models.Cliente;
import com.system.imob.repositories.ClienteRepository;
import com.system.imob.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ClienteRepository clienteRepository;

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> cadastrar (@RequestBody ClienteRequestDTO dto){
        ClienteResponseDTO response = clienteService.cadastrarCliente(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscaPorId(@PathVariable Long id){
        ClienteResponseDTO response = clienteService.buscarClientePorId(id);
        return ResponseEntity.ok(response);
    }
}
