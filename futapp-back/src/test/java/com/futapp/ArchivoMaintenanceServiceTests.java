package com.futapp;

import com.futapp.model.Equipo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PerfilOrganizadorRepository;
import com.futapp.service.ArchivoMaintenanceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArchivoMaintenanceServiceTests {
    @TempDir Path carpeta;

    @Test
    void eliminaSoloHuerfanosAntiguosGeneradosPorLaAplicacion() throws Exception {
        EquipoRepository equipos = mock(EquipoRepository.class);
        JugadorRepository jugadores = mock(JugadorRepository.class);
        PerfilOrganizadorRepository perfiles = mock(PerfilOrganizadorRepository.class);
        String usado = "11111111-1111-1111-1111-111111111111.png";
        String huerfano = "22222222-2222-2222-2222-222222222222.jpg";
        String reciente = "33333333-3333-3333-3333-333333333333.webp";
        Path archivoUsado = Files.write(carpeta.resolve(usado), new byte[]{1});
        Path archivoHuerfano = Files.write(carpeta.resolve(huerfano), new byte[]{1});
        Path archivoReciente = Files.write(carpeta.resolve(reciente), new byte[]{1});
        Path archivoManual = Files.write(carpeta.resolve("escudo-demo.png"), new byte[]{1});
        FileTime antiguo = FileTime.from(Instant.now().minus(2, ChronoUnit.DAYS));
        Files.setLastModifiedTime(archivoUsado, antiguo);
        Files.setLastModifiedTime(archivoHuerfano, antiguo);

        Equipo equipo = new Equipo();
        equipo.setEscudo("/uploads/" + usado);
        when(equipos.findAll()).thenReturn(List.of(equipo));
        when(jugadores.findAll()).thenReturn(List.of());
        when(perfiles.findAll()).thenReturn(List.of());

        new ArchivoMaintenanceService(equipos, jugadores, perfiles, carpeta.toString()).limpiarHuerfanos();

        assertTrue(Files.exists(archivoUsado));
        assertFalse(Files.exists(archivoHuerfano));
        assertTrue(Files.exists(archivoReciente));
        assertTrue(Files.exists(archivoManual));
    }
}
