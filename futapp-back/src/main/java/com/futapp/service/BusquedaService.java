package com.futapp.service;

import com.futapp.dto.BusquedaGlobalDTO;
import com.futapp.dto.ResultadoBusquedaDTO;
import com.futapp.model.Equipo;
import com.futapp.model.Jugador;
import com.futapp.model.Torneo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusquedaService {
    private static final int RESULTADOS_POR_TIPO = 6;

    private final TorneoRepository torneoRepository;
    private final EquipoRepository equipoRepository;
    private final JugadorRepository jugadorRepository;

    public BusquedaService(TorneoRepository torneoRepository, EquipoRepository equipoRepository,
                           JugadorRepository jugadorRepository) {
        this.torneoRepository = torneoRepository;
        this.equipoRepository = equipoRepository;
        this.jugadorRepository = jugadorRepository;
    }

    @Transactional(readOnly = true)
    public BusquedaGlobalDTO buscar(String texto) {
        String termino = texto == null ? "" : texto.trim();
        if (termino.length() < 2) return new BusquedaGlobalDTO(List.of(), List.of(), List.of());

        PageRequest limite = PageRequest.of(0, RESULTADOS_POR_TIPO);
        List<ResultadoBusquedaDTO> torneos = torneoRepository
                .findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCaseOrUbicacionContainingIgnoreCase(
                        termino, termino, termino, limite)
                .map(this::torneoResultado).getContent();
        List<ResultadoBusquedaDTO> equipos = equipoRepository
                .findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCaseOrBarrioContainingIgnoreCase(
                        termino, termino, termino, limite)
                .map(this::equipoResultado).getContent();
        List<ResultadoBusquedaDTO> jugadores = jugadorRepository
                .findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOrApodoContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(
                        termino, termino, termino, termino, limite)
                .map(this::jugadorResultado).getContent();

        return new BusquedaGlobalDTO(torneos, equipos, jugadores);
    }

    private ResultadoBusquedaDTO torneoResultado(Torneo torneo) {
        String subtitulo = torneo.getUbicacion();
        if (torneo.getFormato() != null && !torneo.getFormato().isBlank()) {
            subtitulo = unir(subtitulo, formatoLegible(torneo.getFormato()));
        }
        return new ResultadoBusquedaDTO(torneo.getIdTorneo(), torneo.getNombre(), subtitulo,
                torneo.getCodigoPublico(), null);
    }

    private ResultadoBusquedaDTO equipoResultado(Equipo equipo) {
        String subtitulo = equipo.getBarrio();
        if (equipo.getTorneo() != null) subtitulo = unir(subtitulo, equipo.getTorneo().getNombre());
        return new ResultadoBusquedaDTO(equipo.getIdEquipo(), equipo.getNombre(), subtitulo,
                equipo.getCodigoPublico(), equipo.getEscudo());
    }

    private ResultadoBusquedaDTO jugadorResultado(Jugador jugador) {
        String nombreCompleto = unir(jugador.getNombre(), jugador.getApellido());
        String subtitulo = jugador.getEquipo() == null ? jugador.getPosicion()
                : unir(jugador.getEquipo().getNombre(), jugador.getPosicion());
        return new ResultadoBusquedaDTO(jugador.getIdJugador(), nombreCompleto, subtitulo,
                jugador.getCodigoPublico(), jugador.getFoto());
    }

    private String unir(String primero, String segundo) {
        if (primero == null || primero.isBlank()) return segundo;
        if (segundo == null || segundo.isBlank()) return primero;
        return primero + " · " + segundo;
    }

    private String formatoLegible(String formato) {
        return formato.replace('_', ' ');
    }
}
