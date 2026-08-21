package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "inscripcion_jugador", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_inscripcion_equipo", "id_jugador"})
})
public class InscripcionJugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_inscripcion_jugador")
    private Long idInscripcionJugador;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_inscripcion_equipo", nullable = false)
    private InscripcionEquipo inscripcionEquipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_jugador", nullable = false)
    private Jugador jugador;

    @Column(name = "numero_camiseta")
    private Integer numeroCamiseta;
    private String posicion;
    private String estado = "ACTIVA";
    @Column(name = "fecha_alta", nullable = false)
    private LocalDate fechaAlta = LocalDate.now();
    @Column(name = "fecha_baja")
    private LocalDate fechaBaja;

    public Long getIdInscripcionJugador() { return idInscripcionJugador; }
    public InscripcionEquipo getInscripcionEquipo() { return inscripcionEquipo; }
    public void setInscripcionEquipo(InscripcionEquipo inscripcionEquipo) { this.inscripcionEquipo = inscripcionEquipo; }
    public Jugador getJugador() { return jugador; }
    public void setJugador(Jugador jugador) { this.jugador = jugador; }
    public Integer getNumeroCamiseta() { return numeroCamiseta; }
    public void setNumeroCamiseta(Integer numeroCamiseta) { this.numeroCamiseta = numeroCamiseta; }
    public String getPosicion() { return posicion; }
    public void setPosicion(String posicion) { this.posicion = posicion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public LocalDate getFechaAlta() { return fechaAlta; }
    public void setFechaAlta(LocalDate fechaAlta) { this.fechaAlta = fechaAlta; }
    public LocalDate getFechaBaja() { return fechaBaja; }
    public void setFechaBaja(LocalDate fechaBaja) { this.fechaBaja = fechaBaja; }
}
