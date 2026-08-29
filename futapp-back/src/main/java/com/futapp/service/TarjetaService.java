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
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarjetaService {

    private final TarjetaRepository tarjetaRepository;
    private final PartidoRepository partidoRepository;
    private final JugadorRepository jugadorRepository;
    private final TorneoAuthorizationService authorization;

    public TarjetaService(TarjetaRepository tarjetaRepository,
                          PartidoRepository partidoRepository,
                          JugadorRepository jugadorRepository, TorneoAuthorizationService authorization) {
        this.tarjetaRepository = tarjetaRepository;
        this.partidoRepository = partidoRepository;
        this.jugadorRepository = jugadorRepository;
        this.authorization = authorization;
    }

    public List<TarjetaResponseDTO> listarTarjetas() {
        return tarjetaRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public TarjetaResponseDTO obtenerTarjetaPorId(Long id) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada con id: " + id));

        return convertirAResponseDTO(tarjeta);
    }

    public List<TarjetaResponseDTO> listarTarjetasPorPartido(Long idPartido) {
        return tarjetaRepository.findByPartidoIdPartido(idPartido)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TarjetaResponseDTO> listarTarjetasPorJugador(Long idJugador) {
        return tarjetaRepository.findByJugadorIdJugador(idJugador)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public TarjetaResponseDTO crearTarjeta(TarjetaRequestDTO requestDTO) {
        authorization.verificarGestion(partidoRepository.findById(requestDTO.getIdPartido()).orElseThrow().getTorneo());
        Tarjeta tarjeta = new Tarjeta();
        cargarDatosTarjeta(tarjeta, requestDTO);
        return convertirAResponseDTO(tarjetaRepository.save(tarjeta));
    }

    public TarjetaResponseDTO actualizarTarjeta(Long id, TarjetaRequestDTO requestDTO) {
        Tarjeta tarjeta = tarjetaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarjeta no encontrada con id: " + id));
        authorization.verificarGestion(tarjeta.getPartido().getTorneo());

        cargarDatosTarjeta(tarjeta, requestDTO);
        return convertirAResponseDTO(tarjetaRepository.save(tarjeta));
    }

    public void eliminarTarjeta(Long id) {
        if (!tarjetaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tarjeta no encontrada con id: " + id);
        }
        authorization.verificarGestion(tarjetaRepository.findById(id).orElseThrow().getPartido().getTorneo());
        tarjetaRepository.deleteById(id);
    }

    private void cargarDatosTarjeta(Tarjeta tarjeta, TarjetaRequestDTO requestDTO) {
        Partido partido = partidoRepository.findById(requestDTO.getIdPartido())
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + requestDTO.getIdPartido()));

        Jugador jugador = jugadorRepository.findById(requestDTO.getIdJugador())
                .orElseThrow(() -> new ResourceNotFoundException("Jugador no encontrado con id: " + requestDTO.getIdJugador()));

        Equipo equipo = jugador.getEquipo();

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
                tarjeta.getEquipo().getIdEquipo(),
                tarjeta.getEquipo().getNombre()
        );
    }
}
