package com.futapp.service;

import com.futapp.dto.NotificacionResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.*;
import com.futapp.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class NotificacionService {
    private final NotificacionRepository notificaciones;
    private final EquipoAdministradorRepository administradores;
    private final InscripcionJugadorRepository inscripcionesJugador;
    public NotificacionService(NotificacionRepository notificaciones, EquipoAdministradorRepository administradores,
                               InscripcionJugadorRepository inscripcionesJugador) {
        this.notificaciones = notificaciones; this.administradores = administradores; this.inscripcionesJugador = inscripcionesJugador;
    }
    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listar() {
        return notificaciones.findTop100ByUsuarioIdUsuarioOrderByFechaCreacionDesc(actual().getIdUsuario()).stream().map(this::dto).toList();
    }
    @Transactional(readOnly = true)
    public long noLeidas() { return notificaciones.countByUsuarioIdUsuarioAndFechaLecturaIsNull(actual().getIdUsuario()); }
    public NotificacionResponseDTO marcarLeida(Long id) {
        Notificacion item = propia(id);
        if (item.getFechaLectura() == null) item.setFechaLectura(LocalDateTime.now());
        return dto(notificaciones.save(item));
    }
    public void marcarTodasLeidas() {
        notificaciones.findTop100ByUsuarioIdUsuarioOrderByFechaCreacionDesc(actual().getIdUsuario()).stream()
                .filter(item -> item.getFechaLectura() == null).forEach(item -> item.setFechaLectura(LocalDateTime.now()));
    }
    public void enviar(Usuario usuario, String tipo, String titulo, String mensaje, String url) {
        if (usuario == null) return;
        Notificacion item = new Notificacion(); item.setUsuario(usuario); item.setTipo(tipo); item.setTitulo(titulo);
        item.setMensaje(mensaje); item.setUrl(url); notificaciones.save(item);
    }
    public void avisarResponsablesReclamo(SolicitudReclamo reclamo) {
        Set<Usuario> destinatarios = new LinkedHashSet<>();
        if (reclamo.getTorneo().getPropietario() != null) destinatarios.add(reclamo.getTorneo().getPropietario());
        if ("JUGADOR".equals(reclamo.getTipo())) {
            destinatarios.clear();
            administradores.findByEquipoIdEquipoAndEstado(reclamo.getJugador().getEquipo().getIdEquipo(), "ACTIVO")
                    .forEach(link -> destinatarios.add(link.getUsuario()));
            if (destinatarios.isEmpty() && reclamo.getTorneo().getPropietario() != null) {
                destinatarios.add(reclamo.getTorneo().getPropietario());
            }
        }
        if ("FUSION_EQUIPO".equals(reclamo.getTipo())) {
            destinatarios.clear();
            administradores.findByEquipoIdEquipoAndEstado(reclamo.getEquipoDestino().getIdEquipo(), "ACTIVO")
                    .forEach(link -> destinatarios.add(link.getUsuario()));
            if (destinatarios.isEmpty() && reclamo.getEquipoDestino().getCreador() != null) destinatarios.add(reclamo.getEquipoDestino().getCreador());
        }
        if ("INVITACION_TORNEO".equals(reclamo.getTipo())) {
            destinatarios.clear();
            administradores.findByEquipoIdEquipoAndEstado(reclamo.getEquipo().getIdEquipo(), "ACTIVO")
                    .forEach(link -> destinatarios.add(link.getUsuario()));
            if (destinatarios.isEmpty() && reclamo.getEquipo().getCreador() != null) destinatarios.add(reclamo.getEquipo().getCreador());
        }
        destinatarios.forEach(user -> enviar(user, "SOLICITUD", "Nueva solicitud pendiente",
                "INVITACION_TORNEO".equals(reclamo.getTipo())
                        ? reclamo.getSolicitante().getNombre() + " invitó a " + reclamo.getEquipo().getNombre() + " al torneo " + reclamo.getTorneo().getNombre()
                        : reclamo.getSolicitante().getNombre() + " solicita vincularse con " +
                        ("FUSION_EQUIPO".equals(reclamo.getTipo()) ? "fusionar " + reclamo.getEquipo().getNombre() + " con " + reclamo.getEquipoDestino().getNombre() : "EQUIPO".equals(reclamo.getTipo()) ? reclamo.getEquipo().getNombre() : reclamo.getJugador().getNombre() + " " + reclamo.getJugador().getApellido()),
                "/solicitudes"));
    }
    public void avisarPartido(Partido partido, boolean resultado) {
        Set<Usuario> destinatarios = new LinkedHashSet<>();
        agregarEquipo(destinatarios, partido.getEquipoLocal().getIdEquipo(), partido.getTorneo().getIdTorneo());
        agregarEquipo(destinatarios, partido.getEquipoVisitante().getIdEquipo(), partido.getTorneo().getIdTorneo());
        String cruce = partido.getEquipoLocal().getNombre() + " vs " + partido.getEquipoVisitante().getNombre();
        String mensaje = resultado
                ? cruce + " terminó " + partido.getGolesLocal() + " - " + partido.getGolesVisitante()
                : "Se actualizaron los datos de " + cruce;
        destinatarios.forEach(user -> enviar(user, resultado ? "RESULTADO" : "PARTIDO",
                resultado ? "Resultado cargado" : "Partido actualizado", mensaje, "/partido/" + partido.getIdPartido()));
    }
    private void agregarEquipo(Set<Usuario> users, Long idEquipo, Long idTorneo) {
        administradores.findByEquipoIdEquipoAndEstado(idEquipo, "ACTIVO").forEach(link -> users.add(link.getUsuario()));
        inscripcionesJugador.findByInscripcionEquipoEquipoIdEquipoAndInscripcionEquipoTorneoIdTorneoAndEstado(idEquipo, idTorneo, "ACTIVA")
                .stream().map(InscripcionJugador::getJugador).map(Jugador::getUsuario).filter(Objects::nonNull).forEach(users::add);
    }
    private Notificacion propia(Long id) {
        Notificacion item = notificaciones.findById(id).orElseThrow(() -> new ResourceNotFoundException("Notificación no encontrada"));
        if (!item.getUsuario().getIdUsuario().equals(actual().getIdUsuario())) throw new AccessDeniedException("No podés modificar esta notificación");
        return item;
    }
    private Usuario actual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario user) return user;
        throw new AccessDeniedException("Debés iniciar sesión");
    }
    private NotificacionResponseDTO dto(Notificacion item) {
        return new NotificacionResponseDTO(item.getIdNotificacion(), item.getTipo(), item.getTitulo(), item.getMensaje(),
                item.getUrl(), item.getFechaLectura() != null, item.getFechaCreacion());
    }
}
