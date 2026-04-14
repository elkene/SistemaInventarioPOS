package com.inventario.exceptions;

/**
 * Excepción para errores de acceso a datos
 */
public class DataAccessException extends ApplicationException {
    
    public DataAccessException(String message) {
        super(message);
    }
    
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DataAccessException(Throwable cause) {
        super(cause);
    }
}
