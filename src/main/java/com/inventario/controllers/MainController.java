package com.inventario.controllers;

import com.inventario.config.SessionManager;
import com.inventario.models.Usuario;
import com.inventario.services.ProductoService;
import com.inventario.services.VentaService;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller principal de la aplicación
 */
public class MainController {
    
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);
    
    @FXML private Label usuarioLabel;
    @FXML private Label horaLabel;
    @FXML private Button logoutBtn;
    @FXML private StackPane contentPane;
    
    @FXML private Button btnInventario;
    @FXML private Button btnVentas;
    @FXML private Button btnClientes;
    @FXML private Button btnProveedores;
    @FXML private Button btnReportes;
    
    @FXML private Label lblProductos;
    @FXML private Label lblVentas;
    @FXML private Label lblStockBajo;
    
    private ProductoService productoService;
    private VentaService ventaService;
    private DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    @FXML
    public void initialize() {
        productoService = new ProductoService();
        ventaService = new VentaService();
        
        // Obtener usuario actual
        Usuario usuarioActual = SessionManager.getInstance().getCurrentUser();
        if (usuarioActual != null) {
            usuarioLabel.setText("Usuario: " + usuarioActual.getUsername());
        } else {
            usuarioLabel.setText("Usuario desconocido");
        }
        
        // Actualizar la hora cada segundo
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            horaLabel.setText(LocalDateTime.now().format(hourFormatter));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        
        // Cargar datos del dashboard
        cargarDashboard();
    }
    
    private void cargarDashboard() {
        try {
            // Contar productos
            long totalProductos = productoService.obtenerTodos().size();
            lblProductos.setText(String.valueOf(totalProductos));
            
            // Contar productos con stock bajo
            long productosStockBajo = productoService.obtenerProductosStockBajo().size();
            lblStockBajo.setText(String.valueOf(productosStockBajo));
            
            // Contar ventas de hoy
            long ventasHoy = ventaService.contarVentasHoy();
            lblVentas.setText(String.valueOf(ventasHoy));
            
        } catch (ApplicationException e) {
            logger.error("Error cargando dashboard", e);
            AlertUtil.showError("Error", "Error cargando datos del dashboard");
        }
    }
    
    @FXML
    private void handleInventario() {
        logger.info("Abriendo módulo de inventario");
        try {
            StackPane mainContainer = (StackPane) btnInventario.getScene().lookup("#contentPane");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/inventario.fxml"));
            Parent inventarioView = loader.load();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(inventarioView);
            
        } catch (Exception e) {
            logger.error("Error cargando módulo de inventario", e);
            AlertUtil.showError("Error", "Error cargando módulo de inventario: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleVentas() {
        logger.info("Abriendo módulo de ventas");
        try {
            StackPane mainContainer = (StackPane) btnVentas.getScene().lookup("#contentPane");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ventas.fxml"));
            Parent ventasView = loader.load();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(ventasView);
            
        } catch (Exception e) {
            logger.error("Error cargando módulo de ventas", e);
            AlertUtil.showError("Error", "Error cargando módulo de ventas: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleClientes() {
        logger.info("Abriendo módulo de clientes");
        try {
            StackPane mainContainer = (StackPane) btnClientes.getScene().lookup("#contentPane");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/clientes.fxml"));
            Parent clientesView = loader.load();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(clientesView);
            
        } catch (Exception e) {
            logger.error("Error cargando módulo de clientes", e);
            AlertUtil.showError("Error", "Error cargando módulo de clientes: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleProveedores() {
        logger.info("Abriendo módulo de proveedores");
        try {
            StackPane mainContainer = (StackPane) btnProveedores.getScene().lookup("#contentPane");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/proveedores.fxml"));
            Parent proveedoresView = loader.load();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(proveedoresView);
            
        } catch (Exception e) {
            logger.error("Error cargando módulo de proveedores", e);
            AlertUtil.showError("Error", "Error cargando módulo de proveedores: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleReportes() {
        logger.info("Abriendo módulo de reportes");
        try {
            StackPane mainContainer = (StackPane) btnReportes.getScene().lookup("#contentPane");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/reportes.fxml"));
            Parent reportesView = loader.load();
            
            mainContainer.getChildren().clear();
            mainContainer.getChildren().add(reportesView);
            
        } catch (Exception e) {
            logger.error("Error cargando módulo de reportes", e);
            AlertUtil.showError("Error", "Error cargando módulo de reportes: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogout() {
        if (AlertUtil.showConfirmation("Confirmar", "¿Desea cerrar sesión?")) {
            SessionManager.getInstance().destroySession();
            logger.info("Sesión cerrada");
            
            try {
                // Volver a pantalla de login
                Stage stage = (Stage) logoutBtn.getScene().getWindow();
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/login.fxml"));
                javafx.scene.Parent root = loader.load();
                
                Scene scene = new Scene(root, 600, 500);
                stage.setScene(scene);
                stage.setTitle("Iniciar Sesión");
                stage.centerOnScreen();
                stage.show();
                
            } catch (Exception e) {
                logger.error("Error cargando pantalla de login", e);
                AlertUtil.showError("Error", "Error cargando pantalla de login");
            }
        }
    }
}
