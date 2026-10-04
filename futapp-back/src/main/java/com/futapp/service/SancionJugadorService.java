package com.futapp.service;

import com.futapp.dto.SancionRequestDTO;
import com.futapp.dto.SancionResponseDTO;
import com.futapp.exception.ResourceNotFoundException;
import com.futapp.model.Partido;
import com.futapp.model.SancionJugador;
import com.futapp.model.Tarjeta;
import com.futapp.repository.PartidoRepository;
import com.futapp.repository.SancionJugadorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SancionJugadorService {
    private final SancionJugadorRepository sanciones;
    private final PartidoRepository partidos;
    private final TorneoAuthorizationService authorization;

    public SancionJugadorService(SancionJugadorRepository sanciones, PartidoRepository partidos,
                                  TorneoAuthorizationService authorization) {
        this.sanciones = sanciones;
        this.partidos = partidos;
        this.authorization = authorization;
    }

    public List<SancionResponseDTO> listarPorTorneo(Long idTorneo) {
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

    void sincronizarConTarjeta(Tarjeta tarjeta) {
        boolean esRoja = "ROJA".equalsIgnoreCase(tarjeta.getTipoTarjeta());
        var existente = sanciones.findByTarjetaIdTarjeta(tarjeta.getIdTarjeta());
        if (esRoja && existente.isEmpty()) {
            SancionJugador sancion = new SancionJugador();
            sancion.setTarjeta(tarjeta);
            sancion.setFechasTotal(1);
            sancion.setMotivo(tarjeta.getMotivo());
            sanciones.save(sancion);
        } else if (!esRoja && existente.isPresent()) {
            sanciones.delete(existente.get());
        }
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

