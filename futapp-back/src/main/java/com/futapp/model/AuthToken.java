package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auth_token")
public class AuthToken {
    @Id @Column(length = 64) private String token;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(name = "expires_at", nullable = false) private LocalDateTime expiresAt;
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
}
