package com.inventario.exceptions;

/**
 * Excepción para errores de autenticación
 */
public class AuthenticationException extends ApplicationException {
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
