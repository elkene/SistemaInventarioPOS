package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IMovimientoInventarioDAO;
import com.inventario.dao.IProductoDAO;
import com.inventario.models.MovimientoInventario;
import com.inventario.models.Producto;
import com.inventario.config.SessionManager;
import com.inventario.database.ConnectionFactory;
import com.inventario.models.Usuario;
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
    
    /**
     * Entrada de mercancía (por ejemplo, una compra). proveedorId es opcional.
     */
    public void registrarEntrada(Long productoId, Integer cantidad, String descripcion, Long proveedorId) throws ApplicationException {
        validarMovimiento(productoId, cantidad);
        Long usuarioId = usuarioAdmin();
        try {
            ConnectionFactory.enTransaccion(() -> {
                obtenerProductoExistente(productoId);
                productoDAO.updateStock(productoId, cantidad);
                crearMovimiento(productoId, MovimientoInventario.TipoMovimiento.ENTRADA, cantidad,
                        textoODefecto(descripcion, "Entrada de inventario"), usuarioId, proveedorId);
                return null;
            });
            logger.info("Entrada de inventario registrada: " + cantidad + " unidades para producto " + productoId);
        } catch (DataAccessException e) {
            logger.error("Error registrando entrada de inventario", e);
            throw new ApplicationException("Error registrando entrada de inventario", e);
        }
    }
    
    /**
     * Salida de mercancía que no es venta (merma, caducidad, uso interno...).
     */
    public void registrarSalida(Long productoId, Integer cantidad, String descripcion) throws ApplicationException {
        validarMovimiento(productoId, cantidad);
        Long usuarioId = usuarioAdmin();
        try {
            ConnectionFactory.enTransaccion(() -> {
                Producto producto = obtenerProductoExistente(productoId);
                if (!productoDAO.updateStock(productoId, -cantidad)) {
                    throw new ValidationException("Stock insuficiente. Stock actual: " + producto.getStock());
                }
                crearMovimiento(productoId, MovimientoInventario.TipoMovimiento.SALIDA, cantidad,
                        textoODefecto(descripcion, "Salida de inventario"), usuarioId, null);
                return null;
            });
            logger.info("Salida de inventario registrada: " + cantidad + " unidades para producto " + productoId);
        } catch (DataAccessException e) {
            logger.error("Error registrando salida de inventario", e);
            throw new ApplicationException("Error registrando salida de inventario", e);
        }
    }
    
    /**
     * Ajuste por conteo físico: deja el stock en stockContado y registra la diferencia.
     */
    public void registrarAjuste(Long productoId, Integer stockContado, String descripcion) throws ApplicationException {
        if (productoId == null) {
            throw new ValidationException("El producto es requerido");
        }
        if (stockContado == null || stockContado < 0) {
            throw new ValidationException("El stock contado no puede ser negativo");
        }
        Long usuarioId = usuarioAdmin();
        try {
            ConnectionFactory.enTransaccion(() -> {
                Producto producto = obtenerProductoExistente(productoId);
                int diferencia = stockContado - producto.getStock();
                if (diferencia == 0) {
                    throw new ValidationException("El stock contado es igual al stock actual; no hay nada que ajustar");
                }
                productoDAO.updateStock(productoId, diferencia);
                String detalle = "Conteo físico: " + producto.getStock() + " → " + stockContado;
                crearMovimiento(productoId, MovimientoInventario.TipoMovimiento.AJUSTE, diferencia,
                        descripcion == null || descripcion.isBlank() ? detalle : detalle + ". " + descripcion,
                        usuarioId, null);
                return null;
            });
            logger.info("Ajuste de inventario registrado: stock " + stockContado + " para producto " + productoId);
        } catch (DataAccessException e) {
            logger.error("Error registrando ajuste de inventario", e);
            throw new ApplicationException("Error registrando ajuste de inventario", e);
        }
    }
    
    private Producto obtenerProductoExistente(Long productoId) throws ApplicationException {
        Producto producto = productoDAO.read(productoId);
        if (producto == null) {
            throw new ValidationException("Producto no encontrado");
        }
        return producto;
    }
    
    private void crearMovimiento(Long productoId, MovimientoInventario.TipoMovimiento tipo, int cantidad,
                                 String descripcion, Long usuarioId, Long proveedorId) throws DataAccessException {
        MovimientoInventario movimiento = new MovimientoInventario(productoId, tipo, cantidad, descripcion);
        movimiento.setUsuarioId(usuarioId);
        movimiento.setProveedorId(proveedorId);
        movimientoDAO.create(movimiento);
    }
    
    private Long usuarioAdmin() throws ValidationException {
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario == null) {
            throw new ValidationException("La sesión expiró. Cierre sesión e ingrese de nuevo.");
        }
        if (!"ADMIN".equals(usuario.getRol())) {
            throw new ValidationException("Solo un administrador puede modificar el inventario");
        }
        return usuario.getId();
    }
    
    private static String textoODefecto(String texto, String defecto) {
        return texto == null || texto.isBlank() ? defecto : texto;
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
