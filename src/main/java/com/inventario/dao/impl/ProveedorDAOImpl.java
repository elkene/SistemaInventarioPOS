package com.inventario.dao.impl;

import com.inventario.dao.IProveedorDAO;
import com.inventario.models.Proveedor;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAOImpl extends GenericDAOImpl<Proveedor> implements IProveedorDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ProveedorDAOImpl.class);
    
    public ProveedorDAOImpl() {
        super(Proveedor.class);
    }
    
    @Override
    public void create(Proveedor proveedor) throws DataAccessException {
        String sql = "INSERT INTO proveedores (nombre, rif, email, telefono, direccion, ciudad, condiciones_pago) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, proveedor.getNombre());
            pstmt.setString(2, proveedor.getRif());
            pstmt.setString(3, proveedor.getEmail());
            pstmt.setString(4, proveedor.getTelefono());
            pstmt.setString(5, proveedor.getDireccion());
            pstmt.setString(6, proveedor.getCiudad());
            pstmt.setString(7, proveedor.getCondicionesPago());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        proveedor.setId(generatedKeys.getLong(1));
                        logger.info("Proveedor creado con ID: " + proveedor.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando proveedor", e);
            throw new DataAccessException("Error creando proveedor", e);
        }
    }
    
    @Override
    public Proveedor read(Long id) throws DataAccessException {
        String sql = "SELECT * FROM proveedores WHERE proveedor_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProveedor(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo proveedor", e);
            throw new DataAccessException("Error leyendo proveedor", e);
        }
        
        return null;
    }
    
    @Override
    public List<Proveedor> readAll() throws DataAccessException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedores ORDER BY nombre";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                proveedores.add(mapResultSetToProveedor(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todos los proveedores", e);
            throw new DataAccessException("Error leyendo todos los proveedores", e);
        }
        
        return proveedores;
    }
    
    @Override
    public void update(Proveedor proveedor) throws DataAccessException {
        String sql = "UPDATE proveedores SET nombre = ?, rif = ?, email = ?, telefono = ?, " +
                     "direccion = ?, ciudad = ?, condiciones_pago = ?, " +
                     "fecha_modificacion = CURRENT_TIMESTAMP WHERE proveedor_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, proveedor.getNombre());
            pstmt.setString(2, proveedor.getRif());
            pstmt.setString(3, proveedor.getEmail());
            pstmt.setString(4, proveedor.getTelefono());
            pstmt.setString(5, proveedor.getDireccion());
            pstmt.setString(6, proveedor.getCiudad());
            pstmt.setString(7, proveedor.getCondicionesPago());
            pstmt.setLong(8, proveedor.getId());
            
            pstmt.executeUpdate();
            logger.info("Proveedor actualizado: " + proveedor.getId());
        } catch (SQLException e) {
            logger.error("Error actualizando proveedor", e);
            throw new DataAccessException("Error actualizando proveedor", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM proveedores WHERE proveedor_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Proveedor eliminado: " + id);
        } catch (SQLException e) {
            logger.error("Error eliminando proveedor", e);
            throw new DataAccessException("Error eliminando proveedor", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM proveedores";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando proveedores", e);
            throw new DataAccessException("Error contando proveedores", e);
        }
        
        return 0;
    }
    
    @Override
    public Proveedor findByRIF(String rif) throws DataAccessException {
        String sql = "SELECT * FROM proveedores WHERE rif = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rif);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProveedor(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando proveedor por RIF", e);
            throw new DataAccessException("Error buscando proveedor por RIF", e);
        }
        
        return null;
    }
    
    @Override
    public List<Proveedor> findByNombre(String nombre) throws DataAccessException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedores WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nombre + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(mapResultSetToProveedor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando proveedores por nombre", e);
            throw new DataAccessException("Error buscando proveedores por nombre", e);
        }
        
        return proveedores;
    }
    
    @Override
    public List<Proveedor> findByCiudad(String ciudad) throws DataAccessException {
        List<Proveedor> proveedores = new ArrayList<>();
        String sql = "SELECT * FROM proveedores WHERE ciudad = ? ORDER BY nombre";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, ciudad);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    proveedores.add(mapResultSetToProveedor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando proveedores por ciudad", e);
            throw new DataAccessException("Error buscando proveedores por ciudad", e);
        }
        
        return proveedores;
    }
    
    private Proveedor mapResultSetToProveedor(ResultSet rs) throws SQLException {
        Proveedor proveedor = new Proveedor();
        proveedor.setId(rs.getLong("proveedor_id"));
        proveedor.setNombre(rs.getString("nombre"));
        proveedor.setRif(rs.getString("rif"));
        proveedor.setEmail(rs.getString("email"));
        proveedor.setTelefono(rs.getString("telefono"));
        proveedor.setDireccion(rs.getString("direccion"));
        proveedor.setCiudad(rs.getString("ciudad"));
        proveedor.setCondicionesPago(rs.getString("condiciones_pago"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            proveedor.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return proveedor;
    }
}
