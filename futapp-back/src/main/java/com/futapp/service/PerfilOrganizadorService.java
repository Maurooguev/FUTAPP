package com.futapp.service;

import com.futapp.dto.PerfilOrganizadorRequestDTO;
import com.futapp.dto.PerfilOrganizadorResponseDTO;
import com.futapp.dto.TorneoPerfilOrganizadorDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.PerfilOrganizador;
import com.futapp.model.Usuario;
import com.futapp.repository.PerfilOrganizadorRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PerfilOrganizadorService {
    private final PerfilOrganizadorRepository perfiles;
    private final TorneoRepository torneos;

    public PerfilOrganizadorService(PerfilOrganizadorRepository perfiles, TorneoRepository torneos) {
        this.perfiles = perfiles;
        this.torneos = torneos;
    }

    @Transactional(readOnly = true)
    public PerfilOrganizadorResponseDTO obtenerPublico(Long idUsuario) {
        return dto(perfiles.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Este organizador todavía no creó su perfil público")));
    }

    @Transactional(readOnly = true)
    public PerfilOrganizadorResponseDTO obtenerMio() {
        Usuario usuario = actual();
        return dto(perfiles.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Todavía no creaste tu perfil de organizador")));
    }

    public PerfilOrganizadorResponseDTO crear(PerfilOrganizadorRequestDTO request) {
        Usuario usuario = organizadorActual();
        if (torneos.findByPropietarioIdUsuario(usuario.getIdUsuario()).isEmpty()) {
            throw new IllegalArgumentException("Primero creá un torneo para habilitar tu perfil de organizador");
        }
        if (perfiles.existsByUsuarioIdUsuario(usuario.getIdUsuario())) {
            throw new IllegalArgumentException("Tu perfil de organizador ya existe");
        }
        PerfilOrganizador perfil = new PerfilOrganizador();
        perfil.setUsuario(usuario);
        cargar(perfil, request);
        return dto(perfiles.save(perfil));
    }

    public PerfilOrganizadorResponseDTO actualizar(PerfilOrganizadorRequestDTO request) {
        Usuario usuario = organizadorActual();
        PerfilOrganizador perfil = perfiles.findByUsuarioIdUsuario(usuario.getIdUsuario())
                .orElseThrow(() -> new ResourceNotFoundException("Todavía no creaste tu perfil de organizador"));
        cargar(perfil, request);
        return dto(perfiles.save(perfil));
    }

    private void cargar(PerfilOrganizador perfil, PerfilOrganizadorRequestDTO request) {
        perfil.setNombrePublico(request.nombrePublico().trim());
        perfil.setDescripcion(request.descripcion().trim());
        perfil.setFotoPerfil(opcional(request.fotoPerfil()));
        perfil.setFotoPortada(opcional(request.fotoPortada()));
    }

    private Usuario organizadorActual() {
        Usuario usuario = actual();
        if (!usuario.tieneRol("ORGANIZADOR") && !usuario.tieneRol("ADMIN")) {
            throw new AccessDeniedException("Sólo los creadores de torneos pueden crear este perfil");
        }
        return usuario;
    }

    private Usuario actual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) return usuario;
        throw new AccessDeniedException("Debés iniciar sesión");
    }

    private String opcional(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    private PerfilOrganizadorResponseDTO dto(PerfilOrganizador perfil) {
        var torneosPublicos = torneos.findByPropietarioIdUsuario(perfil.getUsuario().getIdUsuario()).stream()
                .filter(torneo -> !"BORRADOR".equalsIgnoreCase(torneo.getEstado()))
                .map(torneo -> new TorneoPerfilOrganizadorDTO(torneo.getIdTorneo(), torneo.getNombre(),
                        torneo.getDescripcion(), torneo.getUbicacion(), torneo.getEstado(), torneo.getFormato(),
                        torneo.getCantidadEquipos(), torneo.getCodigoPublico()))
                .toList();
        return new PerfilOrganizadorResponseDTO(perfil.getIdPerfil(), perfil.getUsuario().getIdUsuario(),
                perfil.getNombrePublico(), perfil.getDescripcion(), perfil.getFotoPerfil(),
                perfil.getFotoPortada(), torneosPublicos);
    }
}
