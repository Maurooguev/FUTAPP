package com.futapp.service;

import com.futapp.dto.SancionRequestDTO;
import com.futapp.dto.SancionResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Partido;
import com.futapp.model.SancionJugador;
import com.futapp.model.Tarjeta;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.SancionJugadorRepository;
import com.futapp.repository.TorneoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SancionJugadorService {
    private final SancionJugadorRepository sanciones;
    private final PartidoRepository partidos;
    private final TorneoAuthorizationService authorization;
    private final TorneoVisibilityService visibility;
    private final TorneoRepository torneos;

    public SancionJugadorService(SancionJugadorRepository sanciones, PartidoRepository partidos,
                                  TorneoAuthorizationService authorization, TorneoVisibilityService visibility,
                                  TorneoRepository torneos) {
        this.sanciones = sanciones;
        this.partidos = partidos;
        this.authorization = authorization;
        this.visibility = visibility;
        this.torneos = torneos;
    }

    public List<SancionResponseDTO> listarPorTorneo(Long idTorneo) {
        visibility.verificar(torneos.findById(idTorneo)
                .orElseThrow(() -> new ResourceNotFoundException("Torneo no encontrado")));
        return sanciones.findByTarjetaPartidoTorneoIdTorneoOrderByCreadaEnDesc(idTorneo).stream()
                .map(this::convertir).toList();
    }

    @Transactional
    public SancionResponseDTO actualizar(Long id, SancionRequestDTO request) {
        SancionJugador sancion = sanciones.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suspensión no encontrada"));
        authorization.verificarGestion(sancion.getTarjeta().getPartido().getTorneo());
        sancion.setFechasTotal(request.fechasTotal());
        sancion.setMotivo(request.motivo() == null || request.motivo().isBlank() ? null : request.motivo().trim());
        return convertir(sanciones.save(sancion));
    }

    void sincronizarConTarjeta(Tarjeta tarjeta, Integer fechasSuspension) {
        boolean esRoja = "ROJA".equalsIgnoreCase(tarjeta.getTipoTarjeta());
        var existente = sanciones.findByTarjetaIdTarjeta(tarjeta.getIdTarjeta());
        if (esRoja && existente.isEmpty()) {
            SancionJugador sancion = new SancionJugador();
            sancion.setTarjeta(tarjeta);
            sancion.setFechasTotal(fechasSuspension == null ? 1 : fechasSuspension);
            sancion.setMotivo(tarjeta.getMotivo());
            sanciones.save(sancion);
        } else if (esRoja && existente.isPresent() && fechasSuspension != null) {
            existente.get().setFechasTotal(fechasSuspension);
            existente.get().setMotivo(tarjeta.getMotivo());
            sanciones.save(existente.get());
        } else if (!esRoja && existente.isPresent()) {
            sanciones.delete(existente.get());
        }
    }

    public boolean estaSuspendidoParaPartido(Long idJugador, Partido partido) {
        return estaSuspendidoParaEquipo(idJugador, partido, partido.getEquipoLocal().getIdEquipo())
                || estaSuspendidoParaEquipo(idJugador, partido, partido.getEquipoVisitante().getIdEquipo());
    }

    private boolean estaSuspendidoParaEquipo(Long idJugador, Partido objetivo, Long idEquipo) {
        if (!participa(objetivo, idEquipo)) return false;
        return sanciones.findByTarjetaPartidoTorneoIdTorneoAndTarjetaJugadorIdJugadorAndTarjetaEquipoIdEquipo(
                        objetivo.getTorneo().getIdTorneo(), idJugador, idEquipo).stream()
                .anyMatch(sancion -> fechaIncluidaEnSancion(sancion, objetivo, idEquipo));
    }

    private boolean fechaIncluidaEnSancion(SancionJugador sancion, Partido objetivo, Long idEquipo) {
        int origen = sancion.getTarjeta().getPartido().getFecha().getNumeroFecha();
        if (objetivo.getFecha().getNumeroFecha() <= origen) return false;
        return partidos.findByTorneoIdTorneo(objetivo.getTorneo().getIdTorneo()).stream()
                .filter(partido -> partido.getFecha().getNumeroFecha() > origen)
                .filter(partido -> participa(partido, idEquipo))
                .map(Partido::getFecha)
                .collect(java.util.stream.Collectors.toMap(fecha -> fecha.getIdFecha(), fecha -> fecha,
                        (primera, ignorada) -> primera))
                .values().stream()
                .sorted(java.util.Comparator.comparingInt(fecha -> fecha.getNumeroFecha()))
                .limit(sancion.getFechasTotal())
                .anyMatch(fecha -> fecha.getIdFecha().equals(objetivo.getFecha().getIdFecha()));
    }

    void eliminarPorTarjeta(Long idTarjeta) {
        sanciones.deleteByTarjetaIdTarjeta(idTarjeta);
    }

    private SancionResponseDTO convertir(SancionJugador sancion) {
        Tarjeta tarjeta = sancion.getTarjeta();
        int fechaOrigen = tarjeta.getPartido().getFecha().getNumeroFecha();
        Long idTorneo = tarjeta.getPartido().getTorneo().getIdTorneo();
        Long idEquipo = tarjeta.getEquipo().getIdEquipo();
        long cumplidasCalculadas = partidos.findByTorneoIdTorneo(idTorneo).stream()
                .filter(partido -> partido.getFecha().getNumeroFecha() > fechaOrigen)
                .filter(partido -> "FINALIZADA".equalsIgnoreCase(partido.getFecha().getEstado()))
                .filter(partido -> participa(partido, idEquipo))
                .map(partido -> partido.getFecha().getIdFecha())
                .distinct().count();
        int cumplidas = Math.min(sancion.getFechasTotal(), Math.toIntExact(cumplidasCalculadas));
        int restantes = Math.max(0, sancion.getFechasTotal() - cumplidas);
        return new SancionResponseDTO(
                sancion.getIdSancion(), tarjeta.getJugador().getIdJugador(),
                tarjeta.getJugador().getNombre() + " " + tarjeta.getJugador().getApellido(),
                tarjeta.getJugador().getFoto(), idEquipo, tarjeta.getEquipo().getNombre(),
                sancion.getFechasTotal(), cumplidas, restantes, sancion.getMotivo(), fechaOrigen, restantes > 0
        );
    }

    private boolean participa(Partido partido, Long idEquipo) {
        return partido.getEquipoLocal().getIdEquipo().equals(idEquipo)
                || partido.getEquipoVisitante().getIdEquipo().equals(idEquipo);
    }
}

