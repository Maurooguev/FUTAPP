package com.futapp.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ChangePasswordRequestDTO(@NotBlank @Size(max=128) String passwordActual,
        @NotBlank @Size(min=10, max=128, message="La contraseña debe tener entre 10 y 128 caracteres") String nuevaPassword) {}
