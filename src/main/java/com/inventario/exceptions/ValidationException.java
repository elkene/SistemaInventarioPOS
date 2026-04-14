package com.inventario.exceptions;

/**
 * Excepción para errores de validación
 */
public class ValidationException extends ApplicationException {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
