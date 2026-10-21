package com.inventario.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.inventario.config.AppInfo;
import java.awt.Color;
import com.inventario.models.Producto;
import com.inventario.models.Venta;
import com.inventario.models.MovimientoInventario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class PdfReportExporter {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfReportExporter.class);
    
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL);
    private static final Font SUMMARY_FONT = new Font(Font.HELVETICA, 10, Font.BOLD);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC);
    
    private static final Color HEADER_COLOR = new Color(52, 73, 94);
    private static final Color ALT_ROW_COLOR = new Color(236, 240, 241);
    
    private static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public static String exportVentas(String titulo, String periodo, List<Venta> ventas,
                                      BigDecimal totalVentas, int cantidadVentas, BigDecimal promedioVenta,
                                      String filePath) throws IOException, DocumentException {
        
        Document document = new Document(PageSize.A4, 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        
        addHeader(document, titulo, periodo);
        addVentasSummary(document, totalVentas, cantidadVentas, promedioVenta);
        addVentasTable(document, ventas);
        addFooter(document);
        
        document.close();
        return filePath;
    }
    
    public static String exportInventario(String titulo, List<Producto> productos,
                                          int totalProductos, int stockTotal, BigDecimal valorInventario, long stockBajo,
                                          String filePath) throws IOException, DocumentException {
        
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        
        addHeader(document, titulo, "Inventario Actual");
        addInventarioSummary(document, totalProductos, stockTotal, valorInventario, stockBajo);
        addInventarioTable(document, productos);
        addFooter(document);
        
        document.close();
        return filePath;
    }
    
    public static String exportMovimientos(String titulo, String periodo, List<MovimientoInventario> movimientos,
                                           int totalEntradas, int totalSalidas, int totalAjustes,
                                           String filePath) throws IOException, DocumentException {
        
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        
        addHeader(document, titulo, periodo);
        addMovimientosSummary(document, totalEntradas, totalSalidas, totalAjustes);
        addMovimientosTable(document, movimientos);
        addFooter(document);
        
        document.close();
        return filePath;
    }
    
    public static String exportGanancias(String titulo, String periodo, List<GananciasRow> ventas,
                                         BigDecimal totalVentas, BigDecimal costoTotal, BigDecimal gananciaBruta, BigDecimal margen,
                                         String filePath) throws IOException, DocumentException {
        
        Document document = new Document(PageSize.A4.rotate(), 40, 40, 50, 50);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();
        
        addHeader(document, titulo, periodo);
        addGananciasSummary(document, totalVentas, costoTotal, gananciaBruta, margen);
        addGananciasTable(document, ventas);
        addFooter(document);
        
        document.close();
        return filePath;
    }
    
    private static void addHeader(Document document, String titulo, String subtitulo) throws DocumentException {
        Paragraph title = new Paragraph(titulo, TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(5);
        document.add(title);
        
        Paragraph subtitle = new Paragraph(subtitulo, SUBTITLE_FONT);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(3);
        document.add(subtitle);
        
        Paragraph fechaGen = new Paragraph("Generado: " + LocalDateTime.now().format(dateTimeFormatter), SUBTITLE_FONT);
        fechaGen.setAlignment(Element.ALIGN_CENTER);
        fechaGen.setSpacingAfter(15);
        document.add(fechaGen);
        
        document.add(new Paragraph(" ", SUBTITLE_FONT));
    }
    
    private static void addVentasSummary(Document document, BigDecimal totalVentas, 
                                         int cantidadVentas, BigDecimal promedioVenta) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(3);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(15);
        
        addSummaryCell(summaryTable, "TOTAL VENTAS", formatCurrency(totalVentas), new Color(39, 174, 96));
        addSummaryCell(summaryTable, "TRANSACCIONES", String.valueOf(cantidadVentas), new Color(52, 152, 219));
        addSummaryCell(summaryTable, "PROMEDIO", formatCurrency(promedioVenta), new Color(155, 89, 182));
        
        document.add(summaryTable);
    }
    
    private static void addInventarioSummary(Document document, int totalProductos, int stockTotal,
                                             BigDecimal valorInventario, long stockBajo) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(15);
        
        addSummaryCell(summaryTable, "TOTAL PRODUCTOS", String.valueOf(totalProductos), new Color(52, 152, 219));
        addSummaryCell(summaryTable, "STOCK TOTAL", String.format("%,d", stockTotal), new Color(39, 174, 96));
        addSummaryCell(summaryTable, "VALOR INVENTARIO", formatCurrency(valorInventario), new Color(230, 126, 34));
        addSummaryCell(summaryTable, "STOCK BAJO", String.valueOf(stockBajo), new Color(231, 76, 60));
        
        document.add(summaryTable);
    }
    
    private static void addMovimientosSummary(Document document, int totalEntradas, 
                                               int totalSalidas, int totalAjustes) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(15);
        
        addSummaryCell(summaryTable, "ENTRADAS", String.format("%,d", totalEntradas), new Color(39, 174, 96));
        addSummaryCell(summaryTable, "SALIDAS", String.format("%,d", totalSalidas), new Color(231, 76, 60));
        addSummaryCell(summaryTable, "AJUSTES", String.format("%,d", totalAjustes), new Color(52, 152, 219));
        addSummaryCell(summaryTable, "TOTAL MOV.", String.format("%,d", totalEntradas + totalSalidas + totalAjustes), new Color(155, 89, 182));
        
        document.add(summaryTable);
    }
    
    private static void addGananciasSummary(Document document, BigDecimal totalVentas,
                                             BigDecimal costoTotal, BigDecimal gananciaBruta, BigDecimal margen) throws DocumentException {
        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(15);
        
        addSummaryCell(summaryTable, "TOTAL VENTAS", formatCurrency(totalVentas), new Color(52, 152, 219));
        addSummaryCell(summaryTable, "COSTO TOTAL", formatCurrency(costoTotal), new Color(231, 76, 60));
        addSummaryCell(summaryTable, "GANANCIA BRUTA", formatCurrency(gananciaBruta), new Color(39, 174, 96));
        addSummaryCell(summaryTable, "MARGEN", String.format("%.2f%%", margen), new Color(155, 89, 182));
        
        document.add(summaryTable);
    }
    
    private static void addSummaryCell(PdfPTable table, String label, String value, Color color) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(color);
        cell.setPadding(12);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "\n", HEADER_FONT));
        p.add(new Chunk(value, new Font(Font.HELVETICA, 14, Font.BOLD)));
        
        cell.addElement(p);
        table.addCell(cell);
    }
    
    private static void addVentasTable(Document document, List<Venta> ventas) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        // Estado más ancho para que "COMPLETADA" quepa en un renglón
        float[] widths = {0.6f, 1.5f, 1.9f, 1.1f, 1.2f, 1.4f};
        table.setWidths(widths);
        
        addTableHeader(table, new String[]{"N°", "Fecha y Hora", "Cliente", "Monto", "Método Pago", "Estado"});
        
        int i = 0;
        for (Venta venta : ventas) {
            Color rowColor = (i % 2 == 0) ? Color.WHITE : ALT_ROW_COLOR;
            
            addTableCell(table, String.valueOf(venta.getId()), rowColor);
            addTableCell(table, formatDateTime(venta.getFechaCreacion()), rowColor);
            addTableCell(table, venta.getClienteNombre() != null ? venta.getClienteNombre() : "Cliente General", rowColor);
            addTableCell(table, formatCurrency(venta.getTotal()), rowColor);
            addTableCell(table, venta.getMetodoPago() != null ? venta.getMetodoPago() : "-", rowColor);
            addTableCell(table, venta.getEstado() != null ? venta.getEstado() : "-", rowColor);
            
            i++;
        }
        
        document.add(table);
    }
    
    private static void addInventarioTable(Document document, List<Producto> productos) throws DocumentException {
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        float[] widths = {1f, 2.5f, 1.5f, 1f, 1f, 0.7f, 0.8f, 0.8f};
        table.setWidths(widths);
        
        addTableHeader(table, new String[]{"Código", "Producto", "Categoría", "P. Compra", "P. Venta", "Stock", "Stock Mín.", "Estado"});
        
        int i = 0;
        for (Producto producto : productos) {
            Color rowColor = (i % 2 == 0) ? Color.WHITE : ALT_ROW_COLOR;
            
            addTableCell(table, producto.getCodigo() != null ? producto.getCodigo() : "-", rowColor);
            addTableCell(table, producto.getNombre() != null ? producto.getNombre() : "-", rowColor);
            addTableCell(table, producto.getCategoriaNombre() != null ? producto.getCategoriaNombre() : "-", rowColor);
            addTableCell(table, producto.getPrecioCompra() != null ? formatCurrency(producto.getPrecioCompra()) : "-", rowColor);
            addTableCell(table, producto.getPrecioVenta() != null ? formatCurrency(producto.getPrecioVenta()) : "-", rowColor);
            addTableCell(table, String.valueOf(producto.getStock() != null ? producto.getStock() : 0), rowColor);
            addTableCell(table, String.valueOf(producto.getStockMinimo() != null ? producto.getStockMinimo() : 0), rowColor);
            addTableCell(table, producto.isStockBajo() ? "BAJO" : "OK", rowColor);
            
            i++;
        }
        
        document.add(table);
    }
    
    private static void addMovimientosTable(Document document, List<MovimientoInventario> movimientos) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        float[] widths = {1.5f, 2.5f, 1f, 0.8f, 2.5f, 1.2f};
        table.setWidths(widths);
        
        addTableHeader(table, new String[]{"Fecha y Hora", "Producto", "Tipo", "Cantidad", "Descripción", "Usuario"});
        
        int i = 0;
        for (MovimientoInventario mov : movimientos) {
            Color rowColor = (i % 2 == 0) ? Color.WHITE : ALT_ROW_COLOR;
            
            addTableCell(table, formatDateTime(mov.getFechaCreacion()), rowColor);
            addTableCell(table, mov.getProductoNombre() != null ? mov.getProductoNombre() : "-", rowColor);
            addTableCell(table, mov.getTipo() != null ? mov.getTipo().name() : "-", rowColor);
            addTableCell(table, String.valueOf(mov.getCantidad() != null ? mov.getCantidad() : 0), rowColor);
            addTableCell(table, mov.getDescripcion() != null ? mov.getDescripcion() : "-", rowColor);
            addTableCell(table, mov.getUsuarioNombre() != null ? mov.getUsuarioNombre() : "-", rowColor);
            
            i++;
        }
        
        document.add(table);
    }
    
    private static void addGananciasTable(Document document, List<GananciasRow> ventas) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        
        float[] widths = {0.6f, 1f, 2f, 1.2f, 1.2f, 1.2f};
        table.setWidths(widths);
        
        addTableHeader(table, new String[]{"N°", "Fecha", "Cliente", "Total Venta", "Costo", "Ganancia"});
        
        int i = 0;
        for (GananciasRow row : ventas) {
            Color rowColor = (i % 2 == 0) ? Color.WHITE : ALT_ROW_COLOR;
            
            addTableCell(table, String.valueOf(row.id), rowColor);
            addTableCell(table, row.fecha != null ? row.fecha.format(dateFormatter) : "-", rowColor);
            addTableCell(table, row.cliente != null ? row.cliente : "Cliente General", rowColor);
            addTableCell(table, formatCurrency(row.total), rowColor);
            addTableCell(table, formatCurrency(row.costo), rowColor);
            addTableCell(table, formatCurrency(row.ganancia), rowColor);
            
            i++;
        }
        
        document.add(table);
    }
    
    private static void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(HEADER_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }
    
    private static void addTableCell(PdfPTable table, String text, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "-", CELL_FONT));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(cell);
    }
    
    private static void addFooter(Document document) throws DocumentException {
        Paragraph footer = new Paragraph();
        footer.setSpacingBefore(20);
        footer.add(new Chunk(AppInfo.NOMBRE + " · " + AppInfo.COPYRIGHT, FOOTER_FONT));
        
        document.add(footer);
    }
    
    private static String formatCurrency(BigDecimal value) {
        if (value == null) return "$0.00";
        return String.format("$%,.2f", value);
    }
    
    private static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return dateTime.format(dateTimeFormatter);
    }
    
    public static String generateFileName(String reportType, LocalDate desde, LocalDate hasta) {
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if (desde != null && hasta != null) {
            return reportType + "_" + desde.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + 
                   "_a_" + hasta.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "_" + dateStr + ".pdf";
        }
        return reportType + "_" + dateStr + ".pdf";
    }
    
    public static class GananciasRow {
        public Long id;
        public LocalDate fecha;
        public String cliente;
        public BigDecimal total;
        public BigDecimal costo;
        public BigDecimal ganancia;
        
        public GananciasRow(Long id, LocalDate fecha, String cliente, BigDecimal total, BigDecimal costo, BigDecimal ganancia) {
            this.id = id;
            this.fecha = fecha;
            this.cliente = cliente;
            this.total = total;
            this.costo = costo;
            this.ganancia = ganancia;
        }
    }
}
