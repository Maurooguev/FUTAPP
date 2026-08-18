package com.futapp.service;

import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import com.futapp.repository.TorneoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TorneoAuthorizationService {
    private final TorneoRepository torneos;
    public TorneoAuthorizationService(TorneoRepository torneos) { this.torneos = torneos; }
    public void verificarGestion(Long idTorneo) { verificarGestion(torneos.findById(idTorneo).orElseThrow()); }
    public void verificarGestion(Torneo torneo) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuario)) throw new AccessDeniedException("Debes iniciar sesión");
        if ("ADMIN".equals(usuario.getRol())) return;
        if (torneo.getPropietario() == null || !torneo.getPropietario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new AccessDeniedException("No tienes permiso para administrar este torneo");
        }
    }
}
