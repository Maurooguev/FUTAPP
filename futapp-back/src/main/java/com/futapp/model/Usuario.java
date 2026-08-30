package com.futapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false) private String rol;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "id_usuario"))
    @Column(name = "rol")
    private java.util.Set<String> roles = new java.util.HashSet<>();
    public Long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; this.roles.add(rol); }
    public java.util.Set<String> getRoles() { return java.util.Set.copyOf(roles); }
    public boolean tieneRol(String rol) { return roles.contains(rol) || rol.equals(this.rol); }
    public void agregarRol(String rol) {
        roles.add(rol);
        if (this.rol == null || prioridad(rol) > prioridad(this.rol)) this.rol = rol;
    }
    private int prioridad(String value) {
        return switch (value) { case "ADMIN" -> 4; case "ORGANIZADOR" -> 3; case "DIRECTOR_EQUIPO" -> 2; default -> 1; };
    }
}
