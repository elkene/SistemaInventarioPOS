package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.ICategoriaDAO;
import com.inventario.models.Categoria;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CategoriaService {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoriaService.class);
    private final ICategoriaDAO categoriaDAO;
    
    public CategoriaService() {
        this.categoriaDAO = DAOFactory.getCategoriaDAO();
    }
    
    public void crearCategoria(Categoria categoria) throws ApplicationException {
        try {
            validarCategoria(categoria);
            
            Categoria existente = categoriaDAO.findByNombre(categoria.getNombre());
            if (existente != null) {
                throw new ValidationException("Ya existe una categoría con este nombre");
            }
            
            categoriaDAO.create(categoria);
            logger.info("Categoría creada: " + categoria.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error creando categoría", e);
            throw new ApplicationException("Error creando categoría", e);
        }
    }
    
    public void actualizarCategoria(Categoria categoria) throws ApplicationException {
        try {
            validarCategoria(categoria);
            categoriaDAO.update(categoria);
            logger.info("Categoría actualizada: " + categoria.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error actualizando categoría", e);
            throw new ApplicationException("Error actualizando categoría", e);
        }
    }
    
    public Categoria obtenerCategoria(Long id) throws ApplicationException {
        try {
            return categoriaDAO.read(id);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo categoría", e);
            throw new ApplicationException("Error obteniendo categoría", e);
        }
    }
    
    public List<Categoria> obtenerTodas() throws ApplicationException {
        try {
            return categoriaDAO.readAll();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo categorías", e);
            throw new ApplicationException("Error obteniendo categorías", e);
        }
    }
    
    public void eliminarCategoria(Long id) throws ApplicationException {
        try {
            categoriaDAO.delete(id);
            logger.info("Categoría eliminada: " + id);
            
        } catch (DataAccessException e) {
            logger.error("Error eliminando categoría", e);
            throw new ApplicationException("Error eliminando categoría", e);
        }
    }
    
    private void validarCategoria(Categoria categoria) throws ValidationException {
        if (categoria.getNombre() == null || categoria.getNombre().isEmpty()) {
            throw new ValidationException("El nombre de la categoría es requerido");
        }
        
        if (categoria.getNombre().length() > 100) {
            throw new ValidationException("El nombre no puede exceder 100 caracteres");
        }
    }
}
