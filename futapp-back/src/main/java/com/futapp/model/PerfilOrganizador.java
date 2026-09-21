package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;

@Entity
@Table(name = "perfil_organizador")
public class PerfilOrganizador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Long idPerfil;

    @OneToOne(optional = false)
    @JoinColumn(name = "id_usuario", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "nombre_publico", nullable = false, length = 80)
    private String nombrePublico;

    @Column(nullable = false, length = 1200)
    private String descripcion;

    @Column(name = "foto_perfil", length = 500)
    private String fotoPerfil;

    @Column(name = "foto_portada", length = 500)
    private String fotoPortada;

    public Long getIdPerfil() { return idPerfil; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombrePublico() { return nombrePublico; }
    public void setNombrePublico(String nombrePublico) { this.nombrePublico = TextNormalizer.titleCase(nombrePublico); }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = TextNormalizer.titleCase(descripcion); }
    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }
    public String getFotoPortada() { return fotoPortada; }
    public void setFotoPortada(String fotoPortada) { this.fotoPortada = fotoPortada; }
}
