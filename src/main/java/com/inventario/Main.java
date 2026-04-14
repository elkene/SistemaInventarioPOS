package com.inventario;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import com.inventario.database.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Clase principal de la aplicación JavaFX
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Iniciando aplicación Sistema de Inventario POS");
            
            // Cargar FXML de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Parent root = loader.load();
            // Crear escena
            Scene scene = new Scene(root, 600, 500);

            // Configurar ventana
            primaryStage.setTitle("Sistema de Inventario y Punto de Ventas - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(600);
            primaryStage.setMinHeight(500);
            
            primaryStage.show();
            
            logger.info("Aplicación iniciada correctamente");
            
        } catch (Exception e) {
            logger.error("Error iniciando aplicación", e);
            e.printStackTrace();
        }
    }
    
    @Override
    public void stop() {
        logger.info("Deteniendo aplicación");
        
        // Cerrar conexión con la base de datos
        try {
            ConnectionFactory.closeDataSource();
            logger.info("DataSource cerrado correctamente");
        } catch (Exception e) {
            logger.error("Error cerrando DataSource", e);
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
