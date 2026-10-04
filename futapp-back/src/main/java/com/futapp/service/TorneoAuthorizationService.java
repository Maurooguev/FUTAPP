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
        verificarPropiedad(torneo);
        String estado = torneo.getEstado() == null ? "" : torneo.getEstado().toUpperCase();
        if ("FINALIZADO".equals(estado)) {
            throw new IllegalArgumentException("El torneo está finalizado y su historial ya no se puede modificar");
        }
        if (java.util.List.of("CANCELADO", "ARCHIVADO", "ELIMINADO").contains(estado)) {
            throw new IllegalArgumentException("El torneo ya no admite cambios deportivos");
        }
    }
    public void verificarPropiedad(Torneo torneo) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof Usuario usuario)) throw new AccessDeniedException("Debes iniciar sesión");
        if (usuario.tieneRol("ADMIN")) return;
        if (!usuario.tieneRol("ORGANIZADOR")) {
            throw new AccessDeniedException("Solo una cuenta de organizador puede administrar torneos");
        }
        if (torneo.getPropietario() == null || !torneo.getPropietario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new AccessDeniedException("No tienes permiso para administrar este torneo");
        }
    }
}
