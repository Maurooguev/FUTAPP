package com.futapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jugador_alias", uniqueConstraints = @UniqueConstraint(columnNames = "codigo"))
public class JugadorAlias {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alias") private Long idAlias;
    @Column(nullable = false, length = 11) private String codigo;
    @ManyToOne(optional = false) @JoinColumn(name = "id_jugador") private Jugador jugador;

    public Long getIdAlias() { return idAlias; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }
}
