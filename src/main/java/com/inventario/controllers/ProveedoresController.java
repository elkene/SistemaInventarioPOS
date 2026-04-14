package com.inventario.controllers;

import com.inventario.models.Proveedor;
import com.inventario.services.ProveedorService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ProveedoresController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProveedoresController.class);
    
    @FXML private TextField txtBuscar;
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Long> colId;
    @FXML private TableColumn<Proveedor, String> colRif;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colEmail;
    @FXML private TableColumn<Proveedor, String> colCiudad;
    @FXML private TableColumn<Proveedor, String> colCondiciones;
    @FXML private TableColumn<Proveedor, Void> colAcciones;
    @FXML private Label lblTotalProveedores;
    
    private ProveedorService proveedorService;
    private ObservableList<Proveedor> proveedoresObservable;
    
    public void initialize() {
        proveedorService = new ProveedorService();
        proveedoresObservable = FXCollections.observableArrayList();
        
        configurarTabla();
        cargarProveedores();
    }
    
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRif.setCellValueFactory(new PropertyValueFactory<>("rif"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colCondiciones.setCellValueFactory(new PropertyValueFactory<>("condicionesPago"));
        
        colAcciones.setCellFactory(column -> new TableCell<Proveedor, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            
            {
                btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 12 4 12;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 12 4 12;");
                
                btnEditar.setOnAction(e -> {
                    Proveedor proveedor = getTableRow().getItem();
                    if (proveedor != null) {
                        handleEditar(proveedor);
                    }
                });
                
                btnEliminar.setOnAction(e -> {
                    Proveedor proveedor = getTableRow().getItem();
                    if (proveedor != null) {
                        handleEliminar(proveedor);
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox hbox = new javafx.scene.layout.HBox(5, btnEditar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        });
        
        tablaProveedores.setItems(proveedoresObservable);
        
        tablaProveedores.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Proveedor proveedor = tablaProveedores.getSelectionModel().getSelectedItem();
                if (proveedor != null) {
                    handleEditar(proveedor);
                }
            }
        });
    }
    
    private void cargarProveedores() {
        try {
            List<Proveedor> proveedores = proveedorService.obtenerTodos();
            proveedoresObservable.setAll(proveedores);
            lblTotalProveedores.setText(String.format("%d proveedores", proveedores.size()));
        } catch (ApplicationException e) {
            logger.error("Error cargando proveedores", e);
            AlertUtil.showError("Error", "Error cargando proveedores");
        }
    }
    
    @FXML
    private void handleBuscar() {
        String texto = txtBuscar.getText().trim();
        
        if (texto.isEmpty()) {
            cargarProveedores();
            return;
        }
        
        try {
            List<Proveedor> proveedores = proveedorService.buscarPorNombre(texto);
            proveedoresObservable.setAll(proveedores);
            lblTotalProveedores.setText(String.format("%d proveedores encontrados", proveedores.size()));
        } catch (ApplicationException e) {
            logger.error("Error buscando proveedores", e);
            AlertUtil.showError("Error", "Error buscando proveedores");
        }
    }
    
    @FXML
    private void handleNuevo() {
        mostrarDialogoProveedor(null);
    }
    
    @FXML
    private void handleEditar(Proveedor proveedor) {
        mostrarDialogoProveedor(proveedor);
    }
    
    @FXML
    private void handleEliminar(Proveedor proveedor) {
        if (proveedor == null) {
            return;
        }
        
        if (AlertUtil.showConfirmation("Confirmar eliminación", 
            "¿Está seguro de eliminar al proveedor " + proveedor.getNombre() + "?")) {
            try {
                proveedorService.eliminarProveedor(proveedor.getId());
                AlertUtil.showInfo("Éxito", "Proveedor eliminado correctamente");
                cargarProveedores();
            } catch (ApplicationException e) {
                logger.error("Error eliminando proveedor", e);
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefrescar() {
        txtBuscar.clear();
        cargarProveedores();
    }
    
    private void mostrarDialogoProveedor(Proveedor proveedor) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle(proveedor == null ? "Nuevo Proveedor" : "Editar Proveedor");
            dialogStage.centerOnScreen();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/proveedor_dialog.fxml"));
            ProveedorDialogController controller = new ProveedorDialogController(proveedorService, proveedor);
            loader.setController(controller);
            
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 550);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isGuardado()) {
                cargarProveedores();
            }
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de proveedor", e);
            AlertUtil.showError("Error", "Error abriendo formulario de proveedor");
        }
    }
}
