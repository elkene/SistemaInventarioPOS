package com.inventario.controllers;

import com.inventario.models.Categoria;
import com.inventario.models.MovimientoInventario;
import com.inventario.models.Producto;
import com.inventario.services.CategoriaService;
import com.inventario.services.MovimientoInventarioService;
import com.inventario.services.ProductoService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.ValidationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class InventarioController {
    
    private static final Logger logger = LoggerFactory.getLogger(InventarioController.class);
    
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioCompra;
    @FXML private TableColumn<Producto, BigDecimal> colPrecioVenta;
    @FXML private TableColumn<Producto, Integer> colStock;
    @FXML private TableColumn<Producto, Integer> colStockMinimo;
    @FXML private TableColumn<Producto, Integer> colEstado;
    
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private ComboBox<String> cmbFiltroStock;
    @FXML private Button btnBuscar;
    @FXML private Button btnLimpiarFiltros;
    
    @FXML private Button btnNuevoProducto;
    @FXML private Button btnEditarProducto;
    @FXML private Button btnEliminarProducto;
    @FXML private Button btnAjustarStock;
    @FXML private Button btnVerMovimientos;
    @FXML private Button btnCategorias;
    @FXML private Button btnActualizar;
    
    @FXML private Label lblTotalProductos;
    @FXML private Label lblStockTotal;
    @FXML private Label lblValorInventario;
    @FXML private Label lblProductosBajoStock;
    
    private ProductoService productoService;
    private CategoriaService categoriaService;
    private MovimientoInventarioService movimientoService;
    private ObservableList<Producto> productosObservable;
    private ObservableList<Categoria> categoriasObservable;
    
    public void initialize() {
        productoService = new ProductoService();
        categoriaService = new CategoriaService();
        movimientoService = new MovimientoInventarioService();
        productosObservable = FXCollections.observableArrayList();
        categoriasObservable = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarFiltros();
        cargarDatos();
    }
    
    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoriaNombre"));
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colPrecioVenta.setCellValueFactory(new PropertyValueFactory<>("precioVenta"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colStockMinimo.setCellValueFactory(new PropertyValueFactory<>("stockMinimo"));
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
        
        tablaProductos.setItems(productosObservable);
    }
    
    private void configurarFiltros() {
        cmbCategoria.setPromptText("Todas las categorías");
        
        cmbFiltroStock.setItems(FXCollections.observableArrayList(
            "Todos",
            "Stock bajo",
            "Stock normal",
            "Sin stock"
        ));
        cmbFiltroStock.getSelectionModel().selectFirst();
    }
    
    private void cargarDatos() {
        try {
            List<Producto> productos = productoService.obtenerTodos();
            productosObservable.setAll(productos);
            
            List<Categoria> categorias = categoriaService.obtenerTodas();
            categoriasObservable.setAll(categorias);
            cmbCategoria.setItems(FXCollections.observableArrayList(
                new Categoria(null, "Todas")
            ));
            categoriasObservable.forEach(cat -> cmbCategoria.getItems().add(cat));
            
            actualizarEstadisticas();
            
        } catch (ApplicationException e) {
            logger.error("Error cargando datos", e);
            AlertUtil.showError("Error", "Error cargando datos: " + e.getMessage());
        }
    }
    
    private void actualizarEstadisticas() {
        List<Producto> productos = productosObservable;
        
        long totalProductos = productos.size();
        long productosStockBajo = productos.stream().filter(Producto::isStockBajo).count();
        int stockTotal = productos.stream().mapToInt(Producto::getStock).sum();
        BigDecimal valorInventario = productos.stream()
            .map(p -> p.getPrecioCompra() != null ? 
                p.getPrecioCompra().multiply(BigDecimal.valueOf(p.getStock())) : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        lblTotalProductos.setText(String.valueOf(totalProductos));
        lblStockTotal.setText(String.valueOf(stockTotal));
        lblValorInventario.setText(String.format("$%.2f", valorInventario));
        lblProductosBajoStock.setText(String.valueOf(productosStockBajo));
    }
    
    @FXML
    private void handleBuscar() {
        String texto = txtBuscar.getText();
        Categoria categoriaSeleccionada = cmbCategoria.getValue();
        String filtroStock = cmbFiltroStock.getValue();
        
        try {
            List<Producto> productos;
            
            if (texto != null && !texto.isEmpty()) {
                productos = productoService.buscarPorNombre(texto);
            } else {
                productos = productoService.obtenerTodos();
            }
            
            if (categoriaSeleccionada != null && categoriaSeleccionada.getId() != null) {
                productos = productos.stream()
                    .filter(p -> p.getCategoriaId() != null && 
                                p.getCategoriaId().equals(categoriaSeleccionada.getId()))
                    .collect(Collectors.toList());
            }
            
            switch (filtroStock != null ? filtroStock : "Todos") {
                case "Stock bajo":
                    productos = productos.stream().filter(Producto::isStockBajo).collect(Collectors.toList());
                    break;
                case "Stock normal":
                    productos = productos.stream()
                        .filter(p -> !p.isStockBajo() && p.getStock() > 0)
                        .collect(Collectors.toList());
                    break;
                case "Sin stock":
                    productos = productos.stream().filter(p -> p.getStock() == 0).collect(Collectors.toList());
                    break;
            }
            
            productosObservable.setAll(productos);
            actualizarEstadisticas();
            
        } catch (ApplicationException e) {
            logger.error("Error buscando productos", e);
            AlertUtil.showError("Error", "Error buscando productos: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLimpiarFiltros() {
        txtBuscar.clear();
        cmbCategoria.getSelectionModel().clearSelection();
        cmbFiltroStock.getSelectionModel().selectFirst();
        cargarDatos();
    }
    
    @FXML
    private void handleNuevoProducto() {
        mostrarDialogoProducto(null);
    }
    
    @FXML
    private void handleEditarProducto() {
        Producto producto = tablaProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            AlertUtil.showWarning("Advertencia", "Seleccione un producto para editar");
            return;
        }
        mostrarDialogoProducto(producto);
    }
    
    @FXML
    private void handleEliminarProducto() {
        Producto producto = tablaProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            AlertUtil.showWarning("Advertencia", "Seleccione un producto para eliminar");
            return;
        }
        
        if (!AlertUtil.showConfirmation("Confirmar", 
                "¿Está seguro de eliminar el producto '" + producto.getNombre() + "'?\n" +
                "Si tiene ventas o movimientos registrados se desactivará en lugar de borrarse.")) {
            return;
        }

        try {
            boolean borrado = productoService.eliminarProducto(producto.getId());
            AlertUtil.showInfo("Éxito", borrado ? "Producto eliminado correctamente"
                    : "El producto tiene historial, así que se desactivó: ya no aparece en las listas ni se puede vender.\n" +
                      "Para recuperarlo, da de alta un producto nuevo con el mismo código.");
            cargarDatos();
        } catch (ValidationException e) {
            AlertUtil.showWarning("Advertencia", e.getMessage());
        } catch (ApplicationException e) {
            logger.error("Error eliminando producto", e);
            AlertUtil.showError("Error", "Error eliminando producto: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleAjustarStock() {
        Producto producto = tablaProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            AlertUtil.showWarning("Advertencia", "Seleccione un producto para ajustar stock");
            return;
        }
        mostrarDialogoAjusteStock(producto);
    }
    
    @FXML
    private void handleVerMovimientos() {
        Producto producto = tablaProductos.getSelectionModel().getSelectedItem();
        if (producto == null) {
            AlertUtil.showWarning("Advertencia", "Seleccione un producto para ver movimientos");
            return;
        }
        mostrarDialogoMovimientos(producto);
    }
    
    @FXML
    private void handleCategorias() {
        mostrarDialogoCategorias();
    }
    
    @FXML
    private void handleActualizar() {
        cargarDatos();
    }
    
    private void mostrarDialogoProducto(Producto producto) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle(producto == null ? "Nuevo Producto" : "Editar Producto");
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/producto_dialog.fxml"));
            
            javafx.scene.Parent root = loader.load();
            ProductoDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setData(productoService, categoriaService);
            controller.setProducto(producto);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 550, 600);
            dialogStage.setScene(scene);
            dialogStage.setResizable(true);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
            
            if (controller.isGuardado()) {
                cargarDatos();
            }
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de producto", e);
            AlertUtil.showError("Error", "Error abriendo diálogo: " + e.getMessage());
        }
    }
    
    private void mostrarDialogoAjusteStock(Producto producto) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Ajustar Stock - " + producto.getNombre());
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/ajuste_stock_dialog.fxml"));
            
            javafx.scene.Parent root = loader.load();
            AjusteStockDialogController controller = loader.getController();
            controller.setData(movimientoService);
            controller.setProducto(producto);
            controller.setDialogStage(dialogStage);
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 400, 350);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
            
            if (controller.isGuardado()) {
                cargarDatos();
            }
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de ajuste de stock", e);
            AlertUtil.showError("Error", "Error abriendo diálogo: " + e.getMessage());
        }
    }
    
    private void mostrarDialogoMovimientos(Producto producto) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Historial de Movimientos - " + producto.getNombre());
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/historial_movimientos.fxml"));
            
            HistorialMovimientosController controller = new HistorialMovimientosController(movimientoService, producto);
            loader.setController(controller);
            
            javafx.scene.Parent root = loader.load();
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 700, 450);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de movimientos", e);
            AlertUtil.showError("Error", "Error abriendo diálogo: " + e.getMessage());
        }
    }
    
    private void mostrarDialogoCategorias() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Gestionar Categorías");
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/categorias_dialog.fxml"));
            
            javafx.scene.Parent root = loader.load();
            
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 550, 450);
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.centerOnScreen();
            dialogStage.showAndWait();
            
            cargarDatos();
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de categorías", e);
            AlertUtil.showError("Error", "Error abriendo diálogo: " + e.getMessage());
        }
    }
}
