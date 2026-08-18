package com.futapp.service;

import com.futapp.dto.EquipoRequestDTO;
import com.futapp.dto.EquipoResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Torneo;
import com.futapp.model.InscripcionEquipo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.JugadorRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.InscripcionEquipoRepository;
import com.futapp.repository.InscripcionJugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final TorneoRepository torneoRepository;
    private final JugadorRepository jugadorRepository;
    private final PartidoRepository partidoRepository;
    private final TorneoAuthorizationService authorization;
    private final InscripcionEquipoRepository inscripcionesEquipo;
    private final InscripcionJugadorRepository inscripcionesJugador;
    private final EquipoAuthorizationService equipoAuthorization;

    public EquipoService(EquipoRepository equipoRepository, TorneoRepository torneoRepository,
                         JugadorRepository jugadorRepository, PartidoRepository partidoRepository,
                         TorneoAuthorizationService authorization,
                         InscripcionEquipoRepository inscripcionesEquipo,
                         InscripcionJugadorRepository inscripcionesJugador,
                         EquipoAuthorizationService equipoAuthorization) {
        this.equipoRepository = equipoRepository;
        this.torneoRepository = torneoRepository;
        this.jugadorRepository = jugadorRepository;
        this.partidoRepository = partidoRepository;
        this.authorization = authorization;
        this.inscripcionesEquipo = inscripcionesEquipo;
        this.inscripcionesJugador = inscripcionesJugador;
        this.equipoAuthorization = equipoAuthorization;
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

    public boolean puedeGestionar(Long id) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        return equipoAuthorization.puedeGestionar(equipo);
    }

    public List<EquipoResponseDTO> listarEquiposPorTorneo(Long idTorneo) {
        return inscripcionesEquipo.findByTorneoIdTorneo(idTorneo)
                .stream()
                .map(inscripcion -> convertirAResponseDTO(inscripcion.getEquipo(), inscripcion.getTorneo()))
                .collect(Collectors.toList());
    }

    public EquipoResponseDTO crearEquipo(EquipoRequestDTO requestDTO) {
        authorization.verificarGestion(requestDTO.getIdTorneo());
        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        Equipo equipo = new Equipo();
        equipo.setNombre(requestDTO.getNombre());
        equipo.setEscudo(requestDTO.getEscudo());
        equipo.setDescripcion(normalizarOpcional(requestDTO.getDescripcion()));
        equipo.setFotoPlantel(normalizarOpcional(requestDTO.getFotoPlantel()));
        equipo.setEmailContacto(normalizarOpcional(requestDTO.getEmailContacto()));
        equipo.setColorCamiseta(requestDTO.getColorCamiseta());
        equipo.setBarrio(requestDTO.getBarrio());
        equipo.setResponsable(requestDTO.getResponsable());
        equipo.setTelefonoContacto(requestDTO.getTelefonoContacto());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setTorneo(torneo);

        Equipo equipoGuardado = equipoRepository.save(equipo);
        crearInscripcionSiFalta(equipoGuardado, torneo);
        return convertirAResponseDTO(equipoGuardado);
    }

    public EquipoResponseDTO actualizarEquipo(Long id, EquipoRequestDTO requestDTO) {
        Equipo equipo = equipoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipo no encontrado con id: " + id));
        equipoAuthorization.verificarGestion(equipo);

        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        equipo.setNombre(requestDTO.getNombre());
        equipo.setEscudo(requestDTO.getEscudo());
        equipo.setDescripcion(normalizarOpcional(requestDTO.getDescripcion()));
        equipo.setFotoPlantel(normalizarOpcional(requestDTO.getFotoPlantel()));
        equipo.setEmailContacto(normalizarOpcional(requestDTO.getEmailContacto()));
        equipo.setColorCamiseta(requestDTO.getColorCamiseta());
        equipo.setBarrio(requestDTO.getBarrio());
        equipo.setResponsable(requestDTO.getResponsable());
        equipo.setTelefonoContacto(requestDTO.getTelefonoContacto());
        equipo.setEstado(requestDTO.getEstado());
        equipo.setTorneo(torneo);

        Equipo equipoActualizado = equipoRepository.save(equipo);
        crearInscripcionSiFalta(equipoActualizado, torneo);
        return convertirAResponseDTO(equipoActualizado);
    }

    public void eliminarEquipo(Long id) {
        if (!equipoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipo no encontrado con id: " + id);
        }
        equipoAuthorization.verificarGestion(equipoRepository.findById(id).orElseThrow());

        if (!jugadorRepository.findByEquipoIdEquipo(id).isEmpty()) {
            throw new IllegalArgumentException("No se puede eliminar el equipo porque tiene jugadores registrados");
        }
        if (partidoRepository.existsByEquipoLocalIdEquipoOrEquipoVisitanteIdEquipo(id, id)) {
            throw new IllegalArgumentException("No se puede eliminar el equipo porque tiene partidos asociados");
        }

        inscripcionesJugador.deleteByInscripcionEquipoEquipoIdEquipo(id);
        inscripcionesEquipo.deleteByEquipoIdEquipo(id);
        equipoRepository.deleteById(id);
    }

    private EquipoResponseDTO convertirAResponseDTO(Equipo equipo) {
        return convertirAResponseDTO(equipo, equipo.getTorneo());
    }

    private EquipoResponseDTO convertirAResponseDTO(Equipo equipo, Torneo torneo) {
        return new EquipoResponseDTO(
                equipo.getIdEquipo(),
                equipo.getNombre(),
                equipo.getEscudo(),
                equipo.getCodigoPublico(),
                equipo.getDescripcion(),
                equipo.getFotoPlantel(),
                equipo.getEmailContacto(),
                equipo.getColorCamiseta(),
                equipo.getBarrio(),
                equipo.getResponsable(),
                equipo.getTelefonoContacto(),
                equipo.getEstado(),
                torneo.getIdTorneo(),
                torneo.getNombre()
        );
    }

    private String normalizarOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private void crearInscripcionSiFalta(Equipo equipo, Torneo torneo) {
        if (inscripcionesEquipo.findByEquipoIdEquipoAndTorneoIdTorneo(
                equipo.getIdEquipo(), torneo.getIdTorneo()).isPresent()) return;
        InscripcionEquipo inscripcion = new InscripcionEquipo();
        inscripcion.setEquipo(equipo);
        inscripcion.setTorneo(torneo);
        inscripcion.setEstado("ACTIVA");
        inscripcionesEquipo.save(inscripcion);
    }
}
