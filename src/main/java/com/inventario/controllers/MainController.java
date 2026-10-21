package com.inventario.controllers;

import com.inventario.config.AppInfo;
import com.inventario.config.AppPaths;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.inventario.config.SessionManager;
import com.inventario.exceptions.AuthenticationException;
import com.inventario.services.AuthenticationService;
import com.inventario.utils.RespaldoUtil;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
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
    @FXML private Button btnRespaldos;
    @FXML private Button btnUsuarios;
    
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
            usuarioLabel.setText("Usuario: " + usuarioActual.getUsername() + " (" + usuarioActual.getRol() + ")");
        } else {
            usuarioLabel.setText("Usuario desconocido");
        }

        // El vendedor solo cobra y atiende clientes; inventario, proveedores, reportes y respaldos son del administrador
        boolean esAdmin = SessionManager.getInstance().esAdmin();
        for (Button soloAdmin : new Button[]{btnInventario, btnProveedores, btnReportes, btnUsuarios, btnRespaldos}) {
            soloAdmin.setVisible(esAdmin);
            soloAdmin.setManaged(esAdmin);
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
    private void handleUsuarios() {
        logger.info("Abriendo módulo de usuarios");
        try {
            Parent usuariosView = new FXMLLoader(getClass().getResource("/fxml/usuarios.fxml")).load();
            contentPane.getChildren().setAll(usuariosView);
        } catch (Exception e) {
            logger.error("Error cargando módulo de usuarios", e);
            AlertUtil.showError("Error", "Error cargando módulo de usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void handleRespaldos() {
        ButtonType crear = new ButtonType("Crear respaldo");
        ButtonType restaurar = new ButtonType("Restaurar respaldo");
        Alert opciones = new Alert(Alert.AlertType.NONE,
                "Los respaldos automáticos se guardan al cerrar el sistema en:\n" + AppPaths.RESPALDOS +
                "\n\nCopia los respaldos a una USB o a la nube con regularidad.",
                crear, restaurar, ButtonType.CANCEL);
        opciones.setTitle("Respaldos");
        opciones.setHeaderText("Respaldo de la base de datos");
        Window ventana = btnRespaldos.getScene().getWindow();
        opciones.initOwner(ventana);

        ButtonType eleccion = opciones.showAndWait().orElse(ButtonType.CANCEL);
        FileChooser selector = new FileChooser();
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Respaldo (*.zip)", "*.zip"));

        try {
            if (eleccion == crear) {
                selector.setTitle("Guardar respaldo");
                selector.setInitialFileName(RespaldoUtil.nombreSugerido("respaldo"));
                File destino = selector.showSaveDialog(ventana);
                if (destino != null) {
                    RespaldoUtil.crearRespaldo(destino.toPath());
                    AlertUtil.showInfo("Respaldo", "Respaldo guardado en:\n" + destino);
                }
            } else if (eleccion == restaurar) {
                selector.setTitle("Elegir respaldo a restaurar");
                selector.setInitialDirectory(AppPaths.carpeta(AppPaths.RESPALDOS).toFile());
                File origen = selector.showOpenDialog(ventana);
                if (origen != null && AlertUtil.showConfirmation("Restaurar respaldo",
                        "Se reemplazarán TODOS los datos actuales por los del respaldo:\n" + origen +
                        "\n\nLos datos actuales se guardarán antes en la carpeta de respaldos. " +
                        "El sistema se cerrará al terminar. ¿Continuar?")) {
                    java.nio.file.Path copia = RespaldoUtil.restaurar(origen.toPath());
                    AlertUtil.showInfo("Respaldo restaurado",
                            "Datos restaurados. Los datos anteriores quedaron en:\n" + copia +
                            "\n\nVuelve a abrir el sistema.");
                    Platform.exit();
                }
            }
        } catch (Exception e) {
            logger.error("Error con respaldos", e);
            AlertUtil.showError("Error", "No se pudo completar la operación: " + e.getMessage());
        }
    }

    @FXML
    private void handleAcercaDe() {
        Alert acerca = new Alert(Alert.AlertType.INFORMATION);
        acerca.setTitle("Acerca de");
        acerca.setHeaderText(AppInfo.NOMBRE + "\nVersión " + AppInfo.version());
        ImageView icono = new ImageView(new Image(getClass().getResourceAsStream("/icons/app.png")));
        icono.setFitWidth(64);
        icono.setPreserveRatio(true);
        acerca.setGraphic(icono);
        acerca.setContentText(
                "Desarrollado por: " + AppInfo.AUTOR + "\n" +
                "Contacto: " + AppInfo.CONTACTO + "\n\n" +
                AppInfo.COPYRIGHT + "\n\n" +
                "Software propietario. Queda prohibida su copia, distribución, venta o modificación " +
                "sin autorización por escrito del autor. Consulte el archivo LICENCIA.txt.\n\n" +
                "Incluye bibliotecas de código abierto (OpenJFX, H2, HikariCP, SLF4J, Spring Security " +
                "Crypto, Apache Commons Logging, OpenPDF) bajo sus propias licencias.");
        // Sin esto el Alert corta el texto largo con "..."
        acerca.getDialogPane().setPrefWidth(500);
        acerca.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        acerca.initOwner(logoutBtn.getScene().getWindow());
        acerca.showAndWait();
    }

    @FXML
    private void handleCambiarPassword() {
        Usuario usuario = SessionManager.getInstance().getCurrentUser();
        if (usuario == null) {
            AlertUtil.showWarning("Sesión expirada", "Cierre sesión e ingrese de nuevo.");
            return;
        }

        PasswordField actual = new PasswordField();
        PasswordField nueva = new PasswordField();
        PasswordField confirmacion = new PasswordField();
        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(10);
        campos.addRow(0, new Label("Contraseña actual:"), actual);
        campos.addRow(1, new Label("Nueva contraseña:"), nueva);
        campos.addRow(2, new Label("Confirmar nueva:"), confirmacion);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Cambiar contraseña");
        dialogo.setHeaderText("Usuario: " + usuario.getUsername());
        dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.initOwner(logoutBtn.getScene().getWindow());

        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        if (!nueva.getText().equals(confirmacion.getText())) {
            AlertUtil.showWarning("Cambiar contraseña", "La nueva contraseña y su confirmación no coinciden");
            return;
        }
        try {
            new AuthenticationService().changePassword(usuario.getId(), actual.getText(), nueva.getText());
            AlertUtil.showInfo("Cambiar contraseña", "Contraseña actualizada");
        } catch (AuthenticationException e) {
            AlertUtil.showError("Cambiar contraseña", e.getMessage());
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
