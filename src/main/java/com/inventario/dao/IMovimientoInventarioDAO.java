package com.inventario.dao;

import com.inventario.models.MovimientoInventario;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

public interface IMovimientoInventarioDAO extends IGenericDAO<MovimientoInventario> {
    
    List<MovimientoInventario> findByProducto(Long productoId) throws DataAccessException;
    
    List<MovimientoInventario> findByTipo(String tipo) throws DataAccessException;
    
    List<MovimientoInventario> findByFechaRange(java.time.LocalDateTime desde, java.time.LocalDateTime hasta) throws DataAccessException;
    
    List<MovimientoInventario> findRecent(int limit) throws DataAccessException;
}
