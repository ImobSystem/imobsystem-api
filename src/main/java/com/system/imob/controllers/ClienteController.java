package com.system.imob.controllers;

import com.system.imob.dtos.requests.ClienteRequestDTO;
import com.system.imob.dtos.responses.ClienteResponseDTO;
import com.system.imob.enums.TipoCliente;
import com.system.imob.services.ClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clientes")
public class ClienteController {
    @Autowired
    private ClienteService clienteService;

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

    @GetMapping
    public ResponseEntity listarClientes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) TipoCliente tipo) {
        return ResponseEntity.ok(clienteService.listarClientes(nome, email, tipo));
    }
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> atualizarCliente(@PathVariable Long id, @RequestBody ClienteRequestDTO dto){
        return ResponseEntity.ok(clienteService.atualizarClientePorId(id, dto));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarClientePorId(@PathVariable Long id){
        clienteService.deletarClientePorId(id);
    }
}

