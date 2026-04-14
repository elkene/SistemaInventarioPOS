package com.inventario.dao;

import com.inventario.exceptions.DataAccessException;
import java.util.List;

/**
 * Interfaz genérica para las operaciones CRUD
 */
public interface IGenericDAO<T> {
    
    /**
     * Crea una nueva entidad en la base de datos
     */
    void create(T entity) throws DataAccessException;
    
    /**
     * Obtiene una entidad por su ID
     */
    T read(Long id) throws DataAccessException;
    
    /**
     * Obtiene todas las entidades
     */
    List<T> readAll() throws DataAccessException;
    
    /**
     * Actualiza una entidad existente
     */
    void update(T entity) throws DataAccessException;
    
    /**
     * Elimina una entidad por su ID
     */
    void delete(Long id) throws DataAccessException;
    
    /**
     * Cuenta el número total de entidades
     */
    long count() throws DataAccessException;
}
