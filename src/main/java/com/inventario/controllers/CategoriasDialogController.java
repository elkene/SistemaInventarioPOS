package com.inventario.controllers;

import com.inventario.models.Categoria;
import com.inventario.services.CategoriaService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.ValidationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.stage.Stage;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CategoriasDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(CategoriasDialogController.class);
    
    private final CategoriaService categoriaService;
    
    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, String> colNombre;
    @FXML private TableColumn<Categoria, String> colDescripcion;
    
    @FXML private TextField txtNombre;
    @FXML private TextArea txtDescripcion;
    @FXML private Button btnGuardar;
    @FXML private Button btnNuevo;
    @FXML private Button btnEliminar;
    @FXML private Button btnCancelar;
    
    private ObservableList<Categoria> categoriasObservable;
    private Categoria categoriaSeleccionada;
    
    public CategoriasDialogController() {
        this.categoriaService = new CategoriaService();
    }
    
    @FXML
    public void initialize() {
        categoriasObservable = FXCollections.observableArrayList();
        
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        tablaCategorias.setItems(categoriasObservable);
        
        tablaCategorias.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> seleccionarCategoria(newValue));
        
        cargarCategorias();
    }
    
    private void cargarCategorias() {
        try {
            var categorias = categoriaService.obtenerTodas();
            categoriasObservable.setAll(categorias);
        } catch (ApplicationException e) {
            logger.error("Error cargando categorías", e);
            AlertUtil.showError("Error", "Error cargando categorías: " + e.getMessage());
        }
    }
    
    private void seleccionarCategoria(Categoria categoria) {
        this.categoriaSeleccionada = categoria;
        if (categoria != null) {
            txtNombre.setText(categoria.getNombre());
            txtDescripcion.setText(categoria.getDescripcion());
            btnGuardar.setText("Actualizar");
        } else {
            limpiarCampos();
        }
    }
    
    @FXML
    private void handleNuevo() {
        categoriaSeleccionada = null;
        limpiarCampos();
        txtNombre.requestFocus();
    }
    
    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        
        if (nombre.isEmpty()) {
            AlertUtil.showWarning("Validación", "El nombre es requerido");
            return;
        }
        
        try {
            if (categoriaSeleccionada == null) {
                Categoria nueva = new Categoria();
                nueva.setNombre(nombre);
                nueva.setDescripcion(descripcion);
                categoriaService.crearCategoria(nueva);
                AlertUtil.showInfo("Éxito", "Categoría creada correctamente");
            } else {
                categoriaSeleccionada.setNombre(nombre);
                categoriaSeleccionada.setDescripcion(descripcion);
                categoriaService.actualizarCategoria(categoriaSeleccionada);
                AlertUtil.showInfo("Éxito", "Categoría actualizada correctamente");
            }
            
            limpiarCampos();
            cargarCategorias();
            
        } catch (ValidationException e) {
            AlertUtil.showWarning("Validación", e.getMessage());
        } catch (ApplicationException e) {
            logger.error("Error guardando categoría", e);
            AlertUtil.showError("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleEliminar() {
        if (categoriaSeleccionada == null) {
            AlertUtil.showWarning("Advertencia", "Seleccione una categoría para eliminar");
            return;
        }
        
        if (!AlertUtil.showConfirmation("Confirmar", 
                "¿Está seguro de eliminar la categoría '" + categoriaSeleccionada.getNombre() + "'?")) {
            return;
        }
        
        try {
            categoriaService.eliminarCategoria(categoriaSeleccionada.getId());
            AlertUtil.showInfo("Éxito", "Categoría eliminada correctamente");
            limpiarCampos();
            cargarCategorias();
        } catch (ApplicationException e) {
            logger.error("Error eliminando categoría", e);
            AlertUtil.showError("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        ((Stage) txtNombre.getScene().getWindow()).close();
    }
    
    private void limpiarCampos() {
        txtNombre.clear();
        txtDescripcion.clear();
        btnGuardar.setText("Guardar");
        tablaCategorias.getSelectionModel().clearSelection();
    }
}
