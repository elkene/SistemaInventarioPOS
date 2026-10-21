package com.inventario;

import com.inventario.config.AppPaths;
import com.inventario.database.ConnectionFactory;
import com.inventario.exceptions.ApplicationException;
import com.inventario.services.UsuarioService;
import com.inventario.utils.AlertUtil;
import com.inventario.utils.RespaldoUtil;
import com.inventario.utils.TicketPdf;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Clase principal de la aplicación JavaFX
 */
public class Main extends Application {

    // Debe ir antes que el Logger: slf4j-simple lee esta configuración al crear el primer logger
    static {
        try {
            AppPaths.carpeta(AppPaths.LOGS);
            System.setProperty("org.slf4j.simpleLogger.logFile", AppPaths.LOGS.resolve("aplicacion.log").toString());
        } catch (IOException e) {
            System.err.println("No se pudo crear la carpeta de logs: " + e.getMessage());
        }
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "yyyy-MM-dd HH:mm:ss");
    }

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Iniciando aplicación Sistema de Inventario POS");

        try {
            ConnectionFactory.inicializar();
            TicketPdf.prepararNegocio();
        } catch (SQLException | IOException e) {
            logger.error("No se pudo abrir la base de datos", e);
            // 90020: el archivo está bloqueado por otra instancia de la aplicación
            boolean enUso = e instanceof SQLException && ((SQLException) e).getErrorCode() == 90020;
            AlertUtil.showError("No se pudo iniciar", enUso
                    ? "El sistema ya está abierto en otra ventana."
                    : "No se pudo abrir la base de datos en " + AppPaths.DATOS + "\n\n" + e.getMessage());
            Platform.exit();
            return;
        }

        if (getParameters().getRaw().contains("--restablecer-admin")) {
            try {
                new UsuarioService().restablecerAdminDeEmergencia();
                AlertUtil.showInfo("Recuperación de administrador", "El usuario admin quedó activo con la contraseña "
                        + UsuarioService.PASSWORD_EMERGENCIA + ".\nCámbiala en cuanto entres.");
            } catch (ApplicationException e) {
                logger.error("Error restableciendo admin", e);
                AlertUtil.showError("Recuperación de administrador", "No se pudo restablecer: " + e.getMessage());
            }
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root, 600, 500);

            primaryStage.setTitle("Sistema de Inventario y Punto de Ventas - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            primaryStage.show();

            logger.info("Aplicación iniciada correctamente");
        } catch (Exception e) {
            logger.error("Error iniciando aplicación", e);
            AlertUtil.showError("Error", "Error iniciando la aplicación: " + e.getMessage());
            Platform.exit();
        }
    }

    @Override
    public void stop() {
        logger.info("Deteniendo aplicación");
        if (ConnectionFactory.isAvailable()) {
            RespaldoUtil.respaldoAutomatico();
        }
        ConnectionFactory.closeDataSource();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
