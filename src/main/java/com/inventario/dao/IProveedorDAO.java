package com.inventario.dao;

import com.inventario.models.Proveedor;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

/**
 * Interfaz DAO específica para Proveedor
 */
public interface IProveedorDAO extends IGenericDAO<Proveedor> {
    
    /**
     * Obtiene un proveedor por su RIF
     */
    Proveedor findByRIF(String rif) throws DataAccessException;
    
    /**
     * Obtiene proveedores por nombre (búsqueda parcial)
     */
    List<Proveedor> findByNombre(String nombre) throws DataAccessException;
    
    /**
     * Obtiene proveedores por ciudad
     */
    List<Proveedor> findByCiudad(String ciudad) throws DataAccessException;
}
