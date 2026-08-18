package com.futapp.service;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.dto.InscripcionEquipoRequestDTO;
import com.futapp.dto.InscripcionJugadorRequestDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.InscripcionJugador;
import com.futapp.model.Equipo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.TorneoRepository;
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
    private final TorneoRepository torneos;
    private final TorneoAuthorizationService authorization;

    public InscripcionService(InscripcionEquipoRepository inscripcionesEquipo,
                              InscripcionJugadorRepository inscripcionesJugador,
                              EquipoRepository equipos, JugadorRepository jugadores,
                              TorneoRepository torneos, TorneoAuthorizationService authorization) {
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.torneos = torneos;
        this.authorization = authorization;
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

    @Transactional
    public InscripcionEquipoResponseDTO inscribirEquipo(InscripcionEquipoRequestDTO request) {
        authorization.verificarGestion(request.idTorneo());
        Equipo equipo = equipos.findById(request.idEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + request.idEquipo()));
        var torneo = torneos.findById(request.idTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + request.idTorneo()));
        if (inscripcionesEquipo.existsByEquipoIdEquipoAndTorneoIdTorneo(request.idEquipo(), request.idTorneo())) {
            throw new IllegalArgumentException("El club ya está inscrito en este torneo");
        }
        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo);
        inscripcion.setTorneo(torneo);
        inscripcion.setCategoria(normalizar(request.categoria()));
        inscripcion.setEstado("ACTIVA");
        return equipoDto(inscripcionesEquipo.save(inscripcion));
    }

    @Transactional
    public InscripcionJugadorResponseDTO inscribirJugador(Long idInscripcion,
                                                           InscripcionJugadorRequestDTO request) {
        var inscripcionEquipo = inscripcionesEquipo.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion));
        authorization.verificarGestion(inscripcionEquipo.getTorneo());
        var jugador = jugadores.findById(request.idJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + request.idJugador()));
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndJugadorIdJugador(idInscripcion, request.idJugador())) {
            throw new IllegalArgumentException("El jugador ya pertenece a este plantel");
        }
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndNumeroCamiseta(idInscripcion, request.numeroCamiseta())) {
            throw new IllegalArgumentException("Ya existe un jugador con ese número en este plantel");
        }
        InscripcionJugador inscripcion = new InscripcionJugador();
        inscripcion.setInscripcionEquipo(inscripcionEquipo);
        inscripcion.setJugador(jugador);
        inscripcion.setNumeroCamiseta(request.numeroCamiseta());
        inscripcion.setPosicion(normalizar(request.posicion()) == null ? jugador.getPosicion() : request.posicion().trim());
        inscripcion.setEstado("ACTIVA");
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
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
