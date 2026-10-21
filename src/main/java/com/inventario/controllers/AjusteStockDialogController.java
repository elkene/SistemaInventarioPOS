package com.inventario.controllers;

import com.inventario.models.Producto;
import com.inventario.models.Proveedor;
import com.inventario.services.MovimientoInventarioService;
import com.inventario.services.ProveedorService;
import javafx.collections.FXCollections;
import javafx.scene.layout.VBox;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AjusteStockDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(AjusteStockDialogController.class);
    
    private MovimientoInventarioService movimientoService;
    
    @FXML private Label lblProductoNombre;
    @FXML private Label lblStockActual;
    @FXML private RadioButton rbEntrada;
    @FXML private RadioButton rbSalida;
    @FXML private RadioButton rbAjuste;
    @FXML private TextField txtCantidad;
    @FXML private Label lblCantidad;
    @FXML private VBox boxProveedor;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    
    private Producto producto;
    private Stage dialogStage;
    private boolean guardado = false;
    
    public AjusteStockDialogController() {
        this.movimientoService = null;
    }
    
    public void setData(MovimientoInventarioService movimientoService) {
        this.movimientoService = movimientoService;
    }
    
    public void setProducto(Producto producto) {
        this.producto = producto;
        lblProductoNombre.setText(producto.getNombre() + " (" + producto.getCodigo() + ")");
        lblStockActual.setText(String.valueOf(producto.getStock()));
    }
    
    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
    
    public boolean isGuardado() {
        return guardado;
    }
    
    @FXML
    public void initialize() {
        ToggleGroup toggleGroup = new ToggleGroup();
        rbEntrada.setToggleGroup(toggleGroup);
        rbSalida.setToggleGroup(toggleGroup);
        rbAjuste.setToggleGroup(toggleGroup);

        // El proveedor solo aplica a entradas; en un ajuste se captura el stock contado
        boxProveedor.visibleProperty().bind(rbEntrada.selectedProperty());
        boxProveedor.managedProperty().bind(rbEntrada.selectedProperty());
        lblCantidad.textProperty().bind(javafx.beans.binding.Bindings.when(rbAjuste.selectedProperty())
                .then("Stock contado:").otherwise("Cantidad:"));
        rbEntrada.setSelected(true);

        try {
            cmbProveedor.setItems(FXCollections.observableArrayList(new ProveedorService().obtenerTodos()));
        } catch (ApplicationException e) {
            logger.error("Error cargando proveedores", e);
        }
    }
    
    @FXML
    private void handleGuardar() {
        String cantidadText = txtCantidad.getText().trim();
        
        if (cantidadText.isEmpty()) {
            AlertUtil.showWarning("Validación", "La cantidad es requerida");
            return;
        }
        
        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadText);
            if (rbAjuste.isSelected() ? cantidad < 0 : cantidad <= 0) {
                AlertUtil.showWarning("Validación", rbAjuste.isSelected()
                        ? "El stock contado no puede ser negativo"
                        : "La cantidad debe ser mayor a cero");
                return;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Validación", "La cantidad debe ser un número entero");
            return;
        }
        
        if (rbSalida.isSelected() && cantidad > producto.getStock()) {
            AlertUtil.showWarning("Validación", "Stock insuficiente. Stock actual: " + producto.getStock());
            return;
        }
        
        try {
            String descripcion = txtDescripcion.getText().trim();
            
            if (rbEntrada.isSelected()) {
                Proveedor proveedor = cmbProveedor.getValue();
                movimientoService.registrarEntrada(producto.getId(), cantidad, descripcion,
                        proveedor != null ? proveedor.getId() : null);
                AlertUtil.showInfo("Éxito", "Entrada de stock registrada correctamente");
            } else if (rbSalida.isSelected()) {
                movimientoService.registrarSalida(producto.getId(), cantidad, descripcion);
                AlertUtil.showInfo("Éxito", "Salida de stock registrada correctamente");
            } else {
                movimientoService.registrarAjuste(producto.getId(), cantidad, descripcion);
                AlertUtil.showInfo("Éxito", "Ajuste de stock registrado correctamente");
            }
            
            guardado = true;
            dialogStage.close();
            
        } catch (ApplicationException e) {
            logger.error("Error ajustando stock", e);
            AlertUtil.showError("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        dialogStage.close();
    }
}
