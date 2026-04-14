package com.inventario.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Utilidad para mostrar alertas en JavaFX
 */
public class AlertUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertUtil.class);
    
    private AlertUtil() {
        // Constructor privado
    }
    
    /**
     * Muestra un diálogo de información
     */
    public static void showInfo(String title, String message, Window owner) {
        showAlert(AlertType.INFORMATION, title, message, owner);
    }
    
    /**
     * Muestra un diálogo de información
     */
    public static void showInfo(String title, String message) {
        showAlert(AlertType.INFORMATION, title, message, null);
    }
    
    /**
     * Muestra un diálogo de advertencia
     */
    public static void showWarning(String title, String message, Window owner) {
        showAlert(AlertType.WARNING, title, message, owner);
    }
    
    /**
     * Muestra un diálogo de advertencia
     */
    public static void showWarning(String title, String message) {
        showAlert(AlertType.WARNING, title, message, null);
    }
    
    /**
     * Muestra un diálogo de error
     */
    public static void showError(String title, String message, Window owner) {
        showAlert(AlertType.ERROR, title, message, owner);
    }
    
    /**
     * Muestra un diálogo de error
     */
    public static void showError(String title, String message) {
        showAlert(AlertType.ERROR, title, message, null);
    }
    
    /**
     * Muestra un diálogo de confirmación
     */
    public static boolean showConfirmation(String title, String message, Window owner) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        if (owner != null) {
            alert.initOwner(owner);
        }
        
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }
    
    /**
     * Muestra un diálogo de confirmación
     */
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(title, message, null);
    }
    
    private static void showAlert(AlertType type, String title, String message, Window owner) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        if (owner != null) {
            alert.initOwner(owner);
        }
        
        alert.showAndWait();
        logger.info("Alerta mostrada: " + title + " - " + message);
    }
}
