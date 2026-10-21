package com.inventario.utils;

import com.inventario.config.AppPaths;
import com.inventario.database.ConnectionFactory;
import com.inventario.models.CorteCaja;
import com.inventario.models.DetalleVenta;
import com.inventario.models.Producto;
import com.inventario.models.Venta;
import com.inventario.services.ProductoService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class RespaldoYTicketTest {

    @BeforeClass
    public static void abrirBaseTemporal() throws Exception {
        if (System.getProperty("inventario.home") == null) {
            System.setProperty("inventario.home", Files.createTempDirectory("inventario-test").toString());
        }
        ConnectionFactory.inicializar();
    }

    @AfterClass
    public static void cerrar() {
        ConnectionFactory.closeDataSource();
    }

    private static Producto producto(String codigo) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre("Producto " + codigo);
        p.setPrecioCompra(BigDecimal.ONE);
        p.setPrecioVenta(BigDecimal.TEN);
        p.setStock(0);
        p.setStockMinimo(0);
        return p;
    }

    @Test
    public void restaurarRegresaLosDatosDelRespaldo() throws Exception {
        ProductoService productos = new ProductoService();
        productos.crearProducto(producto("ANTES"));
        Path respaldo = AppPaths.RESPALDOS.resolve("ida_y_vuelta.zip");
        RespaldoUtil.crearRespaldo(respaldo);
        productos.crearProducto(producto("DESPUES"));

        Path copiaPrevia = RespaldoUtil.restaurar(respaldo);
        ConnectionFactory.inicializar();

        assertNotNull(productos.buscarPorCodigo("ANTES"));
        assertNull(productos.buscarPorCodigo("DESPUES"));
        assertTrue(Files.exists(copiaPrevia));
    }

    @Test(expected = java.io.IOException.class)
    public void restaurarRechazaZipAjeno() throws Exception {
        Path ajeno = Files.createTempFile("ajeno", ".zip");
        try (var zip = new java.util.zip.ZipOutputStream(Files.newOutputStream(ajeno))) {
            zip.putNextEntry(new java.util.zip.ZipEntry("otra_cosa.txt"));
        }
        RespaldoUtil.restaurar(ajeno);
    }

    @Test
    public void generaTicketYCorteEnPdf() throws Exception {
        Venta venta = new Venta();
        venta.setId(99L);
        venta.setSubtotal(new BigDecimal("30.00"));
        venta.setDescuento(new BigDecimal("5.00"));
        venta.setTotal(new BigDecimal("25.00"));
        venta.setMetodoPago("EFECTIVO");
        DetalleVenta d = new DetalleVenta();
        d.setProductoNombre("Refresco de cola 600 ml con un nombre largo para probar el ajuste de línea");
        d.setCantidad(3);
        d.setPrecioUnitario(BigDecimal.TEN);
        d.setSubtotal(new BigDecimal("30.00"));

        Path ticket = TicketPdf.ticketVenta(venta, List.of(d), "admin", new BigDecimal("50"), new BigDecimal("25"));
        CorteCaja datosCorte = new CorteCaja();
        datosCorte.setId(7L);
        datosCorte.setUsuarioNombre("admin");
        datosCorte.setFecha(LocalDate.now());
        datosCorte.setNumeroVentas(1);
        datosCorte.setTotalVendido(new BigDecimal("25.00"));
        datosCorte.setVentasEfectivo(new BigDecimal("25.00"));
        datosCorte.setFondoInicial(new BigDecimal("500"));
        datosCorte.setEfectivoContado(new BigDecimal("525"));
        datosCorte.setDiferencia(BigDecimal.ZERO);
        Path corte = TicketPdf.corteCaja(datosCorte, Map.of("EFECTIVO", new BigDecimal("25.00")));

        assertTrue(Files.size(ticket) > 0);
        assertTrue(Files.size(corte) > 0);
        assertTrue("debe crear negocio.properties editable", Files.exists(AppPaths.NEGOCIO));
    }
}
