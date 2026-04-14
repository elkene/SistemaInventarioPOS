package com.inventario.controllers;

import com.inventario.utils.AlertUtil;
import com.inventario.utils.PdfReportExporter;
import com.inventario.models.Venta;
import com.inventario.models.Producto;
import com.inventario.models.MovimientoInventario;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfPreviewController {
    
    private static final Logger logger = LoggerFactory.getLogger(PdfPreviewController.class);
    
    @FXML private Label lblTitulo;
    @FXML private Label lblSubtitulo;
    @FXML private Label lblFechaGeneracion;
    @FXML private TextArea txtResumen;
    @FXML private Button btnGuardarPdf;
    @FXML private Button btnCancelar;
    @FXML private BorderPane rootPane;
    
    private Stage dialogStage;
    private Stage ownerStage;
    
    public static void mostrarVentas(Stage parent, List<Venta> ventas, BigDecimal totalVentas,
                                     int cantidadVentas, BigDecimal promedio, LocalDate desde, LocalDate hasta) {
        try {
            FXMLLoader loader = new FXMLLoader(PdfPreviewController.class.getResource("/fxml/pdf_preview.fxml"));
            Parent root = loader.load();
            PdfPreviewController controller = loader.getController();
            
            controller.configurar(parent, "REPORTE DE VENTAS", desde, hasta);
            
            StringBuilder resumen = new StringBuilder();
            resumen.append("RESUMEN DEL REPORTE\n");
            resumen.append("═══════════════════════════════════════\n\n");
            resumen.append("Período: ").append(desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                   .append(" al ").append(hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
            resumen.append("• Total Ventas: $").append(String.format("%,.2f", totalVentas)).append("\n");
            resumen.append("• Transacciones: ").append(cantidadVentas).append("\n");
            resumen.append("• Promedio por venta: $").append(String.format("%,.2f", promedio)).append("\n\n");
            resumen.append("• Registros en detalle: ").append(ventas.size()).append("\n");
            controller.txtResumen.setText(resumen.toString());
            
            controller.btnGuardarPdf.setOnAction(e -> {
                controller.guardarPdfVentas(ventas, totalVentas, cantidadVentas, promedio, desde, hasta);
            });
            
            controller.dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error mostrando previsualización", e);
            AlertUtil.showError("Error", "Error al mostrar previsualización: " + e.getMessage());
        }
    }
    
    public static void mostrarInventario(Stage parent, List<Producto> productos, int totalProductos,
                                         int stockTotal, BigDecimal valorInventario, long stockBajo) {
        try {
            FXMLLoader loader = new FXMLLoader(PdfPreviewController.class.getResource("/fxml/pdf_preview.fxml"));
            Parent root = loader.load();
            PdfPreviewController controller = loader.getController();
            
            controller.configurar(parent, "REPORTE DE INVENTARIO", null, null);
            controller.lblSubtitulo.setText("Inventario Actual");
            
            StringBuilder resumen = new StringBuilder();
            resumen.append("RESUMEN DEL REPORTE\n");
            resumen.append("═══════════════════════════════════════\n\n");
            resumen.append("• Total Productos: ").append(totalProductos).append("\n");
            resumen.append("• Stock Total: ").append(String.format("%,d", stockTotal)).append(" unidades\n");
            resumen.append("• Valor del Inventario: $").append(String.format("%,.2f", valorInventario)).append("\n");
            resumen.append("• Productos con Stock Bajo: ").append(stockBajo).append("\n\n");
            resumen.append("• Registros en detalle: ").append(productos.size()).append("\n");
            controller.txtResumen.setText(resumen.toString());
            
            controller.btnGuardarPdf.setOnAction(e -> {
                controller.guardarPdfInventario(productos, totalProductos, stockTotal, valorInventario, stockBajo);
            });
            
            controller.dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error mostrando previsualización", e);
            AlertUtil.showError("Error", "Error al mostrar previsualización: " + e.getMessage());
        }
    }
    
    public static void mostrarMovimientos(Stage parent, List<MovimientoInventario> movimientos,
                                          int totalEntradas, int totalSalidas, int totalAjustes,
                                          LocalDate desde, LocalDate hasta) {
        try {
            FXMLLoader loader = new FXMLLoader(PdfPreviewController.class.getResource("/fxml/pdf_preview.fxml"));
            Parent root = loader.load();
            PdfPreviewController controller = loader.getController();
            
            controller.configurar(parent, "REPORTE DE MOVIMIENTOS", desde, hasta);
            
            StringBuilder resumen = new StringBuilder();
            resumen.append("RESUMEN DEL REPORTE\n");
            resumen.append("═══════════════════════════════════════\n\n");
            resumen.append("Período: ").append(desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                   .append(" al ").append(hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
            resumen.append("• Entradas: ").append(String.format("%,d", totalEntradas)).append(" unidades\n");
            resumen.append("• Salidas: ").append(String.format("%,d", totalSalidas)).append(" unidades\n");
            resumen.append("• Ajustes: ").append(String.format("%,d", totalAjustes)).append(" unidades\n");
            resumen.append("• Total Movimientos: ").append(movimientos.size()).append("\n");
            controller.txtResumen.setText(resumen.toString());
            
            controller.btnGuardarPdf.setOnAction(e -> {
                controller.guardarPdfMovimientos(movimientos, totalEntradas, totalSalidas, totalAjustes, desde, hasta);
            });
            
            controller.dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error mostrando previsualización", e);
            AlertUtil.showError("Error", "Error al mostrar previsualización: " + e.getMessage());
        }
    }
    
    public static void mostrarGanancias(Stage parent, List<PdfReportExporter.GananciasRow> ventas,
                                        BigDecimal totalVentas, BigDecimal costoTotal, 
                                        BigDecimal gananciaBruta, BigDecimal margen,
                                        LocalDate desde, LocalDate hasta) {
        try {
            FXMLLoader loader = new FXMLLoader(PdfPreviewController.class.getResource("/fxml/pdf_preview.fxml"));
            Parent root = loader.load();
            PdfPreviewController controller = loader.getController();
            
            controller.configurar(parent, "REPORTE DE GANANCIAS", desde, hasta);
            
            StringBuilder resumen = new StringBuilder();
            resumen.append("RESUMEN DEL REPORTE\n");
            resumen.append("═══════════════════════════════════════\n\n");
            resumen.append("Período: ").append(desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                   .append(" al ").append(hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n\n");
            resumen.append("• Total Ventas: $").append(String.format("%,.2f", totalVentas)).append("\n");
            resumen.append("• Costo Total: $").append(String.format("%,.2f", costoTotal)).append("\n");
            resumen.append("• Ganancia Bruta: $").append(String.format("%,.2f", gananciaBruta)).append("\n");
            resumen.append("• Margen de Ganancia: ").append(String.format("%.2f%%", margen)).append("\n\n");
            resumen.append("• Ventas detalladas: ").append(ventas.size()).append("\n");
            controller.txtResumen.setText(resumen.toString());
            
            controller.btnGuardarPdf.setOnAction(e -> {
                controller.guardarPdfGanancias(ventas, totalVentas, costoTotal, gananciaBruta, margen, desde, hasta);
            });
            
            controller.dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error mostrando previsualización", e);
            AlertUtil.showError("Error", "Error al mostrar previsualización: " + e.getMessage());
        }
    }
    
    private void configurar(Stage parent, String titulo, LocalDate desde, LocalDate hasta) {
        this.ownerStage = parent;
        
        this.lblTitulo.setText(titulo);
        
        if (desde != null && hasta != null) {
            this.lblSubtitulo.setText("Período: " + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                                     " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        }
        
        this.lblFechaGeneracion.setText("Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        this.txtResumen.setEditable(false);
        
        this.btnCancelar.setOnAction(e -> dialogStage.close());
        
        dialogStage = new Stage();
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(parent);
        dialogStage.setTitle("Previsualización - " + titulo);
        dialogStage.setWidth(550);
        dialogStage.setHeight(450);
        dialogStage.setResizable(false);
        dialogStage.setScene(new Scene(rootPane));
    }
    
    private void guardarPdfVentas(List<Venta> ventas, BigDecimal totalVentas,
                                  int cantidadVentas, BigDecimal promedio, LocalDate desde, LocalDate hasta) {
        try {
            String fileName = PdfReportExporter.generateFileName("Reporte_Ventas", desde, hasta);
            File file = seleccionarUbicacion(fileName);
            if (file != null) {
                String periodo = desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                               " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                PdfReportExporter.exportVentas("REPORTE DE VENTAS", periodo, ventas, 
                    totalVentas, cantidadVentas, promedio, file.getAbsolutePath());
                AlertUtil.showInfo("Éxito", "PDF guardado exitosamente en:\n" + file.getAbsolutePath());
                dialogStage.close();
            }
        } catch (Exception e) {
            logger.error("Error exportando reporte", e);
            AlertUtil.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }
    
    private void guardarPdfInventario(List<Producto> productos, int totalProductos,
                                       int stockTotal, BigDecimal valorInventario, long stockBajo) {
        try {
            String fileName = PdfReportExporter.generateFileName("Reporte_Inventario", null, null);
            File file = seleccionarUbicacion(fileName);
            if (file != null) {
                PdfReportExporter.exportInventario("REPORTE DE INVENTARIO", productos,
                    totalProductos, stockTotal, valorInventario, stockBajo, file.getAbsolutePath());
                AlertUtil.showInfo("Éxito", "PDF guardado exitosamente en:\n" + file.getAbsolutePath());
                dialogStage.close();
            }
        } catch (Exception e) {
            logger.error("Error exportando reporte", e);
            AlertUtil.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }
    
    private void guardarPdfMovimientos(List<MovimientoInventario> movimientos,
                                        int totalEntradas, int totalSalidas, int totalAjustes,
                                        LocalDate desde, LocalDate hasta) {
        try {
            String fileName = PdfReportExporter.generateFileName("Reporte_Movimientos", desde, hasta);
            File file = seleccionarUbicacion(fileName);
            if (file != null) {
                String periodo = desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                               " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                PdfReportExporter.exportMovimientos("REPORTE DE MOVIMIENTOS", periodo, movimientos,
                    totalEntradas, totalSalidas, totalAjustes, file.getAbsolutePath());
                AlertUtil.showInfo("Éxito", "PDF guardado exitosamente en:\n" + file.getAbsolutePath());
                dialogStage.close();
            }
        } catch (Exception e) {
            logger.error("Error exportando reporte", e);
            AlertUtil.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }
    
    private void guardarPdfGanancias(List<PdfReportExporter.GananciasRow> ventas,
                                      BigDecimal totalVentas, BigDecimal costoTotal, 
                                      BigDecimal gananciaBruta, BigDecimal margen,
                                      LocalDate desde, LocalDate hasta) {
        try {
            String fileName = PdfReportExporter.generateFileName("Reporte_Ganancias", desde, hasta);
            File file = seleccionarUbicacion(fileName);
            if (file != null) {
                String periodo = desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + 
                               " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                PdfReportExporter.exportGanancias("REPORTE DE GANANCIAS", periodo, ventas,
                    totalVentas, costoTotal, gananciaBruta, margen, file.getAbsolutePath());
                AlertUtil.showInfo("Éxito", "PDF guardado exitosamente en:\n" + file.getAbsolutePath());
                dialogStage.close();
            }
        } catch (Exception e) {
            logger.error("Error exportando reporte", e);
            AlertUtil.showError("Error", "Error al exportar: " + e.getMessage());
        }
    }
    
    private File seleccionarUbicacion(String nombreArchivo) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Reporte PDF");
        fileChooser.setInitialFileName(nombreArchivo);
        
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
            "Archivos PDF (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        
        File file = fileChooser.showSaveDialog(dialogStage);
        return file;
    }
}
