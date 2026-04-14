package com.inventario.controllers;

import com.inventario.models.Producto;
import com.inventario.services.ProductoService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

public class BuscarProductoController {
    
    private static final Logger logger = LoggerFactory.getLogger(BuscarProductoController.class);
    
    private final ProductoService productoService;
    
    @FXML private TextField txtBuscar;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private Button btnSeleccionar;
    @FXML private Button btnCancelar;
    
    private ObservableList<Producto> productosObservable;
    private Producto productoSeleccionado;
    
    public BuscarProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }
    
    @FXML
    public void initialize() {
        productosObservable = FXCollections.observableArrayList();
        
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaNombre"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        colPrecio.setCellFactory(column -> new TableCell<Producto, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", precio));
                }
            }
        });
        
        tablaProductos.setItems(productosObservable);
        
        tablaProductos.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                productoSeleccionado = newValue;
            }
        );
        
        tablaProductos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && productoSeleccionado != null) {
                btnSeleccionar.getScene().getWindow().hide();
            }
        });
        
        cargarTodosLosProductos();
    }
    
    private void cargarTodosLosProductos() {
        try {
            List<Producto> productos = productoService.obtenerTodos();
            productosObservable.setAll(productos);
        } catch (ApplicationException e) {
            logger.error("Error cargando productos", e);
            AlertUtil.showError("Error", "Error cargando productos");
        }
    }
    
    @FXML
    private void handleBuscar() {
        String texto = txtBuscar.getText().trim();
        
        if (texto.isEmpty()) {
            cargarTodosLosProductos();
            return;
        }
        
        try {
            List<Producto> productos = productoService.buscarPorNombre(texto);
            productosObservable.setAll(productos);
        } catch (ApplicationException e) {
            logger.error("Error buscando productos", e);
            AlertUtil.showError("Error", "Error buscando productos");
        }
    }
    
    @FXML
    private void handleSeleccionar() {
        if (productoSeleccionado == null) {
            AlertUtil.showWarning("Advertencia", "Seleccione un producto");
            return;
        }
        
        btnSeleccionar.getScene().getWindow().hide();
    }
    
    @FXML
    private void handleCancelar() {
        productoSeleccionado = null;
        btnCancelar.getScene().getWindow().hide();
    }
    
    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }
}
