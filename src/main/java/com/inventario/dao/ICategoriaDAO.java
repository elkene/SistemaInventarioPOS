package com.inventario.dao;

import com.inventario.models.Categoria;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

public interface ICategoriaDAO extends IGenericDAO<Categoria> {
    
    Categoria findByNombre(String nombre) throws DataAccessException;
    
    List<Categoria> findAllActive() throws DataAccessException;
}
