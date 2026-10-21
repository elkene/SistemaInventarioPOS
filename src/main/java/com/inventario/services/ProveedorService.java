package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IProveedorDAO;
import com.inventario.models.Proveedor;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProveedorService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProveedorService.class);
    private final IProveedorDAO proveedorDAO;
    
    public ProveedorService() {
        this.proveedorDAO = DAOFactory.getProveedorDAO();
    }
    
    public void crearProveedor(Proveedor proveedor) throws ApplicationException {
        try {
            validarProveedor(proveedor);
            
            proveedorDAO.create(proveedor);
            logger.info("Proveedor creado: " + proveedor.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error creando proveedor", e);
            throw new ApplicationException("Error creando proveedor", e);
        }
    }
    
    public void actualizarProveedor(Proveedor proveedor) throws ApplicationException {
        try {
            validarProveedor(proveedor);
            proveedorDAO.update(proveedor);
            logger.info("Proveedor actualizado: " + proveedor.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error actualizando proveedor", e);
            throw new ApplicationException("Error actualizando proveedor", e);
        }
    }
    
    public Proveedor obtenerProveedor(Long id) throws ApplicationException {
        try {
            return proveedorDAO.read(id);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo proveedor", e);
            throw new ApplicationException("Error obteniendo proveedor", e);
        }
    }
    
    public List<Proveedor> obtenerTodos() throws ApplicationException {
        try {
            return proveedorDAO.readAll();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo proveedores", e);
            throw new ApplicationException("Error obteniendo proveedores", e);
        }
    }
    
    public List<Proveedor> buscarPorNombre(String nombre) throws ApplicationException {
        try {
            return proveedorDAO.findByNombre(nombre);
        } catch (DataAccessException e) {
            logger.error("Error buscando proveedores", e);
            throw new ApplicationException("Error buscando proveedores", e);
        }
    }
    
    public Proveedor buscarPorRIF(String rif) throws ApplicationException {
        try {
            return proveedorDAO.findByRIF(rif);
        } catch (DataAccessException e) {
            logger.error("Error buscando proveedor por RFC", e);
            throw new ApplicationException("Error buscando proveedor por RFC", e);
        }
    }
    
    public List<Proveedor> buscarPorCiudad(String ciudad) throws ApplicationException {
        try {
            return proveedorDAO.findByCiudad(ciudad);
        } catch (DataAccessException e) {
            logger.error("Error buscando proveedores por ciudad", e);
            throw new ApplicationException("Error buscando proveedores por ciudad", e);
        }
    }
    
    public void eliminarProveedor(Long id) throws ApplicationException {
        try {
            proveedorDAO.delete(id);
            logger.info("Proveedor eliminado: " + id);
            
        } catch (DataAccessException e) {
            logger.error("Error eliminando proveedor", e);
            throw new ApplicationException("Error eliminando proveedor", e);
        }
    }
    
    private void validarProveedor(Proveedor proveedor) throws ValidationException {
        if (proveedor.getNombre() == null || proveedor.getNombre().isEmpty()) {
            throw new ValidationException("El nombre es requerido");
        }
        
        if (proveedor.getRif() == null || proveedor.getRif().isEmpty()) {
            throw new ValidationException("El RFC es requerido");
        }
    }
}
