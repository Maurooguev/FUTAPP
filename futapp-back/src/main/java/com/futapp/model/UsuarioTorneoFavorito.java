package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_torneo_favorito", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_torneo"}))
public class UsuarioTorneoFavorito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito") private Long idFavorito;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @ManyToOne(optional = false) @JoinColumn(name = "id_torneo") private Torneo torneo;
    @Column(name = "fecha_alta", nullable = false) private LocalDateTime fechaAlta = LocalDateTime.now();
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Torneo getTorneo() { return torneo; }
    public void setTorneo(Torneo torneo) { this.torneo = torneo; }
}
