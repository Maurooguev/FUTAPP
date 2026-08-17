package com.futapp.controller;

import com.futapp.dto.JugadorRequestDTO;
import com.futapp.dto.JugadorResponseDTO;
import com.futapp.service.JugadorService;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/jugadores")
public class JugadorController {

    private final JugadorService jugadorService;

    public JugadorController(JugadorService jugadorService) {
        this.jugadorService = jugadorService;
    }

    @GetMapping
    public List<JugadorResponseDTO> listarJugadores() {
        return jugadorService.listarJugadores();
    }

    @GetMapping("/{id}")
    public JugadorResponseDTO obtenerJugador(@PathVariable Long id) {
        return jugadorService.obtenerJugadorPorId(id);
    }

    @GetMapping("/equipo/{idEquipo}")
    public List<JugadorResponseDTO> listarJugadoresPorEquipo(@PathVariable Long idEquipo) {
        return jugadorService.listarJugadoresPorEquipo(idEquipo);
    }

    @PostMapping
    public JugadorResponseDTO crearJugador(@Valid @RequestBody JugadorRequestDTO requestDTO) {
        return jugadorService.crearJugador(requestDTO);
    }

    @PutMapping("/{id}")
    public JugadorResponseDTO actualizarJugador(@PathVariable Long id, @Valid @RequestBody JugadorRequestDTO requestDTO) {
        return jugadorService.actualizarJugador(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    public String eliminarJugador(@PathVariable Long id) {
        jugadorService.eliminarJugador(id);
        return "Jugador eliminado correctamente";
    }
}
