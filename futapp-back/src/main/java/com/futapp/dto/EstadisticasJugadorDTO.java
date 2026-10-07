package com.futapp.dto;

import java.util.List;

public record EstadisticasJugadorDTO(
        long goles,
        long amarillas,
        long rojas,
        long vecesMvp,
        long torneosDisputados,
        long equiposRepresentados,
        List<CampeonatoJugadorDTO> campeonatos,
        long vecesGoleador,
        Double promedioGolesPorPartido,
        Long vallasInvictas
) {
    public record CampeonatoJugadorDTO(Long idTorneo, String nombreTorneo, Long idEquipo, String nombreEquipo) {}
}
