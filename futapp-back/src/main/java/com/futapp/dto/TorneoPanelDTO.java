package com.futapp.dto;

public record TorneoPanelDTO(
        TorneoResponseDTO torneo,
        int equiposInscriptos,
        int equiposSinPlantel,
        int partidosJugados,
        int partidosPendientes,
        Integer proximaJornada,
        String accionRecomendada,
        Long idProximoPartido,
        String proximoPartidoLocal,
        String proximoPartidoVisitante
) {}
