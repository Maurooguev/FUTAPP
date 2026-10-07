package com.futapp.service;

import com.futapp.dto.EquipoRequestDTO;
import com.futapp.dto.EquipoResponseDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Torneo;
import com.futapp.model.InscripcionEquipo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.EquipoAdministradorRepository;
import com.futapp.repository.EquipoAliasRepository;
import com.futapp.model.Usuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final JugadorRepository jugadorRepository;
    private final PartidoRepository partidoRepository;
    private final TorneoAuthorizationService authorization;
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EquipoAuthorizationService equipoAuthorization;
    private final EquipoAdministradorRepository administradores;
    private final EquipoAliasRepository aliases;
    private final TorneoVisibilityService visibility;
    private final ReclamoService reclamos;

    public EquipoService(EquipoRepository equipoRepository, TorneoRepository torneoRepository,
                         JugadorRepository jugadorRepository, PartidoRepository partidoRepository,
                         TorneoAuthorizationService authorization,
                         InscripcionEquipoRepository inscripcionesEquipo,
                         InscripcionJugadorRepository inscripcionesJugador,
                         EquipoAuthorizationService equipoAuthorization,
                         EquipoAdministradorRepository administradores,
                         EquipoAliasRepository aliases, TorneoVisibilityService visibility,
                         ReclamoService reclamos) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.jugadorRepository = jugadorRepository;
        this.partidoRepository = partidoRepository;
        this.authorization = authorization;
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipoAuthorization = equipoAuthorization;
        this.administradores = administradores;
        this.aliases = aliases;
        this.visibility = visibility;
        this.reclamos = reclamos;
    }

    public List<EquipoResponseDTO> listarEquipos() {
        return equipoRepository.findAll()
                .stream()
                .filter(equipo -> visibility.puedeVer(equipo.getTorneo()))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public EquipoResponseDTO obtenerEquipoPorId(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        verificarVisibilidad(equipo);

        return convertirAResponseDTO(equipo);
    }

    public PaginaDTO<EquipoResponseDTO> listarPaginados(int pagina, int cantidad) {
        var pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(cantidad, 1), 100), Sort.by("nombre").ascending());
        return PaginaDTO.desde(equipoRepository.findByTorneoEstadoIn(List.of("ACTIVO", "FINALIZADO"), pageable)
                .map(this::convertirAResponseDTO));
    }

    public List<EquipoResponseDTO> listarMisEquipos() {
        Usuario usuario = usuarioActual();
        if (usuario.tieneRol("ADMIN")) return listarEquipos();
        if (!usuario.tieneRol("DIRECTOR_EQUIPO")) {
            throw new AccessDeniedException("Solo una cuenta de director puede administrar equipos");
        }
        return administradores.findByUsuarioIdUsuarioAndEstado(usuario.getIdUsuario(), "ACTIVO")
                .stream().map(item -> convertirAResponseDTO(item.getEquipo())).toList();
    }

    public List<EquipoResponseDTO> listarEquiposCreados() {
        Usuario usuario = usuarioActual();
        return equipoRepository.findByCreadorIdUsuarioOrderByNombre(usuario.getIdUsuario())
                .stream().map(this::convertirAResponseDTO).toList();
    }

    public EquipoResponseDTO buscarPorCodigo(String codigo) {
        String normalizado = codigo.trim();
        Equipo equipo = equipoRepository.findByCodigoPublicoIgnoreCase(normalizado)
                .orElseGet(() -> aliases.findByCodigoIgnoreCase(normalizado).map(item -> item.getEquipo())
                        .orElseThrow(() -> new ResourceNotFoundException("No encontramos un equipo con ese código")));
        verificarVisibilidad(equipo);
        return convertirAResponseDTO(equipo);
    }

    private void verificarVisibilidad(Equipo equipo) {
        if (!visibility.puedeVer(equipo.getTorneo()) && !equipoAuthorization.puedeGestionar(equipo)) {
            throw new ResourceNotFoundException("Equipo no encontrado");
        }
    }

    public boolean puedeGestionar(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        return equipoAuthorization.puedeGestionar(equipo);
    }

    public List<EquipoResponseDTO> listarEquiposPorTorneo(Long idTorneo) {
        visibility.verificar(torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado")));
        return inscripcionesEquipo.findByTorneoIdTorneo(idTorneo)
                .stream()
                .map(inscripcion -> convertirAResponseDTO(inscripcion.getEquipo(), inscripcion.getTorneo()))
                .collect(Collectors.toList());
    }

    public EquipoResponseDTO crearEquipo(EquipoRequestDTO requestDTO) {
        authorization.verificarGestion(requestDTO.getIdTorneo());
        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        Equipo equipo = new Equipo();
        equipo.setNombre(requestDTO.getNombre());
        equipo.setEscudo(requestDTO.getEscudo());
        equipo.setDescripcion(normalizarOpcional(requestDTO.getDescripcion()));
        equipo.setFotoPlantel(normalizarOpcional(requestDTO.getFotoPlantel()));
        equipo.setEmailContacto(normalizarOpcional(requestDTO.getEmailContacto()));
        equipo.setColorCamiseta(requestDTO.getColorCamiseta());
        equipo.setBarrio(requestDTO.getBarrio());
        equipo.setResponsable(requestDTO.getResponsable());
        equipo.setTelefonoContacto(requestDTO.getTelefonoContacto());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setTorneo(torneo);
        equipo.setCreador(usuarioActual());

        Equipo equipoGuardado = equipoRepository.save(equipo);
        crearInscripcionSiFalta(equipoGuardado, torneo);
        reclamos.invitarGestionPorEmail(equipoGuardado, torneo, usuarioActual(), equipoGuardado.getEmailContacto());
        return convertirAResponseDTO(equipoGuardado);
    }

    public EquipoResponseDTO actualizarEquipo(Long id, EquipoRequestDTO requestDTO) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        equipoAuthorization.verificarGestion(equipo);

        if (!equipo.getTorneo().getIdTorneo().equals(requestDTO.getIdTorneo())) {
            throw new IllegalArgumentException("Un equipo no se puede mover a otro torneo desde la edición");
        }

        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        equipo.setNombre(requestDTO.getNombre());
        equipo.setEscudo(requestDTO.getEscudo());
        equipo.setDescripcion(normalizarOpcional(requestDTO.getDescripcion()));
        equipo.setFotoPlantel(normalizarOpcional(requestDTO.getFotoPlantel()));
        equipo.setEmailContacto(normalizarOpcional(requestDTO.getEmailContacto()));
        equipo.setColorCamiseta(requestDTO.getColorCamiseta());
        equipo.setBarrio(requestDTO.getBarrio());
        equipo.setResponsable(requestDTO.getResponsable());
        equipo.setTelefonoContacto(requestDTO.getTelefonoContacto());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setTorneo(torneo);

        Equipo equipoActualizado = equipoRepository.save(equipo);
        crearInscripcionSiFalta(equipoActualizado, torneo);
        reclamos.invitarGestionPorEmail(equipoActualizado, torneo, usuarioActual(), equipoActualizado.getEmailContacto());
        return convertirAResponseDTO(equipoActualizado);
    }

    public void eliminarEquipo(Long id) {
        if (!equipoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipo no encontrado con id: " + id);
        }
        equipoAuthorization.verificarGestion(equipoRepository.findById(id).orElseThrow());

        if (!jugadorRepository.findByEquipoIdEquipo(id).isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el equipo porque tiene jugadores registrados");
        }
        if (partidoRepository.existsByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(id, id)) {
            throw new IllegalArgumentException("No se puede eliminar el equipo porque tiene partidos asociados");
        }

        inscripcionesJugador.deleteByInscripcionEquipoEquipoIdEquipo(id);
        inscripcionesEquipo.deleteByEquipoIdEquipo(id);
        equipoRepository.deleteById(id);
    }

    private EquipoResponseDTO convertirAResponseDTO(Equipo equipo) {
        return convertirAResponseDTO(equipo, equipo.getTorneo());
    }

    private EquipoResponseDTO convertirAResponseDTO(Equipo equipo, Torneo torneo) {
        boolean datosPrivados = equipoAuthorization.puedeGestionar(equipo);
        EquipoResponseDTO dto = new EquipoResponseDTO(
                equipo.getIdEquipo(),
                equipo.getNombre(),
                equipo.getEscudo(),
                equipo.getCodigoPublico(),
                equipo.getDescripcion(),
                equipo.getFotoPlantel(),
                datosPrivados ? equipo.getEmailContacto() : null,
                equipo.getColorCamiseta(),
                equipo.getBarrio(),
                datosPrivados ? equipo.getResponsable() : null,
                datosPrivados ? equipo.getTelefonoContacto() : null,
                equipo.getEstado(),
                torneo.getIdTorneo(),
                torneo.getNombre()
        );
        dto.setVerificado(administradores.existsByEquipoIdEquipoAndEstado(equipo.getIdEquipo(), "ACTIVO"));
        return dto;
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void crearInscripcionSiFalta(Equipo equipo, Torneo torneo) {
        if (inscripcionesEquipo.findByEquipoIdEquipoAndTorneoIdTorneo(
                equipo.getIdEquipo(), torneo.getIdTorneo()).isPresent()) return;
        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo);
        inscripcion.setTorneo(torneo);
        inscripcion.setEstado("ACTIVA");
        inscripcionesEquipo.save(inscripcion);
    }

    private Usuario usuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) return usuario;
        throw new AccessDeniedException("Debés iniciar sesión");
    }
}
