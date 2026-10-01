package com.futapp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequestDTO(@NotBlank String nombre, @Email @NotBlank String email,
                                 @Size(min=10, message="La contraseña debe tener al menos 10 caracteres") String password,
                                 String rol, String codigoInvitacion) {
    public RegisterRequestDTO(String nombre, String email, String password, String rol) {
        this(nombre, email, password, rol, null);
    }
}
