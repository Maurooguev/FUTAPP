package com.futapp.service;

import com.futapp.dto.EquipoRequestDTO;
import com.futapp.dto.EquipoResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Torneo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final JugadorRepository jugadorRepository;
    private final PartidoRepository partidoRepository;
    private final TorneoAuthorizationService authorization;

    public EquipoService(EquipoRepository equipoRepository, TorneoRepository torneoRepository,
                         JugadorRepository jugadorRepository, PartidoRepository partidoRepository,
                         TorneoAuthorizationService authorization) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.jugadorRepository = jugadorRepository;
        this.partidoRepository = partidoRepository;
        this.authorization = authorization;
    }

    public List<EquipoResponseDTO> listarEquipos() {
        return equipoRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public EquipoResponseDTO obtenerEquipoPorId(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));

        return convertirAResponseDTO(equipo);
    }

    public List<EquipoResponseDTO> listarEquiposPorTorneo(Long idTorneo) {
        return equipoRepository.findByTorneoIdTorneo(idTorneo)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public EquipoResponseDTO crearEquipo(EquipoRequestDTO requestDTO) {
        authorization.verificarGestion(requestDTO.getIdTorneo());
        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        Equipo equipo = new Equipo();
        equipo.setNombre(requestDTO.getNombre());
        equipo.setEscudo(requestDTO.getEscudo());
        equipo.setColorCamiseta(requestDTO.getColorCamiseta());
        equipo.setBarrio(requestDTO.getBarrio());
        equipo.setResponsable(requestDTO.getResponsable());
        equipo.setTelefonoContacto(requestDTO.getTelefonoContacto());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setTorneo(torneo);

        Equipo equipoGuardado = equipoRepository.save(equipo);
        return convertirAResponseDTO(equipoGuardado);
    }

    public EquipoResponseDTO actualizarEquipo(Long id, EquipoRequestDTO requestDTO) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        authorization.verificarGestion(equipo.getTorneo());

        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        equipo.setNombre(requestDTO.getNombre());
        equipo.setEscudo(requestDTO.getEscudo());
        equipo.setColorCamiseta(requestDTO.getColorCamiseta());
        equipo.setBarrio(requestDTO.getBarrio());
        equipo.setResponsable(requestDTO.getResponsable());
        equipo.setTelefonoContacto(requestDTO.getTelefonoContacto());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setTorneo(torneo);

        Equipo equipoActualizado = equipoRepository.save(equipo);
        return convertirAResponseDTO(equipoActualizado);
    }

    public void eliminarEquipo(Long id) {
        if (!equipoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipo no encontrado con id: " + id);
        }
        authorization.verificarGestion(equipoRepository.findById(id).orElseThrow().getTorneo());

        if (!jugadorRepository.findByEquipoIdEquipo(id).isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el equipo porque tiene jugadores registrados");
        }
        if (partidoRepository.existsByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(id, id)) {
            throw new IllegalArgumentException("No se puede eliminar el equipo porque tiene partidos asociados");
        }

        equipoRepository.deleteById(id);
    }

    private EquipoResponseDTO convertirAResponseDTO(Equipo equipo) {
        return new EquipoResponseDTO(
                equipo.getIdEquipo(),
                equipo.getNombre(),
                equipo.getEscudo(),
                equipo.getColorCamiseta(),
                equipo.getBarrio(),
                equipo.getResponsable(),
                equipo.getTelefonoContacto(),
                equipo.getEstado(),
                equipo.getTorneo().getIdTorneo(),
                equipo.getTorneo().getNombre()
        );
    }
}
