package com.inventario.controllers;

import com.inventario.models.Cliente;
import com.inventario.services.ClienteService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClienteDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteDialogController.class);
    
    @FXML private TextField txtCedula;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtEstado;
    @FXML private TextField txtSaldoCredito;
    
    private final ClienteService clienteService;
    private Cliente cliente;
    private Stage dialogStage;
    private boolean guardado = false;
    
    public ClienteDialogController(ClienteService clienteService, Cliente cliente) {
        this.clienteService = clienteService;
        this.cliente = cliente;
    }
    
    @FXML
    public void initialize() {
        if (cliente != null) {
            txtCedula.setText(cliente.getCedula());
            txtNombre.setText(cliente.getNombre());
            txtEmail.setText(cliente.getEmail());
            txtTelefono.setText(cliente.getTelefono());
            txtDireccion.setText(cliente.getDireccion());
            txtCiudad.setText(cliente.getCiudad());
            txtEstado.setText(cliente.getEstado());
            if (cliente.getSaldoCredito() != null) {
                txtSaldoCredito.setText(String.valueOf(cliente.getSaldoCredito()));
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
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }
        
        try {
            if (cliente == null) {
                cliente = new Cliente();
            }
            
            cliente.setCedula(txtCedula.getText().trim());
            cliente.setNombre(txtNombre.getText().trim());
            cliente.setEmail(txtEmail.getText().trim());
            cliente.setTelefono(txtTelefono.getText().trim());
            cliente.setDireccion(txtDireccion.getText().trim());
            cliente.setCiudad(txtCiudad.getText().trim());
            cliente.setEstado(txtEstado.getText().trim());
            
            String saldoText = txtSaldoCredito.getText().trim();
            if (!saldoText.isEmpty()) {
                cliente.setSaldoCredito(Double.parseDouble(saldoText));
            } else {
                cliente.setSaldoCredito(0.0);
            }
            
            if (cliente.getId() == null) {
                clienteService.crearCliente(cliente);
                AlertUtil.showInfo("Éxito", "Cliente creado correctamente");
            } else {
                clienteService.actualizarCliente(cliente);
                AlertUtil.showInfo("Éxito", "Cliente actualizado correctamente");
            }
            
            guardado = true;
            dialogStage.close();
            
        } catch (NumberFormatException e) {
            AlertUtil.showError("Error", "El saldo de crédito debe ser un número válido");
        } catch (ApplicationException e) {
            logger.error("Error guardando cliente", e);
            AlertUtil.showError("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        guardado = false;
        dialogStage.close();
    }
    
    private boolean validarCampos() {
        String cedula = txtCedula.getText().trim();
        String nombre = txtNombre.getText().trim();
        
        if (cedula.isEmpty()) {
            AlertUtil.showWarning("Validación", "La cédula es requerida");
            txtCedula.requestFocus();
            return false;
        }
        
        if (nombre.isEmpty()) {
            AlertUtil.showWarning("Validación", "El nombre es requerido");
            txtNombre.requestFocus();
            return false;
        }
        
        return true;
    }
}
