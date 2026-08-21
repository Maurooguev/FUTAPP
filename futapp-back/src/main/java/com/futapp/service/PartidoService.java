package com.futapp.service;

import com.futapp.dto.PartidoRequestDTO;
import com.futapp.dto.PartidoResponseDTO;
import com.futapp.dto.ResultadoPartidoDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Equipo;
import com.futapp.model.Fecha;
import com.futapp.model.Partido;
import com.futapp.model.Torneo;
import com.futapp.repository.EquipoRepository;
import com.futapp.repository.FechaRepository;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.TorneoRepository;
import com.futapp.repository.GolRepository;
import com.futapp.repository.TarjetaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PartidoService {

    private final PartidoRepository partidoRepository;
    private final TorneoRepository torneoRepository;
    private final FechaRepository fechaRepository;
    private final EquipoRepository equipoRepository;
    private final GolRepository golRepository;
    private final TarjetaRepository tarjetaRepository;
    private final TorneoAuthorizationService authorization;
    private final NotificacionService notificaciones;

    public PartidoService(PartidoRepository partidoRepository,
                          TorneoRepository torneoRepository,
                          FechaRepository fechaRepository,
                          EquipoRepository equipoRepository,
                          GolRepository golRepository,
                          TarjetaRepository tarjetaRepository,
                          TorneoAuthorizationService authorization,
                          NotificacionService notificaciones) {
        this.partidoRepository = partidoRepository;
        this.torneoRepository = torneoRepository;
        this.fechaRepository = fechaRepository;
        this.equipoRepository = equipoRepository;
        this.golRepository = golRepository;
        this.tarjetaRepository = tarjetaRepository;
        this.authorization = authorization;
        this.notificaciones = notificaciones;
    }

    public List<PartidoResponseDTO> listarPartidos() {
        return partidoRepository.findAll()
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PartidoResponseDTO obtenerPartidoPorId(Long id) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));

        return convertirAResponseDTO(partido);
    }

    public List<PartidoResponseDTO> listarPartidosPorTorneo(Long idTorneo) {
        return partidoRepository.findByTorneoIdTorneo(idTorneo)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public List<PartidoResponseDTO> listarPartidosPorFecha(Long idFecha) {
        return partidoRepository.findByFechaIdFecha(idFecha)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    public PartidoResponseDTO crearPartido(PartidoRequestDTO requestDTO) {
        authorization.verificarGestion(requestDTO.getIdTorneo());
        Partido partido = new Partido();
        cargarDatosPartido(partido, requestDTO);
        partido = partidoRepository.save(partido);
        notificaciones.avisarPartido(partido, "FINALIZADO".equalsIgnoreCase(partido.getEstado()));
        return convertirAResponseDTO(partido);
    }

    @Transactional
    public List<PartidoResponseDTO> generarFixture(Long idTorneo) {
        authorization.verificarGestion(idTorneo);
        Torneo torneo = torneoRepository.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + idTorneo));

        if (!"LIGA".equalsIgnoreCase(torneo.getFormato())) {
            throw new IllegalArgumentException("Por ahora el fixture automático está disponible para torneos con formato Liga");
        }

        if (!partidoRepository.findByTorneoIdTorneo(idTorneo).isEmpty()) {
            throw new IllegalArgumentException("El fixture automático solo se puede generar si el torneo todavía no tiene partidos");
        }

        List<Equipo> equipos = new ArrayList<>(equipoRepository.findByTorneoIdTorneo(idTorneo));
        equipos.sort(Comparator.comparing(Equipo::getIdEquipo));
        if (equipos.size() < 2) {
            throw new IllegalArgumentException("Se necesitan al menos 2 equipos para generar el fixture");
        }

        if (equipos.size() % 2 != 0) {
            equipos.add(null);
        }

        List<Equipo> rotacion = new ArrayList<>(equipos);
        List<Partido> partidosGenerados = new ArrayList<>();
        int cantidadFechas = rotacion.size() - 1;
        List<Fecha> fechasExistentes = new ArrayList<>(fechaRepository.findByTorneoIdTorneo(idTorneo));
        fechasExistentes.sort(Comparator.comparing(Fecha::getNumeroFecha));
        int siguienteNumero = fechasExistentes.stream()
                .map(Fecha::getNumeroFecha)
                .max(Integer::compareTo)
                .orElse(0) + 1;

        for (int ronda = 0; ronda < cantidadFechas; ronda++) {
            Fecha fecha;
            if (ronda < fechasExistentes.size()) {
                fecha = fechasExistentes.get(ronda);
            } else {
                fecha = new Fecha();
                fecha.setNumeroFecha(siguienteNumero++);
                fecha.setDescripcion("Fecha " + fecha.getNumeroFecha());
                fecha.setEstado(ronda == 0 ? "ACTIVA" : "PENDIENTE");
                fecha.setTorneo(torneo);
                fecha = fechaRepository.save(fecha);
            }

            for (int indice = 0; indice < rotacion.size() / 2; indice++) {
                Equipo primero = rotacion.get(indice);
                Equipo segundo = rotacion.get(rotacion.size() - 1 - indice);
                if (primero == null || segundo == null) {
                    continue;
                }

                boolean invertirLocalia = (ronda + indice) % 2 != 0;
                Partido partido = new Partido();
                partido.setTorneo(torneo);
                partido.setFecha(fecha);
                partido.setEquipoLocal(invertirLocalia ? segundo : primero);
                partido.setEquipoVisitante(invertirLocalia ? primero : segundo);
                partido.setFechaHora("");
                partido.setEstado("PROGRAMADO");
                partido.setGolesLocal(0);
                partido.setGolesVisitante(0);
                partido.setObservaciones("Horario a confirmar");
                partidosGenerados.add(partidoRepository.save(partido));
            }

            Equipo ultimo = rotacion.remove(rotacion.size() - 1);
            rotacion.add(1, ultimo);
        }

        return partidosGenerados.stream().map(this::convertirAResponseDTO).toList();
    }

    public PartidoResponseDTO actualizarPartido(Long id, PartidoRequestDTO requestDTO) {
        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        authorization.verificarGestion(partido.getTorneo());

        cargarDatosPartido(partido, requestDTO);
        partido = partidoRepository.save(partido);
        notificaciones.avisarPartido(partido, "FINALIZADO".equalsIgnoreCase(partido.getEstado()));
        return convertirAResponseDTO(partido);
    }

    public void eliminarPartido(Long id) {
        if (!partidoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Partido no encontrado con id: " + id);
        }
        authorization.verificarGestion(partidoRepository.findById(id).orElseThrow().getTorneo());
        if (golRepository.existsByPartidoIdPartido(id) || tarjetaRepository.existsByPartidoIdPartido(id)) {
            throw new IllegalArgumentException("No se puede eliminar el partido porque tiene goles o tarjetas cargados");
        }
        partidoRepository.deleteById(id);
    }

    private void cargarDatosPartido(Partido partido, PartidoRequestDTO requestDTO) {
        Torneo torneo = torneoRepository.findById(requestDTO.getIdTorneo())
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado con id: " + requestDTO.getIdTorneo()));

        Fecha fecha = fechaRepository.findById(requestDTO.getIdFecha())
                .orElseThrow(() -> new ResourceNotFoundException("Fecha no encontrada con id: " + requestDTO.getIdFecha()));

        Equipo equipoLocal = equipoRepository.findById(requestDTO.getIdEquipoLocal())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo local no encontrado con id: " + requestDTO.getIdEquipoLocal()));

        Equipo equipoVisitante = equipoRepository.findById(requestDTO.getIdEquipoVisitante())
                .orElseThrow(() -> new ResourceNotFoundException("Equipo visitante no encontrado con id: " + requestDTO.getIdEquipoVisitante()));

        validarReglasDeNegocio(requestDTO, torneo, fecha, equipoLocal, equipoVisitante, partido.getIdPartido());

        partido.setFechaHora(requestDTO.getFechaHora());
        partido.setEstado(requestDTO.getEstado());
        partido.setGolesLocal(requestDTO.getGolesLocal());
        partido.setGolesVisitante(requestDTO.getGolesVisitante());
        partido.setObservaciones(requestDTO.getObservaciones());
        partido.setTorneo(torneo);
        partido.setFecha(fecha);
        partido.setEquipoLocal(equipoLocal);
        partido.setEquipoVisitante(equipoVisitante);
    }

    private void validarReglasDeNegocio(PartidoRequestDTO requestDTO,
                                        Torneo torneo,
                                        Fecha fecha,
                                        Equipo equipoLocal,
                                        Equipo equipoVisitante,
                                        Long partidoActualId) {

        if (equipoLocal.getIdEquipo().equals(equipoVisitante.getIdEquipo())) {
            throw new IllegalArgumentException("El equipo local y el visitante no pueden ser el mismo");
        }

        if (!equipoLocal.getTorneo().getIdTorneo().equals(torneo.getIdTorneo())) {
            throw new IllegalArgumentException("El equipo local no pertenece al torneo indicado");
        }

        if (!equipoVisitante.getTorneo().getIdTorneo().equals(torneo.getIdTorneo())) {
            throw new IllegalArgumentException("El equipo visitante no pertenece al torneo indicado");
        }

        if (!fecha.getTorneo().getIdTorneo().equals(torneo.getIdTorneo())) {
            throw new IllegalArgumentException("La fecha no pertenece al torneo indicado");
        }

        List<Partido> partidosFecha = partidoRepository.findByFechaIdFecha(fecha.getIdFecha());
        for (Partido partidoExistente : partidosFecha) {
            if (partidoActualId != null && partidoActualId.equals(partidoExistente.getIdPartido())) {
                continue;
            }
            boolean participaLocal = partidoExistente.getEquipoLocal().getIdEquipo().equals(equipoLocal.getIdEquipo())
                    || partidoExistente.getEquipoVisitante().getIdEquipo().equals(equipoLocal.getIdEquipo());
            boolean participaVisitante = partidoExistente.getEquipoLocal().getIdEquipo().equals(equipoVisitante.getIdEquipo())
                    || partidoExistente.getEquipoVisitante().getIdEquipo().equals(equipoVisitante.getIdEquipo());
            if (participaLocal || participaVisitante) {
                throw new IllegalArgumentException("Uno de los equipos ya tiene un partido asignado en esta jornada");
            }
        }

        if (requestDTO.getGolesLocal() != null && requestDTO.getGolesLocal() < 0) {
            throw new IllegalArgumentException("Los goles del equipo local no pueden ser negativos");
        }

        if (requestDTO.getGolesVisitante() != null && requestDTO.getGolesVisitante() < 0) {
            throw new IllegalArgumentException("Los goles del equipo visitante no pueden ser negativos");
        }

        if ("FINALIZADO".equalsIgnoreCase(requestDTO.getEstado())) {
            if (requestDTO.getGolesLocal() == null || requestDTO.getGolesVisitante() == null) {
                throw new IllegalArgumentException("Un partido finalizado debe tener goles local y visitante");
            }
        }
    }

    private PartidoResponseDTO convertirAResponseDTO(Partido partido) {
        return new PartidoResponseDTO(
                partido.getIdPartido(),
                partido.getFechaHora(),
                partido.getEstado(),
                partido.getGolesLocal(),
                partido.getGolesVisitante(),
                partido.getObservaciones(),
                partido.getTorneo().getIdTorneo(),
                partido.getTorneo().getNombre(),
                partido.getFecha().getIdFecha(),
                partido.getFecha().getNumeroFecha(),
                partido.getEquipoLocal().getIdEquipo(),
                partido.getEquipoLocal().getNombre(),
                partido.getEquipoVisitante().getIdEquipo(),
                partido.getEquipoVisitante().getNombre()
        );
    }

    public PartidoResponseDTO cargarResultado(Long id, ResultadoPartidoDTO dto) {

        Partido partido = partidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partido no encontrado con id: " + id));
        authorization.verificarGestion(partido.getTorneo());

        if (dto.getGolesLocal() == null || dto.getGolesVisitante() == null) {
            throw new IllegalArgumentException("Debe informar ambos goles");
        }

        if (dto.getGolesLocal() < 0 || dto.getGolesVisitante() < 0) {
            throw new IllegalArgumentException("Los goles no pueden ser negativos");
        }

        partido.setGolesLocal(dto.getGolesLocal());
        partido.setGolesVisitante(dto.getGolesVisitante());
        partido.setEstado("FINALIZADO");

        partido = partidoRepository.save(partido);
        notificaciones.avisarPartido(partido, true);
        return convertirAResponseDTO(partido);
    }

    
}


