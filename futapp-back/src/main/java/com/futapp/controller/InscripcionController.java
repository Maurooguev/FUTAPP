package com.futapp.controller;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.dto.InscripcionEquipoRequestDTO;
import com.futapp.dto.InscripcionJugadorRequestDTO;
import com.futapp.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/inscripciones")
public class InscripcionController {
    private final InscripcionService inscripciones;

    public InscripcionController(InscripcionService inscripciones) {
        this.inscripciones = inscripciones;
    }

    @GetMapping("/equipos/{idEquipo}")
    public List<InscripcionEquipoResponseDTO> historialEquipo(@PathVariable Long idEquipo) {
        return inscripciones.historialEquipo(idEquipo);
    }

    @GetMapping("/{idInscripcion}/jugadores")
    public List<InscripcionJugadorResponseDTO> plantel(@PathVariable Long idInscripcion) {
        return inscripciones.plantel(idInscripcion);
    }

    @GetMapping("/jugadores/{idJugador}")
    public List<InscripcionJugadorResponseDTO> historialJugador(@PathVariable Long idJugador) {
        return inscripciones.historialJugador(idJugador);
    }

    @PostMapping("/equipos")
    public InscripcionEquipoResponseDTO inscribirEquipo(@Valid @RequestBody InscripcionEquipoRequestDTO request) {
        return inscripciones.inscribirEquipo(request);
    }

    @PostMapping("/{idInscripcion}/jugadores")
    public InscripcionJugadorResponseDTO inscribirJugador(
            @PathVariable Long idInscripcion,
            @Valid @RequestBody InscripcionJugadorRequestDTO request) {
        return inscripciones.inscribirJugador(idInscripcion, request);
    }
}
