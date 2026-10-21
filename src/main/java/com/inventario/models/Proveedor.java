package com.inventario.models;

/**
 * Modelo de Proveedor
 */
public class Proveedor extends Entidad {
    
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private String rif;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String condicionesPago;
    
    public Proveedor() {
        super();
    }
    
    public Proveedor(Long id, String nombre, String rif) {
        super(id);
        this.nombre = nombre;
        this.rif = rif;
    }
    
    // Getters and Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getRif() {
        return rif;
    }
    
    public void setRif(String rif) {
        this.rif = rif;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getCiudad() {
        return ciudad;
    }
    
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    public String getCondicionesPago() {
        return condicionesPago;
    }
    
    public void setCondicionesPago(String condicionesPago) {
        this.condicionesPago = condicionesPago;
    }
    
    @Override
    public String toString() {
        return nombre + " (RFC: " + rif + ")";
    }
}
