package com.futapp.service;

import com.futapp.model.Equipo;
import com.futapp.model.Usuario;
import com.futapp.repository.EquipoAdministradorRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class EquipoAuthorizationService {
    private final EquipoAdministradorRepository administradores;

    public EquipoAuthorizationService(EquipoAdministradorRepository administradores) {
        this.administradores = administradores;
    }

    public boolean puedeGestionar(Equipo equipo) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuario)) return false;
        if ("ADMIN".equals(usuario.getRol())) return true;
        if (equipo.getTorneo() != null && equipo.getTorneo().getPropietario() != null
                && equipo.getTorneo().getPropietario().getIdUsuario().equals(usuario.getIdUsuario())) return true;
        return administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(
                equipo.getIdEquipo(), usuario.getIdUsuario(), "ACTIVO");
    }

    public void verificarGestion(Equipo equipo) {
        if (!puedeGestionar(equipo)) {
            throw new AccessDeniedException("No tenés permiso para gestionar este equipo");
        }
    }
}
