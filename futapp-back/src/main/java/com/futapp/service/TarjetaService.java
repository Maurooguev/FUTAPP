package com.futapp.service;

import com.futapp.dto.TarjetaRequestDTO;
import com.futapp.dto.TarjetaResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Jugador;
import com.futapp.model.Partido;
import com.futapp.model.Tarjeta;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TarjetaRepository;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarjetaService {

    private final TarjetaRepository tarjetaRepository;
    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final TorneoAuthorizationService authorization;
    private final EquipoRepository equipoRepository;
    private final InscripcionJugadorRepository inscripciones;
    private final SancionJugadorService sancionService;
    private final TorneoVisibilityService visibility;
    private final TorneoRepository torneos;

    public TarjetaService(TarjetaRepository tarjetaRepository,
                          PartidoRepository partidoRepository,
                          JugadorRepository jugadorRepository, TorneoAuthorizationService authorization,
                          EquipoRepository equipoRepository, InscripcionJugadorRepository inscripciones,
                          SancionJugadorService sancionService, TorneoVisibilityService visibility,
                          TorneoRepository torneos) {
        this.tarjetaRepository = tarjetaRepository;
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.authorization = authorization;
        this.equipoRepository = equipoRepository;
        this.inscripciones = inscripciones;
        this.sancionService = sancionService;
        this.visibility = visibility;
        this.torneos = torneos;
    }

    public List<TarjetaResponseDTO> listarTarjetas() {
        return tarjetaRepository.findAll()
                .stream()
                .filter(tarjeta -> visibility.puedeVer(tarjeta.getPartido().getTorneo()))
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public TarjetaResponseDTO obtenerTarjetaPorId(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada con id: " + id));
        visibility.verificar(tarjeta.getPartido().getTorneo());

        return convertirAResponseDTO(tarjeta);
    }

    public List<TarjetaResponseDTO> listarTarjetasPorPartido(Long idPartido) {
        visibility.verificar(partidoRepository.findById(idPartido)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado")).getTorneo());
        return tarjetaRepository.findByPartidoIdPartido(idPartido)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TarjetaResponseDTO> listarTarjetasPorJugador(Long idJugador) {
        Jugador jugador = jugadorRepository.findById(idJugador)
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado"));
        visibility.verificar(jugador.getEquipo().getTorneo());
        return tarjetaRepository.findByJugadorIdJugador(idJugador)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TarjetaResponseDTO> listarTarjetasPorTorneo(Long idTorneo) {
        visibility.verificar(torneos.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado")));
        return tarjetaRepository.findByPartidoTorneoIdTorneo(idTorneo)
                .stream()
                .map(this::convertirAResponseDTO)
                .toList();
    }

    @Transactional
    public TarjetaResponseDTO crearTarjeta(TarjetaRequestDTO requestDTO) {
        Partido partido = partidoRepository.findByIdForUpdate(requestDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + requestDTO.getIdPartido()));
        authorization.verificarGestion(partido.getTorneo());
        Tarjeta tarjeta = new Tarjeta();
        cargarDatosTarjeta(tarjeta, requestDTO, partido);
        Tarjeta guardada = tarjetaRepository.save(tarjeta);
        sancionService.sincronizarConTarjeta(guardada, requestDTO.getFechasSuspension());
        return convertirAResponseDTO(guardada);
    }

    @Transactional
    public TarjetaResponseDTO actualizarTarjeta(Long id, TarjetaRequestDTO requestDTO) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada con id: " + id));
        authorization.verificarGestion(tarjeta.getPartido().getTorneo());
        if (!tarjeta.getPartido().getIdPartido().equals(requestDTO.getIdPartido())) {
            throw new IllegalArgumentException("Una tarjeta no se puede mover a otro partido");
        }
        Partido partido = partidoRepository.findByIdForUpdate(requestDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));

        cargarDatosTarjeta(tarjeta, requestDTO, partido);
        Tarjeta guardada = tarjetaRepository.save(tarjeta);
        sancionService.sincronizarConTarjeta(guardada, requestDTO.getFechasSuspension());
        return convertirAResponseDTO(guardada);
    }

    @Transactional
    public void eliminarTarjeta(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada con id: " + id));
        partidoRepository.findByIdForUpdate(tarjeta.getPartido().getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado"));
        authorization.verificarGestion(tarjeta.getPartido().getTorneo());
        sancionService.eliminarPorTarjeta(id);
        tarjetaRepository.deleteById(id);
    }

    private void cargarDatosTarjeta(Tarjeta tarjeta, TarjetaRequestDTO requestDTO, Partido partido) {
        Jugador jugador = jugadorRepository.findById(requestDTO.getIdJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + requestDTO.getIdJugador()));

        Equipo equipo = equipoRepository.findById(requestDTO.getIdEquipo())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + requestDTO.getIdEquipo()));

        validarReglasDeNegocio(requestDTO, partido, jugador, equipo);

        tarjeta.setTipoTarjeta(requestDTO.getTipoTarjeta().trim().toUpperCase());
        tarjeta.setMinuto(requestDTO.getMinuto());
        tarjeta.setMotivo(requestDTO.getMotivo());
        tarjeta.setPartido(partido);
        tarjeta.setJugador(jugador);
        tarjeta.setEquipo(equipo);
    }

    private void validarReglasDeNegocio(TarjetaRequestDTO requestDTO,
                                        Partido partido,
                                        Jugador jugador,
                                        Equipo equipo) {

        if (requestDTO.getTipoTarjeta() == null || requestDTO.getTipoTarjeta().isBlank()) {
            throw new IllegalArgumentException("El tipo de tarjeta es obligatorio");
        }

        String tipo = requestDTO.getTipoTarjeta().trim().toUpperCase();
        if (!tipo.equals("AMARILLA") && !tipo.equals("ROJA")) {
            throw new IllegalArgumentException("El tipo de tarjeta debe ser AMARILLA o ROJA");
        }

        if (requestDTO.getMinuto() != null) {
            if (requestDTO.getMinuto() <= 0) {
                throw new IllegalArgumentException("El minuto de la tarjeta debe ser mayor a 0");
            }

            if (requestDTO.getMinuto() > 130) {
                throw new IllegalArgumentException("El minuto de la tarjeta no puede ser mayor a 130");
            }
        }

        boolean equipoParticipaEnPartido =
                partido.getEquipoLocal().getIdEquipo().equals(equipo.getIdEquipo()) ||
                partido.getEquipoVisitante().getIdEquipo().equals(equipo.getIdEquipo());

        if (!equipoParticipaEnPartido) {
            throw new IllegalArgumentException("El jugador seleccionado no pertenece a un equipo que participe en el partido");
        }

        if (!"FINALIZADO".equalsIgnoreCase(partido.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden registrar tarjetas en un partido finalizado");
        }

        if (!inscripciones.existsByJugadorIdJugadorAndInscripcionEquipoTorneoIdTorneoAndInscripcionEquipoEquipoIdEquipoAndEstado(
                jugador.getIdJugador(), partido.getTorneo().getIdTorneo(), equipo.getIdEquipo(), "ACTIVA")) {
            throw new IllegalArgumentException("El jugador no integra el plantel activo de ese equipo en este torneo");
        }
        if (sancionService.estaSuspendidoParaPartido(jugador.getIdJugador(), partido)) {
            throw new IllegalArgumentException("El jugador está suspendido para esta fecha y no puede participar");
        }
    }

    private TarjetaResponseDTO convertirAResponseDTO(Tarjeta tarjeta) {
        return new TarjetaResponseDTO(
                tarjeta.getIdTarjeta(),
                tarjeta.getTipoTarjeta(),
                tarjeta.getMinuto(),
                tarjeta.getMotivo(),
                tarjeta.getPartido().getIdPartido(),
                tarjeta.getJugador().getIdJugador(),
                tarjeta.getJugador().getNombre() + " " + tarjeta.getJugador().getApellido(),
                tarjeta.getJugador().getFoto(),
                tarjeta.getEquipo().getIdEquipo(),
                tarjeta.getEquipo().getNombre()
        );
    }
}
