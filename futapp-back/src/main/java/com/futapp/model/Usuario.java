package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;
    @Column(nullable = false) private String nombre;
    @Column(nullable = false) private String email;
    @Column(name = "password_hash", nullable = false) private String passwordHash;
    @Column(nullable = false) private String rol;
    @Column(name = "failed_login_attempts", nullable = false) private int failedLoginAttempts;
    @Column(name = "login_failure_started_at") private LocalDateTime loginFailureStartedAt;
    @Column(name = "locked_until") private LocalDateTime lockedUntil;
    @Column(name = "password_change_required", nullable = false) private boolean passwordChangeRequired;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "id_usuario"))
    @Column(name = "rol")
    private java.util.Set<String> roles = new java.util.HashSet<>();
    public Long getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = TextNormalizer.titleCase(nombre); }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; this.roles.clear(); this.roles.add(rol); }
    public java.util.Set<String> getRoles() { return this.rol == null ? java.util.Set.of() : java.util.Set.of(this.rol); }
    public boolean tieneRol(String rol) { return rol.equals(this.rol); }
    public int getFailedLoginAttempts() { return failedLoginAttempts; }
    public void setFailedLoginAttempts(int value) { this.failedLoginAttempts = value; }
    public LocalDateTime getLoginFailureStartedAt() { return loginFailureStartedAt; }
    public void setLoginFailureStartedAt(LocalDateTime value) { this.loginFailureStartedAt = value; }
    public LocalDateTime getLockedUntil() { return lockedUntil; }
    public void setLockedUntil(LocalDateTime value) { this.lockedUntil = value; }
    public boolean isPasswordChangeRequired() { return passwordChangeRequired; }
    public void setPasswordChangeRequired(boolean value) { this.passwordChangeRequired = value; }
    public void agregarRol(String rol) {
        if (this.rol == null || prioridad(rol) > prioridad(this.rol)) setRol(rol);
    }
    private int prioridad(String value) {
        return switch (value) { case "ADMIN" -> 4; case "ORGANIZADOR" -> 3; case "DIRECTOR_EQUIPO" -> 2; default -> 1; };
    }
}
