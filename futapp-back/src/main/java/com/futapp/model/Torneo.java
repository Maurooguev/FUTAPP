package com.futapp.model;

import jakarta.persistence.*;
import com.futapp.util.TextNormalizer;

@Entity
@Table(name = "torneo")
public class Torneo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_torneo")
    private Long idTorneo;

    private String nombre;
    @Column(length = 500)
    private String descripcion;
    private String ubicacion;
    @Column(name = "direccion_sede", length = 200)
    private String direccionSede;
    @Column(name = "google_maps_url", length = 1000)
    private String googleMapsUrl;
    private String estado;
    private String formato;
    @Column(name = "ida_vuelta", nullable = false) private Boolean idaVuelta = false;
    @Column(name = "puntos_victoria", nullable = false) private Integer puntosVictoria = 3;
    @Column(name = "puntos_empate", nullable = false) private Integer puntosEmpate = 1;
    @Column(name = "puntos_derrota", nullable = false) private Integer puntosDerrota = 0;
    @Column(name = "criterio_desempate", nullable = false) private String criterioDesempate = "DIFERENCIA_GOLES";

    @Column(name = "cantidad_equipos")
    private Integer cantidadEquipos;

    @Column(name = "codigo_publico", unique = true, length = 12)
    private String codigoPublico;

    @ManyToOne
    @JoinColumn(name = "id_propietario")
    private Usuario propietario;

    @ManyToOne
    @JoinColumn(name = "id_campeon")
    private Equipo campeon;
    @Column(nullable = false) private Long visitas = 0L;

    public Torneo() {
    }

    public Long getIdTorneo() {
        return idTorneo;
    }

    public void setIdTorneo(Long idTorneo) {
        this.idTorneo = idTorneo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = TextNormalizer.titleCase(nombre);
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = TextNormalizer.titleCase(descripcion);
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = TextNormalizer.titleCase(ubicacion);
    }

    public String getDireccionSede() { return direccionSede; }
    public void setDireccionSede(String direccionSede) { this.direccionSede = TextNormalizer.titleCase(direccionSede); }
    public String getGoogleMapsUrl() { return googleMapsUrl; }
    public void setGoogleMapsUrl(String googleMapsUrl) { this.googleMapsUrl = googleMapsUrl == null || googleMapsUrl.isBlank() ? null : googleMapsUrl.trim(); }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }
    public Boolean getIdaVuelta() { return idaVuelta; } public void setIdaVuelta(Boolean idaVuelta) { this.idaVuelta = idaVuelta; }
    public Integer getPuntosVictoria() { return puntosVictoria; } public void setPuntosVictoria(Integer value) { this.puntosVictoria = value; }
    public Integer getPuntosEmpate() { return puntosEmpate; } public void setPuntosEmpate(Integer value) { this.puntosEmpate = value; }
    public Integer getPuntosDerrota() { return puntosDerrota; } public void setPuntosDerrota(Integer value) { this.puntosDerrota = value; }
    public String getCriterioDesempate() { return criterioDesempate; } public void setCriterioDesempate(String value) { this.criterioDesempate = value; }

    public Integer getCantidadEquipos() {
        return cantidadEquipos;
    }

    public void setCantidadEquipos(Integer cantidadEquipos) {
        this.cantidadEquipos = cantidadEquipos;
    }

    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }
    public Usuario getPropietario() { return propietario; }
    public void setPropietario(Usuario propietario) { this.propietario = propietario; }
    public Equipo getCampeon() { return campeon; }
    public void setCampeon(Equipo campeon) { this.campeon = campeon; }
    public Long getVisitas() { return visitas; }
    public void setVisitas(Long visitas) { this.visitas = visitas; }
}
