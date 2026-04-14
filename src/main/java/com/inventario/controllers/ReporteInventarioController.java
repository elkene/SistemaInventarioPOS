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
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReporteInventarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReporteInventarioController.class);
    
    @FXML private TextField txtBuscar;
    @FXML private Label lblFechaGeneracion;
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockTotal;
    @FXML private Label lblValorInventario;
    @FXML private Label lblStockBajo;
    @FXML private TableView<Producto> tablaInventario;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioCompra;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colStockMinimo;
    @FXML private TableColumn<Producto, Integer> colEstado;
    
    private ProductoService productoService;
    private ObservableList<Producto> productosObservable;
    
    private int totalProductosActual = 0;
    private int stockTotalActual = 0;
    private BigDecimal valorInventarioActual = BigDecimal.ZERO;
    private long stockBajoActual = 0;
    private List<Producto> productosActuales;
    
    public ReporteInventarioController() {
    }
    
    @FXML
    public void initialize() {
        this.productoService = new ProductoService();
        this.productosObservable = FXCollections.observableArrayList();
        configurarTabla();
        cargarDatos(false);
    }
    
    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaNombre"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
        
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioCompra.setCellFactory(column -> new TableCell<Producto, BigDecimal>() {
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
        
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colPrecioVenta.setCellFactory(column -> new TableCell<Producto, BigDecimal>() {
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
        
        colEstado.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colEstado.setCellFactory(column -> new TableCell<Producto, Integer>() {
            @Override
            protected void updateItem(Integer stock, boolean empty) {
                super.updateItem(stock, empty);
                if (empty || stock == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Producto producto = getTableRow().getItem();
                    if (producto != null && producto.isStockBajo()) {
                        setText("BAJO");
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setText("OK");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        tablaInventario.setItems(productosObservable);
    }
    
    private void cargarDatos() {
        cargarDatos(true);
    }
    
    private void cargarDatos(boolean mostrarAlerta) {
        try {
            List<Producto> productos = productoService.obtenerTodos();
            productosObservable.setAll(productos);
            actualizarEstadisticas(productos);
            
            if (mostrarAlerta) {
                String mensaje = "Reporte generado exitosamente!\n\n" +
                               "Total productos: " + totalProductosActual + "\n" +
                               "Stock total: " + String.format("%,d", stockTotalActual) + " unidades\n" +
                               "Valor inventario: $" + String.format("%,.2f", valorInventarioActual) + "\n" +
                               "Stock bajo: " + stockBajoActual;
                AlertUtil.showInfo("Reporte Generado", mensaje);
            }
        } catch (ApplicationException e) {
            logger.error("Error cargando inventario", e);
            AlertUtil.showError("Error", "Error cargando datos: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleGenerar() {
        cargarDatos(true);
    }
    
    @FXML
    private void handleBuscar() {
        String texto = txtBuscar.getText().trim();
        
        if (texto.isEmpty()) {
            cargarDatos(true);
            return;
        }
        
        try {
            List<Producto> productos = productoService.buscarPorNombre(texto);
            productosObservable.setAll(productos);
            actualizarEstadisticas(productos);
        } catch (ApplicationException e) {
            logger.error("Error buscando productos", e);
            AlertUtil.showError("Error", "Error buscando productos");
        }
    }
    
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cargarDatos(true);
    }
    
    private void actualizarEstadisticas(List<Producto> productos) {
        lblFechaGeneracion.setText("Generado: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        int totalProductos = productos.size();
        int stockTotal = productos.stream().mapToInt(Producto::getStock).sum();
        long stockBajo = productos.stream().filter(Producto::isStockBajo).count();
        
        BigDecimal valorInventario = productos.stream()
            .map(p -> p.getPrecioCompra() != null 
                ? p.getPrecioCompra().multiply(BigDecimal.valueOf(p.getStock())) 
                : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblStockTotal.setText(String.format("%,d", stockTotal));
        lblValorInventario.setText(String.format("$%,.2f", valorInventario));
        lblStockBajo.setText(String.valueOf(stockBajo));
        
        this.totalProductosActual = totalProductos;
        this.stockTotalActual = stockTotal;
        this.valorInventarioActual = valorInventario;
        this.stockBajoActual = stockBajo;
        this.productosActuales = productos;
    }
    
    @FXML
    private void handleExportar() {
        if (productosActuales == null || productosActuales.isEmpty()) {
            AlertUtil.showWarning("Exportar", "No hay datos para exportar");
            return;
        }
        
        PdfPreviewController.mostrarInventario(
            getStage(),
            productosActuales,
            totalProductosActual,
            stockTotalActual,
            valorInventarioActual,
            stockBajoActual
        );
    }
    
    private Stage getStage() {
        return (Stage) tablaInventario.getScene().getWindow();
    }
}
