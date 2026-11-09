package com.futapp.dto;

import java.util.List;

public record CargaRapidaTorneoResponseDTO(
        List<EquipoResponseDTO> equipos,
        int jugadoresCreados) {}
