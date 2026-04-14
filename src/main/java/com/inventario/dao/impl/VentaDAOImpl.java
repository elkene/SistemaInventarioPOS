package com.inventario.dao.impl;

import com.inventario.dao.IVentaDAO;
import com.inventario.models.Venta;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VentaDAOImpl extends GenericDAOImpl<Venta> implements IVentaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(VentaDAOImpl.class);
    
    public VentaDAOImpl() {
        super(Venta.class);
    }
    
    @Override
    public void create(Venta venta) throws DataAccessException {
        String sql = "INSERT INTO ventas (usuario_id, cliente_id, total, metodo_pago, estado, observaciones) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setObject(1, venta.getUsuarioId());
            pstmt.setObject(2, venta.getClienteId());
            pstmt.setBigDecimal(3, venta.getTotal());
            pstmt.setString(4, venta.getMetodoPago());
            pstmt.setString(5, venta.getEstado());
            pstmt.setString(6, venta.getObservaciones());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        venta.setId(generatedKeys.getLong(1));
                        logger.info("Venta creada con ID: " + venta.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando venta", e);
            throw new DataAccessException("Error creando venta", e);
        }
    }
    
    @Override
    public Venta read(Long id) throws DataAccessException {
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "WHERE v.venta_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToVenta(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo venta", e);
            throw new DataAccessException("Error leyendo venta", e);
        }
        
        return null;
    }
    
    @Override
    public List<Venta> readAll() throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "ORDER BY v.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ventas.add(mapResultSetToVenta(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todas las ventas", e);
            throw new DataAccessException("Error leyendo todas las ventas", e);
        }
        
        return ventas;
    }
    
    @Override
    public void update(Venta venta) throws DataAccessException {
        String sql = "UPDATE ventas SET cliente_id = ?, total = ?, metodo_pago = ?, " +
                     "estado = ?, observaciones = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                     "WHERE venta_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setObject(1, venta.getClienteId());
            pstmt.setBigDecimal(2, venta.getTotal());
            pstmt.setString(3, venta.getMetodoPago());
            pstmt.setString(4, venta.getEstado());
            pstmt.setString(5, venta.getObservaciones());
            pstmt.setLong(6, venta.getId());
            
            pstmt.executeUpdate();
            logger.info("Venta actualizada: " + venta.getId());
        } catch (SQLException e) {
            logger.error("Error actualizando venta", e);
            throw new DataAccessException("Error actualizando venta", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM ventas WHERE venta_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Venta eliminada: " + id);
        } catch (SQLException e) {
            logger.error("Error eliminando venta", e);
            throw new DataAccessException("Error eliminando venta", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM ventas";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando ventas", e);
            throw new DataAccessException("Error contando ventas", e);
        }
        
        return 0;
    }
    
    @Override
    public List<Venta> findByFecha(String fecha) throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "WHERE DATE(v.fecha_creacion) = ? " +
                     "ORDER BY v.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(fecha));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapResultSetToVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando ventas por fecha", e);
            throw new DataAccessException("Error buscando ventas por fecha", e);
        }
        
        return ventas;
    }
    
    @Override
    public List<Venta> findByRangoFechas(String fechaDesde, String fechaHasta) throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "WHERE DATE(v.fecha_creacion) >= ? AND DATE(v.fecha_creacion) <= ? " +
                     "ORDER BY v.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(fechaDesde));
            pstmt.setDate(2, Date.valueOf(fechaHasta));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapResultSetToVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando ventas por rango de fechas", e);
            throw new DataAccessException("Error buscando ventas por rango de fechas", e);
        }
        
        return ventas;
    }
    
    @Override
    public List<Venta> findByUsuario(Long usuarioId) throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "WHERE v.usuario_id = ? ORDER BY v.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, usuarioId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapResultSetToVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando ventas por usuario", e);
            throw new DataAccessException("Error buscando ventas por usuario", e);
        }
        
        return ventas;
    }
    
    @Override
    public List<Venta> findByCliente(Long clienteId) throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "WHERE v.cliente_id = ? ORDER BY v.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, clienteId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapResultSetToVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando ventas por cliente", e);
            throw new DataAccessException("Error buscando ventas por cliente", e);
        }
        
        return ventas;
    }
    
    @Override
    public List<Venta> findRecientes(int limit) throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "ORDER BY v.fecha_creacion DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapResultSetToVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando ventas recientes", e);
            throw new DataAccessException("Error buscando ventas recientes", e);
        }
        
        return ventas;
    }
    
    @Override
    public List<Venta> findByEstado(String estado) throws DataAccessException {
        List<Venta> ventas = new ArrayList<>();
        String sql = "SELECT v.*, c.nombre as cliente_nombre, u.username as usuario_nombre " +
                     "FROM ventas v " +
                     "LEFT JOIN clientes c ON v.cliente_id = c.cliente_id " +
                     "LEFT JOIN usuarios u ON v.usuario_id = u.usuario_id " +
                     "WHERE v.estado = ? ORDER BY v.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, estado);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ventas.add(mapResultSetToVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando ventas por estado", e);
            throw new DataAccessException("Error buscando ventas por estado", e);
        }
        
        return ventas;
    }
    
    @Override
    public long countByFecha(String fecha) throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM ventas WHERE DATE(fecha_creacion) = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, Date.valueOf(fecha));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error contando ventas por fecha", e);
            throw new DataAccessException("Error contando ventas por fecha", e);
        }
        
        return 0;
    }
    
    @Override
    public long countHoy() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM ventas WHERE DATE(fecha_creacion) = CURRENT_DATE";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando ventas de hoy", e);
            throw new DataAccessException("Error contando ventas de hoy", e);
        }
        
        return 0;
    }
    
    private Venta mapResultSetToVenta(ResultSet rs) throws SQLException {
        Venta venta = new Venta();
        venta.setId(rs.getLong("venta_id"));
        venta.setUsuarioId(rs.getLong("usuario_id"));
        
        Long clienteId = rs.getLong("cliente_id");
        if (!rs.wasNull()) {
            venta.setClienteId(clienteId);
        }
        
        venta.setTotal(rs.getBigDecimal("total"));
        venta.setMetodoPago(rs.getString("metodo_pago"));
        venta.setEstado(rs.getString("estado"));
        venta.setObservaciones(rs.getString("observaciones"));
        
        String clienteNombre = rs.getString("cliente_nombre");
        if (clienteNombre != null) {
            venta.setClienteNombre(clienteNombre);
        }
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            venta.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return venta;
    }
}
