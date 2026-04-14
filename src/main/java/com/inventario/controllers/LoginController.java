package com.inventario.controllers;

import com.inventario.services.AuthenticationService;
import com.inventario.models.Usuario;
import com.inventario.exceptions.AuthenticationException;
import com.inventario.utils.AlertUtil;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller para la pantalla de login
 */
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginBtn;
    @FXML private Label errorLabel;
    
    private AuthenticationService authService;
    
    @FXML
    public void initialize() {
        authService = new AuthenticationService();
        
        // Permitir login con Enter
        passwordField.setOnKeyPressed(event -> {
            switch(event.getCode()) {
                case ENTER:
                    handleLogin();
                    break;
                default:
                    break;
            }
        });
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        try {
            // Intentar autenticar
            Usuario usuario = authService.authenticate(username, password);
            
            if (usuario != null) {
                logger.info("Login exitoso para: " + username);
                
                // Navegar a la pantalla principal
                navigateToMain();
            }
        } catch (AuthenticationException e) {
            // Mostrar mensaje de error genérico
            errorLabel.setText("Credenciales inválidas");
            passwordField.clear();
            
            logger.warn("Intento de login fallido: " + username);
        } catch (Exception e) {
            errorLabel.setText("Error en autenticación. Intente nuevamente.");
            logger.error("Error durante autenticación", e);
        }
    }
    
    private void navigateToMain() {
        try {
            // Obtener la ventana actual
            Stage currentStage = (Stage) loginBtn.getScene().getWindow();
            
            // Crear la escena principal
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/main.fxml"));
            javafx.scene.Parent root = loader.load();
            
            Scene scene = new Scene(root, 1400, 900);
            currentStage.setScene(scene);
            currentStage.setTitle("Sistema de Inventario y Punto de Ventas");
            currentStage.centerOnScreen();
            currentStage.show();
            
        } catch (Exception e) {
            logger.error("Error cargando pantalla principal", e);
            AlertUtil.showError("Error", "Error cargando pantalla principal");
        }
    }
}
