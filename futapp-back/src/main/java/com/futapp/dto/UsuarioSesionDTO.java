package com.futapp.dto;

import com.futapp.model.Usuario;

public record UsuarioSesionDTO(Long idUsuario, String nombre, String email, String rol) {
    public static UsuarioSesionDTO from(Usuario usuario) {
        return new UsuarioSesionDTO(
                usuario.getIdUsuario(), usuario.getNombre(), usuario.getEmail(), usuario.getRol()
        );
    }
}
