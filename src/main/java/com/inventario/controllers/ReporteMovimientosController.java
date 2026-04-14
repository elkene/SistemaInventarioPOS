package com.inventario.controllers;

import com.inventario.models.MovimientoInventario;
import com.inventario.models.MovimientoInventario.TipoMovimiento;
import com.inventario.services.MovimientoInventarioService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteMovimientosController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReporteMovimientosController.class);
    
    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private Label lblPeriodo;
    @FXML private Label lblFechaGeneracion;
    @FXML private Label lblTotalEntradas;
    @FXML private Label lblTotalSalidas;
    @FXML private Label lblTotalAjustes;
    @FXML private Label lblTotalMovimientos;
    @FXML private TableView<MovimientoInventario> tablaMovimientos;
    @FXML private TableColumn<MovimientoInventario, String> colFecha;
    @FXML private TableColumn<MovimientoInventario, String> colProducto;
    @FXML private TableColumn<MovimientoInventario, TipoMovimiento> colTipo;
    @FXML private TableColumn<MovimientoInventario, Integer> colCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colDescripcion;
    @FXML private TableColumn<MovimientoInventario, String> colUsuario;
    
    private MovimientoInventarioService movimientoService;
    private ObservableList<MovimientoInventario> movimientosObservable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    private int totalEntradasActual = 0;
    private int totalSalidasActual = 0;
    private int totalAjustesActual = 0;
    private List<MovimientoInventario> movimientosActuales;
    
    public ReporteMovimientosController() {
    }
    
    @FXML
    public void initialize() {
        this.movimientoService = new MovimientoInventarioService();
        this.movimientosObservable = FXCollections.observableArrayList();
        configurarTabla();
        configurarFiltros();
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        generarReporte();
    }
    
    private void configurarFiltros() {
        cmbTipo.setItems(FXCollections.observableArrayList(
            "Todos",
            "ENTRADA",
            "SALIDA",
            "AJUSTE"
        ));
        cmbTipo.getSelectionModel().selectFirst();
    }
    
    private void configurarTabla() {
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFechaCreacion().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        
        colTipo.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTipo() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTipo());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        colTipo.setCellFactory(column -> new TableCell<MovimientoInventario, TipoMovimiento>() {
            @Override
            protected void updateItem(TipoMovimiento tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(tipo.name());
                    if (tipo == TipoMovimiento.ENTRADA) {
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (tipo == TipoMovimiento.SALIDA) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        tablaMovimientos.setItems(movimientosObservable);
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
        String tipoSeleccionado = cmbTipo.getValue();
        
        try {
            List<MovimientoInventario> movimientos;
            if (desde != null && hasta != null) {
                movimientos = movimientoService.obtenerPorRangoFechas(desde, hasta);
            } else {
                movimientos = movimientoService.obtenerTodos();
            }
            
            if (tipoSeleccionado != null && !"Todos".equals(tipoSeleccionado)) {
                movimientos = movimientos.stream()
                    .filter(m -> tipoSeleccionado.equals(m.getTipo().name()))
                    .toList();
            }
            
            movimientosObservable.setAll(movimientos);
            actualizarEstadisticas(movimientos);
            
            if (mostrarAlerta) {
                String mensaje = "Reporte generado exitosamente!\n\n" +
                               "Registros encontrados: " + movimientos.size() + "\n" +
                               "Entradas: " + String.format("%,d", totalEntradasActual) + "\n" +
                               "Salidas: " + String.format("%,d", totalSalidasActual) + "\n" +
                               "Ajustes: " + String.format("%,d", totalAjustesActual);
                AlertUtil.showInfo("Reporte Generado", mensaje);
            }
            
        } catch (ApplicationException e) {
            logger.error("Error generando reporte de movimientos", e);
            AlertUtil.showError("Error", "Error generando reporte: " + e.getMessage());
        }
    }
    
    private void actualizarEstadisticas(List<MovimientoInventario> movimientos) {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        String tipo = cmbTipo.getValue();
        
        if (desde != null && hasta != null) {
            String tipoTexto = (tipo != null && !"Todos".equals(tipo)) ? " - " + tipo : "";
            lblPeriodo.setText("Período: " + desde.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " al " + hasta.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + tipoTexto);
        } else {
            lblPeriodo.setText("Período: Todos los registros");
        }
        
        lblFechaGeneracion.setText("Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        int entradas = 0;
        int salidas = 0;
        int ajustes = 0;
        
        for (MovimientoInventario mov : movimientos) {
            switch (mov.getTipo()) {
                case ENTRADA:
                    entradas += mov.getCantidad();
                    break;
                case SALIDA:
                    salidas += mov.getCantidad();
                    break;
                case AJUSTE:
                    ajustes += mov.getCantidad();
                    break;
            }
        }
        
        lblTotalEntradas.setText(String.format("%,d", entradas));
        lblTotalSalidas.setText(String.format("%,d", salidas));
        lblTotalAjustes.setText(String.format("%,d", ajustes));
        lblTotalMovimientos.setText(String.format("%,d", movimientos.size()));
        
        this.totalEntradasActual = entradas;
        this.totalSalidasActual = salidas;
        this.totalAjustesActual = ajustes;
        this.movimientosActuales = movimientos;
    }
    
    @FXML
    private void handleExportar() {
        if (movimientosActuales == null || movimientosActuales.isEmpty()) {
            AlertUtil.showWarning("Exportar", "No hay datos para exportar");
            return;
        }
        
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();
        
        PdfPreviewController.mostrarMovimientos(
            getStage(),
            movimientosActuales,
            totalEntradasActual,
            totalSalidasActual,
            totalAjustesActual,
            desde != null ? desde : LocalDate.now().minusDays(30),
            hasta != null ? hasta : LocalDate.now()
        );
    }
    
    private Stage getStage() {
        return (Stage) tablaMovimientos.getScene().getWindow();
    }
    
    @FXML
    private void handleLimpiar() {
        dateDesde.setValue(LocalDate.now().minusDays(30));
        dateHasta.setValue(LocalDate.now());
        cmbTipo.getSelectionModel().selectFirst();
        generarReporte(true);
    }
}
