package com.inventario.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad para hash y verificación de contraseñas
 * Usa BCrypt con work factor 10
 */
public class PasswordHasher {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    
    private PasswordHasher() {
        // Constructor privado para evitar instanciación
    }
    
    /**
     * Codifica una contraseña en texto plano
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Contraseña no puede estar vacía");
        }
        
        String hashedPassword = encoder.encode(plainPassword);
        logger.debug("Contraseña hasheada exitosamente");
        return hashedPassword;
    }
    
    /**
     * Verifica si una contraseña en texto plano coincide con el hash
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            logger.warn("Intento de verificación con valores nulos");
            return false;
        }
        
        return encoder.matches(plainPassword, hashedPassword);
    }
}
