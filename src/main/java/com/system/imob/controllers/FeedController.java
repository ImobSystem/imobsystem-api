package com.system.imob.controllers;

import com.system.imob.services.FeedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    // Rota pública: os portais leem essa URL sem token
    @GetMapping(value = "/xml/{imobiliariaId}", produces = "application/xml; charset=UTF-8")
    public ResponseEntity<String> gerarFeed(@PathVariable Long imobiliariaId) {
        String xml = feedService.gerarFeedXml(imobiliariaId);
        return ResponseEntity.ok(xml);
    }
}
