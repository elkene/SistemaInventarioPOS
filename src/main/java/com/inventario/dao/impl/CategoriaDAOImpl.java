package com.inventario.dao.impl;

import com.inventario.dao.ICategoriaDAO;
import com.inventario.models.Categoria;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAOImpl extends GenericDAOImpl<Categoria> implements ICategoriaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoriaDAOImpl.class);
    
    public CategoriaDAOImpl() {
        super(Categoria.class);
    }
    
    @Override
    public void create(Categoria categoria) throws DataAccessException {
        String sql = "INSERT INTO categorias (nombre, descripcion) VALUES (?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescripcion());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        categoria.setId(generatedKeys.getLong(1));
                        logger.info("Categoría creada con ID: " + categoria.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando categoría", e);
            throw new DataAccessException("Error creando categoría", e);
        }
    }
    
    @Override
    public Categoria read(Long id) throws DataAccessException {
        String sql = "SELECT * FROM categorias WHERE categoria_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategoria(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo categoría", e);
            throw new DataAccessException("Error leyendo categoría", e);
        }
        
        return null;
    }
    
    @Override
    public List<Categoria> readAll() throws DataAccessException {
        List<Categoria> categorias = new ArrayList<>();
        String sql = "SELECT * FROM categorias ORDER BY nombre";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                categorias.add(mapResultSetToCategoria(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todas las categorías", e);
            throw new DataAccessException("Error leyendo todas las categorías", e);
        }
        
        return categorias;
    }
    
    @Override
    public void update(Categoria categoria) throws DataAccessException {
        String sql = "UPDATE categorias SET nombre = ?, descripcion = ?, " +
                     "fecha_modificacion = CURRENT_TIMESTAMP WHERE categoria_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categoria.getNombre());
            pstmt.setString(2, categoria.getDescripcion());
            pstmt.setLong(3, categoria.getId());
            
            pstmt.executeUpdate();
            logger.info("Categoría actualizada: " + categoria.getId());
        } catch (SQLException e) {
            logger.error("Error actualizando categoría", e);
            throw new DataAccessException("Error actualizando categoría", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM categorias WHERE categoria_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Categoría eliminada: " + id);
        } catch (SQLException e) {
            logger.error("Error eliminando categoría", e);
            throw new DataAccessException("Error eliminando categoría", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM categorias";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando categorías", e);
            throw new DataAccessException("Error contando categorías", e);
        }
        
        return 0;
    }
    
    @Override
    public Categoria findByNombre(String nombre) throws DataAccessException {
        String sql = "SELECT * FROM categorias WHERE LOWER(nombre) = LOWER(?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombre);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategoria(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando categoría por nombre", e);
            throw new DataAccessException("Error buscando categoría por nombre", e);
        }
        
        return null;
    }
    
    @Override
    public List<Categoria> findAllActive() throws DataAccessException {
        return readAll();
    }
    
    private Categoria mapResultSetToCategoria(ResultSet rs) throws SQLException {
        Categoria categoria = new Categoria();
        categoria.setId(rs.getLong("categoria_id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            categoria.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            categoria.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        return categoria;
    }
}
