package com.futapp;

import com.futapp.repository.PartidoRepository;
import com.futapp.repository.PerfilOrganizadorRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.service.ExplorarService;
import com.futapp.service.TablaPosicionService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExplorarServiceTests {
    private final TorneoRepository torneos = mock(TorneoRepository.class);
    private final PartidoRepository partidos = mock(PartidoRepository.class);
    private final TablaPosicionService tabla = mock(TablaPosicionService.class);
    private final PerfilOrganizadorRepository perfiles = mock(PerfilOrganizadorRepository.class);
    private final ExplorarService service = new ExplorarService(torneos, partidos, tabla, perfiles);

    @Test
    void elFeedConsultaPartidosSoloDeTorneosPublicos() {
        when(torneos.findAll()).thenReturn(List.of());
        when(partidos.findByEstadoIgnoreCaseAndFechaHoraGreaterThanEqualAndTorneoEstadoInOrderByFechaHoraAsc(
                anyString(), any(LocalDateTime.class), any(), any(Pageable.class))).thenReturn(Page.empty());
        when(partidos.findByEstadoIgnoreCaseAndTorneoEstadoInOrderByFechaHoraDesc(
                anyString(), any(), any(Pageable.class))).thenReturn(Page.empty());

        service.obtener();

        List<String> estadosPublicos = List.of("ACTIVO", "FINALIZADO");
        verify(partidos).findByEstadoIgnoreCaseAndFechaHoraGreaterThanEqualAndTorneoEstadoInOrderByFechaHoraAsc(
                eq("PROGRAMADO"), any(LocalDateTime.class), eq(estadosPublicos), any(Pageable.class));
        verify(partidos).findByEstadoIgnoreCaseAndTorneoEstadoInOrderByFechaHoraDesc(
                eq("FINALIZADO"), eq(estadosPublicos), any(Pageable.class));
    }
}
