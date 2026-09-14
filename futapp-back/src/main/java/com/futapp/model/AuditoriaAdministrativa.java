package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_administrativa")
public class AuditoriaAdministrativa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(name = "id_usuario")
    private Long idUsuario;
    @Column(length = 160)
    private String usuario;

    @Column(nullable = false, length = 10)
    private String metodo;
    @Column(nullable = false, length = 500)
    private String ruta;
    @Column(name = "estado_http", nullable = false)
    private Integer estadoHttp;
    @Column(nullable = false, length = 20)
    private String resultado;
    @Column(name = "request_id", nullable = false, length = 64)
    private String requestId;
    @Column(nullable = false)
    private LocalDateTime fecha;

    public Long getIdAuditoria() { return idAuditoria; }
    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public String getMetodo() { return metodo; }
    public void setMetodo(String metodo) { this.metodo = metodo; }
    public String getRuta() { return ruta; }
    public void setRuta(String ruta) { this.ruta = ruta; }
    public Integer getEstadoHttp() { return estadoHttp; }
    public void setEstadoHttp(Integer estadoHttp) { this.estadoHttp = estadoHttp; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}
