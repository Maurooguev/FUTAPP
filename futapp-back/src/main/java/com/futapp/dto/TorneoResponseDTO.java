package com.futapp.dto;

public class TorneoResponseDTO {

    private Long idTorneo;
    private String nombre;
    private String descripcion;
    private String ubicacion;
    private String estado;
    private String formato;
    private Integer cantidadEquipos;

    public TorneoResponseDTO() {
    }

    public TorneoResponseDTO(Long idTorneo, String nombre, String descripcion, String ubicacion,
                             String estado, String formato, Integer cantidadEquipos) {
        this.idTorneo = idTorneo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.formato = formato;
        this.cantidadEquipos = cantidadEquipos;
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
}