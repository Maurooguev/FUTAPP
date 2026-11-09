package com.futapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOrganizerRequestDTO(
        @NotBlank @Size(max = 100) String nombre,
        @Email @NotBlank @Size(max = 254) String email) {}
