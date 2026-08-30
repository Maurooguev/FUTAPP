package com.futapp.dto;
public record AuthResponseDTO(String token, Long idUsuario, String nombre, String email, String rol,
                              java.util.Set<String> roles) {}
