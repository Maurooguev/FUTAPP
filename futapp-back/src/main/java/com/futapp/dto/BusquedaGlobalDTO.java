package com.futapp.dto;

import java.util.List;

public record BusquedaGlobalDTO(
        List<ResultadoBusquedaDTO> torneos,
        List<ResultadoBusquedaDTO> equipos,
        List<ResultadoBusquedaDTO> jugadores
) {
}
