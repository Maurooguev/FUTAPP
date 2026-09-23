package com.futapp.service;

import com.futapp.dto.ExplorarDTO;
import com.futapp.dto.PartidoExplorarDTO;
import com.futapp.dto.TorneoExplorarDTO;
import com.futapp.model.Partido;
import com.futapp.model.Torneo;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExplorarService {
    private static final int CANTIDAD_ACTIVIDAD = 8;
    private static final int CANTIDAD_PROXIMOS_BASE = 100;
    private final TorneoRepository torneos;
    private final PartidoRepository partidos;

    public ExplorarService(TorneoRepository torneos, PartidoRepository partidos) {
        this.torneos = torneos;
        this.partidos = partidos;
    }

    @Transactional(readOnly = true)
    public ExplorarDTO obtener() {
        List<Partido> proximos = partidos
                .findByEstadoIgnoreCaseAndFechaHoraGreaterThanEqualOrderByFechaHoraAsc(
                        "PROGRAMADO", LocalDateTime.now(), PageRequest.of(0, CANTIDAD_PROXIMOS_BASE))
                .getContent();
        List<PartidoExplorarDTO> proximosDto = proximos.stream().limit(CANTIDAD_ACTIVIDAD).map(this::partidoDto).toList();
        Map<Long, PartidoExplorarDTO> proximoPorTorneo = proximos.stream()
                .collect(Collectors.toMap(
                        partido -> partido.getTorneo().getIdTorneo(),
                        this::partidoDto,
                        (primero, ignorado) -> primero));

        List<TorneoExplorarDTO> torneosDto = torneos.findAll().stream()
                .filter(torneo -> !"BORRADOR".equalsIgnoreCase(torneo.getEstado()))
                .map(torneo -> torneoDto(torneo, proximoPorTorneo.get(torneo.getIdTorneo())))
                .toList();
        List<PartidoExplorarDTO> resultados = partidos
                .findByEstadoIgnoreCaseOrderByFechaHoraDesc(
                        "FINALIZADO", PageRequest.of(0, CANTIDAD_ACTIVIDAD))
                .map(this::partidoDto).getContent();
        List<String> ubicaciones = torneosDto.stream()
                .map(TorneoExplorarDTO::ubicacion)
                .filter(Objects::nonNull)
                .filter(ubicacion -> !ubicacion.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        return new ExplorarDTO(torneosDto, resultados, proximosDto, ubicaciones);
    }

    private TorneoExplorarDTO torneoDto(Torneo torneo, PartidoExplorarDTO proximo) {
        return new TorneoExplorarDTO(
                torneo.getIdTorneo(), torneo.getNombre(), torneo.getDescripcion(), torneo.getUbicacion(),
                torneo.getEstado(), torneo.getFormato(), torneo.getCantidadEquipos(), torneo.getCodigoPublico(),
                torneo.getPropietario() == null ? null : torneo.getPropietario().getIdUsuario(),
                torneo.getPropietario() == null ? null : torneo.getPropietario().getNombre(),
                torneo.getCampeon() == null ? null : torneo.getCampeon().getIdEquipo(),
                torneo.getCampeon() == null ? null : torneo.getCampeon().getNombre(),
                torneo.getVisitas(), proximo);
    }

    private PartidoExplorarDTO partidoDto(Partido partido) {
        return new PartidoExplorarDTO(
                partido.getIdPartido(), partido.getFechaHora(), partido.getEstado(),
                partido.getGolesLocal(), partido.getGolesVisitante(),
                partido.getTorneo().getIdTorneo(), partido.getTorneo().getNombre(),
                partido.getEquipoLocal().getIdEquipo(), partido.getEquipoLocal().getNombre(),
                partido.getEquipoVisitante().getIdEquipo(), partido.getEquipoVisitante().getNombre());
    }
}
