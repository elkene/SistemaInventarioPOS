package com.inventario.controllers;

import com.inventario.models.Categoria;
import com.inventario.models.Producto;
import com.inventario.services.CategoriaService;
import com.inventario.services.ProductoService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.ValidationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class ProductoDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductoDialogController.class);
    
    private ProductoService productoService;
    private CategoriaService categoriaService;
    
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtPrecioVenta;
    @FXML private TextField txtStock;
    @FXML private TextField txtStockMinimo;
    @FXML private ComboBox<Categoria> cmbCategoria;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    private Producto producto;
    private Stage dialogStage;
    private boolean guardado = false;
    
    public ProductoDialogController() {
        this.productoService = null;
        this.categoriaService = null;
    }
    
    public void setData(ProductoService productoService, CategoriaService categoriaService) {
        this.productoService = productoService;
        this.categoriaService = categoriaService;
        cargarCategorias();
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        populateFields();
    }
    
    private void cargarCategorias() {
        if (categoriaService != null && cmbCategoria != null) {
            try {
                var categorias = categoriaService.obtenerTodas();
                cmbCategoria.setItems(FXCollections.observableArrayList(categorias));
            } catch (ApplicationException e) {
                logger.error("Error cargando categorías", e);
            }
        }
    }
    
    public void populateFields() {
        if (producto != null) {
            txtCodigo.setText(producto.getCodigo());
            txtNombre.setText(producto.getNombre());
            txtDescripcion.setText(producto.getDescripcion());
            txtPrecioCompra.setText(producto.getPrecioCompra() != null ? producto.getPrecioCompra().toString() : "");
            txtPrecioVenta.setText(producto.getPrecioVenta() != null ? producto.getPrecioVenta().toString() : "");
            txtStock.setText(String.valueOf(producto.getStock()));
            txtStockMinimo.setText(String.valueOf(producto.getStockMinimo()));
            
            if (producto.getCategoriaId() != null && cmbCategoria.getItems() != null) {
                for (Categoria c : cmbCategoria.getItems()) {
                    if (c.getId() != null && c.getId().equals(producto.getCategoriaId())) {
                        cmbCategoria.setValue(c);
                        break;
                    }
                }
            }
        }
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isGuardado() {
        return guardado;
    }
    
    @FXML
    public void initialize() {
    }
    
    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }
        
        try {
            if (producto == null) {
                producto = new Producto();
            }
            
            producto.setCodigo(txtCodigo.getText().trim());
            producto.setNombre(txtNombre.getText().trim());
            producto.setDescripcion(txtDescripcion.getText().trim());
            
            String precioCompraText = txtPrecioCompra.getText().trim();
            if (!precioCompraText.isEmpty()) {
                producto.setPrecioCompra(new BigDecimal(precioCompraText));
            }
            
            producto.setPrecioVenta(new BigDecimal(txtPrecioVenta.getText().trim()));
            producto.setStock(Integer.parseInt(txtStock.getText().trim()));
            producto.setStockMinimo(Integer.parseInt(txtStockMinimo.getText().trim()));
            
            Categoria categoria = cmbCategoria.getValue();
            if (categoria != null) {
                producto.setCategoriaId(categoria.getId());
                producto.setCategoriaNombre(categoria.getNombre());
            }
            
            if (producto.getId() == null) {
                productoService.crearProducto(producto);
                AlertUtil.showInfo("Éxito", "Producto creado correctamente");
            } else {
                productoService.actualizarProducto(producto);
                AlertUtil.showInfo("Éxito", "Producto actualizado correctamente");
            }
            
            guardado = true;
            dialogStage.close();
            
        } catch (ValidationException e) {
            AlertUtil.showWarning("Validación", e.getMessage());
        } catch (NumberFormatException e) {
            AlertUtil.showError("Error", "Formato de número inválido");
        } catch (ApplicationException e) {
            logger.error("Error guardando producto", e);
            AlertUtil.showError("Error", "Error guardando producto: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }
    
    private boolean validarCampos() {
        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validación", "El código es requerido");
            return false;
        }
        
        if (txtNombre.getText() == null || txtNombre.getText().trim().isEmpty()) {
            AlertUtil.showWarning("Validación", "El nombre es requerido");
            return false;
        }
        
        try {
            new BigDecimal(txtPrecioVenta.getText().trim());
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validación", "El precio de venta debe ser un número válido");
            return false;
        }
        
        try {
            String stockText = txtStock.getText().trim();
            if (!stockText.isEmpty()) {
                Integer.parseInt(stockText);
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validación", "El stock debe ser un número entero");
            return false;
        }
        
        try {
            String stockMinText = txtStockMinimo.getText().trim();
            if (!stockMinText.isEmpty()) {
                Integer.parseInt(stockMinText);
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validación", "El stock mínimo debe ser un número entero");
            return false;
        }
        
        return true;
    }
}
