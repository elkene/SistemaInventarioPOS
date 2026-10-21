package com.inventario.services;

import com.inventario.database.ConnectionFactory;
import com.inventario.exceptions.AuthenticationException;
import com.inventario.exceptions.ValidationException;
import com.inventario.models.CorteCaja;
import com.inventario.models.DetalleVenta;
import com.inventario.models.Producto;
import com.inventario.models.Usuario;
import com.inventario.models.Venta;
import com.inventario.utils.TicketPdf;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Usuarios, bloqueo de login, cortes de caja, productos desactivados y reimpresión de tickets.
 */
public class AdministracionTest {

    private static final UsuarioService usuarios = new UsuarioService();
    private static final AuthenticationService auth = new AuthenticationService();
    private static final ProductoService productos = new ProductoService();
    private static final VentaService ventas = new VentaService();

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

    @Before
    public void entrarComoAdmin() throws Exception {
        auth.authenticate("admin", "admin123");
    }

    private static Usuario buscar(String username) throws Exception {
        return usuarios.obtenerTodos().stream().filter(u -> u.getUsername().equals(username)).findFirst().orElse(null);
    }

    @Test
    public void adminCreaUsuarioRestableceContrasenaYLoDesactiva() throws Exception {
        usuarios.crear("cajera1", "cajera1@sucursal.local", "inicial1", "VENDEDOR");
        Usuario cajera = buscar("cajera1");

        usuarios.restablecerPassword(cajera.getId(), "nueva123");
        assertEquals("cajera1", auth.authenticate("cajera1", "nueva123").getUsername());

        auth.authenticate("admin", "admin123");
        usuarios.cambiarActivo(cajera.getId(), false);
        try {
            auth.authenticate("cajera1", "nueva123");
            fail("Un usuario desactivado no debe poder entrar");
        } catch (AuthenticationException esperado) {
            // correcto
        }
    }

    @Test(expected = ValidationException.class)
    public void usuarioDuplicadoSeRechaza() throws Exception {
        usuarios.crear("admin", "otro@sucursal.local", "abcdef", "VENDEDOR");
    }

    @Test(expected = ValidationException.class)
    public void adminNoPuedeDesactivarseASiMismo() throws Exception {
        usuarios.cambiarActivo(buscar("admin").getId(), false);
    }

    @Test(expected = ValidationException.class)
    public void vendedorNoAdministraUsuarios() throws Exception {
        auth.authenticate("vendedor", "vendedor123");
        usuarios.crear("intruso", "intruso@sucursal.local", "abcdef", "ADMIN");
    }

    @Test
    public void recuperacionDeEmergenciaReactivaAlAdmin() throws Exception {
        usuarios.crear("admin2", "admin2@sucursal.local", "segundo1", "ADMIN");
        auth.authenticate("admin2", "segundo1");
        usuarios.cambiarActivo(buscar("admin").getId(), false);

        usuarios.restablecerAdminDeEmergencia();

        assertEquals("ADMIN", auth.authenticate("admin", "admin123").getRol());
    }

    @Test
    public void loginSeBloqueaTrasCincoFallosYSeLibera() throws Exception {
        try {
            for (int i = 0; i < 5; i++) {
                try {
                    auth.authenticate("admin", "incorrecta");
                } catch (AuthenticationException esperado) {
                    // correcto
                }
            }
            try {
                auth.authenticate("admin", "admin123");
                fail("Debe estar bloqueado aunque la contraseña sea correcta");
            } catch (AuthenticationException e) {
                assertTrue(e.getMessage().contains("Espere"));
            }
        } finally {
            // Simula que pasaron los 5 minutos para no afectar a las demás pruebas
            Field bloqueo = AuthenticationService.class.getDeclaredField("bloqueadoHasta");
            bloqueo.setAccessible(true);
            bloqueo.setLong(null, 0);
        }
        assertNotNull(auth.authenticate("admin", "admin123"));
    }

    private static Producto crearProducto(String codigo, int stock) throws Exception {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre("Producto " + codigo);
        p.setPrecioCompra(new BigDecimal("5.00"));
        p.setPrecioVenta(new BigDecimal("10.00"));
        p.setStock(stock);
        p.setStockMinimo(0);
        productos.crearProducto(p);
        return p;
    }

    private static Venta vender(Producto p, int cantidad, String metodo) throws Exception {
        DetalleVenta d = new DetalleVenta();
        d.setProductoId(p.getId());
        d.setProductoNombre(p.getNombre());
        d.setCantidad(cantidad);
        d.setPrecioUnitario(p.getPrecioVenta());
        return ventas.registrarVenta(List.of(d), null, metodo, BigDecimal.ZERO, null, null);
    }

    @Test
    public void corteDeCajaSeGuardaConSuDiferencia() throws Exception {
        Producto p = crearProducto("CORTE-1", 10);
        vender(p, 3, "EFECTIVO");
        BigDecimal efectivoDelDia = ventas.totalesPorMetodoPago(java.time.LocalDate.now()).get("EFECTIVO");

        // Faltan 5 pesos: fondo 100 + ventas en efectivo, menos 5
        CorteCaja corte = ventas.registrarCorte(new BigDecimal("100"),
                new BigDecimal("100").add(efectivoDelDia).subtract(new BigDecimal("5")));

        assertEquals(0, new BigDecimal("-5").compareTo(corte.getDiferencia()));
        CorteCaja guardado = ventas.obtenerCortesRecientes(1).get(0);
        assertEquals(corte.getId(), guardado.getId());
        assertEquals("admin", guardado.getUsuarioNombre());
        assertEquals(0, efectivoDelDia.compareTo(guardado.getVentasEfectivo()));
    }

    @Test(expected = ValidationException.class)
    public void corteConMontoNegativoSeRechaza() throws Exception {
        ventas.registrarCorte(BigDecimal.ZERO, new BigDecimal("-1"));
    }

    @Test
    public void productoConHistorialSeDesactivaYSeReactiva() throws Exception {
        Producto p = crearProducto("DESACT-1", 1);
        vender(p, 1, "EFECTIVO");

        assertFalse("con historial no se borra", productos.eliminarProducto(p.getId()));
        assertTrue(productos.obtenerTodos().stream().noneMatch(x -> x.getId().equals(p.getId())));
        assertFalse(productos.buscarPorCodigo("DESACT-1").isActivo());
        try {
            vender(p, 1, "EFECTIVO");
            fail("No se debe vender un producto desactivado");
        } catch (ValidationException esperado) {
            // correcto
        }

        productos.reactivarProducto(p.getId());
        assertTrue(productos.obtenerTodos().stream().anyMatch(x -> x.getId().equals(p.getId())));
    }

    @Test
    public void productoSinHistorialSeBorra() throws Exception {
        Producto p = crearProducto("BORRAR-1", 0);
        assertTrue(productos.eliminarProducto(p.getId()));
        assertNull(productos.buscarPorCodigo("BORRAR-1"));
    }

    @Test
    public void reimprimeTicketDeUnaVentaGuardada() throws Exception {
        Producto p = crearProducto("TICKET-1", 5);
        Venta venta = ventas.obtenerVenta(vender(p, 2, "TARJETA_DEBITO").getId());

        assertEquals("admin", venta.getUsuarioNombre());
        Path copia = TicketPdf.reimpresion(venta, ventas.obtenerDetallesVenta(venta.getId()));
        assertTrue(Files.size(copia) > 0);
    }
}
