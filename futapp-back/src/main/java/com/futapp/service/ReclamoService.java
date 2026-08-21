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
    public ReclamoService(SolicitudReclamoRepository r, EquipoRepository e, JugadorRepository j, TorneoRepository t, EquipoAdministradorRepository a, InscripcionEquipoRepository i) { reclamos=r; equipos=e; jugadores=j; torneos=t; administradores=a; inscripciones=i; }

    public ReclamoResponseDTO reclamarEquipo(Long idEquipo, ReclamoRequestDTO request) {
        Usuario user = actual(); exigirRol(user, "DIRECTOR_EQUIPO");
        Equipo equipo = equipos.findById(idEquipo).orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        Torneo torneo = torneos.findById(request.idTorneo()).orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        if (!inscripciones.existsByEquipoIdEquipoAndTorneoIdTorneo(idEquipo, torneo.getIdTorneo())) throw new IllegalArgumentException("El equipo no participa en ese torneo");
        if (administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(idEquipo, user.getIdUsuario(), "ACTIVO")) throw new IllegalArgumentException("Ya administrás este equipo");
        if (reclamos.existsBySolicitanteIdUsuarioAndEquipoIdEquipoAndEstado(user.getIdUsuario(), idEquipo, "PENDIENTE")) throw new IllegalArgumentException("Ya tenés una solicitud pendiente");
        SolicitudReclamo item = base(user, torneo, request.mensaje()); item.setTipo("EQUIPO"); item.setEquipo(equipo); return dto(reclamos.save(item));
    }

    public ReclamoResponseDTO reclamarJugador(Long idJugador, ReclamoRequestDTO request) {
        Usuario user = actual(); exigirRol(user, "JUGADOR");
        Jugador jugador = jugadores.findById(idJugador).orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
        if (jugador.getUsuario() != null) throw new IllegalArgumentException("Ese jugador ya tiene una cuenta vinculada");
        Torneo torneo = torneos.findById(request.idTorneo()).orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado"));
        if (!inscripciones.existsByEquipoIdEquipoAndTorneoIdTorneo(jugador.getEquipo().getIdEquipo(), torneo.getIdTorneo())) throw new IllegalArgumentException("El jugador no pertenece a ese torneo");
        if (reclamos.existsBySolicitanteIdUsuarioAndJugadorIdJugadorAndEstado(user.getIdUsuario(), idJugador, "PENDIENTE")) throw new IllegalArgumentException("Ya tenés una solicitud pendiente");
        SolicitudReclamo item = base(user, torneo, request.mensaje()); item.setTipo("JUGADOR"); item.setJugador(jugador); return dto(reclamos.save(item));
    }

    @Transactional(readOnly=true) public List<ReclamoResponseDTO> pendientes() { Usuario user=actual(); return reclamos.findByEstadoOrderByFechaSolicitudAsc("PENDIENTE").stream().filter(r -> puedeRevisar(user,r)).map(this::dto).toList(); }
    @Transactional(readOnly=true) public List<ReclamoResponseDTO> revisables() { Usuario user=actual(); return reclamos.findAllByOrderByFechaSolicitudDesc().stream().filter(r -> puedeRevisar(user,r)).map(this::dto).toList(); }
    @Transactional(readOnly=true) public long cantidadPendientes() { return pendientes().size(); }
    @Transactional(readOnly=true) public List<ReclamoResponseDTO> mios() { return reclamos.findBySolicitanteIdUsuarioOrderByFechaSolicitudDesc(actual().getIdUsuario()).stream().map(this::dto).toList(); }

    public ReclamoResponseDTO resolver(Long id, boolean aprobar, String motivo) {
        Usuario user=actual(); SolicitudReclamo item=reclamos.findById(id).orElseThrow(() -> new ResourceNotFoundException("Solicitud no encontrada"));
        if (!"PENDIENTE".equals(item.getEstado())) throw new IllegalArgumentException("La solicitud ya fue resuelta");
        if (!puedeRevisar(user,item)) throw new AccessDeniedException("No tenés permiso para resolver esta solicitud");
        if (!aprobar && normalizar(motivo) == null) throw new IllegalArgumentException("Indicá el motivo del rechazo");
        if (aprobar && "EQUIPO".equals(item.getTipo())) { EquipoAdministrador link=new EquipoAdministrador(); link.setEquipo(item.getEquipo()); link.setUsuario(item.getSolicitante()); link.setAprobadoPor(user); administradores.save(link); }
        if (aprobar && "JUGADOR".equals(item.getTipo())) { item.getJugador().setUsuario(item.getSolicitante()); jugadores.save(item.getJugador()); }
        item.setEstado(aprobar ? "APROBADA" : "RECHAZADA"); item.setRevisor(user); item.setMotivoResolucion(normalizar(motivo)); item.setFechaResolucion(LocalDateTime.now()); return dto(reclamos.save(item));
    }

    private boolean puedeRevisar(Usuario u, SolicitudReclamo r) { if ("ADMIN".equals(u.getRol())) return true; if (r.getTorneo().getPropietario()!=null && r.getTorneo().getPropietario().getIdUsuario().equals(u.getIdUsuario())) return true; return "JUGADOR".equals(r.getTipo()) && administradores.existsByEquipoIdEquipoAndUsuarioIdUsuarioAndEstado(r.getJugador().getEquipo().getIdEquipo(),u.getIdUsuario(),"ACTIVO"); }
    private SolicitudReclamo base(Usuario u,Torneo t,String m){SolicitudReclamo r=new SolicitudReclamo();r.setSolicitante(u);r.setTorneo(t);r.setMensaje(normalizar(m));return r;}
    private Usuario actual(){Object p=SecurityContextHolder.getContext().getAuthentication().getPrincipal();if(p instanceof Usuario u)return u;throw new AccessDeniedException("Debés iniciar sesión");}
    private void exigirRol(Usuario u,String rol){if(!rol.equals(u.getRol())&&!"ADMIN".equals(u.getRol()))throw new AccessDeniedException("Tu cuenta no puede realizar este reclamo");}
    private String normalizar(String v){return v==null||v.isBlank()?null:v.trim();}
    private ReclamoResponseDTO dto(SolicitudReclamo r){
        Equipo equipo = r.getEquipo() != null ? r.getEquipo() : r.getJugador().getEquipo();
        String foto = r.getJugador() == null ? equipo.getEscudo() : r.getJugador().getFoto();
        return new ReclamoResponseDTO(r.getIdSolicitud(),r.getTipo(),r.getEstado(),
                r.getSolicitante().getIdUsuario(),r.getSolicitante().getNombre(),r.getSolicitante().getEmail(),
                r.getSolicitante().getRol(),foto,equipo.getIdEquipo(),equipo.getNombre(),equipo.getEscudo(),
                r.getJugador()==null?null:r.getJugador().getIdJugador(),
                r.getJugador()==null?null:r.getJugador().getNombre()+" "+r.getJugador().getApellido(),
                r.getTorneo().getIdTorneo(),r.getTorneo().getNombre(),r.getMensaje(),r.getMotivoResolucion(),
                r.getRevisor()==null?null:r.getRevisor().getNombre(),r.getFechaSolicitud(),r.getFechaResolucion());
    }
}
