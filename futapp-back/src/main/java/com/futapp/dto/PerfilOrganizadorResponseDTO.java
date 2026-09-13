package com.futapp.dto;

import java.util.List;

public record PerfilOrganizadorResponseDTO(Long idPerfil, Long idUsuario, String nombrePublico,
                                           String descripcion, String fotoPerfil, String fotoPortada,
                                           List<TorneoPerfilOrganizadorDTO> torneos) {
}
