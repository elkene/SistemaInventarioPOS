package com.inventario.models;

import java.math.BigDecimal;

/**
 * Modelo de Producto
 */
public class Producto extends Entidad {
    
    private static final long serialVersionUID = 1L;
    
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precioCompra;
    private BigDecimal precioVenta;
    private Integer stock;
    private Integer stockMinimo;
    private Long categoriaId;
    private String categoriaNombre;
    private boolean activo = true;

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public Producto() {
        super();
        this.stock = 0;
        this.stockMinimo = 5;
    }
    
    public Producto(Long id, String codigo, String nombre, BigDecimal precioVenta) {
        super(id);
        this.codigo = codigo;
        this.nombre = nombre;
        this.precioVenta = precioVenta;
        this.stock = 0;
        this.stockMinimo = 5;
    }
    
    // Getters and Setters
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
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
    
    public BigDecimal getPrecioCompra() {
        return precioCompra;
    }
    
    public void setPrecioCompra(BigDecimal precioCompra) {
        this.precioCompra = precioCompra;
    }
    
    public BigDecimal getPrecioVenta() {
        return precioVenta;
    }
    
    public void setPrecioVenta(BigDecimal precioVenta) {
        this.precioVenta = precioVenta;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Integer getStockMinimo() {
        return stockMinimo;
    }
    
    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }
    
    public Long getCategoriaId() {
        return categoriaId;
    }
    
    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
    
    public String getCategoriaNombre() {
        return categoriaNombre;
    }
    
    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }
    
    public boolean isStockBajo() {
        return stock != null && stock <= stockMinimo;
    }
    
    @Override
    public String toString() {
        return nombre + " (Cod: " + codigo + ")";
    }
}
