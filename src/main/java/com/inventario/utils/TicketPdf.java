package com.inventario.utils;

import com.inventario.config.AppPaths;
import com.inventario.models.CorteCaja;
import com.inventario.models.DetalleVenta;
import com.inventario.models.Venta;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Genera el ticket de venta y el corte de caja como PDF de 80 mm (impresora de tickets)
 * en la carpeta tickets y los abre con el visor predeterminado para imprimirlos.
 * Los datos del encabezado se toman de negocio.properties.
 */
public final class TicketPdf {

    private static final Logger logger = LoggerFactory.getLogger(TicketPdf.class);
    private static final float ANCHO = 226f; // 80 mm en puntos
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String NEGOCIO_POR_DEFECTO =
            "# Datos que aparecen en el encabezado de tickets y cortes de caja.\n" +
            "# Edita este archivo con el Bloc de notas y guarda como UTF-8.\n" +
            "nombre=Mi Negocio\n" +
            "direccion=\n" +
            "telefono=\n" +
            "rfc=\n" +
            "mensaje=¡Gracias por su compra!\n";

    private TicketPdf() {
    }

    public static Path ticketVenta(Venta venta, List<DetalleVenta> detalles, String cajero) throws IOException {
        return ticket(venta, detalles, cajero, false);
    }

    /** Copia de un ticket ya emitido, con el mismo pago y cambio que el original. */
    public static Path reimpresion(Venta venta, List<DetalleVenta> detalles) throws IOException {
        return ticket(venta, detalles, venta.getUsuarioNombre(), true);
    }

    private static Path ticket(Venta venta, List<DetalleVenta> detalles, String cajero,
                               boolean reimpresion) throws IOException {
        BigDecimal cambio = venta.getCambio();
        List<String> lineas = new ArrayList<>();
        if (reimpresion) {
            lineas.add("*** REIMPRESIÓN ***");
        }
        if ("ANULADA".equals(venta.getEstado())) {
            lineas.add("*** VENTA ANULADA ***");
        }
        lineas.add("Ticket #" + venta.getId());
        lineas.add("Fecha: " + (venta.getFechaCreacion() != null ? venta.getFechaCreacion() : LocalDateTime.now()).format(FECHA));
        lineas.add("Atendió: " + cajero);
        if (venta.getClienteNombre() != null) {
            lineas.add("Cliente: " + venta.getClienteNombre());
        }
        lineas.add(separador());
        for (DetalleVenta d : detalles) {
            lineas.add(d.getProductoNombre());
            lineas.add(columnas("  " + d.getCantidad() + " x " + dinero(d.getPrecioUnitario()), dinero(d.getSubtotal())));
        }
        lineas.add(separador());
        lineas.add(columnas("Subtotal", dinero(venta.getSubtotal())));
        if (venta.getDescuento() != null && venta.getDescuento().signum() > 0) {
            lineas.add(columnas("Descuento", "-" + dinero(venta.getDescuento())));
        }
        lineas.add(columnas("TOTAL", dinero(venta.getTotal())));
        lineas.add(columnas("Pago (" + venta.getMetodoPago() + ")", dinero(venta.getMontoRecibido() != null ? venta.getMontoRecibido() : venta.getTotal())));
        if (cambio != null && cambio.signum() > 0) {
            lineas.add(columnas("Cambio", dinero(cambio)));
        }
        return escribir("ticket_" + venta.getId() + (reimpresion ? "_copia" : "") + ".pdf", lineas, true);
    }

    public static Path corteCaja(CorteCaja corte, Map<String, BigDecimal> porMetodo) throws IOException {
        List<String> lineas = new ArrayList<>();
        lineas.add("CORTE DE CAJA #" + corte.getId());
        lineas.add("Día: " + corte.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        lineas.add("Impreso: " + LocalDateTime.now().format(FECHA));
        lineas.add("Usuario: " + corte.getUsuarioNombre());
        lineas.add(separador());
        lineas.add(columnas("Ventas", String.valueOf(corte.getNumeroVentas())));
        porMetodo.forEach((metodo, monto) -> lineas.add(columnas(metodo, dinero(monto))));
        lineas.add(columnas("TOTAL VENDIDO", dinero(corte.getTotalVendido())));
        lineas.add(separador());
        lineas.add(columnas("Fondo inicial", dinero(corte.getFondoInicial())));
        lineas.add(columnas("Efectivo esperado", dinero(corte.getFondoInicial().add(corte.getVentasEfectivo()))));
        lineas.add(columnas("Efectivo contado", dinero(corte.getEfectivoContado())));
        lineas.add(columnas("Diferencia", dinero(corte.getDiferencia())));
        lineas.add("");
        lineas.add("Firma: ______________________");
        return escribir("corte_" + corte.getId() + ".pdf", lineas, false);
    }

    /** Abre el PDF con el programa predeterminado (en segundo plano para no congelar la ventana). */
    public static void abrir(Path pdf) {
        new Thread(() -> {
            try {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdf.toFile());
                }
            } catch (IOException e) {
                logger.error("No se pudo abrir {}", pdf, e);
            }
        }, "abrir-pdf").start();
    }

    private static Path escribir(String archivo, List<String> lineas, boolean conMensaje) throws IOException {
        Properties negocio = leerNegocio();
        Path destino = AppPaths.carpeta(AppPaths.TICKETS).resolve(archivo);

        Font titulo = FontFactory.getFont(FontFactory.COURIER_BOLD, 10);
        Font normal = FontFactory.getFont(FontFactory.COURIER, 7.5f);
        float alto = 120 + 11 * (lineas.size() + 6);
        Document doc = new Document(new Rectangle(ANCHO, alto), 8, 8, 10, 10);
        try (OutputStream out = Files.newOutputStream(destino)) {
            PdfWriter.getInstance(doc, out);
            doc.open();
            agregar(doc, negocio.getProperty("nombre", "Mi Negocio"), titulo, Element.ALIGN_CENTER);
            for (String clave : new String[]{"direccion", "telefono", "rfc"}) {
                String valor = negocio.getProperty(clave, "").trim();
                if (!valor.isEmpty()) {
                    agregar(doc, ("rfc".equals(clave) ? "RFC: " : "") + valor, normal, Element.ALIGN_CENTER);
                }
            }
            agregar(doc, separador(), normal, Element.ALIGN_LEFT);
            for (String linea : lineas) {
                agregar(doc, linea, normal, Element.ALIGN_LEFT);
            }
            String mensaje = negocio.getProperty("mensaje", "").trim();
            if (conMensaje && !mensaje.isEmpty()) {
                agregar(doc, " ", normal, Element.ALIGN_CENTER);
                agregar(doc, mensaje, normal, Element.ALIGN_CENTER);
            }
            doc.close();
        } catch (DocumentException e) {
            throw new IOException("Error generando PDF", e);
        }
        logger.info("PDF generado: {}", destino);
        return destino;
    }

    /** Crea negocio.properties con valores genéricos si no existe, para que el dueño lo edite. */
    public static void prepararNegocio() throws IOException {
        if (!Files.exists(AppPaths.NEGOCIO)) {
            AppPaths.carpeta(AppPaths.HOME);
            Files.writeString(AppPaths.NEGOCIO, NEGOCIO_POR_DEFECTO, StandardCharsets.UTF_8);
        }
    }

    private static Properties leerNegocio() throws IOException {
        prepararNegocio();
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(AppPaths.NEGOCIO);
             var reader = new java.io.InputStreamReader(in, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    private static void agregar(Document doc, String texto, Font fuente, int alineacion) throws DocumentException {
        Paragraph p = new Paragraph(texto, fuente);
        p.setAlignment(alineacion);
        p.setLeading(fuente.getSize() + 2.5f);
        doc.add(p);
    }

    /** Texto a la izquierda y monto a la derecha en un renglón de 46 caracteres (Courier 7.5 en 80 mm). */
    private static String columnas(String izquierda, String derecha) {
        int espacios = Math.max(1, 46 - izquierda.length() - derecha.length());
        return izquierda + " ".repeat(espacios) + derecha;
    }

    private static String separador() {
        return "-".repeat(46);
    }

    private static String dinero(BigDecimal monto) {
        return String.format("$%,.2f", monto != null ? monto : BigDecimal.ZERO);
    }
}
