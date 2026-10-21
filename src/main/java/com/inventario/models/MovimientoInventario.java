package com.inventario.models;

import java.time.LocalDateTime;

public class MovimientoInventario extends Entidad {
    
    private static final long serialVersionUID = 1L;
    
    public enum TipoMovimiento {
        ENTRADA,
        SALIDA,
        AJUSTE
    }
    
    private Long productoId;
    private String productoCodigo;
    private String productoNombre;
    private TipoMovimiento tipo;
    private Integer cantidad;
    private Long referenciaId;
    private Long proveedorId;
    private String descripcion;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaCreacion;
    
    public MovimientoInventario() {
        super();
    }
    
    public MovimientoInventario(Long productoId, TipoMovimiento tipo, Integer cantidad, String descripcion) {
        super();
        this.productoId = productoId;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.descripcion = descripcion;
    }
    
    public Long getProductoId() {
        return productoId;
    }
    
    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }
    
    public String getProductoCodigo() {
        return productoCodigo;
    }
    
    public void setProductoCodigo(String productoCodigo) {
        this.productoCodigo = productoCodigo;
    }
    
    public String getProductoNombre() {
        return productoNombre;
    }
    
    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }
    
    public TipoMovimiento getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }
    
    public Long getProveedorId() {
        return proveedorId;
    }
    
    public void setProveedorId(Long proveedorId) {
        this.proveedorId = proveedorId;
    }
    
    public Long getReferenciaId() {
        return referenciaId;
    }
    
    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getUsuarioNombre() {
        return usuarioNombre;
    }
    
    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public boolean isEntrada() {
        return tipo == TipoMovimiento.ENTRADA;
    }
    
    public boolean isSalida() {
        return tipo == TipoMovimiento.SALIDA;
    }
}
