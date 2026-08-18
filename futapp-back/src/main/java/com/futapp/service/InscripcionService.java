package com.futapp.service;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.InscripcionJugador;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.JugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InscripcionService {
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;

    public InscripcionService(InscripcionEquipoRepository inscripcionesEquipo,
                              InscripcionJugadorRepository inscripcionesJugador,
                              EquipoRepository equipos, JugadorRepository jugadores) {
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipos = equipos;
        this.jugadores = jugadores;
    }

    public List<InscripcionEquipoResponseDTO> historialEquipo(Long idEquipo) {
        if (!equipos.existsById(idEquipo)) {
            throw new ResourceNotFoundException("Equipo no encontrado con id: " + idEquipo);
        }
        return inscripcionesEquipo.findByEquipoIdEquipoOrderByFechaAltaDesc(idEquipo)
                .stream().map(this::equipoDto).toList();
    }

    public List<InscripcionJugadorResponseDTO> plantel(Long idInscripcion) {
        if (!inscripcionesEquipo.existsById(idInscripcion)) {
            throw new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion);
        }
        return inscripcionesJugador.findByInscripcionEquipoIdInscripcionOrderByNumeroCamiseta(idInscripcion)
                .stream().map(this::jugadorDto).toList();
    }

    public List<InscripcionJugadorResponseDTO> historialJugador(Long idJugador) {
        if (!jugadores.existsById(idJugador)) {
            throw new ResourceNotFoundException("Jugador no encontrado con id: " + idJugador);
        }
        return inscripcionesJugador.findByJugadorIdJugadorOrderByFechaAltaDesc(idJugador)
                .stream().map(this::jugadorDto).toList();
    }

    private InscripcionEquipoResponseDTO equipoDto(InscripcionEquipo item) {
        return new InscripcionEquipoResponseDTO(
                item.getIdInscripcion(), item.getEquipo().getIdEquipo(), item.getEquipo().getNombre(),
                item.getEquipo().getCodigoPublico(), item.getTorneo().getIdTorneo(),
                item.getTorneo().getNombre(), item.getTorneo().getCodigoPublico(), item.getCategoria(),
                item.getEstado(), item.getFechaAlta(), item.getFechaBaja(),
                inscripcionesJugador.countByInscripcionEquipoIdInscripcion(item.getIdInscripcion())
        );
    }

    private InscripcionJugadorResponseDTO jugadorDto(InscripcionJugador item) {
        var inscripcion = item.getInscripcionEquipo();
        var jugador = item.getJugador();
        return new InscripcionJugadorResponseDTO(
                item.getIdInscripcionJugador(), inscripcion.getIdInscripcion(), jugador.getIdJugador(),
                jugador.getNombre() + " " + jugador.getApellido(), jugador.getCodigoPublico(), jugador.getFoto(),
                item.getNumeroCamiseta(), item.getPosicion(), item.getEstado(), item.getFechaAlta(),
                item.getFechaBaja(), inscripcion.getEquipo().getIdEquipo(), inscripcion.getEquipo().getNombre(),
                inscripcion.getTorneo().getIdTorneo(), inscripcion.getTorneo().getNombre()
        );
    }
}
