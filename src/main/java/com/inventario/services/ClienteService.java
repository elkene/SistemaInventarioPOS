package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IClienteDAO;
import com.inventario.models.Cliente;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ClienteService {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    private final IClienteDAO clienteDAO;
    
    public ClienteService() {
        this.clienteDAO = DAOFactory.getClienteDAO();
    }
    
    public void crearCliente(Cliente cliente) throws ApplicationException {
        try {
            validarCliente(cliente);
            
            clienteDAO.create(cliente);
            logger.info("Cliente creado: " + cliente.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error creando cliente", e);
            throw new ApplicationException("Error creando cliente", e);
        }
    }
    
    public void actualizarCliente(Cliente cliente) throws ApplicationException {
        try {
            validarCliente(cliente);
            clienteDAO.update(cliente);
            logger.info("Cliente actualizado: " + cliente.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error actualizando cliente", e);
            throw new ApplicationException("Error actualizando cliente", e);
        }
    }
    
    public Cliente obtenerCliente(Long id) throws ApplicationException {
        try {
            return clienteDAO.read(id);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo cliente", e);
            throw new ApplicationException("Error obteniendo cliente", e);
        }
    }
    
    public List<Cliente> obtenerTodos() throws ApplicationException {
        try {
            return clienteDAO.readAll();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo clientes", e);
            throw new ApplicationException("Error obteniendo clientes", e);
        }
    }
    
    public List<Cliente> buscarPorNombre(String nombre) throws ApplicationException {
        try {
            return clienteDAO.findByNombre(nombre);
        } catch (DataAccessException e) {
            logger.error("Error buscando clientes", e);
            throw new ApplicationException("Error buscando clientes", e);
        }
    }
    
    public Cliente buscarPorCedula(String cedula) throws ApplicationException {
        try {
            return clienteDAO.findByCedula(cedula);
        } catch (DataAccessException e) {
            logger.error("Error buscando cliente por RFC", e);
            throw new ApplicationException("Error buscando cliente por RFC", e);
        }
    }
    
    public List<Cliente> buscarConCredito() throws ApplicationException {
        try {
            return clienteDAO.findConCredito();
        } catch (DataAccessException e) {
            logger.error("Error buscando clientes con crédito", e);
            throw new ApplicationException("Error buscando clientes con crédito", e);
        }
    }
    
    public void eliminarCliente(Long id) throws ApplicationException {
        try {
            clienteDAO.delete(id);
            logger.info("Cliente eliminado: " + id);
            
        } catch (DataAccessException e) {
            logger.error("Error eliminando cliente", e);
            throw new ApplicationException("Error eliminando cliente", e);
        }
    }
    
    private void validarCliente(Cliente cliente) throws ValidationException {
        if (cliente.getNombre() == null || cliente.getNombre().isEmpty()) {
            throw new ValidationException("El nombre es requerido");
        }
        
        if (cliente.getCedula() == null || cliente.getCedula().isEmpty()) {
            throw new ValidationException("El RFC es requerido");
        }
    }
}
