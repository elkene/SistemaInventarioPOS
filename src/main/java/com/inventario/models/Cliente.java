package com.inventario.models;

/**
 * Modelo de Cliente
 */
public class Cliente extends Entidad {
    
    private static final long serialVersionUID = 1L;
    
    private String nombre;
    private String cedula;
    private String email;
    private String telefono;
    private String direccion;
    private String ciudad;
    private String estado;
    private Double saldoCredito;
    
    public Cliente() {
        super();
        this.saldoCredito = 0.0;
    }
    
    public Cliente(Long id, String nombre, String cedula) {
        super(id);
        this.nombre = nombre;
        this.cedula = cedula;
        this.saldoCredito = 0.0;
    }
    
    // Getters and Setters
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getCedula() {
        return cedula;
    }
    
    public void setCedula(String cedula) {
        this.cedula = cedula;
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
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public Double getSaldoCredito() {
        return saldoCredito;
    }
    
    public void setSaldoCredito(Double saldoCredito) {
        this.saldoCredito = saldoCredito;
    }
    
    @Override
    public String toString() {
        return nombre + " (Cédula: " + cedula + ")";
    }
}
