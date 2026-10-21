package com.inventario.controllers;

import com.inventario.models.Venta;
import com.inventario.services.VentaService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import com.inventario.utils.PdfReportExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteVentasController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReporteVentasController.class);
    
    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;
    @FXML private Label lblPeriodo;
    @FXML private Label lblFechaGeneracion;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblCantidadVentas;
    @FXML private Label lblPromedioVenta;
    @FXML private TableView<Venta> tablaVentas;
    @FXML private TableColumn<Venta, Long> colId;
    @FXML private TableColumn<Venta, String> colFecha;
    @FXML private TableColumn<Venta, String> colCliente;
    @FXML private TableColumn<Venta, BigDecimal> colTotal;
    @FXML private TableColumn<Venta, String> colMetodoPago;
    @FXML private TableColumn<Venta, String> colEstado;
    
    private VentaService ventaService;
    private ObservableList<Venta> ventasObservable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private BigDecimal totalVentasActual = BigDecimal.ZERO;
    private int cantidadVentasActual = 0;
    private BigDecimal promedioActual = BigDecimal.ZERO;
    private List<Venta> ventasActuales;
    
    public ReporteVentasController() {
    }
    
    @FXML
    public void initialize() {
        this.ventaService = new VentaService();
        this.ventasObservable = FXCollections.observableArrayList();
        configurarTabla();
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        generarReporte();
    }
    
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFechaCreacion().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colCliente.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
            c.getValue().getClienteNombre() != null ? c.getValue().getClienteNombre() : "Público en general"));
        colMetodoPago.setCellValueFactory(new PropertyValueFactory<>("metodoPago"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(column -> new TableCell<Venta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal total, boolean empty) {
                super.updateItem(total, empty);
                if (empty || total == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", total));
                }
            }
        });
        
        colEstado.setCellFactory(column -> new TableCell<Venta, String>() {
            @Override
            protected void updateItem(String estado, boolean empty) {
                super.updateItem(estado, empty);
                if (empty || estado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(estado);
                    if ("COMPLETADA".equals(estado)) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if ("ANULADA".equals(estado)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        tablaVentas.setItems(ventasObservable);
    }
    
    @FXML
    private void handleGenerarReporte() {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        
        if (desde == null || hasta == null) {
            AlertUtil.showWarning("Validación", "Seleccione un rango de fechas");
            return;
        }
        
        if (desde.isAfter(hasta)) {
            AlertUtil.showWarning("Validación", "La fecha 'Desde' no puede ser mayor que 'Hasta'");
            return;
        }
        
        generarReporte(true);
    }
    
    private void generarReporte() {
        generarReporte(false);
    }
    
    private void generarReporte(boolean mostrarAlerta) {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        
        try {
            List<Venta> ventas;
            if (desde != null && hasta != null) {
                ventas = ventaService.obtenerPorRangoFechas(desde, hasta);
            } else {
                ventas = ventaService.obtenerTodas();
            }
            
            ventasObservable.setAll(ventas);
            actualizarEstadisticas(ventas);
            
            if (mostrarAlerta) {
                String mensaje = "Reporte generado exitosamente!\n\n" +
                               "Registros encontrados: " + ventas.size() + "\n" +
                               "Total Ventas: $" + String.format("%,.2f", totalVentasActual) + "\n" +
                               "Transacciones: " + cantidadVentasActual;
                AlertUtil.showInfo("Reporte Generado", mensaje);
            }
            
        } catch (ApplicationException e) {
            logger.error("Error generando reporte de ventas", e);
            AlertUtil.showError("Error", "Error generando reporte: " + e.getMessage());
        }
    }
    
    private void actualizarEstadisticas(List<Venta> ventas) {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        
        if (desde != null && hasta != null) {
            lblPeriodo.setText("Período: " + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            lblPeriodo.setText("Período: Todos los registros");
        }
        
        lblFechaGeneracion.setText("Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        BigDecimal totalVentas = BigDecimal.ZERO;
        int cantidadVentas = 0;
        
        for (Venta venta : ventas) {
            if ("COMPLETADA".equals(venta.getEstado())) {
                totalVentas = totalVentas.add(venta.getTotal());
                cantidadVentas++;
            }
        }
        
        BigDecimal promedio = cantidadVentas > 0 
            ? totalVentas.divide(BigDecimal.valueOf(cantidadVentas), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
        
        this.totalVentasActual = totalVentas;
        this.cantidadVentasActual = cantidadVentas;
        this.promedioActual = promedio;
        this.ventasActuales = ventas;
        
        lblTotalVentas.setText(String.format("$%,.2f", totalVentas));
        lblCantidadVentas.setText(String.valueOf(cantidadVentas));
        lblPromedioVenta.setText(String.format("$%,.2f", promedio));
    }
    
    @FXML
    private void handleAnular() {
        Venta venta = tablaVentas.getSelectionModel().getSelectedItem();
        if (venta == null) {
            AlertUtil.showWarning("Anular venta", "Seleccione la venta a anular en la tabla");
            return;
        }
        if (!"COMPLETADA".equals(venta.getEstado())) {
            AlertUtil.showWarning("Anular venta", "La venta #" + venta.getId() + " ya está anulada");
            return;
        }
        if (!AlertUtil.showConfirmation("Anular venta", "¿Anular la venta #" + venta.getId() + " por " +
                String.format("$%,.2f", venta.getTotal()) + "?\nLos productos regresarán al inventario.")) {
            return;
        }
        try {
            ventaService.anularVenta(venta.getId());
            AlertUtil.showInfo("Anular venta", "Venta #" + venta.getId() + " anulada");
            generarReporte();
        } catch (ApplicationException e) {
            logger.error("Error anulando venta", e);
            AlertUtil.showError("Anular venta", e.getMessage());
        }
    }

    @FXML
    private void handleExportar() {
        if (ventasActuales == null || ventasActuales.isEmpty()) {
            AlertUtil.showWarning("Exportar", "No hay datos para exportar");
            return;
        }
        
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        
        PdfPreviewController.mostrarVentas(
            getStage(),
            ventasActuales,
            totalVentasActual,
            cantidadVentasActual,
            promedioActual,
            desde != null ? desde : LocalDate.now().minusDays(30),
            hasta != null ? hasta : LocalDate.now()
        );
    }
    
    private Stage getStage() {
        return (Stage) tablaVentas.getScene().getWindow();
    }
    
    @FXML
    private void handleLimpiar() {
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        generarReporte(true);
    }
}
