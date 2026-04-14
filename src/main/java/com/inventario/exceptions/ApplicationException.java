package com.inventario.exceptions;

/**
 * Excepción base para la aplicación
 */
public class ApplicationException extends Exception {
    
    public ApplicationException(String message) {
        super(message);
    }
    
    public ApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ApplicationException(Throwable cause) {
        super(cause);
    }
}
