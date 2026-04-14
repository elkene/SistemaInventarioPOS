package com.inventario.dao.impl;

import com.inventario.dao.IUsuarioDAO;
import com.inventario.models.Usuario;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación DAO para Usuario
 */
public class UsuarioDAOImpl extends GenericDAOImpl<Usuario> implements IUsuarioDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);
    
    public UsuarioDAOImpl() {
        super(Usuario.class);
    }
    
    @Override
    public void create(Usuario usuario) throws DataAccessException {
        String sql = "INSERT INTO usuarios (username, email, password_hash, rol, activo) " +
                     "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, usuario.getUsername());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getPasswordHash());
            pstmt.setString(4, usuario.getRol());
            pstmt.setBoolean(5, usuario.isActivo());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getLong(1));
                        logger.info("Usuario creado con ID: " + usuario.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando usuario", e);
            throw new DataAccessException("Error creando usuario", e);
        }
    }
    
    @Override
    public Usuario read(Long id) throws DataAccessException {
        String sql = "SELECT * FROM usuarios WHERE usuario_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo usuario", e);
            throw new DataAccessException("Error leyendo usuario", e);
        }
        
        return null;
    }
    
    @Override
    public List<Usuario> readAll() throws DataAccessException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY usuario_id";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todos los usuarios", e);
            throw new DataAccessException("Error leyendo todos los usuarios", e);
        }
        
        return usuarios;
    }
    
    @Override
    public void update(Usuario usuario) throws DataAccessException {
        String sql = "UPDATE usuarios SET username = ?, email = ?, password_hash = ?, " +
                     "rol = ?, activo = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                     "WHERE usuario_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, usuario.getUsername());
            pstmt.setString(2, usuario.getEmail());
            pstmt.setString(3, usuario.getPasswordHash());
            pstmt.setString(4, usuario.getRol());
            pstmt.setBoolean(5, usuario.isActivo());
            pstmt.setLong(6, usuario.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Usuario actualizado: " + usuario.getId());
            }
        } catch (SQLException e) {
            logger.error("Error actualizando usuario", e);
            throw new DataAccessException("Error actualizando usuario", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM usuarios WHERE usuario_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Usuario eliminado: " + id);
            }
        } catch (SQLException e) {
            logger.error("Error eliminando usuario", e);
            throw new DataAccessException("Error eliminando usuario", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM usuarios";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando usuarios", e);
            throw new DataAccessException("Error contando usuarios", e);
        }
        
        return 0;
    }
    
    @Override
    public Usuario findByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando usuario por username", e);
            throw new DataAccessException("Error buscando usuario por username", e);
        }
        
        return null;
    }
    
    @Override
    public Usuario findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM usuarios WHERE email = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando usuario por email", e);
            throw new DataAccessException("Error buscando usuario por email", e);
        }
        
        return null;
    }
    
    @Override
    public List<Usuario> findActivos() throws DataAccessException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE activo = true ORDER BY username";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        } catch (SQLException e) {
            logger.error("Error buscando usuarios activos", e);
            throw new DataAccessException("Error buscando usuarios activos", e);
        }
        
        return usuarios;
    }
    
    @Override
    public void updateUltimoLogin(Long usuarioId) throws DataAccessException {
        String sql = "UPDATE usuarios SET ultimo_login = CURRENT_TIMESTAMP WHERE usuario_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, usuarioId);
            pstmt.executeUpdate();
            
            logger.info("Último login actualizado para usuario: " + usuarioId);
        } catch (SQLException e) {
            logger.error("Error actualizando último login", e);
            throw new DataAccessException("Error actualizando último login", e);
        }
    }
    
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("usuario_id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setRol(rs.getString("rol"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            usuario.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        return usuario;
    }
}
