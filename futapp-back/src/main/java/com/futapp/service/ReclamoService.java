package com.futapp.service;

import com.futapp.dto.*;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.*;
import com.futapp.repository.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @Transactional
public class ReclamoService {
    private final SolicitudReclamoRepository reclamos; private final EquipoRepository equipos;
    private final JugadorRepository jugadores; private final TorneoRepository torneos;
    private final EquipoAdministradorRepository administradores; private final InscripcionEquipoRepository inscripciones;
    private final NotificacionService notificaciones;
    private final EquipoAliasRepository aliases; private final FusionEquipoService fusiones; private final InscripcionService inscripcionService;
    public ReclamoService(SolicitudReclamoRepository r, EquipoRepository e, JugadorRepository j, TorneoRepository t, EquipoAdministradorRepository a, InscripcionEquipoRepository i, NotificacionService n, EquipoAliasRepository al, FusionEquipoService f, InscripcionService is) { reclamos=r; equipos=e; jugadores=j; torneos=t; administradores=a; inscripciones=i; notificaciones=n; aliases=al; fusiones=f; inscripcionService=is; }

    @Transactional
    public ReclamoResponseDTO solicitarFusion(Long idDuplicado, String codigoOficial, ReclamoRequestDTO request) {
        Usuario user=actual();
        Equipo duplicado=equipos.findById(idDuplicado).orElseThrow(() -> new ResourceNotFoundException("Equipo a vincular no encontrado"));
        Equipo oficial=equipos.findByCodigoPublicoIgnoreCase(codigoOficial.trim()).orElseGet(() -> aliases.findByCodigoIgnoreCase(codigoOficial.trim()).map(EquipoAlias::getEquipo).orElseThrow(() -> new ResourceNotFoundException("No encontramos un equipo con ese código")));
        if (duplicado.getIdEquipo().equals(oficial.getIdEquipo())) throw new IllegalArgumentException("Ese código pertenece al mismo equipo");
        Torneo torneo=torneos.findById(request.idTorneo()).orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        boolean propietario=torneo.getPropietario()!=null && torneo.getPropietario().getIdUsuario().equals(user.getIdUsuario());
        boolean administra=administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(idDuplicado,user.getIdUsuario(),"ACTIVO");
        boolean creador=duplicado.getCreador()!=null && duplicado.getCreador().getIdUsuario().equals(user.getIdUsuario());
        if (!user.tieneRol("ADMIN") && !propietario && !administra && !creador) throw new AccessDeniedException("No tenés permiso para vincular este equipo");
        if (reclamos.existsBySolicitanteIdUsuarioAndEquipoIdEquipoAndEquipoDestinoIdEquipoAndEstado(user.getIdUsuario(),idDuplicado,oficial.getIdEquipo(),"PENDIENTE")) throw new IllegalArgumentException("Esta vinculación ya está esperando aprobación");
        SolicitudReclamo item=base(user,torneo,request.mensaje()); item.setTipo("FUSION_EQUIPO"); item.setEquipo(duplicado); item.setEquipoDestino(oficial); item=reclamos.save(item); notificaciones.avisarResponsablesReclamo(item); return dto(item);
    }

    @Transactional
    public ReclamoResponseDTO invitarEquipoATorneo(Long idEquipo, ReclamoRequestDTO request) {
        Usuario user=actual();
        Equipo equipo=equipos.findById(idEquipo).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        Torneo torneo=torneos.findById(request.idTorneo()).orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        if (torneo.getPropietario()==null || (!torneo.getPropietario().getIdUsuario().equals(user.getIdUsuario()) && !user.tieneRol("ADMIN"))) throw new AccessDeniedException("Solo el organizador puede invitar equipos");
        if (inscripciones.existsByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, request.idTorneo())) throw new IllegalArgumentException("El club ya está inscrito en este torneo");
        if (reclamos.existsByEquipoIdEquipoAndTorneoIdTorneoAndTipoAndEstado(idEquipo, request.idTorneo(), "INVITACION_TORNEO", "PENDIENTE")) throw new IllegalArgumentException("Este equipo ya tiene una invitación pendiente para el torneo");
        SolicitudReclamo item=base(user,torneo,request.mensaje());
        item.setTipo("INVITACION_TORNEO"); item.setEquipo(equipo); item.setCategoria(normalizar(request.categoria()));
        item=reclamos.save(item); notificaciones.avisarResponsablesReclamo(item); return dto(item);
    }

    public ReclamoResponseDTO reclamarEquipo(Long idEquipo, ReclamoRequestDTO request) {
        Usuario user = actual(); exigirRol(user, "DIRECTOR_EQUIPO");
        Equipo equipo = equipos.findById(idEquipo).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        Torneo torneo = torneos.findById(request.idTorneo()).orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        if (!inscripciones.existsByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, torneo.getIdTorneo())) throw new IllegalArgumentException("El equipo no participa en ese torneo");
        if (administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(idEquipo, user.getIdUsuario(), "ACTIVO")) throw new IllegalArgumentException("Ya administrás este equipo");
        if (administradores.existsByEquipoIdEquipoAndEstado(idEquipo, "ACTIVO")) throw new IllegalArgumentException("Este equipo ya tiene un director verificado");
        if (reclamos.existsBySolicitanteIdUsuarioAndEquipoIdEquipoAndEstado(user.getIdUsuario(), idEquipo, "PENDIENTE")) throw new IllegalArgumentException("Ya tenés una solicitud pendiente");
        SolicitudReclamo item = base(user, torneo, request.mensaje()); item.setTipo("EQUIPO"); item.setEquipo(equipo); item=reclamos.save(item); notificaciones.avisarResponsablesReclamo(item); return dto(item);
    }

    public ReclamoResponseDTO reclamarJugador(Long idJugador, ReclamoRequestDTO request) {
        Usuario user = actual(); exigirRol(user, "JUGADOR");
        Jugador jugador = jugadores.findById(idJugador).orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
        if (jugador.getUsuario() != null) throw new IllegalArgumentException("Ese jugador ya tiene una cuenta vinculada");
        Torneo torneo = torneos.findById(request.idTorneo()).orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        if (!inscripciones.existsByEquipoIdEquipoAndTorneoIdTorneo(jugador.getEquipo().getIdEquipo(), torneo.getIdTorneo())) throw new IllegalArgumentException("El jugador no pertenece a ese torneo");
        if (reclamos.existsBySolicitanteIdUsuarioAndJugadorIdJugadorAndEstado(user.getIdUsuario(), idJugador, "PENDIENTE")) throw new IllegalArgumentException("Ya tenés una solicitud pendiente");
        SolicitudReclamo item = base(user, torneo, request.mensaje()); item.setTipo("JUGADOR"); item.setJugador(jugador); item=reclamos.save(item); notificaciones.avisarResponsablesReclamo(item); return dto(item);
    }

    @Transactional(readOnly=true) public List<ReclamoResponseDTO> pendientes() { Usuario user=actual(); return reclamos.findByEstadoOrderByFechaSolicitudAsc("PENDIENTE").stream().filter(r -> puedeRevisar(user,r)).map(this::dto).toList(); }
    @Transactional(readOnly=true) public List<ReclamoResponseDTO> revisables() { Usuario user=actual(); return reclamos.findAllByOrderByFechaSolicitudDesc().stream().filter(r -> puedeRevisar(user,r)).map(this::dto).toList(); }
    @Transactional(readOnly=true) public long cantidadPendientes() { return pendientes().size(); }
    @Transactional(readOnly=true) public List<ReclamoResponseDTO> mios() { return reclamos.findBySolicitanteIdUsuarioOrderByFechaSolicitudDesc(actual().getIdUsuario()).stream().map(this::dto).toList(); }

    public ReclamoResponseDTO resolver(Long id, boolean aprobar, String motivo) {
        Usuario user=actual(); SolicitudReclamo item=reclamos.findByIdForUpdate(id).orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        if (!"PENDIENTE".equals(item.getEstado())) throw new IllegalArgumentException("La solicitud ya fue resuelta");
        if (!puedeRevisar(user,item)) throw new AccessDeniedException("No tenés permiso para resolver esta solicitud");
        if (!aprobar && normalizar(motivo) == null) throw new IllegalArgumentException("Indicá el motivo del rechazo");
        if (aprobar && "EQUIPO".equals(item.getTipo())) {
            if (administradores.existsByEquipoIdEquipoAndEstado(item.getEquipo().getIdEquipo(), "ACTIVO"))
                throw new IllegalArgumentException("Este equipo ya tiene un director verificado");
            EquipoAdministrador link=new EquipoAdministrador(); link.setEquipo(item.getEquipo()); link.setUsuario(item.getSolicitante()); link.setAprobadoPor(user); administradores.save(link);
        }
        if (aprobar && "JUGADOR".equals(item.getTipo())) {
            if (item.getJugador().getUsuario() != null) throw new IllegalArgumentException("Este jugador ya tiene una cuenta verificada");
            Jugador vinculado = jugadores.findByUsuarioIdUsuario(item.getSolicitante().getIdUsuario()).orElse(null);
            if (vinculado != null && !vinculado.getIdJugador().equals(item.getJugador().getIdJugador())) {
                vinculado.setUsuario(null);
                jugadores.saveAndFlush(vinculado);
            }
            item.getJugador().setUsuario(item.getSolicitante()); jugadores.save(item.getJugador());
        }
        if (aprobar && "FUSION_EQUIPO".equals(item.getTipo())) {
            fusiones.fusionarAprobada(item.getEquipoDestino().getIdEquipo(), item.getEquipo().getIdEquipo());
        }
        if (aprobar && "INVITACION_TORNEO".equals(item.getTipo())) {
            inscripcionService.inscribirEquipoAprobado(item.getEquipo().getIdEquipo(), item.getTorneo().getIdTorneo(), item.getCategoria());
        }
        item.setEstado(aprobar ? "APROBADA" : "RECHAZADA"); item.setRevisor(user); item.setMotivoResolucion(normalizar(motivo)); item.setFechaResolucion(LocalDateTime.now()); item=reclamos.save(item);
        String objetivo = "JUGADOR".equals(item.getTipo()) ? item.getJugador().getNombre()+" "+item.getJugador().getApellido() : item.getEquipo().getNombre();
        notificaciones.enviar(item.getSolicitante(), "SOLICITUD", aprobar ? "Solicitud aprobada" : "Solicitud rechazada",
                "Tu solicitud sobre "+objetivo+" fue "+(aprobar ? "aprobada." : "rechazada."+(item.getMotivoResolucion()==null?"":" Motivo: "+item.getMotivoResolucion())),
                "JUGADOR".equals(item.getTipo()) ? "/jugador/"+item.getJugador().getIdJugador() : "/equipo/"+item.getEquipo().getIdEquipo());
        return dto(item);
    }

    private boolean puedeRevisar(Usuario usuario, SolicitudReclamo reclamo) {
        if (usuario.tieneRol("ADMIN")) return true;

        boolean esPropietarioDelTorneo = reclamo.getTorneo().getPropietario() != null
                && reclamo.getTorneo().getPropietario().getIdUsuario().equals(usuario.getIdUsuario());
        if ("EQUIPO".equals(reclamo.getTipo())) return esPropietarioDelTorneo;
        if ("FUSION_EQUIPO".equals(reclamo.getTipo())) {
            boolean tieneAdministradores=administradores.existsByEquipoIdEquipoAndEstado(reclamo.getEquipoDestino().getIdEquipo(), "ACTIVO");
            return administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(reclamo.getEquipoDestino().getIdEquipo(), usuario.getIdUsuario(), "ACTIVO")
                    || (!tieneAdministradores && reclamo.getEquipoDestino().getCreador()!=null && reclamo.getEquipoDestino().getCreador().getIdUsuario().equals(usuario.getIdUsuario()));
        }
        if ("INVITACION_TORNEO".equals(reclamo.getTipo())) {
            boolean tieneAdministradores=administradores.existsByEquipoIdEquipoAndEstado(reclamo.getEquipo().getIdEquipo(), "ACTIVO");
            return administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(reclamo.getEquipo().getIdEquipo(), usuario.getIdUsuario(), "ACTIVO")
                    || (!tieneAdministradores && reclamo.getEquipo().getCreador()!=null && reclamo.getEquipo().getCreador().getIdUsuario().equals(usuario.getIdUsuario()));
        }

        Long idEquipo = reclamo.getJugador().getEquipo().getIdEquipo();
        boolean esDirector = administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(
                idEquipo, usuario.getIdUsuario(), "ACTIVO");
        if (esDirector) return true;

        boolean tieneDirector = administradores.existsByEquipoIdEquipoAndEstado(idEquipo, "ACTIVO");
        return !tieneDirector && esPropietarioDelTorneo;
    }
    private SolicitudReclamo base(Usuario u,Torneo t,String m){SolicitudReclamo r=new SolicitudReclamo();r.setSolicitante(u);r.setTorneo(t);r.setMensaje(normalizar(m));return r;}
    private Usuario actual(){Object p=SecurityContextHolder.getContext().getAuthentication().getPrincipal();if(p instanceof Usuario u)return u;throw new AccessDeniedException("Debés iniciar sesión");}
    private void exigirRol(Usuario u,String rol){if(!u.tieneRol(rol)&&!u.tieneRol("ADMIN"))throw new AccessDeniedException("Tu cuenta no puede realizar este reclamo");}
    private String normalizar(String v){return v==null||v.isBlank()?null:v.trim();}
    private ReclamoResponseDTO dto(SolicitudReclamo r){
        Equipo equipo = r.getEquipo() != null ? r.getEquipo() : r.getJugador().getEquipo();
        String foto = r.getJugador() == null ? equipo.getEscudo() : r.getJugador().getFoto();
        return new ReclamoResponseDTO(r.getIdSolicitud(),r.getTipo(),r.getEstado(),
                r.getSolicitante().getIdUsuario(),r.getSolicitante().getNombre(),r.getSolicitante().getEmail(),
                r.getSolicitante().getRol(),foto,equipo.getIdEquipo(),equipo.getNombre(),equipo.getEscudo(),
                r.getJugador()==null?null:r.getJugador().getIdJugador(),
                r.getJugador()==null?null:r.getJugador().getNombre()+" "+r.getJugador().getApellido(),
                r.getEquipoDestino()==null?null:r.getEquipoDestino().getIdEquipo(),
                r.getEquipoDestino()==null?null:r.getEquipoDestino().getNombre(),
                r.getEquipoDestino()==null?null:r.getEquipoDestino().getCodigoPublico(),
                r.getTorneo().getIdTorneo(),r.getTorneo().getNombre(),r.getCategoria(),r.getMensaje(),r.getMotivoResolucion(),
                r.getRevisor()==null?null:r.getRevisor().getNombre(),r.getFechaSolicitud(),r.getFechaResolucion());
    }
}
