package com.futapp.service;

import com.futapp.dto.GolRequestDTO;
import com.futapp.dto.GolResponseDTO;
import com.futapp.dto.GoleadorDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Gol;
import com.futapp.model.Jugador;
import com.futapp.model.Partido;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.GolRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GolService {

    private final GolRepository golRepository;
    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final EquipoRepository equipoRepository;
    private final TorneoAuthorizationService authorization;
    private final InscripcionJugadorRepository inscripciones;
    private final TorneoVisibilityService visibility;
    private final TorneoRepository torneos;
    private final SancionJugadorService sanciones;

    public GolService(GolRepository golRepository,
                      PartidoRepository partidoRepository,
                      JugadorRepository jugadorRepository,
                      EquipoRepository equipoRepository, TorneoAuthorizationService authorization,
                      InscripcionJugadorRepository inscripciones, TorneoVisibilityService visibility,
                      TorneoRepository torneos, SancionJugadorService sanciones) {
        this.golRepository = golRepository;
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.equipoRepository = equipoRepository;
        this.authorization = authorization;
        this.inscripciones = inscripciones;
        this.visibility = visibility;
        this.torneos = torneos;
        this.sanciones = sanciones;
    }

    public List<GolResponseDTO> listarGoles() {
        return golRepository.findAll()
                .stream()
                .filter(gol -> visibility.puedeVer(gol.getPartido().getTorneo()))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public GolResponseDTO obtenerGolPorId(Long id) {
        Gol gol = golRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gol no encontrado con id: " + id));
        visibility.verificar(gol.getPartido().getTorneo());

        return convertirAResponseDTO(gol);
    }

    public List<GolResponseDTO> listarGolesPorPartido(Long idPartido) {
        visibility.verificar(partidoRepository.findById(idPartido)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado")).getTorneo());
        return golRepository.obtenerDetallePorPartido(idPartido);
    }

    public List<GolResponseDTO> listarGolesPorJugador(Long idJugador) {
        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
        visibility.verificar(jugador.getEquipo().getTorneo());
        return golRepository.obtenerDetallePorJugador(idJugador);
    }

    public List<GoleadorDTO> obtenerGoleadoresPorTorneo(Long idTorneo) {
        visibility.verificar(torneos.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado")));
        return golRepository.obtenerGoleadoresPorTorneo(idTorneo);
    }

    @Transactional
    public GolResponseDTO crearGol(GolRequestDTO requestDTO) {
        Partido partido = partidoRepository.findByIdForUpdate(requestDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + requestDTO.getIdPartido()));
        authorization.verificarGestion(partido.getTorneo());
        Gol gol = new Gol();
        cargarDatosGol(gol, requestDTO, partido);
        return convertirAResponseDTO(golRepository.save(gol));
    }

    @Transactional
    public GolResponseDTO actualizarGol(Long id, GolRequestDTO requestDTO) {
        Gol gol = golRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gol no encontrado con id: " + id));
        authorization.verificarGestion(gol.getPartido().getTorneo());
        if (!gol.getPartido().getIdPartido().equals(requestDTO.getIdPartido())) {
            throw new IllegalArgumentException("Un gol no se puede mover a otro partido");
        }

        Partido partido = partidoRepository.findByIdForUpdate(requestDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + requestDTO.getIdPartido()));

        cargarDatosGol(gol, requestDTO, partido);
        return convertirAResponseDTO(golRepository.save(gol));
    }

    @Transactional
    public void eliminarGol(Long id) {
        Gol gol = golRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Gol no encontrado con id: " + id));
        partidoRepository.findByIdForUpdate(gol.getPartido().getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
        authorization.verificarGestion(gol.getPartido().getTorneo());
        golRepository.deleteById(id);
    }

    private void cargarDatosGol(Gol gol, GolRequestDTO requestDTO, Partido partido) {
        Jugador jugador = jugadorRepository.findById(requestDTO.getIdJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + requestDTO.getIdJugador()));

        Equipo equipo = equipoRepository.findById(requestDTO.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + requestDTO.getIdEquipo()));

        validarReglasDeNegocio(requestDTO, partido, jugador, equipo);

        long golesDelEquipo = golRepository.countByPartidoIdPartidoAndEquipoIdEquipo(
                partido.getIdPartido(), equipo.getIdEquipo());
        boolean yaContado = gol.getIdGol() != null
                && gol.getPartido().getIdPartido().equals(partido.getIdPartido())
                && gol.getEquipo().getIdEquipo().equals(equipo.getIdEquipo());
        long totalLuegoDeGuardar = golesDelEquipo + (yaContado ? 0 : 1);
        int marcador = partido.getEquipoLocal().getIdEquipo().equals(equipo.getIdEquipo())
                ? partido.getGolesLocal() : partido.getGolesVisitante();
        if (totalLuegoDeGuardar > marcador) {
            throw new IllegalArgumentException("No podés cargar más goles individuales que los indicados en el resultado");
        }

        gol.setMinuto(requestDTO.getMinuto());
        gol.setTipoGol(requestDTO.getTipoGol());
        gol.setObservacion(requestDTO.getObservacion());
        gol.setPartido(partido);
        gol.setJugador(jugador);
        gol.setEquipo(equipo);
    }

    private void validarReglasDeNegocio(GolRequestDTO requestDTO,
                                        Partido partido,
                                        Jugador jugador,
                                        Equipo equipo) {

        if (requestDTO.getMinuto() != null && requestDTO.getMinuto() <= 0) {
            throw new IllegalArgumentException("El minuto del gol debe ser mayor a 0");
        }

        if (requestDTO.getMinuto() != null && requestDTO.getMinuto() > 130) {
            throw new IllegalArgumentException("El minuto del gol no puede ser mayor a 130");
        }

        boolean equipoParticipaEnPartido =
                partido.getEquipoLocal().getIdEquipo().equals(equipo.getIdEquipo()) ||
                partido.getEquipoVisitante().getIdEquipo().equals(equipo.getIdEquipo());

        if (!equipoParticipaEnPartido) {
            throw new IllegalArgumentException("El equipo indicado no participa en el partido");
        }

        if (!"FINALIZADO".equalsIgnoreCase(partido.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden registrar goles en un partido finalizado");
        }

        if (!inscripciones.existsByJugadorIdJugadorAndInscripcionEquipoTorneoIdTorneoAndInscripcionEquipoEquipoIdEquipoAndEstado(
                jugador.getIdJugador(), partido.getTorneo().getIdTorneo(), equipo.getIdEquipo(), "ACTIVA")) {
            throw new IllegalArgumentException("El jugador no integra el plantel activo de ese equipo en este torneo");
        }
        if (sanciones.estaSuspendidoParaPartido(jugador.getIdJugador(), partido)) {
            throw new IllegalArgumentException("El jugador está suspendido para esta fecha y no puede participar");
        }
    }

    private GolResponseDTO convertirAResponseDTO(Gol gol) {
        return new GolResponseDTO(
                gol.getIdGol(),
                gol.getMinuto(),
                gol.getTipoGol(),
                gol.getObservacion(),
                gol.getPartido().getIdPartido(),
                gol.getJugador().getIdJugador(),
                gol.getJugador().getNombre() + " " + gol.getJugador().getApellido(),
                gol.getEquipo().getIdEquipo(),
                gol.getEquipo().getNombre()
        );
    }
}
