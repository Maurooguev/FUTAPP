package com.futapp.controller;

import com.futapp.dto.SancionRequestDTO;
import com.futapp.dto.SancionResponseDTO;
import com.futapp.service.SancionJugadorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sanciones")
public class SancionJugadorController {
    private final SancionJugadorService service;

    public SancionJugadorController(SancionJugadorService service) { this.service = service; }

    @GetMapping("/torneo/{idTorneo}")
    public List<SancionResponseDTO> listarPorTorneo(@PathVariable Long idTorneo) {
        return service.listarPorTorneo(idTorneo);
    }

    @PutMapping("/{id}")
    public SancionResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody SancionRequestDTO request) {
        return service.actualizar(id, request);
    }
}

