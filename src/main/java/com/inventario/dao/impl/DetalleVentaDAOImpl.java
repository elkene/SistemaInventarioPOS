package com.inventario.dao.impl;

import com.inventario.dao.IDetalleVentaDAO;
import com.inventario.models.DetalleVenta;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DetalleVentaDAOImpl extends GenericDAOImpl<DetalleVenta> implements IDetalleVentaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(DetalleVentaDAOImpl.class);
    
    public DetalleVentaDAOImpl() {
        super(DetalleVenta.class);
    }
    
    @Override
    public void create(DetalleVenta detalle) throws DataAccessException {
        String sql = "INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, costo_unitario, subtotal) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, detalle.getVentaId());
            pstmt.setLong(2, detalle.getProductoId());
            pstmt.setInt(3, detalle.getCantidad());
            pstmt.setBigDecimal(4, detalle.getPrecioUnitario());
            pstmt.setBigDecimal(5, detalle.getCostoUnitario() != null ? detalle.getCostoUnitario() : java.math.BigDecimal.ZERO);
            pstmt.setBigDecimal(6, detalle.getSubtotal());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        detalle.setId(generatedKeys.getLong(1));
                        logger.info("Detalle de venta creado con ID: " + detalle.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando detalle de venta", e);
            throw new DataAccessException("Error creando detalle de venta", e);
        }
    }
    
    @Override
    public DetalleVenta read(Long id) throws DataAccessException {
        String sql = "SELECT d.*, p.nombre as producto_nombre " +
                     "FROM detalles_venta d " +
                     "LEFT JOIN productos p ON d.producto_id = p.producto_id " +
                     "WHERE d.detalle_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDetalleVenta(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo detalle de venta", e);
            throw new DataAccessException("Error leyendo detalle de venta", e);
        }
        
        return null;
    }
    
    @Override
    public List<DetalleVenta> readAll() throws DataAccessException {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT d.*, p.nombre as producto_nombre " +
                     "FROM detalles_venta d " +
                     "LEFT JOIN productos p ON d.producto_id = p.producto_id " +
                     "ORDER BY d.detalle_id";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                detalles.add(mapResultSetToDetalleVenta(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todos los detalles de venta", e);
            throw new DataAccessException("Error leyendo todos los detalles de venta", e);
        }
        
        return detalles;
    }
    
    @Override
    public void update(DetalleVenta detalle) throws DataAccessException {
        String sql = "UPDATE detalles_venta SET cantidad = ?, precio_unitario = ?, subtotal = ? " +
                     "WHERE detalle_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, detalle.getCantidad());
            pstmt.setBigDecimal(2, detalle.getPrecioUnitario());
            pstmt.setBigDecimal(3, detalle.getSubtotal());
            pstmt.setLong(4, detalle.getId());
            
            pstmt.executeUpdate();
            logger.info("Detalle de venta actualizado: " + detalle.getId());
        } catch (SQLException e) {
            logger.error("Error actualizando detalle de venta", e);
            throw new DataAccessException("Error actualizando detalle de venta", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM detalles_venta WHERE detalle_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Detalle de venta eliminado: " + id);
        } catch (SQLException e) {
            logger.error("Error eliminando detalle de venta", e);
            throw new DataAccessException("Error eliminando detalle de venta", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM detalles_venta";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando detalles de venta", e);
            throw new DataAccessException("Error contando detalles de venta", e);
        }
        
        return 0;
    }
    
    @Override
    public List<DetalleVenta> findByVenta(Long ventaId) throws DataAccessException {
        List<DetalleVenta> detalles = new ArrayList<>();
        String sql = "SELECT d.*, p.nombre as producto_nombre " +
                     "FROM detalles_venta d " +
                     "LEFT JOIN productos p ON d.producto_id = p.producto_id " +
                     "WHERE d.venta_id = ? ORDER BY d.detalle_id";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, ventaId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    detalles.add(mapResultSetToDetalleVenta(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando detalles por venta", e);
            throw new DataAccessException("Error buscando detalles por venta", e);
        }
        
        return detalles;
    }
    
    @Override
    public void deleteByVenta(Long ventaId) throws DataAccessException {
        String sql = "DELETE FROM detalles_venta WHERE venta_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, ventaId);
            pstmt.executeUpdate();
            logger.info("Detalles de venta eliminados para venta: " + ventaId);
        } catch (SQLException e) {
            logger.error("Error eliminando detalles por venta", e);
            throw new DataAccessException("Error eliminando detalles por venta", e);
        }
    }
    
    private DetalleVenta mapResultSetToDetalleVenta(ResultSet rs) throws SQLException {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setId(rs.getLong("detalle_id"));
        detalle.setVentaId(rs.getLong("venta_id"));
        detalle.setProductoId(rs.getLong("producto_id"));
        detalle.setCantidad(rs.getInt("cantidad"));
        detalle.setPrecioUnitario(rs.getBigDecimal("precio_unitario"));
        detalle.setCostoUnitario(rs.getBigDecimal("costo_unitario"));
        detalle.setSubtotal(rs.getBigDecimal("subtotal"));
        
        String productoNombre = rs.getString("producto_nombre");
        if (productoNombre != null) {
            detalle.setProductoNombre(productoNombre);
        }
        
        return detalle;
    }
}
