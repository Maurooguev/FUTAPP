package com.futapp.controller;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.dto.InscripcionEquipoRequestDTO;
import com.futapp.dto.InscripcionJugadorRequestDTO;
import com.futapp.dto.ActualizacionInscripcionJugadorDTO;
import com.futapp.dto.BajaEquipoTorneoRequestDTO;
import com.futapp.dto.NamesBatchRequestDTO;
import com.futapp.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @DeleteMapping("/equipos/torneo/{idTorneo}/equipo/{idEquipo}")
    public void quitarEquipoDelTorneo(@PathVariable Long idTorneo, @PathVariable Long idEquipo) {
        inscripciones.quitarEquipoDelTorneo(idTorneo, idEquipo);
    }

    @PutMapping("/equipos/torneo/{idTorneo}/equipo/{idEquipo}/baja")
    public InscripcionEquipoResponseDTO darDeBajaEquipoDelTorneo(
            @PathVariable Long idTorneo,
            @PathVariable Long idEquipo,
            @Valid @RequestBody BajaEquipoTorneoRequestDTO request) {
        return inscripciones.darDeBajaEquipoDelTorneo(idTorneo, idEquipo, request);
    }

    @PostMapping("/{idInscripcion}/jugadores")
    public InscripcionJugadorResponseDTO inscribirJugador(
            @PathVariable Long idInscripcion,
            @Valid @RequestBody InscripcionJugadorRequestDTO request) {
        return inscripciones.inscribirJugador(idInscripcion, request);
    }

    @PostMapping("/{idInscripcion}/jugadores/prueba")
    public List<InscripcionJugadorResponseDTO> generarJugadoresDePrueba(
            @PathVariable Long idInscripcion,
            @RequestParam(defaultValue = "5") int cantidad) {
        return inscripciones.generarJugadoresDePrueba(idInscripcion, cantidad);
    }

    @PostMapping("/{idInscripcion}/jugadores/lote")
    public List<InscripcionJugadorResponseDTO> cargarJugadoresPorNombre(
            @PathVariable Long idInscripcion, @Valid @RequestBody NamesBatchRequestDTO request) {
        return inscripciones.cargarJugadoresPorNombre(idInscripcion, request.nombres());
    }

    @PutMapping("/jugadores/{id}")
    public InscripcionJugadorResponseDTO actualizarJugador(@PathVariable Long id,
            @Valid @RequestBody ActualizacionInscripcionJugadorDTO request) {
        return inscripciones.actualizarJugador(id, request);
    }

    @PutMapping("/jugadores/{id}/baja")
    public InscripcionJugadorResponseDTO darDeBaja(@PathVariable Long id) {
        return inscripciones.darDeBaja(id);
    }

    @PutMapping("/jugadores/{id}/reactivar")
    public InscripcionJugadorResponseDTO reactivarJugador(@PathVariable Long id,
            @Valid @RequestBody ActualizacionInscripcionJugadorDTO request) {
        return inscripciones.reactivarJugador(id, request);
    }
}
