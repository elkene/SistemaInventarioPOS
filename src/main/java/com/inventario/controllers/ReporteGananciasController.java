package com.inventario.controllers;

import com.inventario.models.DetalleVenta;
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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteGananciasController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReporteGananciasController.class);
    
    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;
    @FXML private Label lblPeriodo;
    @FXML private Label lblFechaGeneracion;
    @FXML private Label lblTotalVentas;
    @FXML private Label lblCostoTotal;
    @FXML private Label lblGananciaBruta;
    @FXML private Label lblPorcentajeGanancia;
    @FXML private TableView<VentaReporte> tablaVentas;
    @FXML private TableColumn<VentaReporte, Long> colId;
    @FXML private TableColumn<VentaReporte, String> colFecha;
    @FXML private TableColumn<VentaReporte, String> colCliente;
    @FXML private TableColumn<VentaReporte, BigDecimal> colTotal;
    @FXML private TableColumn<VentaReporte, BigDecimal> colCosto;
    @FXML private TableColumn<VentaReporte, BigDecimal> colGanancia;
    
    private VentaService ventaService;
    private ObservableList<VentaReporte> ventasObservable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private BigDecimal totalVentasActual = BigDecimal.ZERO;
    private BigDecimal costoTotalActual = BigDecimal.ZERO;
    private BigDecimal gananciaBrutaActual = BigDecimal.ZERO;
    private BigDecimal porcentajeActual = BigDecimal.ZERO;
    private List<PdfReportExporter.GananciasRow> ventasActuales;
    
    public static class VentaReporte {
        private Long id;
        private LocalDate fecha;
        private String cliente;
        private BigDecimal total;
        private BigDecimal costo;
        private BigDecimal ganancia;
        
        public VentaReporte() {}
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public LocalDate getFecha() { return fecha; }
        public void setFecha(LocalDate fecha) { this.fecha = fecha; }
        public String getCliente() { return cliente; }
        public void setCliente(String cliente) { this.cliente = cliente; }
        public BigDecimal getTotal() { return total; }
        public void setTotal(BigDecimal total) { this.total = total; }
        public BigDecimal getCosto() { return costo; }
        public void setCosto(BigDecimal costo) { this.costo = costo; }
        public BigDecimal getGanancia() { return ganancia; }
        public void setGanancia(BigDecimal ganancia) { this.ganancia = ganancia; }
    }
    
    public ReporteGananciasController() {
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
            if (cellData.getValue().getFecha() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFecha().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colTotal.setCellFactory(column -> new TableCell<VentaReporte, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", valor));
                }
            }
        });
        
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costo"));
        colCosto.setCellFactory(column -> new TableCell<VentaReporte, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", valor));
                }
            }
        });
        
        colGanancia.setCellValueFactory(new PropertyValueFactory<>("ganancia"));
        colGanancia.setCellFactory(column -> new TableCell<VentaReporte, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal valor, boolean empty) {
                super.updateItem(valor, empty);
                if (empty || valor == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("$%.2f", valor));
                    if (valor.compareTo(BigDecimal.ZERO) >= 0) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
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
        
        if (desde != null && hasta != null) {
            lblPeriodo.setText("Período: " + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            lblPeriodo.setText("Período: Todos los registros");
        }
        
        lblFechaGeneracion.setText("Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        try {
            List<Venta> ventas;
            if (desde != null && hasta != null) {
                ventas = ventaService.obtenerPorRangoFechas(desde, hasta);
            } else {
                ventas = ventaService.obtenerTodas();
            }
            
            ventasObservable.clear();
            
            BigDecimal totalVentas = BigDecimal.ZERO;
            BigDecimal totalCostos = BigDecimal.ZERO;
            BigDecimal totalGanancias = BigDecimal.ZERO;
            
            for (Venta venta : ventas) {
                if (!"COMPLETADA".equals(venta.getEstado())) {
                    continue;
                }
                
                BigDecimal costoVenta = BigDecimal.ZERO;
                try {
                    List<DetalleVenta> detalles = ventaService.obtenerDetallesVenta(venta.getId());
                    // Se usa el costo guardado al momento de la venta, no el precio de compra actual
                    for (DetalleVenta detalle : detalles) {
                        if (detalle.getCostoUnitario() != null) {
                            costoVenta = costoVenta.add(detalle.getCostoUnitario()
                                .multiply(BigDecimal.valueOf(detalle.getCantidad())));
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Error obteniendo detalles de venta {}: {}", venta.getId(), e.getMessage());
                }
                
                BigDecimal gananciaVenta = venta.getTotal().subtract(costoVenta);
                
                VentaReporte reporte = new VentaReporte();
                reporte.setId(venta.getId());
                reporte.setFecha(venta.getFechaCreacion() != null ? venta.getFechaCreacion().toLocalDate() : LocalDate.now());
                reporte.setCliente(venta.getClienteNombre() != null ? venta.getClienteNombre() : "Cliente General");
                reporte.setTotal(venta.getTotal());
                reporte.setCosto(costoVenta);
                reporte.setGanancia(gananciaVenta);
                
                ventasObservable.add(reporte);
                
                totalVentas = totalVentas.add(venta.getTotal());
                totalCostos = totalCostos.add(costoVenta);
                totalGanancias = totalGanancias.add(gananciaVenta);
            }
            
            BigDecimal porcentaje = BigDecimal.ZERO;
            if (totalVentas.compareTo(BigDecimal.ZERO) > 0) {
                porcentaje = totalGanancias.divide(totalVentas, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            }
            
            lblTotalVentas.setText(String.format("$%,.2f", totalVentas));
            lblCostoTotal.setText(String.format("$%,.2f", totalCostos));
            lblGananciaBruta.setText(String.format("$%,.2f", totalGanancias));
            lblPorcentajeGanancia.setText(String.format("%.2f%%", porcentaje));
            
            this.totalVentasActual = totalVentas;
            this.costoTotalActual = totalCostos;
            this.gananciaBrutaActual = totalGanancias;
            this.porcentajeActual = porcentaje;
            this.ventasActuales = ventasObservable.stream()
                .map(vr -> new PdfReportExporter.GananciasRow(
                    vr.getId(),
                    vr.getFecha(),
                    vr.getCliente(),
                    vr.getTotal(),
                    vr.getCosto(),
                    vr.getGanancia()))
                .toList();
            
            if (mostrarAlerta) {
                String mensaje = "Reporte generado exitosamente!\n\n" +
                               "Ventas encontradas: " + ventasActuales.size() + "\n" +
                               "Total Ventas: $" + String.format("%,.2f", totalVentasActual) + "\n" +
                               "Costo Total: $" + String.format("%,.2f", costoTotalActual) + "\n" +
                               "Ganancia: $" + String.format("%,.2f", gananciaBrutaActual) + "\n" +
                               "Margen: " + String.format("%.2f%%", porcentajeActual);
                AlertUtil.showInfo("Reporte Generado", mensaje);
            }
            
        } catch (ApplicationException e) {
            logger.error("Error generando reporte de ganancias", e);
            AlertUtil.showError("Error", "Error generando reporte: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLimpiar() {
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        generarReporte(true);
    }
    
    @FXML
    private void handleExportar() {
        if (ventasActuales == null || ventasActuales.isEmpty()) {
            AlertUtil.showWarning("Exportar", "No hay datos para exportar");
            return;
        }
        
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        
        PdfPreviewController.mostrarGanancias(
            getStage(),
            ventasActuales,
            totalVentasActual,
            costoTotalActual,
            gananciaBrutaActual,
            porcentajeActual,
            desde != null ? desde : LocalDate.now().minusDays(30),
            hasta != null ? hasta : LocalDate.now()
        );
    }
    
    private Stage getStage() {
        return (Stage) tablaVentas.getScene().getWindow();
    }
}
