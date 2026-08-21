package com.futapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificacion")
public class Notificacion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_notificacion") private Long idNotificacion;
    @ManyToOne(optional = false) @JoinColumn(name = "id_usuario") private Usuario usuario;
    @Column(nullable = false, length = 40) private String tipo;
    @Column(nullable = false, length = 160) private String titulo;
    @Column(nullable = false, length = 500) private String mensaje;
    private String url;
    @Column(name = "fecha_creacion", nullable = false) private LocalDateTime fechaCreacion = LocalDateTime.now();
    @Column(name = "fecha_lectura") private LocalDateTime fechaLectura;
    public Long getIdNotificacion() { return idNotificacion; }
    public Usuario getUsuario() { return usuario; } public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getTipo() { return tipo; } public void setTipo(String tipo) { this.tipo = tipo; }
    public String getTitulo() { return titulo; } public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getMensaje() { return mensaje; } public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public String getUrl() { return url; } public void setUrl(String url) { this.url = url; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public LocalDateTime getFechaLectura() { return fechaLectura; } public void setFechaLectura(LocalDateTime fechaLectura) { this.fechaLectura = fechaLectura; }
}
