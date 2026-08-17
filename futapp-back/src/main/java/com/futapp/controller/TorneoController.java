package com.futapp.controller;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.service.TorneoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/torneos")
@CrossOrigin(origins = "http://localhost:5173")
public class TorneoController {

    private final TorneoService torneoService;

    public TorneoController(TorneoService torneoService) {
        this.torneoService = torneoService;
    }

    @GetMapping
    public List<TorneoResponseDTO> listarTorneos() {
        return torneoService.listarTorneos();
    }

    @GetMapping("/{id}")
    public TorneoResponseDTO obtenerTorneo(@PathVariable Long id) {
        return torneoService.obtenerTorneoPorId(id);
    }

    @PostMapping
    public TorneoResponseDTO crearTorneo(@Valid @RequestBody TorneoRequestDTO requestDTO) {
        return torneoService.crearTorneo(requestDTO);
    }

    @PutMapping("/{id}")
    public TorneoResponseDTO actualizarTorneo(@PathVariable Long id, @Valid @RequestBody TorneoRequestDTO requestDTO) {
        return torneoService.actualizarTorneo(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public String eliminarTorneo(@PathVariable Long id) {
        torneoService.eliminarTorneo(id);
        return "Torneo eliminado correctamente";
    }
}
