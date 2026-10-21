package com.inventario.dao.impl;

import com.inventario.dao.IProductoDAO;
import com.inventario.models.Producto;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación DAO para Producto
 */
public class ProductoDAOImpl extends GenericDAOImpl<Producto> implements IProductoDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoDAOImpl.class);
    
    public ProductoDAOImpl() {
        super(Producto.class);
    }
    
    @Override
    public void create(Producto producto) throws DataAccessException {
        String sql = "INSERT INTO productos (codigo, nombre, descripcion, precio_compra, " +
                     "precio_venta, stock, stock_minimo, categoria_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setBigDecimal(4, producto.getPrecioCompra());
            pstmt.setBigDecimal(5, producto.getPrecioVenta());
            pstmt.setInt(6, producto.getStock());
            pstmt.setInt(7, producto.getStockMinimo());
            pstmt.setObject(8, producto.getCategoriaId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        producto.setId(generatedKeys.getLong(1));
                        logger.info("Producto creado con ID: " + producto.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando producto", e);
            throw new DataAccessException("Error creando producto", e);
        }
    }
    
    @Override
    public Producto read(Long id) throws DataAccessException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM productos p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.categoria_id " +
                     "WHERE p.producto_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo producto", e);
            throw new DataAccessException("Error leyendo producto", e);
        }
        
        return null;
    }
    
    @Override
    public List<Producto> readAll() throws DataAccessException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM productos p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.categoria_id " +
                     "WHERE p.activo = TRUE ORDER BY p.producto_id";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todos los productos", e);
            throw new DataAccessException("Error leyendo todos los productos", e);
        }
        
        return productos;
    }
    
    @Override
    public void update(Producto producto) throws DataAccessException {
        String sql = "UPDATE productos SET codigo = ?, nombre = ?, descripcion = ?, " +
                     "precio_compra = ?, precio_venta = ?, " +
                     "stock_minimo = ?, categoria_id = ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                     "WHERE producto_id = ?";
        // El stock no se edita aquí: solo cambia con updateStock() para que quede en el historial
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, producto.getCodigo());
            pstmt.setString(2, producto.getNombre());
            pstmt.setString(3, producto.getDescripcion());
            pstmt.setBigDecimal(4, producto.getPrecioCompra());
            pstmt.setBigDecimal(5, producto.getPrecioVenta());
            pstmt.setInt(6, producto.getStockMinimo());
            pstmt.setObject(7, producto.getCategoriaId());
            pstmt.setLong(8, producto.getId());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Producto actualizado: " + producto.getId());
            }
        } catch (SQLException e) {
            logger.error("Error actualizando producto", e);
            throw new DataAccessException("Error actualizando producto", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM productos WHERE producto_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Producto eliminado: " + id);
            }
        } catch (SQLException e) {
            logger.error("Error eliminando producto", e);
            throw new DataAccessException("Error eliminando producto", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM productos";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando productos", e);
            throw new DataAccessException("Error contando productos", e);
        }
        
        return 0;
    }
    
    @Override
    public Producto findByCodigo(String codigo) throws DataAccessException {
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM productos p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.categoria_id " +
                     "WHERE p.codigo = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, codigo);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProducto(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando producto por código", e);
            throw new DataAccessException("Error buscando producto por código", e);
        }
        
        return null;
    }
    
    @Override
    public List<Producto> findByCategoria(Long categoriaId) throws DataAccessException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM productos p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.categoria_id " +
                     "WHERE p.categoria_id = ? AND p.activo = TRUE ORDER BY p.nombre";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, categoriaId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProducto(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando productos por categoría", e);
            throw new DataAccessException("Error buscando productos por categoría", e);
        }
        
        return productos;
    }
    
    @Override
    public List<Producto> findStockBajo() throws DataAccessException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM productos p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.categoria_id " +
                     "WHERE p.stock <= p.stock_minimo AND p.activo = TRUE ORDER BY p.stock";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                productos.add(mapResultSetToProducto(rs));
            }
        } catch (SQLException e) {
            logger.error("Error buscando productos con stock bajo", e);
            throw new DataAccessException("Error buscando productos con stock bajo", e);
        }
        
        return productos;
    }
    
    @Override
    public List<Producto> findByNombre(String nombre) throws DataAccessException {
        List<Producto> productos = new ArrayList<>();
        String sql = "SELECT p.*, c.nombre as categoria_nombre FROM productos p " +
                     "LEFT JOIN categorias c ON p.categoria_id = c.categoria_id " +
                     "WHERE LOWER(p.nombre) LIKE LOWER(?) AND p.activo = TRUE ORDER BY p.nombre";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nombre + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    productos.add(mapResultSetToProducto(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando productos por nombre", e);
            throw new DataAccessException("Error buscando productos por nombre", e);
        }
        
        return productos;
    }
    
    @Override
    public boolean updateStock(Long productoId, Integer cantidad) throws DataAccessException {
        String sql = "UPDATE productos SET stock = stock + ?, fecha_modificacion = CURRENT_TIMESTAMP " +
                     "WHERE producto_id = ? AND stock + ? >= 0";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, cantidad);
            pstmt.setLong(2, productoId);
            pstmt.setInt(3, cantidad);

            boolean actualizado = pstmt.executeUpdate() > 0;
            logger.info("Stock actualizado para producto: " + productoId);
            return actualizado;
        } catch (SQLException e) {
            logger.error("Error actualizando stock", e);
            throw new DataAccessException("Error actualizando stock", e);
        }
    }
    
    @Override
    public void setActivo(Long productoId, boolean activo) throws DataAccessException {
        String sql = "UPDATE productos SET activo = ?, fecha_modificacion = CURRENT_TIMESTAMP WHERE producto_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, activo);
            pstmt.setLong(2, productoId);
            pstmt.executeUpdate();
            logger.info("Producto " + productoId + (activo ? " activado" : " desactivado"));
        } catch (SQLException e) {
            logger.error("Error cambiando estado del producto", e);
            throw new DataAccessException("Error cambiando estado del producto", e);
        }
    }

    private Producto mapResultSetToProducto(ResultSet rs) throws SQLException {
        Producto producto = new Producto();
        producto.setId(rs.getLong("producto_id"));
        producto.setCodigo(rs.getString("codigo"));
        producto.setNombre(rs.getString("nombre"));
        producto.setDescripcion(rs.getString("descripcion"));
        producto.setPrecioCompra(rs.getBigDecimal("precio_compra"));
        producto.setPrecioVenta(rs.getBigDecimal("precio_venta"));
        producto.setStock(rs.getInt("stock"));
        producto.setStockMinimo(rs.getInt("stock_minimo"));
        producto.setActivo(rs.getBoolean("activo"));
        producto.setCategoriaId(rs.getLong("categoria_id"));
        
        String categoriaNombre = rs.getString("categoria_nombre");
        if (categoriaNombre != null) {
            producto.setCategoriaNombre(categoriaNombre);
        }
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            producto.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaModificacion = rs.getTimestamp("fecha_modificacion");
        if (fechaModificacion != null) {
            producto.setFechaModificacion(fechaModificacion.toLocalDateTime());
        }
        
        return producto;
    }
}
