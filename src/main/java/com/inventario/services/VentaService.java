package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IVentaDAO;
import com.inventario.dao.IDetalleVentaDAO;
import com.inventario.dao.IProductoDAO;
import com.inventario.models.Venta;
import com.inventario.models.DetalleVenta;
import com.inventario.models.Producto;
import com.inventario.models.Usuario;
import com.inventario.config.SessionManager;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class VentaService {
    
    private static final Logger logger = LoggerFactory.getLogger(VentaService.class);
    private final IVentaDAO ventaDAO;
    private final IDetalleVentaDAO detalleVentaDAO;
    private final IProductoDAO productoDAO;
    
    public VentaService() {
        this.ventaDAO = DAOFactory.getVentaDAO();
        this.detalleVentaDAO = DAOFactory.getDetalleVentaDAO();
        this.productoDAO = DAOFactory.getProductoDAO();
    }
    
    public Venta registrarVenta(List<DetalleVenta> detalles, Long clienteId, 
                               String metodoPago, BigDecimal descuento, String observaciones) 
                               throws ApplicationException {
        try {
            if (detalles == null || detalles.isEmpty()) {
                throw new ValidationException("Debe agregar productos a la venta");
            }
            
            if (metodoPago == null || metodoPago.isEmpty()) {
                throw new ValidationException("Debe seleccionar un método de pago");
            }
            
            BigDecimal subtotal = BigDecimal.ZERO;
            for (DetalleVenta detalle : detalles) {
                detalle.setSubtotal(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())));
                subtotal = subtotal.add(detalle.getSubtotal());
            }
            
            BigDecimal montoDescuento = BigDecimal.ZERO;
            if (descuento != null && descuento.compareTo(BigDecimal.ZERO) > 0) {
                montoDescuento = descuento;
            }
            
            BigDecimal total = subtotal.subtract(montoDescuento);
            if (total.compareTo(BigDecimal.ZERO) < 0) {
                total = BigDecimal.ZERO;
            }
            
            Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
            Long usuarioId = usuarioActual != null ? usuarioActual.getId() : null;
            
            Venta venta = new Venta();
            venta.setUsuarioId(usuarioId);
            venta.setClienteId(clienteId);
            venta.setTotal(total);
            venta.setMetodoPago(metodoPago);
            venta.setEstado("COMPLETADA");
            venta.setObservaciones(observaciones);
            
            ventaDAO.create(venta);
            
            for (DetalleVenta detalle : detalles) {
                detalle.setVentaId(venta.getId());
                detalleVentaDAO.create(detalle);
                
                productoDAO.updateStock(detalle.getProductoId(), -detalle.getCantidad());
            }
            
            logger.info("Venta registrada: " + venta.getId() + " - Total: " + total);
            return venta;
            
        } catch (DataAccessException e) {
            logger.error("Error registrando venta", e);
            throw new ApplicationException("Error registrando venta", e);
        }
    }
    
    public Venta obtenerVenta(Long id) throws ApplicationException {
        try {
            return ventaDAO.read(id);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo venta", e);
            throw new ApplicationException("Error obteniendo venta", e);
        }
    }
    
    public List<DetalleVenta> obtenerDetallesVenta(Long ventaId) throws ApplicationException {
        try {
            return detalleVentaDAO.findByVenta(ventaId);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo detalles de venta", e);
            throw new ApplicationException("Error obteniendo detalles de venta", e);
        }
    }
    
    public List<Venta> obtenerTodas() throws ApplicationException {
        try {
            return ventaDAO.readAll();
        } catch (DataAccessException e) {
            logger.error("Error obteniendo ventas", e);
            throw new ApplicationException("Error obteniendo ventas", e);
        }
    }
    
    public List<Venta> obtenerPorFecha(LocalDate fecha) throws ApplicationException {
        try {
            return ventaDAO.findByFecha(fecha.toString());
        } catch (DataAccessException e) {
            logger.error("Error obteniendo ventas por fecha", e);
            throw new ApplicationException("Error obteniendo ventas por fecha", e);
        }
    }
    
    public List<Venta> obtenerPorRangoFechas(LocalDate desde, LocalDate hasta) throws ApplicationException {
        try {
            return ventaDAO.findByRangoFechas(desde.toString(), hasta.toString());
        } catch (DataAccessException e) {
            logger.error("Error obteniendo ventas por rango de fechas", e);
            throw new ApplicationException("Error obteniendo ventas por rango de fechas", e);
        }
    }
    
    public List<Venta> obtenerRecientes(int limit) throws ApplicationException {
        try {
            return ventaDAO.findRecientes(limit);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo ventas recientes", e);
            throw new ApplicationException("Error obteniendo ventas recientes", e);
        }
    }
    
    public List<Venta> obtenerPorCliente(Long clienteId) throws ApplicationException {
        try {
            return ventaDAO.findByCliente(clienteId);
        } catch (DataAccessException e) {
            logger.error("Error obteniendo ventas por cliente", e);
            throw new ApplicationException("Error obteniendo ventas por cliente", e);
        }
    }
    
    public long contarVentasHoy() throws ApplicationException {
        try {
            return ventaDAO.countHoy();
        } catch (DataAccessException e) {
            logger.error("Error contando ventas de hoy", e);
            throw new ApplicationException("Error contando ventas de hoy", e);
        }
    }
    
    public BigDecimal calcularTotalVentasHoy() throws ApplicationException {
        try {
            List<Venta> ventasHoy = ventaDAO.findByFecha(LocalDate.now().toString());
            BigDecimal total = BigDecimal.ZERO;
            for (Venta venta : ventasHoy) {
                if ("COMPLETADA".equals(venta.getEstado())) {
                    total = total.add(venta.getTotal());
                }
            }
            return total;
        } catch (DataAccessException e) {
            logger.error("Error calculando total de ventas de hoy", e);
            throw new ApplicationException("Error calculando total de ventas de hoy", e);
        }
    }
    
    public void anularVenta(Long ventaId) throws ApplicationException {
        try {
            Venta venta = ventaDAO.read(ventaId);
            if (venta == null) {
                throw new ValidationException("Venta no encontrada");
            }
            
            if (!"COMPLETADA".equals(venta.getEstado())) {
                throw new ValidationException("La venta ya está anulada");
            }
            
            List<DetalleVenta> detalles = detalleVentaDAO.findByVenta(ventaId);
            
            for (DetalleVenta detalle : detalles) {
                productoDAO.updateStock(detalle.getProductoId(), detalle.getCantidad());
            }
            
            venta.setEstado("ANULADA");
            ventaDAO.update(venta);
            
            logger.info("Venta anulada: " + ventaId);
            
        } catch (DataAccessException e) {
            logger.error("Error anulando venta", e);
            throw new ApplicationException("Error anulando venta", e);
        }
    }
}
