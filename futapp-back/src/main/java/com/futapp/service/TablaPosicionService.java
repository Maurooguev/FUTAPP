package com.futapp.service;

import com.futapp.dto.TablaPosicionDTO;
import com.futapp.model.Equipo;
import com.futapp.model.Partido;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TablaPosicionService {

    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final TorneoVisibilityService visibility;

    public TablaPosicionService(InscripcionEquipoRepository inscripcionesEquipo, PartidoRepository partidoRepository,
                                TorneoRepository torneoRepository, TorneoVisibilityService visibility) {
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
        this.visibility = visibility;
    }

    public List<TablaPosicionDTO> obtenerTablaPorTorneo(Long idTorneo) {
        var torneo = torneoRepository.findById(idTorneo).orElseThrow();
        visibility.verificar(torneo);
        // Las bajas deben conservar su fila y todos los resultados ya disputados.
        List<Equipo> equipos = inscripcionesEquipo.findByTorneoIdTorneo(idTorneo)
                .stream().map(inscripcion -> inscripcion.getEquipo()).toList();
        List<Partido> partidos = partidoRepository.findByTorneoIdTorneo(idTorneo);

        Map<Long, TablaPosicionDTO> tabla = new HashMap<>();

        for (Equipo equipo : equipos) {
            tabla.put(
                equipo.getIdEquipo(),
                new TablaPosicionDTO(equipo.getIdEquipo(), equipo.getNombre(), equipo.getEscudo())
            );
        }

        for (Partido partido : partidos) {
            if (!"FINALIZADO".equalsIgnoreCase(partido.getEstado())) {
                continue;
            }
            if (esEliminatorio(partido)) {
                continue;
            }
            if (partido.getGolesLocal() == null || partido.getGolesVisitante() == null) {
                continue;
            }

            Long idLocal = partido.getEquipoLocal().getIdEquipo();
            Long idVisitante = partido.getEquipoVisitante().getIdEquipo();

            TablaPosicionDTO local = tabla.get(idLocal);
            TablaPosicionDTO visitante = tabla.get(idVisitante);

            if (local == null || visitante == null) {
                continue;
            }

            int golesLocal = partido.getGolesLocal();
            int golesVisitante = partido.getGolesVisitante();

            local.setPartidosJugados(local.getPartidosJugados() + 1);
            visitante.setPartidosJugados(visitante.getPartidosJugados() + 1);

            local.setGolesFavor(local.getGolesFavor() + golesLocal);
            local.setGolesContra(local.getGolesContra() + golesVisitante);

            visitante.setGolesFavor(visitante.getGolesFavor() + golesVisitante);
            visitante.setGolesContra(visitante.getGolesContra() + golesLocal);

            if (golesLocal > golesVisitante) {
                local.setGanados(local.getGanados() + 1);
                local.setPuntos(local.getPuntos() + torneo.getPuntosVictoria());

                visitante.setPerdidos(visitante.getPerdidos() + 1);
                visitante.setPuntos(visitante.getPuntos() + torneo.getPuntosDerrota());
            } else if (golesLocal < golesVisitante) {
                visitante.setGanados(visitante.getGanados() + 1);
                visitante.setPuntos(visitante.getPuntos() + torneo.getPuntosVictoria());

                local.setPerdidos(local.getPerdidos() + 1);
                local.setPuntos(local.getPuntos() + torneo.getPuntosDerrota());
            } else {
                local.setEmpatados(local.getEmpatados() + 1);
                visitante.setEmpatados(visitante.getEmpatados() + 1);

                local.setPuntos(local.getPuntos() + torneo.getPuntosEmpate());
                visitante.setPuntos(visitante.getPuntos() + torneo.getPuntosEmpate());
            }
        }

        for (TablaPosicionDTO fila : tabla.values()) {
            fila.setDiferenciaGol(fila.getGolesFavor() - fila.getGolesContra());
        }

        List<TablaPosicionDTO> resultado = new ArrayList<>(tabla.values());

        Comparator<TablaPosicionDTO> desempate = "GOLES_FAVOR".equals(torneo.getCriterioDesempate())
                ? Comparator.comparingInt(TablaPosicionDTO::getGolesFavor).reversed().thenComparing(Comparator.comparingInt(TablaPosicionDTO::getDiferenciaGol).reversed())
                : Comparator.comparingInt(TablaPosicionDTO::getDiferenciaGol).reversed().thenComparing(Comparator.comparingInt(TablaPosicionDTO::getGolesFavor).reversed());
        resultado.sort(Comparator.comparingInt(TablaPosicionDTO::getPuntos).reversed().thenComparing(desempate).thenComparing(TablaPosicionDTO::getNombreEquipo));

        return resultado;
    }

    private boolean esEliminatorio(Partido partido) {
        String detalle = partido.getObservaciones();
        return detalle != null && java.util.Set.of("Final", "Semifinales", "Cuartos de final",
                "Octavos de final", "Primera ronda eliminatoria").contains(detalle);
    }
}
