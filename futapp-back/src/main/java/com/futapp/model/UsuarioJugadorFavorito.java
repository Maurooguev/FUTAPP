package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_jugador_favorito", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_jugador"}))
public class UsuarioJugadorFavorito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito") private Long idFavorito;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @ManyToOne(optional = false) @JoinColumn(name = "id_jugador") private Jugador jugador;
    @Column(name = "fecha_alta", nullable = false) private LocalDateTime fechaAlta = LocalDateTime.now();
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }
}
