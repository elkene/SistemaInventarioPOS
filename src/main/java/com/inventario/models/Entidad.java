package com.inventario.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Clase abstracta base para todas las entidades
 */
public abstract class Entidad implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    protected Long id;
    protected LocalDateTime fechaCreacion;
    protected LocalDateTime fechaModificacion;
    
    public Entidad() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaModificacion = LocalDateTime.now();
    }
    
    public Entidad(Long id) {
        this();
        this.id = id;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }
    
    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Entidad entidad = (Entidad) o;
        
        return id != null ? id.equals(entidad.id) : entidad.id == null;
    }
    
    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
