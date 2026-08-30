package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario_equipo_favorito", uniqueConstraints = @UniqueConstraint(columnNames = {"id_usuario", "id_equipo"}))
public class UsuarioEquipoFavorito {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_favorito") private Long idFavorito;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @ManyToOne(optional = false) @JoinColumn(name = "id_equipo") private Equipo equipo;
    @Column(name = "fecha_alta", nullable = false) private LocalDateTime fechaAlta = LocalDateTime.now();
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Equipo getEquipo() { return equipo; }
    public void setEquipo(Equipo equipo) { this.equipo = equipo; }
}
