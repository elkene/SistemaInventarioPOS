package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IProductoDAO;
import com.inventario.models.Producto;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de negocio para productos
 */
public class ProductoService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoService.class);
    private final IProductoDAO productoDAO;
    
    public ProductoService() {
        this.productoDAO = DAOFactory.getProductoDAO();
    }
    
    /**
     * Crea un nuevo producto
     */
    public void crearProducto(Producto producto) throws ApplicationException {
        try {
            validarProducto(producto);
            
            // Verificar que el código sea único
            Producto existente = productoDAO.findByCodigo(producto.getCodigo());
            if (existente != null) {
                throw new ValidationException("Ya existe un producto con este código");
            }
            
            productoDAO.create(producto);
            logger.info("Producto creado: " + producto.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error creando producto", e);
            throw new ApplicationException("Error creando producto", e);
        }
    }
    
    /**
     * Actualiza un producto existente
     */
    public void actualizarProducto(Producto producto) throws ApplicationException {
        try {
            validarProducto(producto);
            productoDAO.update(producto);
            logger.info("Producto actualizado: " + producto.getNombre());
            
        } catch (DataAccessException e) {
            logger.error("Error actualizando producto", e);
            throw new ApplicationException("Error actualizando producto", e);
        }
    }
    
    /**
     * Obtiene un producto por ID
     */
    public Producto obtenerProducto(Long id) throws ApplicationException {
        try {
            return productoDAO.read(id);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo producto", e);
            throw new ApplicationException("Error obteniendo producto", e);
        }
    }
    
    /**
     * Obtiene todos los productos
     */
    public List<Producto> obtenerTodos() throws ApplicationException {
        try {
            return productoDAO.readAll();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo productos", e);
            throw new ApplicationException("Error obteniendo productos", e);
        }
    }
    
    /**
     * Busca productos por nombre
     */
    public List<Producto> buscarPorNombre(String nombre) throws ApplicationException {
        try {
            return productoDAO.findByNombre(nombre);
        } catch (DataAccessException e) {
            logger.error("Error buscando productos", e);
            throw new ApplicationException("Error buscando productos", e);
        }
    }
    
    /**
     * Obtiene productos con stock bajo
     */
    public List<Producto> obtenerProductosStockBajo() throws ApplicationException {
        try {
            return productoDAO.findStockBajo();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo productos con stock bajo", e);
            throw new ApplicationException("Error obteniendo productos con stock bajo", e);
        }
    }
    
    /**
     * Actualiza el stock de un producto
     */
    public void actualizarStock(Long productoId, Integer cantidad) throws ApplicationException {
        try {
            Producto producto = productoDAO.read(productoId);
            if (producto == null) {
                throw new ValidationException("Producto no encontrado");
            }
            
            int nuevoStock = producto.getStock() + cantidad;
            if (nuevoStock < 0) {
                throw new ValidationException("Stock insuficiente");
            }
            
            productoDAO.updateStock(productoId, cantidad);
            logger.info("Stock actualizado para producto: " + productoId);
            
        } catch (DataAccessException e) {
            logger.error("Error actualizando stock", e);
            throw new ApplicationException("Error actualizando stock", e);
        }
    }
    
    /**
     * Elimina un producto
     */
    public void eliminarProducto(Long id) throws ApplicationException {
        try {
            Producto producto = productoDAO.read(id);
            if (producto == null) {
                throw new ValidationException("Producto no encontrado");
            }
            
            if (producto.getStock() > 0) {
                throw new ValidationException("No se puede eliminar un producto con stock");
            }
            
            productoDAO.delete(id);
            logger.info("Producto eliminado: " + id);
            
        } catch (DataAccessException e) {
            logger.error("Error eliminando producto", e);
            throw new ApplicationException("Error eliminando producto", e);
        }
    }
    
    /**
     * Busca un producto por código
     */
    public Producto buscarPorCodigo(String codigo) throws ApplicationException {
        try {
            return productoDAO.findByCodigo(codigo);
        } catch (DataAccessException e) {
            logger.error("Error buscando producto por código", e);
            throw new ApplicationException("Error buscando producto por código", e);
        }
    }
    
    /**
     * Valida los datos del producto
     */
    private void validarProducto(Producto producto) throws ValidationException {
        if (producto.getCodigo() == null || producto.getCodigo().isEmpty()) {
            throw new ValidationException("El código del producto es requerido");
        }
        
        if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
            throw new ValidationException("El nombre del producto es requerido");
        }
        
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("El precio de venta debe ser mayor a cero");
        }
        
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new ValidationException("El stock no puede ser negativo");
        }
        
        if (producto.getStockMinimo() == null || producto.getStockMinimo() < 0) {
            throw new ValidationException("El stock mínimo no puede ser negativo");
        }
    }
}
