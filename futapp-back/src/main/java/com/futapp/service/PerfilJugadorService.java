package com.futapp.service;
import com.futapp.dto.*; import com.futapp.exception.ResourceNotFoundException; import com.futapp.model.*; import com.futapp.repository.JugadorRepository;
import org.springframework.security.access.AccessDeniedException; import org.springframework.security.core.context.SecurityContextHolder; import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional;
@Service @Transactional
public class PerfilJugadorService {
 private final JugadorRepository jugadores; public PerfilJugadorService(JugadorRepository j){jugadores=j;}
 @Transactional(readOnly=true) public PerfilJugadorResponseDTO mio(){return dto(vinculado());}
 public PerfilJugadorResponseDTO actualizar(PerfilJugadorPersonalDTO r){Jugador j=vinculado();j.setApodo(n(r.apodo()));j.setFechaNacimiento(n(r.fechaNacimiento()));j.setPiernaHabil(n(r.piernaHabil()));j.setEquipoFavorito(n(r.equipoFavorito()));j.setBarrio(n(r.barrio()));j.setAlturaCm(r.alturaCm());j.setNacionalidad(n(r.nacionalidad()));j.setBiografia(n(r.biografia()));j.setInstagram(n(r.instagram()));return dto(jugadores.save(j));}
 private Jugador vinculado(){Usuario u=actual();return jugadores.findByUsuarioIdUsuario(u.getIdUsuario()).orElseThrow(()->new ResourceNotFoundException("Tu cuenta todavía no está vinculada con un jugador"));}
 private Usuario actual(){Object p=SecurityContextHolder.getContext().getAuthentication().getPrincipal();if(p instanceof Usuario u){if(u.tieneRol("JUGADOR")||u.tieneRol("ADMIN"))return u;throw new AccessDeniedException("Solo una cuenta de jugador puede administrar este perfil");}throw new AccessDeniedException("Debés iniciar sesión");}
 private String n(String v){return v==null||v.isBlank()?null:v.trim();}
 private PerfilJugadorResponseDTO dto(Jugador j){return new PerfilJugadorResponseDTO(j.getIdJugador(),j.getCodigoPublico(),j.getNombre(),j.getApellido(),j.getFoto(),j.getApodo(),j.getFechaNacimiento(),j.getPiernaHabil(),j.getEquipoFavorito(),j.getBarrio(),j.getAlturaCm(),j.getNacionalidad(),j.getBiografia(),j.getInstagram());}
}
