package com.inventario.controllers;

import com.inventario.exceptions.ApplicationException;
import com.inventario.models.CorteCaja;
import com.inventario.services.VentaService;
import com.inventario.utils.AlertUtil;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class ReporteCortesController {

    private static final Logger logger = LoggerFactory.getLogger(ReporteCortesController.class);

    @FXML private TableView<CorteCaja> tablaCortes;
    @FXML private TableColumn<CorteCaja, String> colId;
    @FXML private TableColumn<CorteCaja, String> colFecha;
    @FXML private TableColumn<CorteCaja, String> colUsuario;
    @FXML private TableColumn<CorteCaja, String> colVentas;
    @FXML private TableColumn<CorteCaja, BigDecimal> colTotal;
    @FXML private TableColumn<CorteCaja, BigDecimal> colFondo;
    @FXML private TableColumn<CorteCaja, BigDecimal> colEsperado;
    @FXML private TableColumn<CorteCaja, BigDecimal> colContado;
    @FXML private TableColumn<CorteCaja, BigDecimal> colDiferencia;

    @FXML
    public void initialize() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFechaCreacion().format(formato)));
        colUsuario.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuarioNombre()));
        colVentas.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNumeroVentas())));
        dinero(colTotal, CorteCaja::getTotalVendido);
        dinero(colFondo, CorteCaja::getFondoInicial);
        dinero(colEsperado, k -> k.getFondoInicial().add(k.getVentasEfectivo()));
        dinero(colContado, CorteCaja::getEfectivoContado);
        dinero(colDiferencia, CorteCaja::getDiferencia);

        try {
            tablaCortes.setItems(FXCollections.observableArrayList(new VentaService().obtenerCortesRecientes(200)));
        } catch (ApplicationException e) {
            logger.error("Error cargando cortes de caja", e);
            AlertUtil.showError("Cortes de caja", "Error cargando cortes: " + e.getMessage());
        }
    }

    private static void dinero(TableColumn<CorteCaja, BigDecimal> columna, Function<CorteCaja, BigDecimal> valor) {
        columna.setCellValueFactory(c -> new SimpleObjectProperty<>(valor.apply(c.getValue())));
        columna.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal monto, boolean empty) {
                super.updateItem(monto, empty);
                setText(empty || monto == null ? null : String.format("$%,.2f", monto));
                // Resalta faltantes en rojo
                setStyle(!empty && monto != null && monto.signum() < 0 ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "");
            }
        });
    }
}
