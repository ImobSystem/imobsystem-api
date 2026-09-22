package com.system.imob.controllers;

import com.system.imob.dtos.requests.ChatRequestDTO;
import com.system.imob.dtos.responses.ChatResponseDTO;
import com.system.imob.services.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponseDTO> processarMensagem(@RequestBody ChatRequestDTO dto) {
        ChatResponseDTO response = chatService.processarMensagem(dto.mensagem());
        return ResponseEntity.ok(response);
    }
}
