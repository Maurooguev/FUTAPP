package com.futapp.dto;

public record TorneoPerfilOrganizadorDTO(Long idTorneo, String nombre, String descripcion, String ubicacion,
                                         String estado, String formato, Integer cantidadEquipos, String codigoPublico) {
}
