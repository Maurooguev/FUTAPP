package com.futapp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SancionRequestDTO(
        @NotNull @Min(1) Integer fechasTotal,
        @Size(max = 500) String motivo
) {}

