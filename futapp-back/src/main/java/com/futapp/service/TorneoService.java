package com.futapp.service;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Torneo;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;

    public TorneoService(TorneoRepository torneoRepository) {
        this.torneoRepository = torneoRepository;
    }

    public List<TorneoResponseDTO> listarTorneos() {
        return torneoRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public TorneoResponseDTO obtenerTorneoPorId(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));

        return convertirAResponseDTO(torneo);
    }

    public TorneoResponseDTO crearTorneo(TorneoRequestDTO requestDTO) {
        Torneo torneo = new Torneo();
        torneo.setNombre(requestDTO.getNombre());
        torneo.setDescripcion(requestDTO.getDescripcion());
        torneo.setUbicacion(requestDTO.getUbicacion());
        torneo.setEstado(requestDTO.getEstado());
        torneo.setFormato(requestDTO.getFormato());
        torneo.setCantidadEquipos(requestDTO.getCantidadEquipos());

        Torneo torneoGuardado = torneoRepository.save(torneo);
        return convertirAResponseDTO(torneoGuardado);
    }

    public TorneoResponseDTO actualizarTorneo(Long id, TorneoRequestDTO requestDTO) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));

        torneo.setNombre(requestDTO.getNombre());
        torneo.setDescripcion(requestDTO.getDescripcion());
        torneo.setUbicacion(requestDTO.getUbicacion());
        torneo.setEstado(requestDTO.getEstado());
        torneo.setFormato(requestDTO.getFormato());
        torneo.setCantidadEquipos(requestDTO.getCantidadEquipos());

        Torneo torneoActualizado = torneoRepository.save(torneo);
        return convertirAResponseDTO(torneoActualizado);
    }

    public void eliminarTorneo(Long id) {
        if (!torneoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Torneo no encontrado con id: " + id);
        }

        torneoRepository.deleteById(id);
    }

    private TorneoResponseDTO convertirAResponseDTO(Torneo torneo) {
        return new TorneoResponseDTO(
                torneo.getIdTorneo(),
                torneo.getNombre(),
                torneo.getDescripcion(),
                torneo.getUbicacion(),
                torneo.getEstado(),
                torneo.getFormato(),
                torneo.getCantidadEquipos()
        );
    }
}