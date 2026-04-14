package com.inventario.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportesController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportesController.class);
    
    @FXML private VBox contenedorReportes;
    
    public void initialize() {
        logger.info("Módulo de reportes inicializado");
    }
    
    @FXML
    private void handleReporteVentas() {
        logger.info("Abriendo reporte de ventas");
        cargarReporte("/fxml/reporte_ventas.fxml");
    }
    
    @FXML
    private void handleReporteInventario() {
        logger.info("Abriendo reporte de inventario");
        cargarReporte("/fxml/reporte_inventario.fxml");
    }
    
    @FXML
    private void handleReporteMovimientos() {
        logger.info("Abriendo reporte de movimientos");
        cargarReporte("/fxml/reporte_movimientos.fxml");
    }
    
    @FXML
    private void handleReporteGanancias() {
        logger.info("Abriendo reporte de ganancias");
        cargarReporte("/fxml/reporte_ganancias.fxml");
    }
    
    private void cargarReporte(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent reporteView = loader.load();
            
            contenedorReportes.getChildren().clear();
            contenedorReportes.getChildren().add(reporteView);
            
        } catch (Exception e) {
            logger.error("Error cargando reporte: " + fxmlPath, e);
            e.printStackTrace();
        }
    }
}
