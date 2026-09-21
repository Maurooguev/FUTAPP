package com.futapp.service;

import com.futapp.dto.InscripcionEquipoResponseDTO;
import com.futapp.dto.InscripcionJugadorResponseDTO;
import com.futapp.dto.InscripcionEquipoRequestDTO;
import com.futapp.dto.InscripcionJugadorRequestDTO;
import com.futapp.dto.ActualizacionInscripcionJugadorDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.InscripcionJugador;
import com.futapp.model.Equipo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class InscripcionService {
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EquipoRepository equipos;
    private final JugadorRepository jugadores;
    private final TorneoRepository torneos;
    private final TorneoAuthorizationService authorization;
    private final EquipoAuthorizationService equipoAuthorization;
    private final PartidoRepository partidos;

    public InscripcionService(InscripcionEquipoRepository inscripcionesEquipo,
                              InscripcionJugadorRepository inscripcionesJugador,
                              EquipoRepository equipos, JugadorRepository jugadores,
                              TorneoRepository torneos, TorneoAuthorizationService authorization,
                              EquipoAuthorizationService equipoAuthorization,
                              PartidoRepository partidos) {
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipos = equipos;
        this.jugadores = jugadores;
        this.torneos = torneos;
        this.authorization = authorization;
        this.equipoAuthorization = equipoAuthorization;
        this.partidos = partidos;
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
        equipoAuthorization.verificarGestion(equipo);
        return inscribirEquipoInterno(request.idEquipo(), request.idTorneo(), request.categoria());
    }

    @Transactional
    public InscripcionEquipoResponseDTO inscribirEquipoAprobado(Long idEquipo, Long idTorneo, String categoria) {
        return inscribirEquipoInterno(idEquipo, idTorneo, categoria);
    }

    private InscripcionEquipoResponseDTO inscribirEquipoInterno(Long idEquipo, Long idTorneo, String categoria) {
        Equipo equipo = equipos.findById(idEquipo)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + idEquipo));
        var torneo = torneos.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo));
        if (inscripcionesEquipo.existsByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, idTorneo)) {
            throw new IllegalArgumentException("El club ya está inscrito en este torneo");
        }
        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo);
        inscripcion.setTorneo(torneo);
        inscripcion.setCategoria(normalizar(categoria));
        inscripcion.setEstado("ACTIVA");
        inscripcion = inscripcionesEquipo.save(inscripcion);
        copiarPlantelExistente(inscripcion, equipo);
        return equipoDto(inscripcion);
    }

    private void copiarPlantelExistente(InscripcionEquipo inscripcion, Equipo equipo) {
        java.util.Set<Integer> dorsales = new java.util.HashSet<>();
        jugadores.findByEquipoIdEquipo(equipo.getIdEquipo()).stream()
                .filter(jugador -> !"INACTIVO".equalsIgnoreCase(jugador.getEstado()))
                .forEach(jugador -> {
                    InscripcionJugador alta = new InscripcionJugador();
                    alta.setInscripcionEquipo(inscripcion);
                    alta.setJugador(jugador);
                    Integer dorsal = jugador.getNumeroCamiseta();
                    alta.setNumeroCamiseta(dorsal != null && dorsales.add(dorsal) ? dorsal : null);
                    alta.setPosicion(normalizar(jugador.getPosicion()));
                    alta.setEstado("ACTIVA");
                    inscripcionesJugador.save(alta);
                });
    }

    @Transactional
    public void quitarEquipoDelTorneo(Long idTorneo, Long idEquipo) {
        authorization.verificarGestion(idTorneo);
        InscripcionEquipo inscripcion = inscripcionesEquipo
                .findByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("El equipo no está inscripto en este torneo"));
        if (partidos.existsByTorneoIdTorneoAndEquipoLocalIdEquipoOrTorneoIdTorneoAndEquipoVisitanteIdEquipo(
                idTorneo, idEquipo, idTorneo, idEquipo)) {
            throw new IllegalArgumentException("No se puede quitar el equipo porque ya tiene partidos en este torneo");
        }
        inscripcionesJugador.deleteByInscripcionEquipoIdInscripcion(inscripcion.getIdInscripcion());
        inscripcionesEquipo.delete(inscripcion);
    }

    @Transactional
    public InscripcionJugadorResponseDTO inscribirJugador(Long idInscripcion,
                                                           InscripcionJugadorRequestDTO request) {
        var inscripcionEquipo = inscripcionesEquipo.findById(idInscripcion)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con id: " + idInscripcion));
        equipoAuthorization.verificarGestionPlantel(inscripcionEquipo);
        var jugador = jugadores.findById(request.idJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + request.idJugador()));
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndJugadorIdJugador(idInscripcion, request.idJugador())) {
            throw new IllegalArgumentException("El jugador ya pertenece a este plantel");
        }
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndNumeroCamisetaAndEstado(idInscripcion, request.numeroCamiseta(), "ACTIVA")) {
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

    @Transactional
    public InscripcionJugadorResponseDTO actualizarJugador(Long id, ActualizacionInscripcionJugadorDTO request) {
        InscripcionJugador inscripcion = obtenerInscripcionJugador(id);
        equipoAuthorization.verificarGestionPlantel(inscripcion.getInscripcionEquipo());
        validarDorsalDisponible(inscripcion, request.numeroCamiseta());
        inscripcion.setNumeroCamiseta(request.numeroCamiseta());
        inscripcion.setPosicion(normalizar(request.posicion()));
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    @Transactional
    public InscripcionJugadorResponseDTO darDeBaja(Long id) {
        InscripcionJugador inscripcion = obtenerInscripcionJugador(id);
        equipoAuthorization.verificarGestionPlantel(inscripcion.getInscripcionEquipo());
        if ("INACTIVA".equals(inscripcion.getEstado())) throw new IllegalArgumentException("El jugador ya está dado de baja en este plantel");
        inscripcion.setEstado("INACTIVA");
        inscripcion.setFechaBaja(LocalDate.now());
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    @Transactional
    public InscripcionJugadorResponseDTO reactivarJugador(Long id, ActualizacionInscripcionJugadorDTO request) {
        InscripcionJugador inscripcion = obtenerInscripcionJugador(id);
        equipoAuthorization.verificarGestionPlantel(inscripcion.getInscripcionEquipo());
        validarDorsalDisponible(inscripcion, request.numeroCamiseta());
        inscripcion.setNumeroCamiseta(request.numeroCamiseta());
        inscripcion.setPosicion(normalizar(request.posicion()));
        inscripcion.setEstado("ACTIVA");
        inscripcion.setFechaBaja(null);
        return jugadorDto(inscripcionesJugador.save(inscripcion));
    }

    private InscripcionJugador obtenerInscripcionJugador(Long id) {
        return inscripcionesJugador.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción de jugador no encontrada con id: " + id));
    }

    private void validarDorsalDisponible(InscripcionJugador inscripcion, Integer dorsal) {
        if (inscripcionesJugador.existsByInscripcionEquipoIdInscripcionAndNumeroCamisetaAndEstadoAndIdInscripcionJugadorNot(
                inscripcion.getInscripcionEquipo().getIdInscripcion(), dorsal, "ACTIVA", inscripcion.getIdInscripcionJugador())) {
            throw new IllegalArgumentException("Ya existe un jugador activo con ese número en este plantel");
        }
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
                inscripcionesJugador.countByInscripcionEquipoIdInscripcionAndEstado(item.getIdInscripcion(), "ACTIVA")
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
