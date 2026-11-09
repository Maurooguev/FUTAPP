package com.futapp.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record NamesBatchRequestDTO(
        @NotEmpty(message = "Ingresá al menos un nombre")
        @Size(max = 100, message = "No podés cargar más de 100 nombres por vez")
        List<@Size(max = 120, message = "Cada nombre puede tener hasta 120 caracteres") String> nombres) {}
