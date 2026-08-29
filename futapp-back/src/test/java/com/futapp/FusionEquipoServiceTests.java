package com.futapp;

import com.futapp.model.Equipo;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import com.futapp.repository.*;
import com.futapp.service.FusionEquipoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FusionEquipoServiceTests {
    private final EquipoRepository equipos = mock(EquipoRepository.class);
    private final InscripcionEquipoRepository inscripciones = mock(InscripcionEquipoRepository.class);
    private final JugadorRepository jugadores = mock(JugadorRepository.class);
    private final PartidoRepository partidos = mock(PartidoRepository.class);
    private final GolRepository goles = mock(GolRepository.class);
    private final TarjetaRepository tarjetas = mock(TarjetaRepository.class);
    private final EquipoAdministradorRepository administradores = mock(EquipoAdministradorRepository.class);
    private final SolicitudReclamoRepository reclamos = mock(SolicitudReclamoRepository.class);
    private final EquipoAliasRepository aliases = mock(EquipoAliasRepository.class);
    private final FusionEquipoService service = new FusionEquipoService(equipos, inscripciones, jugadores, partidos, goles, tarjetas, administradores, reclamos, aliases);

    @AfterEach void limpiar() { SecurityContextHolder.clearContext(); }

    @Test
    void exigeAdministrarAmbosClubes() {
        Usuario user = usuario(8L, "DIRECTOR_EQUIPO"); autenticar(user);
        when(equipos.findById(1L)).thenReturn(Optional.of(equipo(1L)));
        when(equipos.findById(2L)).thenReturn(Optional.of(equipo(2L)));
        when(administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(1L, 8L, "ACTIVO")).thenReturn(true);
        when(administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(2L, 8L, "ACTIVO")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.fusionar(1L, 2L));
        verify(equipos, never()).delete(any());
    }

    @Test
    void bloqueaSiCompartieronUnTorneo() {
        Usuario user = usuario(8L, "DIRECTOR_EQUIPO"); autenticar(user);
        Equipo oficial = equipo(1L); Equipo duplicado = equipo(2L);
        when(equipos.findById(1L)).thenReturn(Optional.of(oficial));
        when(equipos.findById(2L)).thenReturn(Optional.of(duplicado));
        when(administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(anyLong(), eq(8L), eq("ACTIVO"))).thenReturn(true);
        Torneo torneo = new Torneo(); torneo.setIdTorneo(4L); torneo.setNombre("Apertura");
        when(inscripciones.findByEquipoIdEquipoOrderByFechaAltaDesc(1L)).thenReturn(List.of(inscripcion(oficial, torneo)));
        when(inscripciones.findByEquipoIdEquipoOrderByFechaAltaDesc(2L)).thenReturn(List.of(inscripcion(duplicado, torneo)));

        assertThrows(IllegalArgumentException.class, () -> service.fusionar(1L, 2L));
        verify(equipos, never()).delete(any());
    }

    private Equipo equipo(Long id) { Equipo equipo = new Equipo(); equipo.setIdEquipo(id); return equipo; }
    private InscripcionEquipo inscripcion(Equipo equipo, Torneo torneo) { InscripcionEquipo item = new InscripcionEquipo(); item.setEquipo(equipo); item.setTorneo(torneo); return item; }
    private Usuario usuario(Long id, String rol) { Usuario user = mock(Usuario.class); when(user.getIdUsuario()).thenReturn(id); when(user.getRol()).thenReturn(rol); return user; }
    private void autenticar(Usuario user) { SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, null, List.of())); }
}
