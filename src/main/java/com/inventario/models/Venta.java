package com.inventario.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo de Venta
 */
public class Venta extends Entidad {
    
    private static final long serialVersionUID = 1L;
    
    private Long usuarioId;
    private Long clienteId;
    private String clienteNombre;
    private String usuarioNombre;
    private BigDecimal subtotal;
    private BigDecimal descuento;
    private BigDecimal total;
    private String metodoPago;
    private String observaciones;
    private String estado;
    
    public Venta() {
        super();
        this.total = BigDecimal.ZERO;
        this.estado = "COMPLETADA";
    }
    
    public Venta(Long id, Long usuarioId, Long clienteId, BigDecimal total) {
        super(id);
        this.usuarioId = usuarioId;
        this.clienteId = clienteId;
        this.total = total;
        this.estado = "COMPLETADA";
    }
    
    // Getters and Setters
    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public String getClienteNombre() {
        return clienteNombre;
    }
    
    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
    
    public BigDecimal getTotal() {
        return total;
    }
    
    public void setTotal(BigDecimal total) {
        this.total = total;
    }
    
    public String getMetodoPago() {
        return metodoPago;
    }
    
    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    @Override
    public String toString() {
        return "Venta{" +
                "id=" + id +
                ", fecha=" + fechaCreacion +
                ", total=" + total +
                ", estado='" + estado + '\'' +
                '}';
    }
}
