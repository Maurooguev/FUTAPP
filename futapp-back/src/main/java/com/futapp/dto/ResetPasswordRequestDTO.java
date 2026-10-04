package com.futapp.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record ResetPasswordRequestDTO(@NotBlank @Size(max=512) String token,
        @NotBlank @Size(min=10, max=128, message="La contraseña debe tener entre 10 y 128 caracteres") String nuevaPassword) {}
