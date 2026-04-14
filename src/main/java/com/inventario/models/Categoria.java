package com.inventario.models;

/**
 * Modelo de Categoría de Productos
 */
public class Categoria extends Entidad {
    
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private String descripcion;
    
    public Categoria() {
        super();
    }
    
    public Categoria(Long id, String nombre) {
        super(id);
        this.nombre = nombre;
    }
    
    // Getters and Setters
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
    
    @Override
    public String toString() {
        return nombre;
    }
}
