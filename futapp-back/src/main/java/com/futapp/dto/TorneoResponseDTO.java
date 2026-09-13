package com.futapp.dto;

public class TorneoResponseDTO {

    private Long idTorneo;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String estado;
    private String formato;
    private Integer cantidadEquipos;
    private String codigoPublico;
    private Long idPropietario;
    private String nombrePropietario;
    private Boolean idaVuelta;
    private Integer puntosVictoria;
    private Integer puntosEmpate;
    private Integer puntosDerrota;
    private String criterioDesempate;
    private Long idCampeon;
    private String nombreCampeon;

    public TorneoResponseDTO() {
    }

    public TorneoResponseDTO(Long idTorneo, String nombre, String descripcion, String ubicacion,
                             String estado, String formato, Integer cantidadEquipos,
                             String codigoPublico, Long idPropietario, String nombrePropietario,
                             Boolean idaVuelta, Integer puntosVictoria, Integer puntosEmpate,
                             Integer puntosDerrota, String criterioDesempate) {
        this.idTorneo = idTorneo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.formato = formato;
        this.cantidadEquipos = cantidadEquipos;
        this.codigoPublico = codigoPublico;
        this.idPropietario = idPropietario;
        this.nombrePropietario = nombrePropietario;
        this.idaVuelta = idaVuelta; this.puntosVictoria = puntosVictoria; this.puntosEmpate = puntosEmpate;
        this.puntosDerrota = puntosDerrota; this.criterioDesempate = criterioDesempate;
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
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

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

    public Integer getCantidadEquipos() {
        return cantidadEquipos;
    }

    public void setCantidadEquipos(Integer cantidadEquipos) {
        this.cantidadEquipos = cantidadEquipos;
    }
    public String getCodigoPublico() { return codigoPublico; }
    public void setCodigoPublico(String codigoPublico) { this.codigoPublico = codigoPublico; }
    public Long getIdPropietario() { return idPropietario; }
    public void setIdPropietario(Long idPropietario) { this.idPropietario = idPropietario; }
    public String getNombrePropietario() { return nombrePropietario; }
    public void setNombrePropietario(String nombrePropietario) { this.nombrePropietario = nombrePropietario; }
    public Boolean getIdaVuelta() { return idaVuelta; } public void setIdaVuelta(Boolean value) { this.idaVuelta = value; }
    public Integer getPuntosVictoria() { return puntosVictoria; } public void setPuntosVictoria(Integer value) { this.puntosVictoria = value; }
    public Integer getPuntosEmpate() { return puntosEmpate; } public void setPuntosEmpate(Integer value) { this.puntosEmpate = value; }
    public Integer getPuntosDerrota() { return puntosDerrota; } public void setPuntosDerrota(Integer value) { this.puntosDerrota = value; }
    public String getCriterioDesempate() { return criterioDesempate; } public void setCriterioDesempate(String value) { this.criterioDesempate = value; }
    public Long getIdCampeon() { return idCampeon; }
    public void setIdCampeon(Long idCampeon) { this.idCampeon = idCampeon; }
    public String getNombreCampeon() { return nombreCampeon; }
    public void setNombreCampeon(String nombreCampeon) { this.nombreCampeon = nombreCampeon; }
}
