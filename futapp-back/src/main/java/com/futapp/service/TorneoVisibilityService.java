package com.futapp.service;

import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class TorneoVisibilityService {
    private static final Set<String> PUBLICOS = Set.of("ACTIVO", "FINALIZADO");

    public boolean puedeVer(Torneo torneo) {
        if (torneo.getEstado() != null && PUBLICOS.contains(torneo.getEstado().toUpperCase())) return true;
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (!(principal instanceof Usuario usuario)) return false;
        return usuario.tieneRol("ADMIN") || (torneo.getPropietario() != null
                && torneo.getPropietario().getIdUsuario().equals(usuario.getIdUsuario()));
    }

    public void verificar(Torneo torneo) {
        if (!puedeVer(torneo)) throw new ResourceNotFoundException("Torneo no encontrado");
    }
}
