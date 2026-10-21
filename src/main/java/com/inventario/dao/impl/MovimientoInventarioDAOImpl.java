package com.inventario.dao.impl;

import com.inventario.dao.IMovimientoInventarioDAO;
import com.inventario.models.MovimientoInventario;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoInventarioDAOImpl extends GenericDAOImpl<MovimientoInventario> implements IMovimientoInventarioDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(MovimientoInventarioDAOImpl.class);
    
    public MovimientoInventarioDAOImpl() {
        super(MovimientoInventario.class);
    }
    
    @Override
    public void create(MovimientoInventario movimiento) throws DataAccessException {
        String sql = "INSERT INTO movimientos_inventario (producto_id, tipo, cantidad, referencia_id, descripcion, usuario_id, proveedor_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setLong(1, movimiento.getProductoId());
            pstmt.setString(2, movimiento.getTipo().name());
            pstmt.setInt(3, movimiento.getCantidad());
            
            if (movimiento.getReferenciaId() != null) {
                pstmt.setLong(4, movimiento.getReferenciaId());
            } else {
                pstmt.setNull(4, Types.BIGINT);
            }
            
            pstmt.setString(5, movimiento.getDescripcion());
            
            if (movimiento.getUsuarioId() != null) {
                pstmt.setLong(6, movimiento.getUsuarioId());
            } else {
                pstmt.setNull(6, Types.BIGINT);
            }

            pstmt.setObject(7, movimiento.getProveedorId(), Types.BIGINT);

            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        movimiento.setId(generatedKeys.getLong(1));
                        logger.info("Movimiento de inventario creado con ID: " + movimiento.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando movimiento de inventario", e);
            throw new DataAccessException("Error creando movimiento de inventario", e);
        }
    }
    
    @Override
    public MovimientoInventario read(Long id) throws DataAccessException {
        String sql = "SELECT m.*, p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                     "u.username as usuario_nombre " +
                     "FROM movimientos_inventario m " +
                     "LEFT JOIN productos p ON m.producto_id = p.producto_id " +
                     "LEFT JOIN usuarios u ON m.usuario_id = u.usuario_id " +
                     "WHERE m.movimiento_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMovimiento(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo movimiento de inventario", e);
            throw new DataAccessException("Error leyendo movimiento de inventario", e);
        }
        
        return null;
    }
    
    @Override
    public List<MovimientoInventario> readAll() throws DataAccessException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT m.*, p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                     "u.username as usuario_nombre " +
                     "FROM movimientos_inventario m " +
                     "LEFT JOIN productos p ON m.producto_id = p.producto_id " +
                     "LEFT JOIN usuarios u ON m.usuario_id = u.usuario_id " +
                     "ORDER BY m.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                movimientos.add(mapResultSetToMovimiento(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todos los movimientos de inventario", e);
            throw new DataAccessException("Error leyendo todos los movimientos de inventario", e);
        }
        
        return movimientos;
    }
    
    @Override
    public void update(MovimientoInventario movimiento) throws DataAccessException {
        throw new UnsupportedOperationException("No se permite actualizar movimientos de inventario");
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        throw new UnsupportedOperationException("No se permite eliminar movimientos de inventario");
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM movimientos_inventario";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando movimientos de inventario", e);
            throw new DataAccessException("Error contando movimientos de inventario", e);
        }
        
        return 0;
    }
    
    @Override
    public List<MovimientoInventario> findByProducto(Long productoId) throws DataAccessException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT m.*, p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                     "u.username as usuario_nombre " +
                     "FROM movimientos_inventario m " +
                     "LEFT JOIN productos p ON m.producto_id = p.producto_id " +
                     "LEFT JOIN usuarios u ON m.usuario_id = u.usuario_id " +
                     "WHERE m.producto_id = ? ORDER BY m.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, productoId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando movimientos por producto", e);
            throw new DataAccessException("Error buscando movimientos por producto", e);
        }
        
        return movimientos;
    }
    
    @Override
    public List<MovimientoInventario> findByTipo(String tipo) throws DataAccessException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT m.*, p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                     "u.username as usuario_nombre " +
                     "FROM movimientos_inventario m " +
                     "LEFT JOIN productos p ON m.producto_id = p.producto_id " +
                     "LEFT JOIN usuarios u ON m.usuario_id = u.usuario_id " +
                     "WHERE m.tipo = ? ORDER BY m.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, tipo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando movimientos por tipo", e);
            throw new DataAccessException("Error buscando movimientos por tipo", e);
        }
        
        return movimientos;
    }
    
    @Override
    public List<MovimientoInventario> findByFechaRange(LocalDateTime desde, LocalDateTime hasta) throws DataAccessException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT m.*, p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                     "u.username as usuario_nombre " +
                     "FROM movimientos_inventario m " +
                     "LEFT JOIN productos p ON m.producto_id = p.producto_id " +
                     "LEFT JOIN usuarios u ON m.usuario_id = u.usuario_id " +
                     "WHERE m.fecha_creacion BETWEEN ? AND ? ORDER BY m.fecha_creacion DESC";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setTimestamp(1, Timestamp.valueOf(desde));
            pstmt.setTimestamp(2, Timestamp.valueOf(hasta));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando movimientos por rango de fechas", e);
            throw new DataAccessException("Error buscando movimientos por rango de fechas", e);
        }
        
        return movimientos;
    }
    
    @Override
    public List<MovimientoInventario> findRecent(int limit) throws DataAccessException {
        List<MovimientoInventario> movimientos = new ArrayList<>();
        String sql = "SELECT m.*, p.codigo as producto_codigo, p.nombre as producto_nombre, " +
                     "u.username as usuario_nombre " +
                     "FROM movimientos_inventario m " +
                     "LEFT JOIN productos p ON m.producto_id = p.producto_id " +
                     "LEFT JOIN usuarios u ON m.usuario_id = u.usuario_id " +
                     "ORDER BY m.fecha_creacion DESC LIMIT ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimiento(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando movimientos recientes", e);
            throw new DataAccessException("Error buscando movimientos recientes", e);
        }
        
        return movimientos;
    }
    
    private MovimientoInventario mapResultSetToMovimiento(ResultSet rs) throws SQLException {
        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setId(rs.getLong("movimiento_id"));
        movimiento.setProductoId(rs.getLong("producto_id"));
        movimiento.setProductoCodigo(rs.getString("producto_codigo"));
        movimiento.setProductoNombre(rs.getString("producto_nombre"));
        movimiento.setTipo(MovimientoInventario.TipoMovimiento.valueOf(rs.getString("tipo")));
        movimiento.setCantidad(rs.getInt("cantidad"));
        
        Long referenciaId = rs.getLong("referencia_id");
        if (!rs.wasNull()) {
            movimiento.setReferenciaId(referenciaId);
        }
        
        movimiento.setDescripcion(rs.getString("descripcion"));
        
        Long usuarioId = rs.getLong("usuario_id");
        if (!rs.wasNull()) {
            movimiento.setUsuarioId(usuarioId);
        }
        
        movimiento.setUsuarioNombre(rs.getString("usuario_nombre"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            movimiento.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return movimiento;
    }
}
