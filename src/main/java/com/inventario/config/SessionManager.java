package com.inventario.config;

import com.inventario.models.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Gestor de sesiones para mantener la sesión del usuario autenticado
 */
public class SessionManager {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutos
    private static final long ABSOLUTE_TIMEOUT = 8 * 60 * 60 * 1000; // 8 horas
    
    private static SessionManager instance;
    
    private String sessionId;
    private Usuario currentUser;
    private long createdAt;
    private long lastAccessTime;
    private long absoluteCreatedAt;
    
    private SessionManager() {
        // Constructor privado para patrón Singleton
    }
    
    /**
     * Obtiene la instancia única del SessionManager
     */
    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Crea una nueva sesión para un usuario autenticado
     */
    public void createSession(Usuario usuario) {
        this.sessionId = generateSecureSessionId();
        this.currentUser = usuario;
        this.createdAt = System.currentTimeMillis();
        this.lastAccessTime = createdAt;
        this.absoluteCreatedAt = createdAt;
        
        logger.info("Nueva sesión creada para usuario: " + usuario.getUsername());
    }
    
    /**
     * Verifica si la sesión es válida
     */
    public boolean isSessionValid() {
        if (currentUser == null || sessionId == null) {
            return false;
        }
        
        long now = System.currentTimeMillis();
        
        // Verificar timeout por inactividad
        if (now - lastAccessTime > SESSION_TIMEOUT) {
            logger.warn("Sesión expirada por inactividad");
            destroySession();
            return false;
        }
        
        // Verificar timeout absoluto
        if (now - absoluteCreatedAt > ABSOLUTE_TIMEOUT) {
            logger.warn("Sesión expirada por timeout absoluto");
            destroySession();
            return false;
        }
        
        // Actualizar tiempo de último acceso
        lastAccessTime = now;
        return true;
    }
    
    /**
     * Obtiene el usuario actual
     */
    public Usuario getCurrentUser() {
        if (isSessionValid()) {
            return currentUser;
        }
        return null;
    }
    
    /**
     * Obtiene el ID de sesión
     */
    public String getSessionId() {
        return sessionId;
    }
    
    /**
     * Destruye la sesión actual
     */
    public void destroySession() {
        if (currentUser != null) {
            logger.info("Sesión destruida para usuario: " + currentUser.getUsername());
        }
        
        sessionId = null;
        currentUser = null;
        createdAt = 0;
        lastAccessTime = 0;
        absoluteCreatedAt = 0;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return isSessionValid();
    }
    
    /**
     * Genera un ID de sesión seguro de 128 bits
     */
    private String generateSecureSessionId() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBytes = new byte[32]; // 256 bits
        secureRandom.nextBytes(randomBytes);
        
        return Base64.getEncoder().encodeToString(randomBytes);
    }
    
    /**
     * Obtiene información de la sesión (para debugging)
     */
    public String getSessionInfo() {
        if (currentUser == null) {
            return "Sin sesión activa";
        }
        
        long now = System.currentTimeMillis();
        long minutosInactivo = (now - lastAccessTime) / 60000;
        
        return "Usuario: " + currentUser.getUsername() + 
               " | Inactivo: " + minutosInactivo + " min" +
               " | Activa: " + isSessionValid();
    }
}
