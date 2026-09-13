package com.futapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PerfilOrganizadorRequestDTO(
        @NotBlank(message = "Ingresá un nombre público") @Size(max = 80) String nombrePublico,
        @NotBlank(message = "Ingresá una descripción") @Size(max = 1200) String descripcion,
        @Size(max = 500) String fotoPerfil,
        @Size(max = 500) String fotoPortada) {
}
