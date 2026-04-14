package com.inventario.dao;

import com.inventario.models.Cliente;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

/**
 * Interfaz DAO específica para Cliente
 */
public interface IClienteDAO extends IGenericDAO<Cliente> {
    
    /**
     * Obtiene un cliente por su cédula
     */
    Cliente findByCedula(String cedula) throws DataAccessException;
    
    /**
     * Obtiene clientes por nombre (búsqueda parcial)
     */
    List<Cliente> findByNombre(String nombre) throws DataAccessException;
    
    /**
     * Obtiene clientes por ciudad
     */
    List<Cliente> findByCiudad(String ciudad) throws DataAccessException;
    
    /**
     * Obtiene clientes con saldo de crédito
     */
    List<Cliente> findConCredito() throws DataAccessException;
}
