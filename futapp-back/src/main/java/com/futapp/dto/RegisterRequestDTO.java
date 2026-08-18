package com.futapp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequestDTO(@NotBlank String nombre, @Email @NotBlank String email,
                                 @Size(min=8, message="La contraseña debe tener al menos 8 caracteres") String password,
                                 String rol) {}
