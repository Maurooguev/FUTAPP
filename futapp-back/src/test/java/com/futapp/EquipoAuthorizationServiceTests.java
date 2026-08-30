package com.futapp;

import com.futapp.model.Equipo;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import com.futapp.repository.EquipoAdministradorRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.service.EquipoAuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EquipoAuthorizationServiceTests {
    private final EquipoAdministradorRepository administradores = mock(EquipoAdministradorRepository.class);
    private final InscripcionEquipoRepository inscripciones = mock(InscripcionEquipoRepository.class);
    private final EquipoAuthorizationService service = new EquipoAuthorizationService(administradores, inscripciones);

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void organizadorPuedeGestionarEquipoSinDirectorInscritoEnSuTorneo() {
        Usuario organizador = usuario(7L, "ORGANIZADOR");
        Equipo equipo = equipo(45L);
        autenticar(organizador);
        when(administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(45L, 7L, "ACTIVO")).thenReturn(false);
        when(administradores.existsByEquipoIdEquipoAndEstado(45L, "ACTIVO")).thenReturn(false);
        when(inscripciones.existsByEquipoIdEquipoAndTorneoPropietarioIdUsuarioAndEstado(45L, 7L, "ACTIVA")).thenReturn(true);

        assertTrue(service.puedeGestionar(equipo));
    }

    @Test
    void organizadorNoGestionaEquipoQueYaTieneDirector() {
        Usuario organizador = usuario(7L, "ORGANIZADOR");
        Equipo equipo = equipo(43L);
        autenticar(organizador);
        when(administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(43L, 7L, "ACTIVO")).thenReturn(false);
        when(administradores.existsByEquipoIdEquipoAndEstado(43L, "ACTIVO")).thenReturn(true);

        assertFalse(service.puedeGestionar(equipo));
    }

    @Test
    void propietarioDelTorneoGestionaElPlantelAunqueSuRolSeaDirector() {
        Usuario propietario = usuario(7L, "DIRECTOR_EQUIPO");
        Equipo equipo = equipo(45L);
        Torneo torneo = new Torneo();
        torneo.setPropietario(propietario);
        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo);
        inscripcion.setTorneo(torneo);
        autenticar(propietario);

        assertDoesNotThrow(() -> service.verificarGestionPlantel(inscripcion));
    }

    private Usuario usuario(Long id, String rol) {
        Usuario usuario = mock(Usuario.class);
        when(usuario.getIdUsuario()).thenReturn(id);
        when(usuario.getRol()).thenReturn(rol);
        when(usuario.tieneRol(rol)).thenReturn(true);
        return usuario;
    }

    private Equipo equipo(Long id) {
        Equipo equipo = new Equipo();
        equipo.setIdEquipo(id);
        return equipo;
    }

    private void autenticar(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, java.util.List.of()));
    }
}
