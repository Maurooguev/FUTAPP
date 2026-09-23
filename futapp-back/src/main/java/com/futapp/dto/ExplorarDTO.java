package com.futapp.dto;

import java.util.List;

public record ExplorarDTO(
        List<TorneoExplorarDTO> torneos,
        List<PartidoExplorarDTO> ultimosResultados,
        List<PartidoExplorarDTO> proximosPartidos,
        List<String> ubicaciones
) {
}
