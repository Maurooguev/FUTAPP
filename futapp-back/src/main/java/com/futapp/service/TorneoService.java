package com.futapp.service;

import com.futapp.dto.TorneoRequestDTO;
import com.futapp.dto.TorneoResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Torneo;
import com.futapp.model.Usuario;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

@Service
public class TorneoService {

    private final TorneoRepository torneoRepository;
    private final TorneoAuthorizationService authorization;

    public TorneoService(TorneoRepository torneoRepository, TorneoAuthorizationService authorization) {
        this.torneoRepository = torneoRepository;
        this.authorization = authorization;
    }

    public List<TorneoResponseDTO> listarTorneos() {
        return torneoRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<TorneoResponseDTO> buscar(String query) {
        return torneoRepository.findByNombreContainingIgnoreCaseOrCodigoPublicoContainingIgnoreCase(query, query).stream().map(this::convertirAResponseDTO).toList();
    }

    public List<TorneoResponseDTO> listarPropios(Usuario usuario) {
        if ("ADMIN".equals(usuario.getRol())) return listarTorneos();
        return torneoRepository.findByPropietarioIdUsuario(usuario.getIdUsuario()).stream().map(this::convertirAResponseDTO).toList();
    }

    public TorneoResponseDTO obtenerTorneoPorId(Long id) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));

        return convertirAResponseDTO(torneo);
    }

    public TorneoResponseDTO crearTorneo(TorneoRequestDTO requestDTO, Usuario propietario) {
        Torneo torneo = new Torneo();
        torneo.setNombre(requestDTO.getNombre());
        torneo.setDescripcion(requestDTO.getDescripcion());
        torneo.setUbicacion(requestDTO.getUbicacion());
        torneo.setEstado(requestDTO.getEstado());
        torneo.setFormato(requestDTO.getFormato());
        torneo.setCantidadEquipos(requestDTO.getCantidadEquipos());
        torneo.setCodigoPublico(generarCodigo());
        torneo.setPropietario(propietario);

        Torneo torneoGuardado = torneoRepository.save(torneo);
        return convertirAResponseDTO(torneoGuardado);
    }

    public TorneoResponseDTO actualizarTorneo(Long id, TorneoRequestDTO requestDTO) {
        Torneo torneo = torneoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + id));
        authorization.verificarGestion(torneo);

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

        authorization.verificarGestion(id);

        torneoRepository.deleteById(id);
    }

    private TorneoResponseDTO convertirAResponseDTO(Torneo torneo) {
        Usuario propietario = torneo.getPropietario();
        return new TorneoResponseDTO(
                torneo.getIdTorneo(),
                torneo.getNombre(),
                torneo.getDescripcion(),
                torneo.getUbicacion(),
                torneo.getEstado(),
                torneo.getFormato(),
                torneo.getCantidadEquipos(),
                torneo.getCodigoPublico(),
                propietario != null ? propietario.getIdUsuario() : null,
                propietario != null ? propietario.getNombre() : null
        );
    }

    private String generarCodigo() {
        String codigo;
        do { codigo = "FUT-" + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase(); }
        while (torneoRepository.existsByCodigoPublico(codigo));
        return codigo;
    }
}
