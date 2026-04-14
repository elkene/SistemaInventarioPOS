package com.inventario.dao;

import com.inventario.models.Usuario;
import com.inventario.exceptions.DataAccessException;
import java.util.List;

/**
 * Interfaz DAO específica para Usuario
 */
public interface IUsuarioDAO extends IGenericDAO<Usuario> {
    
    /**
     * Obtiene un usuario por su nombre de usuario
     */
    Usuario findByUsername(String username) throws DataAccessException;
    
    /**
     * Obtiene un usuario por su email
     */
    Usuario findByEmail(String email) throws DataAccessException;
    
    /**
     * Obtiene todos los usuarios activos
     */
    List<Usuario> findActivos() throws DataAccessException;
    
    /**
     * Actualiza el último login de un usuario
     */
    void updateUltimoLogin(Long usuarioId) throws DataAccessException;
}
