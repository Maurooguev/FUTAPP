package com.futapp.service;

import com.futapp.model.Equipo;
import com.futapp.model.InscripcionEquipo;
import com.futapp.model.Usuario;
import com.futapp.repository.EquipoAdministradorRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EquipoAuthorizationService {
    private final EquipoAdministradorRepository administradores;
    private final InscripcionEquipoRepository inscripciones;

    public EquipoAuthorizationService(EquipoAdministradorRepository administradores,
                                      InscripcionEquipoRepository inscripciones) {
        this.administradores = administradores;
        this.inscripciones = inscripciones;
    }

    public boolean puedeGestionar(Equipo equipo) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuario)) return false;
        if (usuario.tieneRol("ADMIN")) return true;
        if (administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(
                equipo.getIdEquipo(), usuario.getIdUsuario(), "ACTIVO")) return true;
        if (administradores.existsByEquipoIdEquipoAndEstado(equipo.getIdEquipo(), "ACTIVO")) return false;
        return inscripciones.existsByEquipoIdEquipoAndTorneoPropietarioIdUsuarioAndEstado(
                equipo.getIdEquipo(), usuario.getIdUsuario(), "ACTIVA");
    }

    public void verificarGestionPlantel(InscripcionEquipo inscripcion) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuario)) throw new AccessDeniedException("Debés iniciar sesión");
        if (usuario.tieneRol("ADMIN")
                || (inscripcion.getTorneo().getPropietario() != null
                    && inscripcion.getTorneo().getPropietario().getIdUsuario().equals(usuario.getIdUsuario()))
                || administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(
                        inscripcion.getEquipo().getIdEquipo(), usuario.getIdUsuario(), "ACTIVO")) return;
        throw new AccessDeniedException("No tenés permiso para gestionar este plantel");
    }

    public void verificarGestion(Equipo equipo) {
        if (!puedeGestionar(equipo)) {
            throw new AccessDeniedException("No tenés permiso para gestionar este equipo");
        }
    }
}
