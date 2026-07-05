package com.system.imob.controllers;

import com.system.imob.dtos.requests.LoginRequestDTO;
import com.system.imob.dtos.requests.RegistroRequestDTO;
import com.system.imob.dtos.responses.LoginResponseDTO;
import com.system.imob.dtos.responses.RegistroResponseDTO;
import com.system.imob.services.CorretorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private CorretorService corretorService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO dto) {
        LoginResponseDTO response = corretorService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity registro(@RequestBody RegistroRequestDTO dto) {
        RegistroResponseDTO response = corretorService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}