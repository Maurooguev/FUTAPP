package com.futapp.controller;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.service.InscripcionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
