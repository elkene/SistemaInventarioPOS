package com.inventario.services;

import com.inventario.config.AppPaths;
import com.inventario.database.ConnectionFactory;
import com.inventario.exceptions.ValidationException;
import com.inventario.models.DetalleVenta;
import com.inventario.models.MovimientoInventario;
import com.inventario.models.Producto;
import com.inventario.models.Venta;
import com.inventario.utils.RespaldoUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Prueba el flujo de venta completo contra una base H2 nueva en una carpeta temporal.
 */
public class VentaServiceTest {

    private static final VentaService ventas = new VentaService();
    private static final ProductoService productos = new ProductoService();
    private static final MovimientoInventarioService movimientos = new MovimientoInventarioService();

    @BeforeClass
    public static void abrirBaseTemporal() throws Exception {
        System.setProperty("inventario.home", Files.createTempDirectory("inventario-test").toString());
        ConnectionFactory.inicializar();
        // También verifica que el usuario y la contraseña iniciales funcionen
        new AuthenticationService().authenticate("admin", "admin123");
    }

    @AfterClass
    public static void cerrar() {
        ConnectionFactory.closeDataSource();
    }

    private static Producto crearProducto(String codigo, int stock) throws Exception {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre("Producto " + codigo);
        p.setPrecioCompra(new BigDecimal("6.00"));
        p.setPrecioVenta(new BigDecimal("10.00"));
        p.setStock(stock);
        p.setStockMinimo(1);
        productos.crearProducto(p);
        return p;
    }

    private static List<DetalleVenta> carrito(Producto p, int cantidad) {
        DetalleVenta d = new DetalleVenta();
        d.setProductoId(p.getId());
        d.setProductoNombre(p.getNombre());
        d.setCantidad(cantidad);
        d.setPrecioUnitario(p.getPrecioVenta());
        return List.of(d);
    }

    private static int stock(Producto p) throws Exception {
        return productos.obtenerProducto(p.getId()).getStock();
    }

    @Test
    public void ventaDescuentaStockGuardaCostoYRegistraMovimiento() throws Exception {
        Producto p = crearProducto("A-1", 5);

        Venta venta = ventas.registrarVenta(carrito(p, 2), null, "EFECTIVO", new BigDecimal("1.00"), null);

        assertEquals(3, stock(p));
        Venta guardada = ventas.obtenerVenta(venta.getId());
        assertEquals(0, new BigDecimal("20.00").compareTo(guardada.getSubtotal()));
        assertEquals(0, new BigDecimal("1.00").compareTo(guardada.getDescuento()));
        assertEquals(0, new BigDecimal("19.00").compareTo(guardada.getTotal()));
        assertEquals(0, new BigDecimal("6.00").compareTo(ventas.obtenerDetallesVenta(venta.getId()).get(0).getCostoUnitario()));

        List<MovimientoInventario> movs = movimientos.obtenerMovimientosProducto(p.getId());
        assertTrue(movs.stream().anyMatch(m -> m.getTipo() == MovimientoInventario.TipoMovimiento.SALIDA
                && m.getCantidad() == 2 && venta.getId().equals(m.getReferenciaId())));
        assertTrue("el stock inicial debe quedar en el historial", movs.stream().anyMatch(
                m -> m.getTipo() == MovimientoInventario.TipoMovimiento.ENTRADA && m.getCantidad() == 5));
    }

    @Test
    public void ventaSinStockNoGuardaNada() throws Exception {
        Producto p = crearProducto("B-1", 3);
        int ventasAntes = ventas.obtenerTodas().size();

        try {
            ventas.registrarVenta(carrito(p, 10), null, "EFECTIVO", BigDecimal.ZERO, null);
            fail("Debió rechazar la venta por falta de stock");
        } catch (ValidationException e) {
            assertTrue(e.getMessage().contains("Stock insuficiente"));
        }

        assertEquals(3, stock(p));
        assertEquals("la venta a medias debe deshacerse", ventasAntes, ventas.obtenerTodas().size());
    }

    @Test(expected = ValidationException.class)
    public void descuentoMayorAlSubtotalSeRechaza() throws Exception {
        Producto p = crearProducto("C-1", 3);
        ventas.registrarVenta(carrito(p, 1), null, "EFECTIVO", new BigDecimal("50"), null);
    }

    @Test
    public void anularVentaDevuelveStock() throws Exception {
        Producto p = crearProducto("D-1", 4);
        Venta venta = ventas.registrarVenta(carrito(p, 3), null, "TARJETA_DEBITO", BigDecimal.ZERO, null);
        assertEquals(1, stock(p));

        ventas.anularVenta(venta.getId());

        assertEquals(4, stock(p));
        assertEquals("ANULADA", ventas.obtenerVenta(venta.getId()).getEstado());
    }

    @Test
    public void ajusteDejaElStockContadoYRegistraLaDiferencia() throws Exception {
        Producto p = crearProducto("E-1", 10);

        movimientos.registrarAjuste(p.getId(), 7, "Conteo mensual");

        assertEquals(7, stock(p));
        assertTrue(movimientos.obtenerMovimientosProducto(p.getId()).stream().anyMatch(
                m -> m.getTipo() == MovimientoInventario.TipoMovimiento.AJUSTE && m.getCantidad() == -3));
    }

    @Test
    public void salidaMayorAlStockSeRechaza() throws Exception {
        Producto p = crearProducto("F-1", 2);
        try {
            movimientos.registrarSalida(p.getId(), 5, "Merma");
            fail("Debió rechazar la salida");
        } catch (ValidationException e) {
            assertEquals(2, stock(p));
        }
    }

    @Test
    public void respaldoGeneraZip() throws Exception {
        Path zip = AppPaths.RESPALDOS.resolve("prueba.zip");
        RespaldoUtil.crearRespaldo(zip);
        assertTrue(Files.size(zip) > 0);
    }
}
