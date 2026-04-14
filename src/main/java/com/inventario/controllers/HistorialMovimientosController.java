package com.inventario.controllers;

import com.inventario.models.MovimientoInventario;
import com.inventario.models.MovimientoInventario.TipoMovimiento;
import com.inventario.models.Producto;
import com.inventario.services.MovimientoInventarioService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;

public class HistorialMovimientosController {
    
    private static final Logger logger = LoggerFactory.getLogger(HistorialMovimientosController.class);
    
    private MovimientoInventarioService movimientoService;
    private Producto producto;
    
    @FXML private Label lblProducto;
    @FXML private TableView<MovimientoInventario> tablaMovimientos;
    @FXML private TableColumn<MovimientoInventario, String> colFecha;
    @FXML private TableColumn<MovimientoInventario, TipoMovimiento> colTipo;
    @FXML private TableColumn<MovimientoInventario, Integer> colCantidad;
    @FXML private TableColumn<MovimientoInventario, String> colDescripcion;
    @FXML private TableColumn<MovimientoInventario, String> colUsuario;
    
    private ObservableList<MovimientoInventario> movimientosObservable;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public HistorialMovimientosController() {
        this.movimientoService = null;
        this.producto = null;
    }
    
    public HistorialMovimientosController(MovimientoInventarioService movimientoService, Producto producto) {
        this.movimientoService = movimientoService;
        this.producto = producto;
    }
    
    public void setData(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
    }
    
    @FXML
    public void initialize() {
        movimientosObservable = FXCollections.observableArrayList();
        
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getFechaCreacion() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cellData.getValue().getFechaCreacion().format(dateFormatter));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colTipo.setCellValueFactory(cellData -> {
            if (cellData.getValue().getTipo() != null) {
                return new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTipo());
            }
            return new javafx.beans.property.SimpleObjectProperty<>(null);
        });
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        
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
        
        if (producto != null) {
            lblProducto.setText(producto.getNombre() + " (" + producto.getCodigo() + ") - Stock: " + producto.getStock());
            cargarMovimientos(producto.getId());
        }
    }
    
    private void cargarMovimientos(Long productoId) {
        try {
            var movimientos = movimientoService.obtenerMovimientosProducto(productoId);
            movimientosObservable.setAll(movimientos);
        } catch (ApplicationException e) {
            logger.error("Error cargando movimientos", e);
            AlertUtil.showError("Error", "Error cargando movimientos: " + e.getMessage());
        }
    }
}
