package com.inventario.controllers;

import com.inventario.models.Proveedor;
import com.inventario.services.ProveedorService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProveedorDialogController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProveedorDialogController.class);
    
    @FXML private TextField txtRif;
    @FXML private TextField txtNombre;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtCiudad;
    @FXML private TextField txtCondicionesPago;
    
    private final ProveedorService proveedorService;
    private Proveedor proveedor;
    private Stage dialogStage;
    private boolean guardado = false;
    
    public ProveedorDialogController(ProveedorService proveedorService, Proveedor proveedor) {
        this.proveedorService = proveedorService;
        this.proveedor = proveedor;
    }
    
    @FXML
    public void initialize() {
        if (proveedor != null) {
            txtRif.setText(proveedor.getRif());
            txtNombre.setText(proveedor.getNombre());
            txtEmail.setText(proveedor.getEmail());
            txtTelefono.setText(proveedor.getTelefono());
            txtDireccion.setText(proveedor.getDireccion());
            txtCiudad.setText(proveedor.getCiudad());
            txtCondicionesPago.setText(proveedor.getCondicionesPago());
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
            if (proveedor == null) {
                proveedor = new Proveedor();
            }
            
            proveedor.setRif(txtRif.getText().trim());
            proveedor.setNombre(txtNombre.getText().trim());
            proveedor.setEmail(txtEmail.getText().trim());
            proveedor.setTelefono(txtTelefono.getText().trim());
            proveedor.setDireccion(txtDireccion.getText().trim());
            proveedor.setCiudad(txtCiudad.getText().trim());
            proveedor.setCondicionesPago(txtCondicionesPago.getText().trim());
            
            if (proveedor.getId() == null) {
                proveedorService.crearProveedor(proveedor);
                AlertUtil.showInfo("Éxito", "Proveedor creado correctamente");
            } else {
                proveedorService.actualizarProveedor(proveedor);
                AlertUtil.showInfo("Éxito", "Proveedor actualizado correctamente");
            }
            
            guardado = true;
            dialogStage.close();
            
        } catch (ApplicationException e) {
            logger.error("Error guardando proveedor", e);
            AlertUtil.showError("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleCancelar() {
        guardado = false;
        dialogStage.close();
    }
    
    private boolean validarCampos() {
        String rif = txtRif.getText().trim();
        String nombre = txtNombre.getText().trim();
        
        if (rif.isEmpty()) {
            AlertUtil.showWarning("Validación", "El RFC es requerido");
            txtRif.requestFocus();
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
