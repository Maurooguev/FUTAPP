package com.futapp.controller;

import com.futapp.dto.GolRequestDTO;
import com.futapp.dto.GolResponseDTO;
import com.futapp.dto.GoleadorDTO;
import com.futapp.service.GolService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/goles")
public class GolController {

    private final GolService golService;

    public GolController(GolService golService) {
        this.golService = golService;
    }

    @GetMapping
    public List<GolResponseDTO> listarGoles() {
        return golService.listarGoles();
    }

    @GetMapping("/{id}")
    public GolResponseDTO obtenerGol(@PathVariable Long id) {
        return golService.obtenerGolPorId(id);
    }

    @GetMapping("/partido/{idPartido}")
    public List<GolResponseDTO> listarGolesPorPartido(@PathVariable Long idPartido) {
        return golService.listarGolesPorPartido(idPartido);
    }

    @GetMapping("/jugador/{idJugador}")
    public List<GolResponseDTO> listarGolesPorJugador(@PathVariable Long idJugador) {
        return golService.listarGolesPorJugador(idJugador);
    }

    @GetMapping("/torneo/{idTorneo}/goleadores")
    public List<GoleadorDTO> obtenerGoleadoresPorTorneo(@PathVariable Long idTorneo) {
        return golService.obtenerGoleadoresPorTorneo(idTorneo);
    }

    @PostMapping
    public GolResponseDTO crearGol(@Valid @RequestBody GolRequestDTO requestDTO) {
        return golService.crearGol(requestDTO);
    }

    @PutMapping("/{id}")
    public GolResponseDTO actualizarGol(@PathVariable Long id, @Valid @RequestBody GolRequestDTO requestDTO) {
        return golService.actualizarGol(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public String eliminarGol(@PathVariable Long id) {
        golService.eliminarGol(id);
        return "Gol eliminado correctamente";
    }
}
