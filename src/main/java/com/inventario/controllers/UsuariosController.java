package com.inventario.controllers;

import com.inventario.exceptions.ApplicationException;
import com.inventario.models.Usuario;
import com.inventario.services.UsuarioService;
import com.inventario.utils.AlertUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Administración de usuarios (pantalla solo visible para ADMIN)
 */
public class UsuariosController {

    private static final Logger logger = LoggerFactory.getLogger(UsuariosController.class);

    @FXML private TableView<Usuario> tablaUsuarios;
    @FXML private TableColumn<Usuario, Long> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colEmail;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colEstado;

    private final UsuarioService usuarioService = new UsuarioService();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));
        colEstado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().isActivo() ? "Activo" : "Inactivo"));
        cargar();
    }

    private void cargar() {
        try {
            tablaUsuarios.setItems(FXCollections.observableArrayList(usuarioService.obtenerTodos()));
        } catch (ApplicationException e) {
            logger.error("Error cargando usuarios", e);
            AlertUtil.showError("Usuarios", "Error cargando usuarios: " + e.getMessage());
        }
    }

    @FXML
    private void handleNuevo() {
        TextField username = new TextField();
        TextField email = new TextField();
        PasswordField password = new PasswordField();
        ComboBox<String> rol = new ComboBox<>(FXCollections.observableArrayList(UsuarioService.ROLES));
        rol.setValue("VENDEDOR");
        if (!confirmar("Nuevo usuario", null,
                "Usuario:", username, "Email:", email, "Contraseña (mín. 6):", password, "Rol:", rol)) {
            return;
        }
        ejecutar("Usuario " + username.getText().trim() + " creado",
                () -> usuarioService.crear(username.getText(), email.getText(), password.getText(), rol.getValue()));
    }

    @FXML
    private void handleCambiarRol() {
        Usuario usuario = seleccionado();
        if (usuario == null) {
            return;
        }
        ComboBox<String> rol = new ComboBox<>(FXCollections.observableArrayList(UsuarioService.ROLES));
        rol.setValue(usuario.getRol());
        if (confirmar("Cambiar rol", "Usuario: " + usuario.getUsername(), "Rol:", rol)) {
            ejecutar("Rol actualizado", () -> usuarioService.cambiarRol(usuario.getId(), rol.getValue()));
        }
    }

    @FXML
    private void handleActivar() {
        Usuario usuario = seleccionado();
        if (usuario == null) {
            return;
        }
        boolean activar = !usuario.isActivo();
        if (AlertUtil.showConfirmation("Usuarios", "¿" + (activar ? "Activar" : "Desactivar") + " al usuario "
                + usuario.getUsername() + "?" + (activar ? "" : "\nYa no podrá iniciar sesión."))) {
            ejecutar("Usuario " + (activar ? "activado" : "desactivado"),
                    () -> usuarioService.cambiarActivo(usuario.getId(), activar));
        }
    }

    @FXML
    private void handleRestablecer() {
        Usuario usuario = seleccionado();
        if (usuario == null) {
            return;
        }
        PasswordField nueva = new PasswordField();
        PasswordField confirmacion = new PasswordField();
        if (!confirmar("Restablecer contraseña", "Usuario: " + usuario.getUsername(),
                "Nueva contraseña:", nueva, "Confirmar:", confirmacion)) {
            return;
        }
        if (!nueva.getText().equals(confirmacion.getText())) {
            AlertUtil.showWarning("Restablecer contraseña", "Las contraseñas no coinciden");
            return;
        }
        ejecutar("Contraseña restablecida. Entrégasela al usuario para que la cambie al entrar.",
                () -> usuarioService.restablecerPassword(usuario.getId(), nueva.getText()));
    }

    private Usuario seleccionado() {
        Usuario usuario = tablaUsuarios.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            AlertUtil.showWarning("Usuarios", "Seleccione un usuario en la tabla");
        }
        return usuario;
    }

    /** Muestra un formulario de pares etiqueta/campo; devuelve true si se pulsó Aceptar. */
    private boolean confirmar(String titulo, String encabezado, Object... etiquetasYCampos) {
        GridPane campos = new GridPane();
        campos.setHgap(10);
        campos.setVgap(10);
        for (int i = 0; i < etiquetasYCampos.length; i += 2) {
            campos.addRow(i / 2, new Label((String) etiquetasYCampos[i]), (Node) etiquetasYCampos[i + 1]);
        }
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(encabezado);
        dialogo.getDialogPane().setContent(campos);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialogo.initOwner(tablaUsuarios.getScene().getWindow());
        return dialogo.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private interface Accion {
        void ejecutar() throws ApplicationException;
    }

    private void ejecutar(String mensajeExito, Accion accion) {
        try {
            accion.ejecutar();
            AlertUtil.showInfo("Usuarios", mensajeExito);
            cargar();
        } catch (ApplicationException e) {
            logger.error("Error administrando usuarios", e);
            AlertUtil.showError("Usuarios", e.getMessage());
        }
    }
}
