package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IVentaDAO;
import com.inventario.dao.IDetalleVentaDAO;
import com.inventario.dao.IProductoDAO;
import com.inventario.dao.IMovimientoInventarioDAO;
import com.inventario.dao.ICorteCajaDAO;
import com.inventario.models.CorteCaja;
import com.inventario.database.ConnectionFactory;
import com.inventario.models.MovimientoInventario;
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
import java.util.Map;
import java.util.TreeMap;

public class VentaService {
    
    private static final Logger logger = LoggerFactory.getLogger(VentaService.class);
    private final IVentaDAO ventaDAO;
    private final IDetalleVentaDAO detalleVentaDAO;
    private final IProductoDAO productoDAO;
    private final IMovimientoInventarioDAO movimientoDAO;
    private final ICorteCajaDAO corteCajaDAO = DAOFactory.getCorteCajaDAO();

    public VentaService() {
        this.ventaDAO = DAOFactory.getVentaDAO();
        this.detalleVentaDAO = DAOFactory.getDetalleVentaDAO();
        this.productoDAO = DAOFactory.getProductoDAO();
        this.movimientoDAO = DAOFactory.getMovimientoInventarioDAO();
    }

    private void registrarMovimiento(Long productoId, MovimientoInventario.TipoMovimiento tipo, int cantidad,
                                     Long ventaId, String descripcion, Long usuarioId) throws DataAccessException {
        MovimientoInventario movimiento = new MovimientoInventario(productoId, tipo, cantidad, descripcion);
        movimiento.setReferenciaId(ventaId);
        movimiento.setUsuarioId(usuarioId);
        movimientoDAO.create(movimiento);
    }
    
    public Venta registrarVenta(List<DetalleVenta> detalles, Long clienteId, 
                               String metodoPago, BigDecimal descuento, BigDecimal montoRecibido,
                               String observaciones)
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
                if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                    throw new ValidationException("La cantidad de cada producto debe ser mayor a cero");
                }
                detalle.setSubtotal(detalle.getPrecioUnitario().multiply(BigDecimal.valueOf(detalle.getCantidad())));
                subtotal = subtotal.add(detalle.getSubtotal());
            }

            BigDecimal montoDescuento = descuento != null ? descuento : BigDecimal.ZERO;
            if (montoDescuento.compareTo(BigDecimal.ZERO) < 0) {
                throw new ValidationException("El descuento no puede ser negativo");
            }
            if (montoDescuento.compareTo(subtotal) > 0) {
                throw new ValidationException("El descuento no puede ser mayor al subtotal");
            }
            BigDecimal total = subtotal.subtract(montoDescuento);

            Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
            if (usuarioActual == null) {
                throw new ValidationException("La sesión expiró. Cierre sesión e ingrese de nuevo para vender.");
            }

            Venta venta = new Venta();
            venta.setUsuarioId(usuarioActual.getId());
            venta.setClienteId(clienteId);
            venta.setSubtotal(subtotal);
            venta.setDescuento(montoDescuento);
            venta.setTotal(total);
            venta.setMetodoPago(metodoPago);
            venta.setEstado("COMPLETADA");
            venta.setObservaciones(observaciones);
            if (montoRecibido != null && montoRecibido.compareTo(total) < 0) {
                throw new ValidationException("El monto recibido es menor al total");
            }
            venta.setMontoRecibido(montoRecibido);

            // Venta, detalles, stock y movimientos se guardan juntos o no se guarda nada
            ConnectionFactory.enTransaccion(() -> {
                ventaDAO.create(venta);

                for (DetalleVenta detalle : detalles) {
                    Producto producto = productoDAO.read(detalle.getProductoId());
                    if (producto == null || !producto.isActivo()) {
                        throw new ValidationException("Producto no disponible para venta: " + detalle.getProductoNombre());
                    }
                    if (!productoDAO.updateStock(producto.getId(), -detalle.getCantidad())) {
                        throw new ValidationException("Stock insuficiente de " + producto.getNombre() +
                                ". Disponible: " + producto.getStock());
                    }

                    detalle.setVentaId(venta.getId());
                    detalle.setCostoUnitario(producto.getPrecioCompra());
                    detalleVentaDAO.create(detalle);

                    registrarMovimiento(producto.getId(), MovimientoInventario.TipoMovimiento.SALIDA,
                            detalle.getCantidad(), venta.getId(), "Venta #" + venta.getId(), usuarioActual.getId());
                }
                return null;
            });

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
    
    /**
     * Total de ventas completadas del día agrupado por método de pago (para el corte de caja).
     */
    public Map<String, BigDecimal> totalesPorMetodoPago(LocalDate fecha) throws ApplicationException {
        Map<String, BigDecimal> totales = new TreeMap<>();
        for (Venta venta : obtenerPorFecha(fecha)) {
            if ("COMPLETADA".equals(venta.getEstado())) {
                totales.merge(venta.getMetodoPago(), venta.getTotal(), BigDecimal::add);
            }
        }
        return totales;
    }

    /**
     * Calcula y guarda el corte de caja del día con el efectivo que el cajero contó.
     */
    public CorteCaja registrarCorte(BigDecimal fondoInicial, BigDecimal efectivoContado) throws ApplicationException {
        if (fondoInicial == null || fondoInicial.signum() < 0 || efectivoContado == null || efectivoContado.signum() < 0) {
            throw new ValidationException("El fondo inicial y el efectivo contado no pueden ser negativos");
        }
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario == null) {
            throw new ValidationException("La sesión expiró. Cierre sesión e ingrese de nuevo.");
        }

        LocalDate hoy = LocalDate.now();
        Map<String, BigDecimal> porMetodo = totalesPorMetodoPago(hoy);
        BigDecimal ventasEfectivo = porMetodo.getOrDefault("EFECTIVO", BigDecimal.ZERO);

        CorteCaja corte = new CorteCaja();
        corte.setUsuarioId(usuario.getId());
        corte.setUsuarioNombre(usuario.getUsername());
        corte.setFecha(hoy);
        corte.setNumeroVentas((int) contarVentasHoy());
        corte.setTotalVendido(porMetodo.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        corte.setVentasEfectivo(ventasEfectivo);
        corte.setFondoInicial(fondoInicial);
        corte.setEfectivoContado(efectivoContado);
        corte.setDiferencia(efectivoContado.subtract(fondoInicial.add(ventasEfectivo)));
        corteCajaDAO.create(corte);
        logger.info("Corte de caja registrado: " + corte.getId() + " - Diferencia: " + corte.getDiferencia());
        return corte;
    }

    public List<CorteCaja> obtenerCortesRecientes(int limit) throws ApplicationException {
        return corteCajaDAO.findRecientes(limit);
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
            
            Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
            if (usuarioActual == null || !"ADMIN".equals(usuarioActual.getRol())) {
                throw new ValidationException("Solo un administrador puede anular ventas");
            }

            List<DetalleVenta> detalles = detalleVentaDAO.findByVenta(ventaId);

            ConnectionFactory.enTransaccion(() -> {
                for (DetalleVenta detalle : detalles) {
                    productoDAO.updateStock(detalle.getProductoId(), detalle.getCantidad());
                    registrarMovimiento(detalle.getProductoId(), MovimientoInventario.TipoMovimiento.ENTRADA,
                            detalle.getCantidad(), ventaId, "Anulación de venta #" + ventaId, usuarioActual.getId());
                }

                venta.setEstado("ANULADA");
                ventaDAO.update(venta);
                return null;
            });
            
            logger.info("Venta anulada: " + ventaId);
            
        } catch (DataAccessException e) {
            logger.error("Error anulando venta", e);
            throw new ApplicationException("Error anulando venta", e);
        }
    }
}
