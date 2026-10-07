package com.futapp.service;

import com.futapp.dto.JugadorRequestDTO;
import com.futapp.dto.JugadorResponseDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.dto.EstadisticasJugadorDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Jugador;
import com.futapp.model.InscripcionJugador;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.GolRepository;
import com.futapp.repository.TarjetaRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.JugadorAliasRepository;
import com.futapp.repository.PartidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import com.futapp.model.Usuario;

import java.util.List;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
@Transactional
public class JugadorService {

    private final JugadorRepository jugadorRepository;
    private final EquipoRepository equipoRepository;
    private final GolRepository golRepository;
    private final TarjetaRepository tarjetaRepository;
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EquipoAuthorizationService equipoAuthorization;
    private final JugadorAliasRepository aliases;
    private final TorneoVisibilityService visibility;
    private final PartidoRepository partidos;

    public JugadorService(JugadorRepository jugadorRepository, EquipoRepository equipoRepository,
                          GolRepository golRepository, TarjetaRepository tarjetaRepository,
                          InscripcionEquipoRepository inscripcionesEquipo,
                          InscripcionJugadorRepository inscripcionesJugador,
                          EquipoAuthorizationService equipoAuthorization,
                          JugadorAliasRepository aliases, TorneoVisibilityService visibility,
                          PartidoRepository partidos) {
        this.jugadorRepository = jugadorRepository;
        this.equipoRepository = equipoRepository;
        this.golRepository = golRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipoAuthorization = equipoAuthorization;
        this.aliases = aliases;
        this.visibility = visibility;
        this.partidos = partidos;
    }

    public List<JugadorResponseDTO> listarJugadores() {
        return jugadorRepository.findAll()
                .stream()
                .filter(jugador -> visibility.puedeVer(jugador.getEquipo().getTorneo()))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public JugadorResponseDTO obtenerJugadorPorId(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        verificarVisibilidad(jugador);

        return convertirAResponseDTO(jugador);
    }

    @Transactional(readOnly = true)
    public EstadisticasJugadorDTO estadisticas(Long id) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        verificarVisibilidad(jugador);

        List<InscripcionJugador> trayectoria = inscripcionesJugador.findByJugadorIdJugadorOrderByFechaAltaDesc(id);
        long goles = golRepository.countByJugadorIdJugador(id);
        long amarillas = tarjetaRepository.countByJugadorIdJugadorAndTipoTarjetaIgnoreCase(id, "AMARILLA");
        long rojas = tarjetaRepository.countByJugadorIdJugadorAndTipoTarjetaIgnoreCase(id, "ROJA");
        Set<Long> torneos = new HashSet<>();
        Set<Long> equipos = new HashSet<>();
        var campeonatos = new LinkedHashMap<Long, EstadisticasJugadorDTO.CampeonatoJugadorDTO>();
        long vecesGoleador = 0;
        boolean fueDelantero = false;

        for (InscripcionJugador alta : trayectoria) {
            var inscripcion = alta.getInscripcionEquipo();
            var torneo = inscripcion.getTorneo();
            var equipo = inscripcion.getEquipo();
            torneos.add(torneo.getIdTorneo());
            equipos.add(equipo.getIdEquipo());
            String posicion = alta.getPosicion() == null ? "" : alta.getPosicion().toLowerCase(Locale.ROOT);
            fueDelantero = fueDelantero || posicion.contains("delantero");

            if (torneo.getCampeon() != null && torneo.getCampeon().getIdEquipo().equals(equipo.getIdEquipo())) {
                campeonatos.putIfAbsent(torneo.getIdTorneo(), new EstadisticasJugadorDTO.CampeonatoJugadorDTO(
                        torneo.getIdTorneo(), torneo.getNombre(), equipo.getIdEquipo(), equipo.getNombre()));
            }
            if ("FINALIZADO".equalsIgnoreCase(torneo.getEstado())) {
                var goleadores = golRepository.obtenerGoleadoresPorTorneo(torneo.getIdTorneo());
                if (!goleadores.isEmpty()) {
                    long maximo = goleadores.getFirst().getCantidadGoles();
                    if (goleadores.stream().anyMatch(g -> g.getIdJugador().equals(id) && g.getCantidadGoles() == maximo)) vecesGoleador++;
                }
            }
        }

        long partidosDelantero = fueDelantero ? partidos.countPartidosComputablesDelantero(id) : 0;
        Double promedio = partidosDelantero > 0
                ? Math.round((goles * 100.0) / partidosDelantero) / 100.0 : null;
        Long vallas = trayectoria.stream().anyMatch(alta -> {
            String posicion = alta.getPosicion() == null ? "" : alta.getPosicion().toLowerCase(Locale.ROOT);
            return posicion.contains("arquero") || posicion.contains("defensor");
        }) ? partidos.countVallasInvictasJugador(id) : null;

        if (!fueDelantero) vecesGoleador = 0;

        return new EstadisticasJugadorDTO(partidos.countPartidosComputablesJugador(id), goles, amarillas, rojas, partidos.countByMvpIdJugador(id),
                torneos.size(), equipos.size(), List.copyOf(campeonatos.values()), vecesGoleador, promedio, vallas);
    }

    public List<JugadorResponseDTO> listarJugadoresPorEquipo(Long idEquipo) {
        Equipo equipo = equipoRepository.findById(idEquipo)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado"));
        if (!visibility.puedeVer(equipo.getTorneo()) && !equipoAuthorization.puedeGestionar(equipo)) {
            throw new ResourceNotFoundException("Equipo no encontrado");
        }
        return jugadorRepository.findByEquipoIdEquipo(idEquipo)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public JugadorResponseDTO crearJugador(JugadorRequestDTO requestDTO) {
        Equipo equipo = equipoRepository.findById(requestDTO.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + requestDTO.getIdEquipo()));
        equipoAuthorization.verificarGestion(equipo);

        validarDatosUnicos(requestDTO, null);

        Jugador jugador = new Jugador();
        jugador.setNombre(requestDTO.getNombre());
        jugador.setApellido(requestDTO.getApellido());
        jugador.setDni(normalizarOpcional(requestDTO.getDni()));
        jugador.setFechaNacimiento(requestDTO.getFechaNacimiento());
        jugador.setNumeroCamiseta(requestDTO.getNumeroCamiseta());
        jugador.setPosicion(requestDTO.getPosicion());
        jugador.setFoto(requestDTO.getFoto());
        jugador.setApodo(normalizarOpcional(requestDTO.getApodo()));
        jugador.setPiernaHabil(normalizarOpcional(requestDTO.getPiernaHabil()));
        jugador.setEstado(requestDTO.getEstado());
        jugador.setEquipo(equipo);

        Jugador jugadorGuardado = jugadorRepository.save(jugador);
        crearInscripcionSiFalta(jugadorGuardado, equipo);
        return convertirAResponseDTO(jugadorGuardado);
    }

    public JugadorResponseDTO actualizarJugador(Long id, JugadorRequestDTO requestDTO) {
        Jugador jugador = jugadorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + id));
        equipoAuthorization.verificarGestion(jugador.getEquipo());

        if (!jugador.getEquipo().getIdEquipo().equals(requestDTO.getIdEquipo())) {
            throw new IllegalArgumentException("Un jugador no se puede mover a otro equipo desde la edición");
        }

        Equipo equipo = equipoRepository.findById(requestDTO.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + requestDTO.getIdEquipo()));

        validarDatosUnicos(requestDTO, id);

        Usuario actual = usuarioActual();
        boolean identidadProtegida = jugador.getUsuario() != null
                && !jugador.getUsuario().getIdUsuario().equals(actual.getIdUsuario());
        if (!identidadProtegida) {
            jugador.setNombre(requestDTO.getNombre());
            jugador.setApellido(requestDTO.getApellido());
            jugador.setDni(normalizarOpcional(requestDTO.getDni()));
            jugador.setFechaNacimiento(requestDTO.getFechaNacimiento());
        }
        jugador.setNumeroCamiseta(requestDTO.getNumeroCamiseta());
        jugador.setPosicion(requestDTO.getPosicion());
        jugador.setFoto(requestDTO.getFoto());
        jugador.setApodo(normalizarOpcional(requestDTO.getApodo()));
        jugador.setPiernaHabil(normalizarOpcional(requestDTO.getPiernaHabil()));
        jugador.setEstado(requestDTO.getEstado());
        jugador.setEquipo(equipo);

        Jugador jugadorActualizado = jugadorRepository.save(jugador);
        crearInscripcionSiFalta(jugadorActualizado, equipo);
        return convertirAResponseDTO(jugadorActualizado);
    }

    public void eliminarJugador(Long id) {
        if (!jugadorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jugador no encontrado con id: " + id);
        }
        equipoAuthorization.verificarGestion(jugadorRepository.findById(id).orElseThrow().getEquipo());

        if (golRepository.existsByJugadorIdJugador(id) || tarjetaRepository.existsByJugadorIdJugador(id)) {
            throw new IllegalArgumentException("No se puede eliminar el jugador porque tiene goles o tarjetas asociados");
        }

        inscripcionesJugador.deleteByJugadorIdJugador(id);
        jugadorRepository.deleteById(id);
    }

    private JugadorResponseDTO convertirAResponseDTO(Jugador jugador) {
        boolean datosPrivados = puedeVerDatosPrivados(jugador);
        JugadorResponseDTO dto = new JugadorResponseDTO(
                jugador.getIdJugador(),
                jugador.getNombre(),
                jugador.getApellido(),
                datosPrivados ? jugador.getDni() : null,
                jugador.getCodigoPublico(),
                jugador.getApodo(),
                jugador.getPiernaHabil(),
                datosPrivados ? jugador.getFechaNacimiento() : null,
                jugador.getNumeroCamiseta(),
                jugador.getPosicion(),
                jugador.getFoto(),
                jugador.getEstado(),
                jugador.getEquipo().getIdEquipo(),
                jugador.getEquipo().getNombre()
        );
        dto.setVerificado(jugador.getUsuario() != null);
        dto.setVisitas(jugador.getVisitas());
        return dto;
    }

    private boolean puedeVerDatosPrivados(Jugador jugador) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication == null ? null : authentication.getPrincipal();
        if (principal instanceof Usuario usuario && jugador.getUsuario() != null
                && jugador.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) return true;
        return equipoAuthorization.puedeGestionar(jugador.getEquipo());
    }

    public long registrarVisita(Long id) {
        if (jugadorRepository.incrementarVisitas(id) == 0) throw new ResourceNotFoundException("Jugador no encontrado con id: " + id);
        return jugadorRepository.findById(id).map(Jugador::getVisitas).orElse(0L);
    }

    public PaginaDTO<JugadorResponseDTO> listarPaginados(int pagina, int cantidad) {
        var pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(cantidad, 1), 100),
                Sort.by("apellido").ascending().and(Sort.by("nombre").ascending()));
        return PaginaDTO.desde(jugadorRepository.findByEquipoTorneoEstadoIn(List.of("ACTIVO", "FINALIZADO"), pageable)
                .map(this::convertirAResponseDTO));
    }

    public JugadorResponseDTO buscarPorCodigo(String codigo) {
        String normalizado = codigo.trim();
        Jugador jugador = jugadorRepository.findByCodigoPublicoIgnoreCase(normalizado)
                .orElseGet(() -> aliases.findByCodigoIgnoreCase(normalizado).map(item -> item.getJugador())
                        .orElseThrow(() -> new ResourceNotFoundException("No encontramos un jugador con ese código")));
        verificarVisibilidad(jugador);
        return convertirAResponseDTO(jugador);
    }

    private void verificarVisibilidad(Jugador jugador) {
        if (!visibility.puedeVer(jugador.getEquipo().getTorneo()) && !puedeVerDatosPrivados(jugador)) {
            throw new ResourceNotFoundException("Jugador no encontrado");
        }
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Usuario usuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) return usuario;
        throw new org.springframework.security.access.AccessDeniedException("Debés iniciar sesión");
    }

    private void validarDatosUnicos(JugadorRequestDTO requestDTO, Long idJugador) {
        boolean camisetaRepetida = idJugador == null
                ? jugadorRepository.existsByEquipoIdEquipoAndNumeroCamiseta(requestDTO.getIdEquipo(), requestDTO.getNumeroCamiseta())
                : jugadorRepository.existsByEquipoIdEquipoAndNumeroCamisetaAndIdJugadorNot(requestDTO.getIdEquipo(), requestDTO.getNumeroCamiseta(), idJugador);
        if (camisetaRepetida) {
            throw new IllegalArgumentException("Ya existe un jugador con ese número de camiseta en el equipo");
        }

        String dni = normalizarOpcional(requestDTO.getDni());
        if (dni != null) {
            boolean dniRepetido = idJugador == null
                    ? jugadorRepository.existsByDni(dni)
                    : jugadorRepository.existsByDniAndIdJugadorNot(dni, idJugador);
            if (dniRepetido) {
                throw new IllegalArgumentException("Ya existe un jugador con ese DNI");
            }
        }
    }

    private void crearInscripcionSiFalta(Jugador jugador, Equipo equipo) {
        var inscripcionEquipo = inscripcionesEquipo
                .findByEquipoIdEquipoAndTorneoIdTorneo(
                        equipo.getIdEquipo(), equipo.getTorneo().getIdTorneo())
                .orElseThrow(() -> new IllegalStateException("El equipo no tiene una inscripción activa"));
        InscripcionJugador inscripcion = inscripcionesJugador
                .findByInscripcionEquipoIdInscripcionAndJugadorIdJugador(
                        inscripcionEquipo.getIdInscripcion(), jugador.getIdJugador())
                .orElseGet(() -> {
                    InscripcionJugador nueva = new InscripcionJugador();
                    nueva.setInscripcionEquipo(inscripcionEquipo);
                    nueva.setJugador(jugador);
                    return nueva;
                });
        inscripcion.setNumeroCamiseta(jugador.getNumeroCamiseta());
        inscripcion.setPosicion(jugador.getPosicion());
        inscripcion.setEstado("ACTIVA");
        inscripcionesJugador.save(inscripcion);
    }
}
