package com.inventario.services;

import com.inventario.config.SessionManager;
import com.inventario.dao.IUsuarioDAO;
import com.inventario.dao.factory.DAOFactory;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.exceptions.ValidationException;
import com.inventario.models.Usuario;
import com.inventario.utils.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Administración de usuarios (solo ADMIN). Nunca deja al sistema sin un administrador activo.
 */
public class UsuarioService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    public static final List<String> ROLES = List.of("ADMIN", "VENDEDOR");
    public static final String PASSWORD_EMERGENCIA = "admin123";

    private final IUsuarioDAO usuarioDAO;

    public UsuarioService() {
        this.usuarioDAO = DAOFactory.getUsuarioDAO();
    }

    public List<Usuario> obtenerTodos() throws ApplicationException {
        return usuarioDAO.readAll();
    }

    public void crear(String username, String email, String password, String rol) throws ApplicationException {
        validarAdmin();
        username = username == null ? "" : username.trim();
        email = email == null ? "" : email.trim();
        if (username.isEmpty()) {
            throw new ValidationException("El nombre de usuario es requerido");
        }
        if (email.isEmpty()) {
            throw new ValidationException("El email es requerido");
        }
        validarPassword(password);
        validarRol(rol);
        if (usuarioDAO.findByUsername(username) != null) {
            throw new ValidationException("Ya existe el usuario " + username);
        }
        if (usuarioDAO.findByEmail(email) != null) {
            throw new ValidationException("Ya existe un usuario con el email " + email);
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setPasswordHash(PasswordHasher.hashPassword(password));
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuarioDAO.create(usuario);
        logger.info("Usuario creado: " + username + " (" + rol + ")");
    }

    public void cambiarRol(Long usuarioId, String rol) throws ApplicationException {
        validarRol(rol);
        Usuario usuario = obtenerOtroUsuario(usuarioId, "cambiar su propio rol");
        if (!"ADMIN".equals(rol)) {
            validarQueNoEsUltimoAdmin(usuario);
        }
        usuario.setRol(rol);
        usuarioDAO.update(usuario);
        logger.info("Rol de " + usuario.getUsername() + " cambiado a " + rol);
    }

    public void cambiarActivo(Long usuarioId, boolean activo) throws ApplicationException {
        Usuario usuario = obtenerOtroUsuario(usuarioId, "desactivarse a sí mismo");
        if (!activo) {
            validarQueNoEsUltimoAdmin(usuario);
        }
        usuario.setActivo(activo);
        usuarioDAO.update(usuario);
        logger.info("Usuario " + usuario.getUsername() + (activo ? " activado" : " desactivado"));
    }

    /** El administrador asigna una contraseña nueva (por ejemplo, si el usuario la olvidó). */
    public void restablecerPassword(Long usuarioId, String nueva) throws ApplicationException {
        validarAdmin();
        validarPassword(nueva);
        Usuario usuario = usuarioDAO.read(usuarioId);
        if (usuario == null) {
            throw new ValidationException("Usuario no encontrado");
        }
        usuario.setPasswordHash(PasswordHasher.hashPassword(nueva));
        usuarioDAO.update(usuario);
        logger.info("Contraseña restablecida para " + usuario.getUsername());
    }

    /**
     * Recuperación de emergencia (se ejecuta al abrir el sistema con --restablecer-admin):
     * deja al usuario "admin" activo, con rol ADMIN y contraseña admin123. No requiere sesión
     * porque quien puede abrir el programa en la laptop ya tiene acceso al archivo de datos.
     */
    public void restablecerAdminDeEmergencia() throws DataAccessException {
        Usuario admin = usuarioDAO.findByUsername("admin");
        if (admin == null) {
            admin = new Usuario();
            admin.setUsername("admin");
            admin.setEmail("admin@sucursal.local");
        }
        admin.setPasswordHash(PasswordHasher.hashPassword(PASSWORD_EMERGENCIA));
        admin.setRol("ADMIN");
        admin.setActivo(true);
        if (admin.getId() == null) {
            usuarioDAO.create(admin);
        } else {
            usuarioDAO.update(admin);
        }
        logger.warn("Usuario admin restablecido por recuperación de emergencia");
    }

    private Usuario obtenerOtroUsuario(Long usuarioId, String accionPropia) throws ApplicationException {
        Usuario actual = validarAdmin();
        if (actual.getId().equals(usuarioId)) {
            throw new ValidationException("No puede " + accionPropia + ". Pídeselo a otro administrador.");
        }
        Usuario usuario = usuarioDAO.read(usuarioId);
        if (usuario == null) {
            throw new ValidationException("Usuario no encontrado");
        }
        return usuario;
    }

    private void validarQueNoEsUltimoAdmin(Usuario usuario) throws ApplicationException {
        if (!"ADMIN".equals(usuario.getRol()) || !usuario.isActivo()) {
            return;
        }
        long adminsActivos = usuarioDAO.findActivos().stream().filter(u -> "ADMIN".equals(u.getRol())).count();
        if (adminsActivos <= 1) {
            throw new ValidationException("Debe quedar al menos un administrador activo");
        }
    }

    private Usuario validarAdmin() throws ValidationException {
        Usuario actual = SessionManager.getInstance().getCurrentUser();
        if (actual == null || !"ADMIN".equals(actual.getRol())) {
            throw new ValidationException("Solo un administrador puede administrar usuarios");
        }
        return actual;
    }

    private static void validarPassword(String password) throws ValidationException {
        if (password == null || password.length() < 6) {
            throw new ValidationException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    private static void validarRol(String rol) throws ValidationException {
        if (!ROLES.contains(rol)) {
            throw new ValidationException("Rol inválido: " + rol);
        }
    }
}
