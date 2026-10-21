package com.inventario.ui;

import com.inventario.database.ConnectionFactory;
import com.inventario.models.Producto;
import com.inventario.services.ProductoService;
import com.inventario.services.VentaService;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Prueba de punta a punta sobre la interfaz real: un robot llena campos, pulsa botones y
 * contesta diálogos como un cajero (alta de productos, venta, ticket, reimpresión, corte,
 * anulación, usuarios y permisos). Abre ventanas, por eso no corre con el mvn test normal:
 *
 *   mvn test -Dui=true -Dtest=PruebaInterfazVentaTest
 *
 * Usa una base de datos nueva en una carpeta temporal y deja capturas en target/capturas-interfaz.
 */
public class PruebaInterfazVentaTest {

    static Stage principal;
    static final List<String> fallas = new ArrayList<>();
    static Path capturas;
    static int numCaptura = 0;

    @Test
    public void ventaCompletaDesdeLaInterfaz() throws Exception {
        Assume.assumeTrue("Prueba de interfaz: activar con -Dui=true", Boolean.getBoolean("ui"));
        System.setProperty("java.awt.headless", "true"); // AWT solo para guardar PNG; no abrir PDFs
        System.setProperty("inventario.home", Files.createTempDirectory("inventario-ui").toString());
        capturas = Paths.get("target", "capturas-interfaz");
        Files.createDirectories(capturas);
        ConnectionFactory.inicializar();
        CompletableFuture<Void> listo = new CompletableFuture<>();
        Platform.startup(() -> {
            try {
                principal = new Stage();
                principal.setScene(new Scene(new FXMLLoader(PruebaInterfazVentaTest.class.getResource("/fxml/login.fxml")).load(), 600, 500));
                principal.show();
                listo.complete(null);
            } catch (Exception e) {
                listo.completeExceptionally(e);
            }
        });
        listo.get(10, TimeUnit.SECONDS);
        try {
            escenario();
        } finally {
            Platform.exit();
            ConnectionFactory.closeDataSource();
        }
        assertTrue("Fallas en la interfaz:\n  " + String.join("\n  ", fallas), fallas.isEmpty());
    }

    static void escenario() throws Exception {
        ProductoService productos = new ProductoService();
        VentaService ventas = new VentaService();

        paso("1. Login con contraseña incorrecta");
        login("admin", "mal");
        DialogPane d = dialogoOpcional(1500);
        if (d != null) { cerrar(d); }
        verificar(fx(() -> ((Label) nodo("#errorLabel")).getText()).length() > 0 || d != null, "muestra error de login");

        paso("2. Login admin correcto");
        login("admin", "admin123");
        espera(800);
        verificar(nodo("#btnUsuarios") != null && fx(() -> nodo("#btnUsuarios").isVisible()), "admin ve el menú Usuarios");
        captura("menu_admin");

        paso("3. Alta de productos desde Inventario");
        pulsar(nodo("#btnInventario"));
        espera(600);
        crearProducto("CB-001", "Coca-Cola 600 ml", "12", "18", "5", "2");
        crearProducto("SB-001", "Sabritas originales 45 g", "10", "17", "10", "2");
        captura("inventario");

        paso("4. Punto de venta: escanear, cantidades y tope de stock");
        pulsar(nodo("#btnVentas"));
        espera(600);
        escanear("CB-001");
        escanear("CB-001");
        for (int i = 0; i < 3; i++) {
            pulsar(botonEnTabla("+"));
        }
        verificar(cantidadCarrito(0) == 5, "cantidad sube hasta 5 (todo el stock)");
        pulsar(botonEnTabla("+"));
        DialogPane aviso = dialogo();
        verificar(texto(aviso).contains("solo hay 5"), "avisa stock insuficiente al pasar de 5: " + texto(aviso));
        cerrar(aviso);
        pulsar(botonEnTabla("-"));
        pulsar(botonEnTabla("-"));
        escanear("SB-001");
        verificar(cantidadCarrito(0) == 3, "Coca-Cola queda en 3");

        paso("5. Descuento, paga con y cambio");
        escribir("#txtDescuento", "1");
        escribir("#txtPagaCon", "100");
        verificar(etiqueta("#lblSubtotal").equals("$71.00"), "subtotal $71.00 = " + etiqueta("#lblSubtotal"));
        verificar(etiqueta("#lblTotal").equals("$70.00"), "total $70.00 = " + etiqueta("#lblTotal"));
        verificar(etiqueta("#lblCambio").equals("$30.00"), "cambio $30.00 = " + etiqueta("#lblCambio"));
        captura("venta_antes_de_cobrar");

        paso("6. Cobrar e imprimir ticket");
        pulsar(nodo("#btnCobrar"));
        DialogPane cobro = dialogo();
        verificar(texto(cobro).contains("Venta #1") && texto(cobro).contains("$70.00") && texto(cobro).contains("$30.00"),
                "confirmación con total y cambio: " + texto(cobro));
        capturaVentana(cobro, "confirmacion_venta");
        responder(cobro, "Imprimir ticket");
        espera(1500);
        verificar(Files.exists(home().resolve("tickets/ticket_1.pdf")), "se generó tickets/ticket_1.pdf");
        verificar(productos.buscarPorCodigo("CB-001").getStock() == 2, "stock Coca-Cola 5 → 2");
        verificar(productos.buscarPorCodigo("SB-001").getStock() == 9, "stock Sabritas 10 → 9");
        verificar(etiqueta("#lblVentasHoy").equals("1") && etiqueta("#lblTotalHoy").equals("$70.00"),
                "contadores del día: " + etiqueta("#lblVentasHoy") + " / " + etiqueta("#lblTotalHoy"));
        verificar(etiqueta("#lblTotal").equals("$0.00") && etiqueta("#lblCambio").equals("$0.00"), "carrito limpio tras cobrar");

        paso("7. Intentar vender más de lo que queda");
        escanear("CB-001");
        escanear("CB-001");
        escanear("CB-001");
        DialogPane sinStock = dialogo();
        verificar(texto(sinStock).contains("solo hay 2"), "bloquea la 3a pieza: " + texto(sinStock));
        cerrar(sinStock);
        pulsar(boton("Limpiar Carrito"));
        responder(dialogo(), "Aceptar");
        verificar(fx(() -> ((TableView<?>) nodo("#tablaCarrito")).getItems().isEmpty()), "carrito vaciado");

        paso("8. Reimprimir ticket");
        pulsar(boton("Reimprimir ticket"));
        DialogPane reimp = dialogo();
        verificar(fx(() -> ((TextField) reimp.lookup(".text-field")).getText()).equals("1"), "propone la última venta (#1)");
        responder(reimp, "Aceptar");
        espera(1500);
        verificar(Files.exists(home().resolve("tickets/ticket_1_copia.pdf")), "se generó la copia del ticket");
        String copia = new PdfTextExtractor(new PdfReader(home().resolve("tickets/ticket_1_copia.pdf").toString())).getTextFromPage(1);
        verificar(copia.contains("REIMPRESI") && copia.contains("$100.00") && copia.contains("Cambio") && copia.contains("$30.00"),
                "la copia muestra paga con $100.00 y cambio $30.00");

        paso("9. Corte de caja con faltante de $5");
        pulsar(boton("Corte de caja"));
        DialogPane corte = dialogo();
        fx(() -> {
            List<TextField> campos = new ArrayList<>();
            corte.lookupAll(".text-field").forEach(n -> campos.add((TextField) n));
            campos.get(0).setText("500");
            campos.get(1).setText("565");
            return null;
        });
        capturaVentana(corte, "corte_captura");
        responder(corte, "Aceptar");
        DialogPane resultadoCorte = dialogo();
        verificar(texto(resultadoCorte).contains("Corte #1") && texto(resultadoCorte).contains("-5.00")
                && texto(resultadoCorte).contains("faltante"), "corte guardado con faltante: " + texto(resultadoCorte));
        cerrar(resultadoCorte);
        espera(1500);
        verificar(Files.exists(home().resolve("tickets/corte_1.pdf")), "se generó tickets/corte_1.pdf");

        paso("10. Anular la venta desde Reportes → Ventas");
        pulsar(nodo("#btnReportes"));
        espera(500);
        pulsar(nodo("#btnReporteVentas"));
        espera(800);
        fx(() -> { ((TableView<?>) nodo("#tablaVentas")).getSelectionModel().select(0); return null; });
        pulsar(boton("Anular venta seleccionada"));
        responder(dialogo(), "Aceptar");
        DialogPane anulada = dialogo();
        verificar(texto(anulada).contains("anulada"), "confirma anulación: " + texto(anulada));
        cerrar(anulada);
        espera(500);
        captura("reporte_ventas_anulada");
        verificar(productos.buscarPorCodigo("CB-001").getStock() == 5, "Coca-Cola regresa a 5");
        verificar(productos.buscarPorCodigo("SB-001").getStock() == 10, "Sabritas regresa a 10");
        verificar("ANULADA".equals(ventas.obtenerVenta(1L).getEstado()), "venta #1 ANULADA");

        paso("11. Historial de cortes");
        pulsar(boton("Cortes de caja"));
        espera(800);
        verificar(fx(() -> ((TableView<?>) nodo("#tablaCortes")).getItems().size()) == 1, "aparece el corte guardado");
        captura("reporte_cortes");

        paso("12. Usuarios: alta de una cajera");
        pulsar(nodo("#btnUsuarios"));
        espera(600);
        pulsar(boton("+ Nuevo Usuario"));
        DialogPane alta = dialogo();
        fx(() -> {
            List<TextField> campos = new ArrayList<>();
            alta.lookupAll(".text-field").forEach(n -> campos.add((TextField) n));
            campos.get(0).setText("maria");
            campos.get(1).setText("maria@sucursal.local");
            campos.get(2).setText("maria123");
            return null;
        });
        responder(alta, "Aceptar");
        cerrar(dialogo());
        verificar(fx(() -> ((TableView<?>) nodo("#tablaUsuarios")).getItems().size()) == 3, "3 usuarios en la tabla");
        captura("usuarios");

        paso("13. Cerrar sesión y entrar como la cajera nueva");
        pulsar(nodo("#logoutBtn"));
        responder(dialogo(), "Aceptar");
        espera(600);
        login("maria", "maria123");
        espera(800);
        verificar(!fx(() -> nodo("#btnInventario").isVisible()) && !fx(() -> nodo("#btnReportes").isVisible())
                && !fx(() -> nodo("#btnUsuarios").isVisible()) && fx(() -> nodo("#btnVentas").isVisible()),
                "vendedor solo ve Punto de Venta y Clientes");
        captura("menu_vendedor");

        paso("14. La cajera vende y hace su corte");
        pulsar(nodo("#btnVentas"));
        espera(600);
        escanear("SB-001");
        fx(() -> { ((ComboBox<String>) nodo("#cmbMetodoPago")).setValue("TARJETA_DEBITO"); return null; });
        pulsar(nodo("#btnCobrar"));
        DialogPane cobro2 = dialogo();
        verificar(texto(cobro2).contains("Venta #2"), "venta #2 registrada: " + texto(cobro2));
        responder(cobro2, "Cerrar");
        verificar("maria".equals(ventas.obtenerVenta(2L).getUsuarioNombre()), "la venta queda a nombre de maria");
        verificar(productos.buscarPorCodigo("SB-001").getStock() == 9, "Sabritas 10 → 9");
    }

    // ---------- acciones ----------

    static void login(String usuario, String password) throws Exception {
        fx(() -> {
            ((TextField) nodo("#usernameField")).setText(usuario);
            ((PasswordField) nodo("#passwordField")).setText(password);
            return null;
        });
        pulsar(nodo("#loginBtn"));
    }

    static void crearProducto(String codigo, String nombre, String compra, String venta, String stock, String minimo) throws Exception {
        pulsar(boton("+ Nuevo Producto"));
        Stage dlg = esperarVentanaCon("#txtCodigo");
        fx(() -> {
            Scene s = dlg.getScene();
            ((TextField) s.lookup("#txtCodigo")).setText(codigo);
            ((TextField) s.lookup("#txtNombre")).setText(nombre);
            ((TextField) s.lookup("#txtPrecioCompra")).setText(compra);
            ((TextField) s.lookup("#txtPrecioVenta")).setText(venta);
            ((TextField) s.lookup("#txtStock")).setText(stock);
            ((TextField) s.lookup("#txtStockMinimo")).setText(minimo);
            ComboBox<?> cat = (ComboBox<?>) s.lookup("#cmbCategoria");
            if (!cat.getItems().isEmpty()) cat.getSelectionModel().select(0);
            return null;
        });
        disparar(() -> ((Button) dlg.getScene().lookup("#btnGuardar")).fire());
        DialogPane ok = dialogo();
        verificar(texto(ok).contains("creado"), "producto " + codigo + " creado: " + texto(ok));
        cerrar(ok);
    }

    static void escanear(String codigo) throws Exception {
        fx(() -> { ((TextField) nodo("#txtCodigoProducto")).setText(codigo); return null; });
        disparar(() -> nodo("#txtCodigoProducto").fireEvent(new ActionEvent()));
    }

    static void escribir(String id, String valor) throws Exception {
        fx(() -> {
            TextField t = (TextField) nodo(id);
            t.setText(valor);
            t.fireEvent(new KeyEvent(KeyEvent.KEY_RELEASED, "", "", KeyCode.DIGIT0, false, false, false, false));
            return null;
        });
    }

    static int cantidadCarrito(int fila) throws Exception {
        return fx(() -> {
            TableView<?> t = (TableView<?>) nodo("#tablaCarrito");
            return ((com.inventario.models.ItemVenta) t.getItems().get(fila)).getCantidad();
        });
    }

    static String etiqueta(String id) throws Exception {
        return fx(() -> ((Label) nodo(id)).getText());
    }

    // ---------- utilidades JavaFX ----------

    static Node nodo(String selector) {
        return principal.getScene().lookup(selector);
    }

    static Button boton(String texto) throws Exception {
        return fx(() -> (Button) principal.getScene().getRoot().lookupAll(".button").stream()
                .filter(n -> ((Button) n).getText() != null && ((Button) n).getText().contains(texto)).findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay botón '" + texto + "'")));
    }

    static Button botonEnTabla(String texto) throws Exception {
        return fx(() -> (Button) nodo("#tablaCarrito").lookupAll(".button").stream()
                .filter(n -> texto.equals(((Button) n).getText()) && n.isVisible()).findFirst()
                .orElseThrow(() -> new IllegalStateException("No hay botón '" + texto + "' en el carrito")));
    }

    static void pulsar(Node boton) throws Exception {
        disparar(() -> ((ButtonBase) boton).fire());
    }

    /** Ejecuta sin esperar (la acción puede abrir un diálogo modal que bloquea). */
    static void disparar(Runnable r) throws Exception {
        Platform.runLater(r);
        espera(350);
    }

    static <T> T fx(Callable<T> c) throws Exception {
        CompletableFuture<T> f = new CompletableFuture<>();
        Platform.runLater(() -> {
            try { f.complete(c.call()); } catch (Throwable t) { f.completeExceptionally(t); }
        });
        return f.get(10, TimeUnit.SECONDS);
    }

    static DialogPane dialogo() throws Exception {
        DialogPane d = dialogoOpcional(8000);
        if (d == null) throw new IllegalStateException("Se esperaba un diálogo y no apareció");
        return d;
    }

    static DialogPane dialogoOpcional(long ms) throws Exception {
        long fin = System.currentTimeMillis() + ms;
        while (System.currentTimeMillis() < fin) {
            DialogPane d = fx(() -> Window.getWindows().stream()
                    .filter(w -> w.isShowing() && w.getScene() != null && w.getScene().getRoot() instanceof DialogPane)
                    .map(w -> (DialogPane) w.getScene().getRoot()).reduce((a, b) -> b).orElse(null));
            if (d != null) return d;
            espera(150);
        }
        return null;
    }

    static Stage esperarVentanaCon(String selector) throws Exception {
        for (int i = 0; i < 50; i++) {
            Stage s = fx(() -> (Stage) Window.getWindows().stream()
                    .filter(w -> w != principal && w.isShowing() && w.getScene() != null && w.getScene().lookup(selector) != null)
                    .findFirst().orElse(null));
            if (s != null) return s;
            espera(150);
        }
        throw new IllegalStateException("No apareció la ventana con " + selector);
    }

    static String texto(DialogPane d) throws Exception {
        return fx(() -> (d.getHeaderText() == null ? "" : d.getHeaderText() + " | ") + d.getContentText());
    }

    static void cerrar(DialogPane d) throws Exception {
        disparar(() -> {
            ButtonType bt = d.getButtonTypes().get(0);
            ((Button) d.lookupButton(bt)).fire();
        });
    }

    static void responder(DialogPane d, String textoBoton) throws Exception {
        disparar(() -> {
            ButtonType bt = d.getButtonTypes().stream().filter(b -> b.getText().equals(textoBoton)).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Sin botón " + textoBoton + " en " + d.getButtonTypes()));
            ((Button) d.lookupButton(bt)).fire();
        });
    }

    static void captura(String nombre) throws Exception {
        guardar(fx(() -> principal.getScene().snapshot(null)), nombre);
    }

    static void capturaVentana(DialogPane d, String nombre) throws Exception {
        guardar(fx(() -> d.getScene().snapshot(null)), nombre);
    }

    static void guardar(WritableImage img, String nombre) throws Exception {
        int w = (int) img.getWidth(), h = (int) img.getHeight();
        BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        PixelReader pr = img.getPixelReader();
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) bi.setRGB(x, y, pr.getArgb(x, y));
        File f = capturas.resolve(String.format("%02d_%s.png", ++numCaptura, nombre)).toFile();
        ImageIO.write(bi, "png", f);
    }

    static Path home() {
        return com.inventario.config.AppPaths.HOME;
    }

    static void paso(String nombre) {
        System.out.println("\n>> " + nombre);
    }

    static void verificar(boolean condicion, String descripcion) {
        System.out.println((condicion ? "   OK    " : "   FALLA ") + descripcion);
        if (!condicion) fallas.add(descripcion);
    }

    static void espera(long ms) throws InterruptedException {
        Thread.sleep(ms);
    }
}
