package com.example.myatomsbackend.controller;

import com.example.myatomsbackend.service.KnowledgeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {
    private final KnowledgeService service;
    public KnowledgeController(KnowledgeService service) { this.service = service; }
    @GetMapping public Map<String,Object> list() { return Map.of("items", service.list()); }
    @PostMapping public Map<String,Object> upload(@RequestParam MultipartFile file, @RequestParam(defaultValue="") String title) throws IOException {
        return Map.of("ok", true, "item", service.upload(file, title));
    }
    @GetMapping("/search") public Map<String,Object> search(@RequestParam String q) { return Map.of("results", service.search(q, List.of(), 6)); }
    @DeleteMapping("/{id}") public Map<String,Object> remove(@PathVariable String id) { service.remove(id); return Map.of("ok", true); }
}
