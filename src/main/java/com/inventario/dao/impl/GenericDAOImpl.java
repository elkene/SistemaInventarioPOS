package com.inventario.dao.impl;

import com.inventario.database.ConnectionFactory;
import com.inventario.dao.IGenericDAO;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación genérica base para DAOs
 * Esta clase proporciona una base para todos los DAOs específicos
 */
public abstract class GenericDAOImpl<T> implements IGenericDAO<T> {
    
    protected static final Logger logger = LoggerFactory.getLogger(GenericDAOImpl.class);
    protected Class<T> entityClass;
    
    public GenericDAOImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * Obtiene una conexión de la pool
     */
    protected Connection getConnection() throws DataAccessException {
        try {
            return ConnectionFactory.getConnection();
        } catch (SQLException e) {
            logger.error("Error obteniendo conexión a la base de datos", e);
            throw new DataAccessException("Error obteniendo conexión a la base de datos", e);
        }
    }
    
    /**
     * Cierra una conexión
     */
    protected void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error cerrando conexión", e);
            }
        }
    }
    
    @Override
    public abstract void create(T entity) throws DataAccessException;
    
    @Override
    public abstract T read(Long id) throws DataAccessException;
    
    @Override
    public abstract List<T> readAll() throws DataAccessException;
    
    @Override
    public abstract void update(T entity) throws DataAccessException;
    
    @Override
    public abstract void delete(Long id) throws DataAccessException;
    
    @Override
    public abstract long count() throws DataAccessException;
}
