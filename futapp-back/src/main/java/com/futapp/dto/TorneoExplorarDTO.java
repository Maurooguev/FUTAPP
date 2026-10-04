package com.futapp.dto;

import java.util.List;

public record TorneoExplorarDTO(
        Long idTorneo,
        String nombre,
        String descripcion,
        String ubicacion,
        String estado,
        String formato,
        Integer cantidadEquipos,
        String codigoPublico,
        Long idPropietario,
        String nombrePropietario,
        Long idCampeon,
        String nombreCampeon,
        String escudoCampeon,
        Long visitas,
        PartidoExplorarDTO proximoPartido,
        List<TablaPosicionDTO> podio
) {
}
