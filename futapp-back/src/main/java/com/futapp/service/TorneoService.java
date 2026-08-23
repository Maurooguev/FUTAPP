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
        cargarReglas(torneo, requestDTO);
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
                propietario != null ? propietario.getNombre() : null,
                torneo.getIdaVuelta(), torneo.getPuntosVictoria(), torneo.getPuntosEmpate(),
                torneo.getPuntosDerrota(), torneo.getCriterioDesempate()
        );
    }

    private void cargarReglas(Torneo torneo, TorneoRequestDTO request) {
        String formato = request.getFormato().trim().toUpperCase();
        if (!java.util.Set.of("LIGA", "ELIMINACIÓN DIRECTA", "GRUPOS Y ELIMINACIÓN").contains(formato)) {
            throw new IllegalArgumentException("El formato seleccionado no es válido");
        }
        torneo.setFormato(formato.equals("LIGA") ? "Liga" : formato.equals("ELIMINACIÓN DIRECTA") ? "Eliminación directa" : "Grupos y eliminación");
        torneo.setIdaVuelta(Boolean.TRUE.equals(request.getIdaVuelta()) && formato.equals("LIGA"));
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
