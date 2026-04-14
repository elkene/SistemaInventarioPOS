package com.inventario.dao;

import com.inventario.models.DetalleVenta;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

public interface IDetalleVentaDAO extends IGenericDAO<DetalleVenta> {
    
    List<DetalleVenta> findByVenta(Long ventaId) throws DataAccessException;
    
    void deleteByVenta(Long ventaId) throws DataAccessException;
}
