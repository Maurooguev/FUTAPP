package com.futapp.dto;

import com.futapp.model.Usuario;

public record UsuarioSesionDTO(Long idUsuario, String nombre, String email, String rol,
                                java.util.Set<String> roles, boolean passwordChangeRequired) {
    public static UsuarioSesionDTO from(Usuario usuario) {
        return new UsuarioSesionDTO(
                usuario.getIdUsuario(), usuario.getNombre(), usuario.getEmail(), usuario.getRol(), usuario.getRoles(),
                usuario.isPasswordChangeRequired()
        );
    }
}
