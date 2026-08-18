package com.futapp.service;

import com.futapp.dto.FechaRequestDTO;
import com.futapp.dto.FechaResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Fecha;
import com.futapp.model.Torneo;
import com.futapp.repository.FechaRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.PartidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FechaService {

    private final FechaRepository fechaRepository;
    private final TorneoRepository torneoRepository;
    private final PartidoRepository partidoRepository;
    private final TorneoAuthorizationService authorization;

    public FechaService(FechaRepository fechaRepository, TorneoRepository torneoRepository,
                        PartidoRepository partidoRepository, TorneoAuthorizationService authorization) {
        this.fechaRepository = fechaRepository;
        this.torneoRepository = torneoRepository;
        this.partidoRepository = partidoRepository;
        this.authorization = authorization;
    }

    public List<FechaResponseDTO> listarFechas() {
        return fechaRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public FechaResponseDTO obtenerFechaPorId(Long id) {
        Fecha fecha = fechaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada con id: " + id));

        return convertirAResponseDTO(fecha);
    }

    public List<FechaResponseDTO> listarFechasPorTorneo(Long idTorneo) {
        return fechaRepository.findByTorneoIdTorneo(idTorneo)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public FechaResponseDTO crearFecha(FechaRequestDTO requestDTO) {
        authorization.verificarGestion(requestDTO.getIdTorneo());
        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        validarNumeroUnico(requestDTO, null);

        Fecha fecha = new Fecha();
        fecha.setNumeroFecha(requestDTO.getNumeroFecha());
        fecha.setDescripcion(requestDTO.getDescripcion());
        fecha.setEstado(requestDTO.getEstado());
        fecha.setTorneo(torneo);

        return convertirAResponseDTO(fechaRepository.save(fecha));
    }

    public FechaResponseDTO actualizarFecha(Long id, FechaRequestDTO requestDTO) {
        Fecha fecha = fechaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada con id: " + id));
        authorization.verificarGestion(fecha.getTorneo());

        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        validarNumeroUnico(requestDTO, id);

        fecha.setNumeroFecha(requestDTO.getNumeroFecha());
        fecha.setDescripcion(requestDTO.getDescripcion());
        fecha.setEstado(requestDTO.getEstado());
        fecha.setTorneo(torneo);

        return convertirAResponseDTO(fechaRepository.save(fecha));
    }

    public void eliminarFecha(Long id) {
        if (!fechaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Fecha no encontrada con id: " + id);
        }
        authorization.verificarGestion(fechaRepository.findById(id).orElseThrow().getTorneo());
        if (partidoRepository.existsByFechaIdFecha(id)) {
            throw new IllegalArgumentException("No se puede eliminar la jornada porque tiene partidos asociados");
        }
        fechaRepository.deleteById(id);
    }

    private FechaResponseDTO convertirAResponseDTO(Fecha fecha) {
        return new FechaResponseDTO(
                fecha.getIdFecha(),
                fecha.getNumeroFecha(),
                fecha.getDescripcion(),
                fecha.getEstado(),
                fecha.getTorneo().getIdTorneo(),
                fecha.getTorneo().getNombre()
        );
    }

    private void validarNumeroUnico(FechaRequestDTO requestDTO, Long idFecha) {
        boolean repetida = idFecha == null
                ? fechaRepository.existsByTorneoIdTorneoAndNumeroFecha(requestDTO.getIdTorneo(), requestDTO.getNumeroFecha())
                : fechaRepository.existsByTorneoIdTorneoAndNumeroFechaAndIdFechaNot(requestDTO.getIdTorneo(), requestDTO.getNumeroFecha(), idFecha);
        if (repetida) {
            throw new IllegalArgumentException("Ya existe una jornada con ese número en el torneo");
        }
    }
}
