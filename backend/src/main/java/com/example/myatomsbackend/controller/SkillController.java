package com.example.myatomsbackend.controller;

import com.example.myatomsbackend.service.SkillMarketplaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/skills")
public class SkillController {
    private final SkillMarketplaceService service;
    public SkillController(SkillMarketplaceService service) { this.service = service; }

    @GetMapping public Map<String, Object> list() { return Map.of("items", service.list()); }
    @PostMapping("/{id}/install") public Map<String, Object> install(@PathVariable String id) {
        return Map.of("ok", true, "skill", service.install(id));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> uninstall(@PathVariable String id) {
        service.uninstall(id); return ResponseEntity.noContent().build();
    }
}
