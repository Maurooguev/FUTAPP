package com.futapp.service;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.dto.TorneoPanelDTO;
import com.futapp.dto.PaginaDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import com.futapp.model.Partido;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.PerfilOrganizadorRepository;
import com.futapp.repository.FechaRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final TorneoAuthorizationService authorization;
    private final PartidoRepository partidos;
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EstructuraCompetenciaService estructuraCompetencia;
    private final EquipoRepository equipos;
    private final TablaPosicionService tablaPosiciones;
    private final PerfilOrganizadorRepository perfilesOrganizador;
    private final FechaRepository fechas;

    public TorneoService(TorneoRepository torneoRepository, TorneoAuthorizationService authorization,
                         PartidoRepository partidos, InscripcionEquipoRepository inscripcionesEquipo,
                         InscripcionJugadorRepository inscripcionesJugador,
                         EstructuraCompetenciaService estructuraCompetencia, EquipoRepository equipos,
                         TablaPosicionService tablaPosiciones, PerfilOrganizadorRepository perfilesOrganizador,
                         FechaRepository fechas) {
        this.torneoRepository = torneoRepository;
        this.authorization = authorization;
        this.partidos = partidos;
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.estructuraCompetencia = estructuraCompetencia;
        this.equipos = equipos;
        this.tablaPosiciones = tablaPosiciones;
        this.perfilesOrganizador = perfilesOrganizador;
        this.fechas = fechas;
    }

    public List<TorneoResponseDTO> listarTorneos() {
        return torneoRepository.findAll()
                .stream()
                .filter(torneo -> !"BORRADOR".equalsIgnoreCase(torneo.getEstado()))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PaginaDTO<TorneoResponseDTO> listarTorneosPaginados(int pagina, int cantidad) {
        var pageable = PageRequest.of(Math.max(pagina, 0), limitar(cantidad), Sort.by(Sort.Direction.DESC, "idTorneo"));
        return PaginaDTO.desde(torneoRepository.findByEstadoNotIgnoreCase("BORRADOR", pageable).map(this::convertirAResponseDTO));
    }

    public PaginaDTO<TorneoResponseDTO> buscarPaginado(String query, int pagina, int cantidad) {
        var pageable = PageRequest.of(Math.max(pagina, 0), limitar(cantidad), Sort.by(Sort.Direction.DESC, "idTorneo"));
        var resultados = torneoRepository.findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(
                query.trim(), query.trim(), pageable).map(this::convertirAResponseDTO);
        return PaginaDTO.desde(resultados);
    }

    private int limitar(int cantidad) { return Math.min(Math.max(cantidad, 1), 100); }

    public List<TorneoResponseDTO> buscar(String query) {
        return torneoRepository.findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(query, query).stream()
                .filter(torneo -> !"BORRADOR".equalsIgnoreCase(torneo.getEstado())).map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoResponseDTO> listarPropios(Usuario usuario) {
        if (usuario.tieneRol("ADMIN")) return torneoRepository.findAll().stream().map(this::convertirAResponseDTO).toList();
        if (!usuario.tieneRol("ORGANIZADOR")) throw new AccessDeniedException("Solo una cuenta de organizador puede administrar torneos");
        return torneoRepository.findByPropietarioIdUsuario(usuario.getIdUsuario()).stream().map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoResponseDTO> listarTitulosEquipo(Long idEquipo) {
        return torneoRepository.findByCampeonIdEquipoOrderByIdTorneoDesc(idEquipo).stream()
                .filter(torneo -> "FINALIZADO".equalsIgnoreCase(torneo.getEstado()))
                .map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoPanelDTO> obtenerPanel(Usuario usuario) {
        if (!usuario.tieneRol("ADMIN") && !usuario.tieneRol("ORGANIZADOR")) {
            throw new AccessDeniedException("Solo una cuenta de organizador puede administrar torneos");
        }
        List<Torneo> propios = usuario.tieneRol("ADMIN")
                ? torneoRepository.findAll()
                : torneoRepository.findByPropietarioIdUsuario(usuario.getIdUsuario());
        return propios.stream().map(this::crearResumenPanel).toList();
    }

    private TorneoPanelDTO crearResumenPanel(Torneo torneo) {
        var equipos = inscripcionesEquipo.findByTorneoIdTorneoAndEstado(torneo.getIdTorneo(), "ACTIVA");
        int sinPlantel = (int) equipos.stream()
                .filter(item -> inscripcionesJugador.countByInscripcionEquipoIdInscripcionAndEstado(
                        item.getIdInscripcion(), "ACTIVA") == 0)
                .count();
        var partidosTorneo = partidos.findByTorneoIdTorneo(torneo.getIdTorneo());
        int jugados = (int) partidosTorneo.stream()
                .filter(item -> "FINALIZADO".equalsIgnoreCase(item.getEstado())).count();
        int pendientes = (int) partidosTorneo.stream().filter(item -> !partidoResuelto(item)).count();
        Integer proximaJornada = partidosTorneo.stream()
                .filter(item -> !partidoResuelto(item))
                .map(item -> item.getFecha().getNumeroFecha())
                .min(Integer::compareTo).orElse(null);
        var proximoPartido = partidosTorneo.stream()
                .filter(item -> !partidoResuelto(item))
                .min(java.util.Comparator.comparingInt(
                        (Partido item) -> item.getFecha().getNumeroFecha()))
                .orElse(null);

        String accion;
        if (equipos.size() < 2) accion = "Agregá al menos 2 equipos";
        else if (sinPlantel > 0) accion = "Completá los planteles";
        else if (partidosTorneo.isEmpty()) accion = "Generá el fixture";
        else if (pendientes > 0) accion = "Gestioná la próxima jornada";
        else if (!"FINALIZADO".equalsIgnoreCase(torneo.getEstado())) accion = "Finalizá el torneo";
        else accion = "Torneo completo";

        return new TorneoPanelDTO(convertirAResponseDTO(torneo), equipos.size(), sinPlantel,
                jugados, pendientes, proximaJornada, accion,
                proximoPartido == null ? null : proximoPartido.getIdPartido(),
                proximoPartido == null ? null : proximoPartido.getEquipoLocal().getNombre(),
                proximoPartido == null ? null : proximoPartido.getEquipoVisitante().getNombre());
    }

    @Transactional
    public TorneoResponseDTO obtenerTorneoPorId(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));

        if ("FINALIZADO".equalsIgnoreCase(torneo.getEstado()) && torneo.getCampeon() == null) {
            torneo.setCampeon(determinarCampeon(torneo, partidos.findByTorneoIdTorneo(id)));
            torneo = torneoRepository.save(torneo);
        }

        return convertirAResponseDTO(torneo);
    }

    @Transactional
    public TorneoResponseDTO crearTorneo(TorneoRequestDTO requestDTO, Usuario propietario) {
        if (!propietario.tieneRol("ORGANIZADOR") && !propietario.tieneRol("ADMIN"))
            throw new org.springframework.security.access.AccessDeniedException(
                    "Debés crear una cuenta de organizador de torneos para realizar esta acción");
        Torneo torneo = new Torneo();
        torneo.setNombre(requestDTO.getNombre());
        torneo.setDescripcion(requestDTO.getDescripcion());
        torneo.setUbicacion(requestDTO.getUbicacion());
        torneo.setDireccionSede(requestDTO.getDireccionSede());
        torneo.setGoogleMapsUrl(requestDTO.getGoogleMapsUrl());
        torneo.setReglasTexto(requestDTO.getReglasTexto());
        torneo.setPremiosTexto(requestDTO.getPremiosTexto());
        torneo.setEstado("BORRADOR");
        torneo.setFormato(requestDTO.getFormato());
        torneo.setCantidadEquipos(requestDTO.getCantidadEquipos());
        cargarReglas(torneo, requestDTO);
        torneo.setCodigoPublico(generarCodigo());
        torneo.setPropietario(propietario);

        Torneo torneoGuardado = torneoRepository.save(torneo);
        estructuraCompetencia.crear(torneoGuardado, requestDTO);
        return convertirAResponseDTO(torneoGuardado);
    }

    public TorneoResponseDTO actualizarTorneo(Long id, TorneoRequestDTO requestDTO) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));
        authorization.verificarGestion(torneo);

        torneo.setNombre(requestDTO.getNombre());
        torneo.setDescripcion(requestDTO.getDescripcion());
        torneo.setUbicacion(requestDTO.getUbicacion());
        torneo.setDireccionSede(requestDTO.getDireccionSede());
        torneo.setGoogleMapsUrl(requestDTO.getGoogleMapsUrl());
        torneo.setReglasTexto(requestDTO.getReglasTexto());
        torneo.setPremiosTexto(requestDTO.getPremiosTexto());
        torneo.setFormato(requestDTO.getFormato());
        torneo.setCantidadEquipos(requestDTO.getCantidadEquipos());
        cargarReglas(torneo, requestDTO);

        Torneo torneoActualizado = torneoRepository.save(torneo);
        return convertirAResponseDTO(torneoActualizado);
    }

    public void eliminarTorneo(Long id) {
        if (!torneoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Torneo no encontrado con id: " + id);
        }

        authorization.verificarGestion(id);

        torneoRepository.deleteById(id);
    }

    @Transactional
    public TorneoResponseDTO iniciarTorneo(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));
        authorization.verificarGestion(torneo);
        if ("FINALIZADO".equalsIgnoreCase(torneo.getEstado())) {
            throw new IllegalArgumentException("Un torneo finalizado no puede volver a iniciarse");
        }
        var inscripciones = inscripcionesEquipo.findByTorneoIdTorneoAndEstado(id, "ACTIVA");
        int cantidadRequerida = torneo.getCantidadEquipos() == null ? 2 : torneo.getCantidadEquipos();
        if (inscripciones.size() != cantidadRequerida) {
            throw new IllegalArgumentException("Para iniciar el torneo deben estar inscriptos los "
                    + cantidadRequerida + " equipos configurados. Actualmente hay " + inscripciones.size());
        }
        var plantelesIncompletos = inscripciones.stream().map(item -> {
            long jugadores = inscripcionesJugador.countByInscripcionEquipoIdInscripcionAndEstado(
                    item.getIdInscripcion(), "ACTIVA");
            return jugadores < 5 ? item.getEquipo().getNombre() + " (" + jugadores + "/5)" : null;
        }).filter(java.util.Objects::nonNull).toList();
        if (!plantelesIncompletos.isEmpty()) {
            throw new IllegalArgumentException("Cada equipo necesita al menos 5 jugadores activos. Faltan: "
                    + String.join(", ", plantelesIncompletos));
        }
        if (!fechas.existsByTorneoIdTorneoAndNumeroFecha(id, 1)) {
            throw new IllegalArgumentException("Creá la primera fecha antes de iniciar el torneo");
        }
        torneo.setEstado("ACTIVO");
        return convertirAResponseDTO(torneoRepository.save(torneo));
    }

    @Transactional
    public TorneoResponseDTO publicarTorneo(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));
        authorization.verificarGestion(torneo);
        if (!"BORRADOR".equalsIgnoreCase(torneo.getEstado())) {
            throw new IllegalArgumentException("Solo se puede publicar un torneo que está en borrador");
        }
        torneo.setEstado("PUBLICADO");
        return convertirAResponseDTO(torneoRepository.save(torneo));
    }

    @Transactional
    public TorneoResponseDTO finalizarTorneo(Long id) {
        Torneo torneo = torneoRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));
        authorization.verificarGestion(torneo);
        var partidosTorneo = partidos.findByTorneoIdTorneo(id);
        if (partidosTorneo.isEmpty()) throw new IllegalArgumentException("El torneo todavía no tiene partidos");
        long pendientes = partidosTorneo.stream().filter(partido -> !partidoResuelto(partido)).count();
        if (pendientes > 0) throw new IllegalArgumentException("Todavía quedan " + pendientes + " partidos sin finalizar");
        torneo.setCampeon(determinarCampeon(torneo, partidosTorneo));
        torneo.setEstado("FINALIZADO");
        return convertirAResponseDTO(torneoRepository.save(torneo));
    }

    private TorneoResponseDTO convertirAResponseDTO(Torneo torneo) {
        Usuario propietario = torneo.getPropietario();
        TorneoResponseDTO response = new TorneoResponseDTO(
                torneo.getIdTorneo(),
                torneo.getNombre(),
                torneo.getDescripcion(),
                torneo.getUbicacion(),
                torneo.getEstado(),
                torneo.getFormato(),
                torneo.getCantidadEquipos(),
                torneo.getCodigoPublico(),
                propietario != null ? propietario.getIdUsuario() : null,
                propietario != null ? propietario.getNombre() : null,
                torneo.getIdaVuelta(), torneo.getPuntosVictoria(), torneo.getPuntosEmpate(),
                torneo.getPuntosDerrota(), torneo.getCriterioDesempate()
        );
        if (torneo.getCampeon() != null) {
            response.setIdCampeon(torneo.getCampeon().getIdEquipo());
            response.setNombreCampeon(torneo.getCampeon().getNombre());
        }
        response.setVisitas(torneo.getVisitas());
        response.setDireccionSede(torneo.getDireccionSede());
        response.setGoogleMapsUrl(torneo.getGoogleMapsUrl());
        response.setReglasTexto(torneo.getReglasTexto());
        response.setPremiosTexto(torneo.getPremiosTexto());
        if (propietario != null) {
            perfilesOrganizador.findByUsuarioIdUsuario(propietario.getIdUsuario())
                    .ifPresent(perfil -> response.setLogoOrganizador(perfil.getFotoPerfil()));
        }
        return response;
    }

    @Transactional
    public long registrarVisita(Long id) {
        if (torneoRepository.incrementarVisitas(id) == 0) throw new ResourceNotFoundException("Torneo no encontrado con id: " + id);
        return torneoRepository.findById(id).map(Torneo::getVisitas).orElse(0L);
    }

    private com.futapp.model.Equipo determinarCampeon(Torneo torneo, List<com.futapp.model.Partido> partidosTorneo) {
        String formato = torneo.getFormato() == null ? "" : torneo.getFormato().toUpperCase();
        boolean tieneEliminacion = formato.contains("ELIMINACIÓN") || formato.contains("ELIMINACION") || formato.contains("PLAYOFF");
        if (tieneEliminacion) {
            var finalPartido = partidosTorneo.stream()
                    .filter(partido -> "Final".equalsIgnoreCase(partido.getObservaciones()))
                    .max(java.util.Comparator.comparing(com.futapp.model.Partido::getIdPartido))
                    .orElseThrow(() -> new IllegalArgumentException("Generá y finalizá la final antes de cerrar el torneo"));
            if (!"FINALIZADO".equalsIgnoreCase(finalPartido.getEstado())) {
                throw new IllegalArgumentException("Finalizá el partido final antes de cerrar el torneo");
            }
            if (finalPartido.getGolesLocal() > finalPartido.getGolesVisitante()) return finalPartido.getEquipoLocal();
            if (finalPartido.getGolesVisitante() > finalPartido.getGolesLocal()) return finalPartido.getEquipoVisitante();
            if (finalPartido.getPenalesLocal() == null || finalPartido.getPenalesVisitante() == null
                    || finalPartido.getPenalesLocal().equals(finalPartido.getPenalesVisitante())) {
                throw new IllegalArgumentException("Definí el ganador de la final por penales antes de cerrar el torneo");
            }
            return finalPartido.getPenalesLocal() > finalPartido.getPenalesVisitante()
                    ? finalPartido.getEquipoLocal() : finalPartido.getEquipoVisitante();
        }
        if (partidosTorneo.stream().noneMatch(partido -> "FINALIZADO".equalsIgnoreCase(partido.getEstado()))) {
            throw new IllegalArgumentException("Debe existir al menos un partido con resultado para definir al campeón");
        }
        var tabla = tablaPosiciones.obtenerTablaPorTorneo(torneo.getIdTorneo());
        if (tabla.isEmpty()) throw new IllegalArgumentException("No se pudo determinar el campeón desde la tabla");
        return equipos.findById(tabla.get(0).getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el equipo campeón"));
    }

    private boolean partidoResuelto(com.futapp.model.Partido partido) {
        return java.util.Set.of("FINALIZADO", "CANCELADO").contains(partido.getEstado().toUpperCase());
    }

    private void cargarReglas(Torneo torneo, TorneoRequestDTO request) {
        String formato = estructuraCompetencia.normalizarFormato(request.getFormato());
        torneo.setFormato(switch (formato) {
            case "LIGA" -> "Liga";
            case "ELIMINACION_DIRECTA" -> "Eliminación directa";
            case "LIGA_PLAYOFF" -> "Liga + Playoffs";
            default -> "Zonas + Playoffs";
        });
        torneo.setIdaVuelta(Boolean.TRUE.equals(request.getIdaVuelta()) && !"ELIMINACION_DIRECTA".equals(formato));
        if ("ELIMINACION_DIRECTA".equals(formato)) {
            torneo.setPuntosVictoria(0);
            torneo.setPuntosEmpate(0);
            torneo.setPuntosDerrota(0);
            torneo.setCriterioDesempate("PENALES");
            return;
        }
        torneo.setPuntosVictoria(request.getPuntosVictoria() == null ? 3 : request.getPuntosVictoria());
        torneo.setPuntosEmpate(request.getPuntosEmpate() == null ? 1 : request.getPuntosEmpate());
        torneo.setPuntosDerrota(request.getPuntosDerrota() == null ? 0 : request.getPuntosDerrota());
        String criterio = request.getCriterioDesempate() == null ? "DIFERENCIA_GOLES" : request.getCriterioDesempate().trim().toUpperCase();
        if (!java.util.Set.of("DIFERENCIA_GOLES", "GOLES_FAVOR").contains(criterio)) throw new IllegalArgumentException("El criterio de desempate no es válido");
        torneo.setCriterioDesempate(criterio);
    }

    private String generarCodigo() {
        String codigo;
        do { codigo = "FUT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(); }
        while (torneoRepository.existsByCodigoPublico(codigo));
        return codigo;
    }
}
