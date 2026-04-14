package com.inventario.dao;

import com.inventario.models.Producto;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

/**
 * Interfaz DAO específica para Producto
 */
public interface IProductoDAO extends IGenericDAO<Producto> {
    
    /**
     * Obtiene un producto por su código
     */
    Producto findByCodigo(String codigo) throws DataAccessException;
    
    /**
     * Obtiene productos por categoría
     */
    List<Producto> findByCategoria(Long categoriaId) throws DataAccessException;
    
    /**
     * Obtiene productos con stock bajo
     */
    List<Producto> findStockBajo() throws DataAccessException;
    
    /**
     * Obtiene productos por nombre (búsqueda parcial)
     */
    List<Producto> findByNombre(String nombre) throws DataAccessException;
    
    /**
     * Actualiza el stock de un producto
     */
    void updateStock(Long productoId, Integer cantidad) throws DataAccessException;
}
