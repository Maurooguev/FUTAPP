package com.futapp.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequestDTO(@NotBlank @Size(max=100) String nombre,
                                 @Email @NotBlank @Size(max=254) String email,
                                 @NotBlank @Size(min=10, max=128, message="La contraseña debe tener entre 10 y 128 caracteres") String password,
                                 @Size(max=30) String rol, @Size(max=128) String codigoInvitacion) {
    public RegisterRequestDTO(String nombre, String email, String password, String rol) {
        this(nombre, email, password, rol, null);
    }
}
