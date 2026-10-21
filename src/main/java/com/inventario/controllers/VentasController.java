package com.inventario.controllers;

import com.inventario.models.*;
import com.inventario.services.*;
import com.inventario.exceptions.ApplicationException;
import com.inventario.utils.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inventario.config.SessionManager;
import com.inventario.utils.TicketPdf;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class VentasController {
    
    private static final Logger logger = LoggerFactory.getLogger(VentasController.class);
    
    @FXML private TextField txtCodigoProducto;
    @FXML private TextField txtBuscarProducto;
    @FXML private TableView<ItemVenta> tablaCarrito;
    @FXML private TableColumn<ItemVenta, String> colCodigo;
    @FXML private TableColumn<ItemVenta, String> colProducto;
    @FXML private TableColumn<ItemVenta, Integer> colCantidad;
    @FXML private TableColumn<ItemVenta, BigDecimal> colPrecio;
    @FXML private TableColumn<ItemVenta, BigDecimal> colSubtotal;
    @FXML private TableColumn<ItemVenta, Void> colAcciones;
    
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private ComboBox<String> cmbMetodoPago;
    
    @FXML private Label lblSubtotal;
    @FXML private Label lblDescuento;
    @FXML private Label lblTotal;
    @FXML private TextField txtDescuento;
    @FXML private TextField txtPagaCon;
    @FXML private Label lblCambio;
    
    @FXML private Label lblVentasHoy;
    @FXML private Label lblTotalHoy;
    
    private ProductoService productoService;
    private ClienteService clienteService;
    private VentaService ventaService;
    
    private ObservableList<ItemVenta> carritoObservable;
    private BigDecimal subtotal = BigDecimal.ZERO;
    private BigDecimal descuento = BigDecimal.ZERO;
    private BigDecimal total = BigDecimal.ZERO;
    private Long ultimaVentaId;
    
    public void initialize() {
        productoService = new ProductoService();
        clienteService = new ClienteService();
        ventaService = new VentaService();
        
        carritoObservable = FXCollections.observableArrayList();
        
        configurarTabla();
        configurarCombos();
        cargarEstadisticas();
        
        txtCodigoProducto.requestFocus();
    }
    
    private void configurarTabla() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("productoCodigo"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("productoNombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        colSubtotal.setCellValueFactory(new PropertyValueFactory<>("subtotal"));
        
        colPrecio.setCellFactory(column -> new TableCell<ItemVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal precio, boolean empty) {
                super.updateItem(precio, empty);
                if (empty || precio == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", precio));
                }
            }
        });
        
        colSubtotal.setCellFactory(column -> new TableCell<ItemVenta, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", subtotal));
                }
            }
        });
        
        colCantidad.setCellFactory(column -> new TableCell<ItemVenta, Integer>() {
            @Override
            protected void updateItem(Integer cantidad, boolean empty) {
                super.updateItem(cantidad, empty);
                if (empty || cantidad == null) {
                    setText(null);
                } else {
                    setText(String.valueOf(cantidad));
                }
            }
        });
        
        colAcciones.setCellFactory(column -> new TableCell<ItemVenta, Void>() {
            private final Button btnIncrementar = new Button("+");
            private final Button btnDecrementar = new Button("-");
            private final Button btnEliminar = new Button("X");
            
            {
                btnIncrementar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-padding: 2 8 2 8;");
                btnDecrementar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-padding: 2 8 2 8;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 2 8 2 8;");
                
                btnIncrementar.setOnAction(e -> {
                    ItemVenta item = getTableRow().getItem();
                    if (item != null) {
                        if (!item.incrementarCantidad()) {
                            avisarSinStock(item.getProductoNombre(), item.getStockDisponible());
                        }
                        tablaCarrito.refresh();
                        calcularTotales();
                    }
                });
                
                btnDecrementar.setOnAction(e -> {
                    ItemVenta item = getTableRow().getItem();
                    if (item != null) {
                        item.decrementarCantidad();
                        tablaCarrito.refresh();
                        calcularTotales();
                    }
                });
                
                btnEliminar.setOnAction(e -> {
                    ItemVenta item = getTableRow().getItem();
                    if (item != null) {
                        carritoObservable.remove(item);
                        calcularTotales();
                    }
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, btnIncrementar, btnDecrementar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        });
        
        tablaCarrito.setItems(carritoObservable);
    }
    
    private void configurarCombos() {
        cmbMetodoPago.setItems(FXCollections.observableArrayList(
            "EFECTIVO",
            "TARJETA_DEBITO",
            "TARJETA_CREDITO",
            "TRANSFERENCIA",
            "MOVIL"
        ));
        cmbMetodoPago.getSelectionModel().selectFirst();
        
        try {
            // Por defecto la venta no se asigna a ningún cliente registrado
            Cliente publicoGeneral = new Cliente();
            publicoGeneral.setNombre("Público en general");
            ObservableList<Cliente> clientes = FXCollections.observableArrayList(publicoGeneral);
            clientes.addAll(clienteService.obtenerTodos());
            cmbCliente.setItems(clientes);
            cmbCliente.getSelectionModel().selectFirst();
        } catch (ApplicationException e) {
            logger.error("Error cargando clientes", e);
        }
    }
    
    private void cargarEstadisticas() {
        try {
            long ventasHoy = ventaService.contarVentasHoy();
            BigDecimal totalHoy = ventaService.calcularTotalVentasHoy();
            
            lblVentasHoy.setText(String.valueOf(ventasHoy));
            lblTotalHoy.setText(String.format("$%.2f", totalHoy));
        } catch (ApplicationException e) {
            logger.error("Error cargando estadísticas", e);
        }
    }
    
    @FXML
    private void handleBuscarPorCodigo() {
        String codigo = txtCodigoProducto.getText().trim();
        if (codigo.isEmpty()) {
            return;
        }
        
        try {
            Producto producto = productoService.buscarPorCodigo(codigo);
            if (producto != null && !producto.isActivo()) {
                AlertUtil.showWarning("Producto desactivado", producto.getNombre() + " está desactivado y no se puede vender");
            } else if (producto != null) {
                agregarProductoAlCarrito(producto);
                txtCodigoProducto.clear();
                txtCodigoProducto.requestFocus();
            } else {
                AlertUtil.showWarning("Producto no encontrado", "No existe un producto con el código: " + codigo);
            }
        } catch (ApplicationException e) {
            logger.error("Error buscando producto", e);
            AlertUtil.showError("Error", "Error buscando producto");
        }
    }
    
    @FXML
    private void handleBuscarProducto() {
        mostrarDialogoBusquedaProducto();
    }
    
    @FXML
    private void handleCobrar() {
        if (carritoObservable.isEmpty()) {
            AlertUtil.showWarning("Carrito vacío", "Agregue productos antes de cobrar");
            return;
        }
        
        String metodoPago = cmbMetodoPago.getValue();
        if (metodoPago == null || metodoPago.isEmpty()) {
            AlertUtil.showWarning("Método de pago", "Seleccione un método de pago");
            return;
        }
        
        Cliente cliente = cmbCliente.getValue();

        try {
            String descuentoText = txtDescuento.getText().trim();
            if (!descuentoText.isEmpty()) {
                descuento = new BigDecimal(descuentoText);
            } else {
                descuento = BigDecimal.ZERO;
            }

            BigDecimal pagaCon = null;
            if ("EFECTIVO".equals(metodoPago) && !txtPagaCon.getText().trim().isEmpty()) {
                pagaCon = new BigDecimal(txtPagaCon.getText().trim());
                if (pagaCon.compareTo(subtotal.subtract(descuento)) < 0) {
                    AlertUtil.showWarning("Pago insuficiente", "El monto recibido es menor al total");
                    return;
                }
            }

            List<DetalleVenta> detalles = carritoObservable.stream()
                .map(ItemVenta::toDetalleVenta)
                .toList();

            Long clienteId = cliente != null ? cliente.getId() : null;

            Venta venta = ventaService.registrarVenta(detalles, clienteId, metodoPago, descuento, null);
            if (clienteId != null) {
                venta.setClienteNombre(cliente.getNombre());
            }
            BigDecimal cambio = pagaCon != null ? pagaCon.subtract(venta.getTotal()) : BigDecimal.ZERO;
            ultimaVentaId = venta.getId();

            ButtonType imprimir = new ButtonType("Imprimir ticket");
            Alert confirmacion = new Alert(Alert.AlertType.INFORMATION,
                    "Total: " + String.format("$%,.2f", venta.getTotal()) +
                    (cambio.signum() > 0 ? "\nCambio: " + String.format("$%,.2f", cambio) : ""),
                    imprimir, ButtonType.CLOSE);
            confirmacion.setTitle("Venta completada");
            confirmacion.setHeaderText("Venta #" + venta.getId() + " registrada");
            if (confirmacion.showAndWait().orElse(ButtonType.CLOSE) == imprimir) {
                Usuario cajero = SessionManager.getInstance().getCurrentUser();
                TicketPdf.abrir(TicketPdf.ticketVenta(venta, detalles,
                        cajero != null ? cajero.getUsername() : "", pagaCon, cambio));
            }

            carritoObservable.clear();
            txtDescuento.clear();
            txtPagaCon.clear();
            calcularTotales();
            cargarEstadisticas();

            txtCodigoProducto.requestFocus();

        } catch (NumberFormatException e) {
            AlertUtil.showError("Monto inválido", "El descuento y el pago deben ser numéricos");
        } catch (IOException e) {
            logger.error("Error generando ticket", e);
            AlertUtil.showError("Ticket", "La venta se guardó, pero no se pudo generar el ticket: " + e.getMessage());
        } catch (ApplicationException e) {
            logger.error("Error procesando venta", e);
            AlertUtil.showError("Error", e.getMessage());
        }
    }
    
    @FXML
    private void handleLimpiarCarrito() {
        if (carritoObservable.isEmpty()) {
            return;
        }
        
        if (AlertUtil.showConfirmation("Limpiar carrito", "¿Está seguro de vaciar el carrito?")) {
            carritoObservable.clear();
            calcularTotales();
        }
    }
    
    @FXML
    private void handleDescuentoChanged() {
        calcularTotales();
    }
    
    private void agregarProductoAlCarrito(Producto producto) {
        for (ItemVenta item : carritoObservable) {
            if (item.getProductoId().equals(producto.getId())) {
                if (!item.incrementarCantidad()) {
                    avisarSinStock(producto.getNombre(), item.getStockDisponible());
                }
                tablaCarrito.refresh();
                calcularTotales();
                return;
            }
        }

        if (producto.getStock() == null || producto.getStock() <= 0) {
            avisarSinStock(producto.getNombre(), 0);
            return;
        }

        ItemVenta nuevoItem = new ItemVenta(producto);
        carritoObservable.add(nuevoItem);
        calcularTotales();
    }
    
    private void calcularTotales() {
        subtotal = BigDecimal.ZERO;
        for (ItemVenta item : carritoObservable) {
            subtotal = subtotal.add(item.getSubtotal());
        }
        
        try {
            String descuentoText = txtDescuento.getText().trim();
            if (!descuentoText.isEmpty()) {
                descuento = new BigDecimal(descuentoText);
            } else {
                descuento = BigDecimal.ZERO;
            }
        } catch (NumberFormatException e) {
            descuento = BigDecimal.ZERO;
        }
        
        total = subtotal.subtract(descuento);
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        
        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblDescuento.setText(String.format("-$%.2f", descuento));
        lblTotal.setText(String.format("$%.2f", total));

        BigDecimal cambio = BigDecimal.ZERO;
        try {
            String pagaConText = txtPagaCon.getText().trim();
            if (!pagaConText.isEmpty()) {
                cambio = new BigDecimal(pagaConText).subtract(total).max(BigDecimal.ZERO);
            }
        } catch (NumberFormatException e) {
            cambio = BigDecimal.ZERO;
        }
        lblCambio.setText(String.format("$%.2f", cambio));
    }

    private void avisarSinStock(String producto, Integer disponible) {
        AlertUtil.showWarning("Stock insuficiente",
                producto + ": solo hay " + (disponible != null ? disponible : 0) + " unidad(es) disponibles");
    }

    @FXML
    private void handleReimprimirTicket() {
        TextInputDialog dialogo = new TextInputDialog(ultimaVentaId != null ? String.valueOf(ultimaVentaId) : "");
        dialogo.setTitle("Reimprimir ticket");
        dialogo.setHeaderText("Número de venta (aparece como Ticket # en el ticket)");
        dialogo.setContentText("Venta #:");
        String texto = dialogo.showAndWait().orElse("").trim();
        if (texto.isEmpty()) {
            return;
        }
        try {
            Venta venta = ventaService.obtenerVenta(Long.parseLong(texto));
            if (venta == null) {
                AlertUtil.showWarning("Reimprimir ticket", "No existe la venta #" + texto);
                return;
            }
            TicketPdf.abrir(TicketPdf.reimpresion(venta, ventaService.obtenerDetallesVenta(venta.getId())));
        } catch (NumberFormatException e) {
            AlertUtil.showWarning("Reimprimir ticket", "Escriba solo el número de la venta");
        } catch (ApplicationException | IOException e) {
            logger.error("Error reimprimiendo ticket", e);
            AlertUtil.showError("Reimprimir ticket", "No se pudo generar el ticket: " + e.getMessage());
        }
    }

    @FXML
    private void handleCorteCaja() {
        TextField txtFondo = new TextField("0");
        TextField txtContado = new TextField();
        txtContado.setPromptText("Efectivo en caja");
        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(10);
        campos.addRow(0, new Label("Fondo inicial:"), txtFondo);
        campos.addRow(1, new Label("Efectivo contado:"), txtContado);

        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle("Corte de caja");
        dialogo.setHeaderText("Corte de caja del día " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        if (dialogo.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            BigDecimal fondo = new BigDecimal(txtFondo.getText().trim().isEmpty() ? "0" : txtFondo.getText().trim());
            BigDecimal contado = new BigDecimal(txtContado.getText().trim());
            CorteCaja corte = ventaService.registrarCorte(fondo, contado);
            Map<String, BigDecimal> porMetodo = ventaService.totalesPorMetodoPago(corte.getFecha());
            AlertUtil.showInfo("Corte de caja", "Corte #" + corte.getId() + " guardado.\nDiferencia: "
                    + String.format("$%,.2f", corte.getDiferencia())
                    + (corte.getDiferencia().signum() < 0 ? " (faltante)" : corte.getDiferencia().signum() > 0 ? " (sobrante)" : ""));
            TicketPdf.abrir(TicketPdf.corteCaja(corte, porMetodo));
        } catch (NumberFormatException e) {
            AlertUtil.showError("Corte de caja", "El fondo inicial y el efectivo contado deben ser numéricos");
        } catch (ApplicationException | IOException e) {
            logger.error("Error generando corte de caja", e);
            AlertUtil.showError("Corte de caja", "No se pudo generar el corte: " + e.getMessage());
        }
    }
    
    private void mostrarDialogoBusquedaProducto() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Buscar Producto");
            dialogStage.centerOnScreen();
            
            BuscarProductoController controller = new BuscarProductoController(productoService);
            
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                getClass().getResource("/fxml/buscar_producto_dialog.fxml"));
            loader.setController(controller);
            
            javafx.scene.Parent root = loader.load();
            javafx.scene.Scene scene = new javafx.scene.Scene(root, 600, 400);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
            Producto productoSeleccionado = controller.getProductoSeleccionado();
            if (productoSeleccionado != null) {
                agregarProductoAlCarrito(productoSeleccionado);
            }
            
        } catch (Exception e) {
            logger.error("Error mostrando diálogo de búsqueda", e);
            AlertUtil.showError("Error", "Error abriendo búsqueda de productos");
        }
    }
}
