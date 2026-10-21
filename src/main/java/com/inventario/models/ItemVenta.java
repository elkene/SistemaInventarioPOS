package com.inventario.models;

import java.math.BigDecimal;

public class ItemVenta {
    
    private Long productoId;
    private String productoCodigo;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
    private Integer stockDisponible;
    
    public ItemVenta() {
        this.cantidad = 1;
    }
    
    public ItemVenta(Producto producto) {
        this.productoId = producto.getId();
        this.productoCodigo = producto.getCodigo();
        this.productoNombre = producto.getNombre();
        this.cantidad = 1;
        this.precioUnitario = producto.getPrecioVenta();
        this.stockDisponible = producto.getStock();
        this.subtotal = this.precioUnitario.multiply(BigDecimal.valueOf(this.cantidad));
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
    
    public Integer getCantidad() {
        return cantidad;
    }
    
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
        actualizarSubtotal();
    }
    
    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }
    
    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
        actualizarSubtotal();
    }
    
    public BigDecimal getSubtotal() {
        return subtotal;
    }
    
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
    
    private void actualizarSubtotal() {
        if (precioUnitario != null && cantidad != null) {
            this.subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
        }
    }
    
    public Integer getStockDisponible() {
        return stockDisponible;
    }

    /**
     * Suma una unidad si hay stock. Devuelve false si ya no hay más unidades disponibles.
     */
    public boolean incrementarCantidad() {
        if (stockDisponible != null && cantidad >= stockDisponible) {
            return false;
        }
        this.cantidad++;
        actualizarSubtotal();
        return true;
    }
    
    public void decrementarCantidad() {
        if (this.cantidad > 1) {
            this.cantidad--;
            actualizarSubtotal();
        }
    }
    
    public DetalleVenta toDetalleVenta() {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId(this.productoId);
        detalle.setCantidad(this.cantidad);
        detalle.setPrecioUnitario(this.precioUnitario);
        detalle.setSubtotal(this.subtotal);
        detalle.setProductoNombre(this.productoNombre);
        return detalle;
    }
}
