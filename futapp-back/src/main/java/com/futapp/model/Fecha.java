package com.futapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "fecha")
public class Fecha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_fecha")
    private Long idFecha;

    @Column(name = "numero_fecha")
    private Integer numeroFecha;

    private String descripcion;
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_torneo", nullable = false)
    private Torneo torneo;

    public Fecha() {
    }

    public Long getIdFecha() {
        return idFecha;
    }

    public void setIdFecha(Long idFecha) {
        this.idFecha = idFecha;
    }

    public Integer getNumeroFecha() {
        return numeroFecha;
    }

    public void setNumeroFecha(Integer numeroFecha) {
        this.numeroFecha = numeroFecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Torneo getTorneo() {
        return torneo;
    }

    public void setTorneo(Torneo torneo) {
        this.torneo = torneo;
    }
}