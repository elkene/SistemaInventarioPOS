package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IMovimientoInventarioDAO;
import com.inventario.dao.IProductoDAO;
import com.inventario.models.MovimientoInventario;
import com.inventario.models.Producto;
import com.inventario.config.SessionManager;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class MovimientoInventarioService {
    
    private static final Logger logger = LoggerFactory.getLogger(MovimientoInventarioService.class);
    private final IMovimientoInventarioDAO movimientoDAO;
    private final IProductoDAO productoDAO;
    
    public MovimientoInventarioService() {
        this.movimientoDAO = DAOFactory.getMovimientoInventarioDAO();
        this.productoDAO = DAOFactory.getProductoDAO();
    }
    
    public void registrarEntrada(Long productoId, Integer cantidad, String descripcion) throws ApplicationException {
        try {
            validarMovimiento(productoId, cantidad);
            
            Producto producto = productoDAO.read(productoId);
            if (producto == null) {
                throw new ValidationException("Producto no encontrado");
            }
            
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProductoId(productoId);
            movimiento.setTipo(MovimientoInventario.TipoMovimiento.ENTRADA);
            movimiento.setCantidad(cantidad);
            movimiento.setDescripcion(descripcion != null ? descripcion : "Entrada de inventario");
            
            Long usuarioId = SessionManager.getInstance().getCurrentUser() != null ? 
                           SessionManager.getInstance().getCurrentUser().getId() : null;
            movimiento.setUsuarioId(usuarioId);
            
            movimientoDAO.create(movimiento);
            
            productoDAO.updateStock(productoId, cantidad);
            logger.info("Entrada de inventario registrada: " + cantidad + " unidades para producto " + productoId);
            
        } catch (DataAccessException e) {
            logger.error("Error registrando entrada de inventario", e);
            throw new ApplicationException("Error registrando entrada de inventario", e);
        }
    }
    
    public void registrarSalida(Long productoId, Integer cantidad, String descripcion) throws ApplicationException {
        try {
            validarMovimiento(productoId, cantidad);
            
            Producto producto = productoDAO.read(productoId);
            if (producto == null) {
                throw new ValidationException("Producto no encontrado");
            }
            
            if (producto.getStock() < cantidad) {
                throw new ValidationException("Stock insuficiente. Stock actual: " + producto.getStock());
            }
            
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProductoId(productoId);
            movimiento.setTipo(MovimientoInventario.TipoMovimiento.SALIDA);
            movimiento.setCantidad(cantidad);
            movimiento.setDescripcion(descripcion != null ? descripcion : "Salida de inventario");
            
            Long usuarioId = SessionManager.getInstance().getCurrentUser() != null ? 
                           SessionManager.getInstance().getCurrentUser().getId() : null;
            movimiento.setUsuarioId(usuarioId);
            
            movimientoDAO.create(movimiento);
            
            productoDAO.updateStock(productoId, -cantidad);
            logger.info("Salida de inventario registrada: " + cantidad + " unidades para producto " + productoId);
            
        } catch (DataAccessException e) {
            logger.error("Error registrando salida de inventario", e);
            throw new ApplicationException("Error registrando salida de inventario", e);
        }
    }
    
    public void registrarAjuste(Long productoId, Integer cantidad, String descripcion) throws ApplicationException {
        try {
            validarMovimiento(productoId, cantidad);
            
            Producto producto = productoDAO.read(productoId);
            if (producto == null) {
                throw new ValidationException("Producto no encontrado");
            }
            
            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProductoId(productoId);
            movimiento.setTipo(MovimientoInventario.TipoMovimiento.AJUSTE);
            movimiento.setCantidad(cantidad);
            movimiento.setDescripcion(descripcion != null ? descripcion : "Ajuste de inventario");
            
            Long usuarioId = SessionManager.getInstance().getCurrentUser() != null ? 
                           SessionManager.getInstance().getCurrentUser().getId() : null;
            movimiento.setUsuarioId(usuarioId);
            
            movimientoDAO.create(movimiento);
            
            productoDAO.updateStock(productoId, cantidad);
            logger.info("Ajuste de inventario registrado: " + cantidad + " unidades para producto " + productoId);
            
        } catch (DataAccessException e) {
            logger.error("Error registrando ajuste de inventario", e);
            throw new ApplicationException("Error registrando ajuste de inventario", e);
        }
    }
    
    public List<MovimientoInventario> obtenerMovimientosProducto(Long productoId) throws ApplicationException {
        try {
            return movimientoDAO.findByProducto(productoId);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo movimientos del producto", e);
            throw new ApplicationException("Error obteniendo movimientos del producto", e);
        }
    }
    
    public List<MovimientoInventario> obtenerMovimientosRecientes(int limit) throws ApplicationException {
        try {
            return movimientoDAO.findRecent(limit);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo movimientos recientes", e);
            throw new ApplicationException("Error obteniendo movimientos recientes", e);
        }
    }
    
    public List<MovimientoInventario> obtenerTodos() throws ApplicationException {
        try {
            return movimientoDAO.readAll();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo todos los movimientos", e);
            throw new ApplicationException("Error obteniendo todos los movimientos", e);
        }
    }
    
    public List<MovimientoInventario> obtenerPorRangoFechas(LocalDate desde, LocalDate hasta) throws ApplicationException {
        try {
            LocalDateTime desdeDateTime = desde.atStartOfDay();
            LocalDateTime hastaDateTime = hasta.atTime(LocalTime.MAX);
            return movimientoDAO.findByFechaRange(desdeDateTime, hastaDateTime);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo movimientos por rango de fechas", e);
            throw new ApplicationException("Error obteniendo movimientos por rango de fechas", e);
        }
    }
    
    private void validarMovimiento(Long productoId, Integer cantidad) throws ValidationException {
        if (productoId == null) {
            throw new ValidationException("El producto es requerido");
        }
        
        if (cantidad == null || cantidad <= 0) {
            throw new ValidationException("La cantidad debe ser mayor a cero");
        }
    }
}
