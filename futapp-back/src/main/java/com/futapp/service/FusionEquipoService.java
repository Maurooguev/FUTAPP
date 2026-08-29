package com.futapp.service;

import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Usuario;
import com.futapp.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
public class FusionEquipoService {
    private final EquipoRepository equipos;
    private final InscripcionEquipoRepository inscripciones;
    private final JugadorRepository jugadores;
    private final PartidoRepository partidos;
    private final GolRepository goles;
    private final TarjetaRepository tarjetas;
    private final EquipoAdministradorRepository administradores;
    private final SolicitudReclamoRepository reclamos;

    public FusionEquipoService(EquipoRepository equipos, InscripcionEquipoRepository inscripciones,
                               JugadorRepository jugadores, PartidoRepository partidos,
                               GolRepository goles, TarjetaRepository tarjetas,
                               EquipoAdministradorRepository administradores,
                               SolicitudReclamoRepository reclamos) {
        this.equipos = equipos; this.inscripciones = inscripciones; this.jugadores = jugadores;
        this.partidos = partidos; this.goles = goles; this.tarjetas = tarjetas;
        this.administradores = administradores; this.reclamos = reclamos;
    }

    @Transactional
    public void fusionar(Long idOficial, Long idDuplicado) {
        if (idOficial.equals(idDuplicado)) throw new IllegalArgumentException("Seleccioná dos clubes diferentes");
        Equipo oficial = equipos.findById(idOficial).orElseThrow(() -> new ResourceNotFoundException("Club oficial no encontrado"));
        Equipo duplicado = equipos.findById(idDuplicado).orElseThrow(() -> new ResourceNotFoundException("Club duplicado no encontrado"));
        Usuario usuario = usuarioActual();
        if (!puedeFusionar(usuario, oficial, duplicado)) {
            throw new AccessDeniedException("Debés administrar ambos clubes para poder fusionarlos");
        }

        var inscripcionesOficial = inscripciones.findByEquipoIdEquipoOrderByFechaAltaDesc(idOficial);
        var inscripcionesDuplicado = inscripciones.findByEquipoIdEquipoOrderByFechaAltaDesc(idDuplicado);
        Set<Long> torneosOficial = new HashSet<>();
        inscripcionesOficial.forEach(item -> torneosOficial.add(item.getTorneo().getIdTorneo()));
        var repetidos = inscripcionesDuplicado.stream().filter(item -> torneosOficial.contains(item.getTorneo().getIdTorneo()))
                .map(item -> item.getTorneo().getNombre()).distinct().toList();
        if (!repetidos.isEmpty()) {
            throw new IllegalArgumentException("Los dos perfiles participaron en el mismo torneo: " + String.join(", ", repetidos) + ". Esta fusión necesita revisión manual.");
        }

        inscripcionesDuplicado.forEach(item -> item.setEquipo(oficial));
        inscripciones.saveAll(inscripcionesDuplicado);

        Set<Integer> dorsales = new HashSet<>();
        jugadores.findByEquipoIdEquipo(idOficial).stream().map(item -> item.getNumeroCamiseta()).filter(Objects::nonNull).forEach(dorsales::add);
        var jugadoresDuplicado = jugadores.findByEquipoIdEquipo(idDuplicado);
        jugadoresDuplicado.forEach(jugador -> {
            if (jugador.getNumeroCamiseta() != null && !dorsales.add(jugador.getNumeroCamiseta())) jugador.setNumeroCamiseta(null);
            jugador.setEquipo(oficial);
        });
        jugadores.saveAll(jugadoresDuplicado);

        var partidosDuplicado = partidos.findByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(idDuplicado, idDuplicado);
        partidosDuplicado.forEach(partido -> {
            if (partido.getEquipoLocal().getIdEquipo().equals(idDuplicado)) partido.setEquipoLocal(oficial);
            if (partido.getEquipoVisitante().getIdEquipo().equals(idDuplicado)) partido.setEquipoVisitante(oficial);
        });
        partidos.saveAll(partidosDuplicado);

        var golesDuplicado = goles.findByEquipoIdEquipo(idDuplicado); golesDuplicado.forEach(gol -> gol.setEquipo(oficial)); goles.saveAll(golesDuplicado);
        var tarjetasDuplicado = tarjetas.findByEquipoIdEquipo(idDuplicado); tarjetasDuplicado.forEach(tarjeta -> tarjeta.setEquipo(oficial)); tarjetas.saveAll(tarjetasDuplicado);
        reclamos.findByEquipoIdEquipo(idDuplicado).forEach(reclamo -> reclamo.setEquipo(oficial));

        administradores.findByEquipoIdEquipo(idDuplicado).forEach(admin -> {
            if (administradores.existsByEquipoIdEquipoAndUsuarioIdUsuario(idOficial, admin.getUsuario().getIdUsuario())) administradores.delete(admin);
            else admin.setEquipo(oficial);
        });

        completarDatosFaltantes(oficial, duplicado);
        equipos.save(oficial);
        equipos.delete(duplicado);
    }

    private boolean puedeFusionar(Usuario usuario, Equipo oficial, Equipo duplicado) {
        if ("ADMIN".equals(usuario.getRol())) return true;
        boolean administraAmbos = administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(oficial.getIdEquipo(), usuario.getIdUsuario(), "ACTIVO")
                && administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(duplicado.getIdEquipo(), usuario.getIdUsuario(), "ACTIVO");
        boolean creoAmbos = oficial.getCreador() != null && duplicado.getCreador() != null
                && oficial.getCreador().getIdUsuario().equals(usuario.getIdUsuario())
                && duplicado.getCreador().getIdUsuario().equals(usuario.getIdUsuario());
        return administraAmbos || creoAmbos;
    }

    private void completarDatosFaltantes(Equipo oficial, Equipo duplicado) {
        if (vacio(oficial.getEscudo())) oficial.setEscudo(duplicado.getEscudo());
        if (vacio(oficial.getDescripcion())) oficial.setDescripcion(duplicado.getDescripcion());
        if (vacio(oficial.getFotoPlantel())) oficial.setFotoPlantel(duplicado.getFotoPlantel());
        if (vacio(oficial.getEmailContacto())) oficial.setEmailContacto(duplicado.getEmailContacto());
        if (vacio(oficial.getTelefonoContacto())) oficial.setTelefonoContacto(duplicado.getTelefonoContacto());
    }

    private boolean vacio(String valor) { return valor == null || valor.isBlank(); }
    private Usuario usuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) return usuario;
        throw new AccessDeniedException("Debés iniciar sesión");
    }
}
