package com.futapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import java.time.Instant;
import java.util.Map;

@RestController
public class HomeController {
    private final JdbcTemplate database;

    public HomeController(JdbcTemplate database) { this.database = database; }

    @GetMapping("/hola")
    public String saludar() {
        return "FUTAPP funcionando";
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        try {
            database.queryForObject("SELECT 1", Integer.class);
            return ResponseEntity.ok(Map.of("status", "UP", "database", "UP", "timestamp", Instant.now().toString()));
        } catch (Exception exception) {
            return ResponseEntity.status(503).body(Map.of("status", "DOWN", "database", "DOWN", "timestamp", Instant.now().toString()));
        }
    }
}
