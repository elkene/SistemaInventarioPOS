package com.inventario.dao;

import com.inventario.models.Venta;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

public interface IVentaDAO extends IGenericDAO<Venta> {
    
    List<Venta> findByFecha(String fecha) throws DataAccessException;
    
    List<Venta> findByRangoFechas(String fechaDesde, String fechaHasta) throws DataAccessException;
    
    List<Venta> findByUsuario(Long usuarioId) throws DataAccessException;
    
    List<Venta> findByCliente(Long clienteId) throws DataAccessException;
    
    List<Venta> findRecientes(int limit) throws DataAccessException;
    
    List<Venta> findByEstado(String estado) throws DataAccessException;
    
    long countByFecha(String fecha) throws DataAccessException;
    
    long countHoy() throws DataAccessException;
}
