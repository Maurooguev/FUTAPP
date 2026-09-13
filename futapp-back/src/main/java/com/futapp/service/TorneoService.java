package com.futapp.service;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.dto.TorneoPanelDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.UsuarioRepository;
import com.futapp.repository.EquipoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final TorneoAuthorizationService authorization;
    private final PartidoRepository partidos;
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final UsuarioRepository usuarios;
    private final EstructuraCompetenciaService estructuraCompetencia;
    private final EquipoRepository equipos;
    private final TablaPosicionService tablaPosiciones;

    public TorneoService(TorneoRepository torneoRepository, TorneoAuthorizationService authorization,
                         PartidoRepository partidos, InscripcionEquipoRepository inscripcionesEquipo,
                         InscripcionJugadorRepository inscripcionesJugador, UsuarioRepository usuarios,
                         EstructuraCompetenciaService estructuraCompetencia, EquipoRepository equipos,
                         TablaPosicionService tablaPosiciones) {
        this.torneoRepository = torneoRepository;
        this.authorization = authorization;
        this.partidos = partidos;
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.usuarios = usuarios;
        this.estructuraCompetencia = estructuraCompetencia;
        this.equipos = equipos;
        this.tablaPosiciones = tablaPosiciones;
    }

    public List<TorneoResponseDTO> listarTorneos() {
        return torneoRepository.findAll()
                .stream()
                .filter(torneo -> !"BORRADOR".equalsIgnoreCase(torneo.getEstado()))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TorneoResponseDTO> buscar(String query) {
        return torneoRepository.findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(query, query).stream()
                .filter(torneo -> !"BORRADOR".equalsIgnoreCase(torneo.getEstado())).map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoResponseDTO> listarPropios(Usuario usuario) {
        if (usuario.tieneRol("ADMIN")) return torneoRepository.findAll().stream().map(this::convertirAResponseDTO).toList();
        return torneoRepository.findByPropietarioIdUsuario(usuario.getIdUsuario()).stream().map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoResponseDTO> listarTitulosEquipo(Long idEquipo) {
        return torneoRepository.findByCampeonIdEquipoOrderByIdTorneoDesc(idEquipo).stream()
                .filter(torneo -> "FINALIZADO".equalsIgnoreCase(torneo.getEstado()))
                .map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoPanelDTO> obtenerPanel(Usuario usuario) {
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
        int pendientes = partidosTorneo.size() - jugados;
        Integer proximaJornada = partidosTorneo.stream()
                .filter(item -> !"FINALIZADO".equalsIgnoreCase(item.getEstado()))
                .map(item -> item.getFecha().getNumeroFecha())
                .min(Integer::compareTo).orElse(null);

        String accion;
        if (equipos.size() < 2) accion = "Agregá al menos 2 equipos";
        else if (sinPlantel > 0) accion = "Completá los planteles";
        else if (partidosTorneo.isEmpty()) accion = "Generá el fixture";
        else if (pendientes > 0) accion = "Gestioná la próxima jornada";
        else if (!"FINALIZADO".equalsIgnoreCase(torneo.getEstado())) accion = "Finalizá el torneo";
        else accion = "Torneo completo";

        return new TorneoPanelDTO(convertirAResponseDTO(torneo), equipos.size(), sinPlantel,
                jugados, pendientes, proximaJornada, accion);
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
        if (!propietario.tieneRol("ORGANIZADOR")) {
            propietario.agregarRol("ORGANIZADOR");
            propietario = usuarios.save(propietario);
        }
        Torneo torneo = new Torneo();
        torneo.setNombre(requestDTO.getNombre());
        torneo.setDescripcion(requestDTO.getDescripcion());
        torneo.setUbicacion(requestDTO.getUbicacion());
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
        if (inscripciones.size() < 2) throw new IllegalArgumentException("Se necesitan al menos 2 equipos para iniciar el torneo");
        var sinJugadores = inscripciones.stream()
                .filter(item -> inscripcionesJugador.countByInscripcionEquipoIdInscripcionAndEstado(item.getIdInscripcion(), "ACTIVA") == 0)
                .map(item -> item.getEquipo().getNombre()).toList();
        if (!sinJugadores.isEmpty()) {
            throw new IllegalArgumentException("Estos equipos todavía no tienen jugadores: " + String.join(", ", sinJugadores));
        }
        if (partidos.findByTorneoIdTorneo(id).isEmpty()) throw new IllegalArgumentException("Generá el fixture antes de iniciar el torneo");
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
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));
        authorization.verificarGestion(torneo);
        var partidosTorneo = partidos.findByTorneoIdTorneo(id);
        if (partidosTorneo.isEmpty()) throw new IllegalArgumentException("El torneo todavía no tiene partidos");
        long pendientes = partidosTorneo.stream().filter(partido -> !"FINALIZADO".equalsIgnoreCase(partido.getEstado())).count();
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
        return response;
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
        var tabla = tablaPosiciones.obtenerTablaPorTorneo(torneo.getIdTorneo());
        if (tabla.isEmpty()) throw new IllegalArgumentException("No se pudo determinar el campeón desde la tabla");
        return equipos.findById(tabla.get(0).getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el equipo campeón"));
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
