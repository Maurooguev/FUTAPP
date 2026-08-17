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
    public Long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
