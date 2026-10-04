package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sancion_jugador")
public class SancionJugador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sancion")
    private Long idSancion;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_tarjeta", nullable = false, unique = true)
    private Tarjeta tarjeta;

    @Column(name = "fechas_total", nullable = false)
    private Integer fechasTotal = 1;

    @Column(length = 500)
    private String motivo;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn = LocalDateTime.now();

    public Long getIdSancion() { return idSancion; }
    public Tarjeta getTarjeta() { return tarjeta; }
    public void setTarjeta(Tarjeta tarjeta) { this.tarjeta = tarjeta; }
    public Integer getFechasTotal() { return fechasTotal; }
    public void setFechasTotal(Integer fechasTotal) { this.fechasTotal = fechasTotal; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
}

