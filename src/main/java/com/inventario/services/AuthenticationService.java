package com.inventario.services;

import com.inventario.dao.factory.DAOFactory;
import com.inventario.dao.IUsuarioDAO;
import com.inventario.models.Usuario;
import com.inventario.exceptions.AuthenticationException;
import com.inventario.exceptions.DataAccessException;
import com.inventario.utils.PasswordHasher;
import com.inventario.config.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servicio de autenticación
 */
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final IUsuarioDAO usuarioDAO;
    private static final int MAX_INTENTOS = 5;
    private static final long MINUTOS_BLOQUEO = 5;
    // Estáticos para que volver a cargar la pantalla de login no reinicie el conteo
    private static int intentosFallidos = 0;
    private static long bloqueadoHasta = 0;
    
    public AuthenticationService() {
        this.usuarioDAO = DAOFactory.getUsuarioDAO();
    }
    
    /**
     * Autentica a un usuario con su nombre de usuario y contraseña
     */
    public Usuario authenticate(String username, String password) throws AuthenticationException {
        
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.warn("Intento de autenticación con credenciales vacías");
            throw new AuthenticationException("Credenciales inválidas");
        }
        
        long restante = bloqueadoHasta - System.currentTimeMillis();
        if (restante > 0) {
            throw new AuthenticationException("Demasiados intentos fallidos. Espere " +
                    (restante / 60000 + 1) + " minuto(s) e intente de nuevo.");
        }
        
        try {
            Usuario usuario = usuarioDAO.findByUsername(username);
            
            if (usuario == null || !usuario.isActivo()) {
                registrarFallo();
                logger.warn("Usuario no encontrado o inactivo: " + username);
                throw new AuthenticationException("Credenciales inválidas");
            }
            
            if (!PasswordHasher.verifyPassword(password, usuario.getPasswordHash())) {
                registrarFallo();
                logger.warn("Contraseña incorrecta para usuario: " + username);
                throw new AuthenticationException("Credenciales inválidas");
            }
            
            // Autenticación exitosa
            intentosFallidos = 0;
            
            // Actualizar último login
            usuarioDAO.updateUltimoLogin(usuario.getId());
            
            // Crear sesión
            SessionManager.getInstance().createSession(usuario);
            
            logger.info("Usuario autenticado exitosamente: " + username);
            return usuario;
            
        } catch (DataAccessException e) {
            logger.error("Error accediendo a la base de datos durante autenticación", e);
            throw new AuthenticationException("Error en la autenticación. Intente nuevamente.", e);
        }
    }
    
    private static void registrarFallo() {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            bloqueadoHasta = System.currentTimeMillis() + MINUTOS_BLOQUEO * 60_000;
            intentosFallidos = 0;
            logger.warn("Login bloqueado " + MINUTOS_BLOQUEO + " minutos por intentos fallidos");
        }
    }

    /**
     * Cambia la contraseña de un usuario
     */
    public void changePassword(Long usuarioId, String oldPassword, String newPassword) throws AuthenticationException {
        
        if (newPassword == null || newPassword.length() < 6) {
            throw new AuthenticationException("La nueva contraseña debe tener al menos 6 caracteres");
        }
        
        try {
            Usuario usuario = usuarioDAO.read(usuarioId);
            
            if (usuario == null) {
                throw new AuthenticationException("Usuario no encontrado");
            }
            
            if (!PasswordHasher.verifyPassword(oldPassword, usuario.getPasswordHash())) {
                logger.warn("Intento fallido de cambio de contraseña para usuario: " + usuarioId);
                throw new AuthenticationException("La contraseña actual es incorrecta");
            }
            
            usuario.setPasswordHash(PasswordHasher.hashPassword(newPassword));
            usuarioDAO.update(usuario);
            
            logger.info("Contraseña cambiada para usuario: " + usuarioId);
            
        } catch (DataAccessException e) {
            logger.error("Error cambiando contraseña", e);
            throw new AuthenticationException("Error cambiando contraseña", e);
        }
    }
}
