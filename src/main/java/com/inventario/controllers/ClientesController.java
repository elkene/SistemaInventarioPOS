package com.inventario.controllers;

import com.inventario.models.Cliente;
import com.inventario.services.ClienteService;
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

public class ClientesController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientesController.class);
    
    @FXML private TextField txtBuscar;
    @FXML private TableView<Cliente> tablaClientes;
    @FXML private TableColumn<Cliente, Long> colId;
    @FXML private TableColumn<Cliente, String> colCedula;
    @FXML private TableColumn<Cliente, String> colNombre;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colCiudad;
    @FXML private TableColumn<Cliente, Double> colSaldo;
    @FXML private TableColumn<Cliente, Void> colAcciones;
    @FXML private Label lblTotalClientes;
    
    private ClienteService clienteService;
    private ObservableList<Cliente> clientesObservable;
    
    public void initialize() {
        clienteService = new ClienteService();
        clientesObservable = FXCollections.observableArrayList();
        
        configurarTabla();
        cargarClientes();
    }
    
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCedula.setCellValueFactory(new PropertyValueFactory<>("cedula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCiudad.setCellValueFactory(new PropertyValueFactory<>("ciudad"));
        colSaldo.setCellValueFactory(new PropertyValueFactory<>("saldoCredito"));
        
        colSaldo.setCellFactory(column -> new TableCell<Cliente, Double>() {
            @Override
            protected void updateItem(Double saldo, boolean empty) {
                super.updateItem(saldo, empty);
                if (empty || saldo == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", saldo));
                }
            }
        });
        
        colAcciones.setCellFactory(column -> new TableCell<Cliente, Void>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            
            {
                btnEditar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 4 12 4 12;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 4 12 4 12;");
                
                btnEditar.setOnAction(e -> {
                    Cliente cliente = getTableRow().getItem();
                    if (cliente != null) {
                        handleEditar(cliente);
                    }
                });
                
                btnEliminar.setOnAction(e -> {
                    Cliente cliente = getTableRow().getItem();
                    if (cliente != null) {
                        handleEliminar(cliente);
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
        
        tablaClientes.setItems(clientesObservable);
    }
    
    private void cargarClientes() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodos();
            clientesObservable.setAll(clientes);
            lblTotalClientes.setText(String.format("%d clientes", clientes.size()));
        } catch (ApplicationException e) {
            logger.error("Error cargando clientes", e);
            AlertUtil.showError("Error", "Error cargando clientes");
        }
    }
    
    @FXML
    private void handleBuscar() {
        String texto = txtBuscar.getText().trim();
        
        if (texto.isEmpty()) {
            cargarClientes();
            return;
        }
        
        try {
            List<Cliente> clientes = clienteService.buscarPorNombre(texto);
            clientesObservable.setAll(clientes);
            lblTotalClientes.setText(String.format("%d clientes encontrados", clientes.size()));
        } catch (ApplicationException e) {
            logger.error("Error buscando clientes", e);
            AlertUtil.showError("Error", "Error buscando clientes");
        }
    }
    
    @FXML
    private void handleNuevo() {
        mostrarDialogoCliente(null);
    }
    
    @FXML
    private void handleEditar(Cliente cliente) {
        mostrarDialogoCliente(cliente);
    }
    
    @FXML
    private void handleEliminar(Cliente cliente) {
        if (cliente == null) {
            return;
        }
        
        if (AlertUtil.showConfirmation("Confirmar eliminación", 
            "¿Está seguro de eliminar al cliente " + cliente.getNombre() + "?")) {
            try {
                clienteService.eliminarCliente(cliente.getId());
                AlertUtil.showInfo("Éxito", "Cliente eliminado correctamente");
                cargarClientes();
            } catch (ApplicationException e) {
                logger.error("Error eliminando cliente", e);
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleRefrescar() {
        txtBuscar.clear();
        cargarClientes();
    }
    
    private void mostrarDialogoCliente(Cliente cliente) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle(cliente == null ? "Nuevo Cliente" : "Editar Cliente");
            dialogStage.centerOnScreen();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente_dialog.fxml"));
            ClienteDialogController controller = new ClienteDialogController(clienteService, cliente);
            loader.setController(controller);
            
            Parent root = loader.load();
            Scene scene = new Scene(root, 500, 550);
            dialogStage.setScene(scene);
            
            controller.setDialogStage(dialogStage);
            
            dialogStage.showAndWait();
            
            if (controller.isGuardado()) {
                cargarClientes();
            }
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de cliente", e);
            AlertUtil.showError("Error", "Error abriendo formulario de cliente");
        }
    }
}
