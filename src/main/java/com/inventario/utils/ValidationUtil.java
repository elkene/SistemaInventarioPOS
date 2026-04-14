package com.inventario.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilidad para validaciones comunes
 */
public class ValidationUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);
    
    private ValidationUtil() {
        // Constructor privado
    }
    
    /**
     * Valida que una cadena no esté vacía
     */
    public static boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * Valida que un email tenga formato válido
     */
    public static boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email != null && email.matches(emailRegex);
    }
    
    /**
     * Valida que una cédula tenga formato válido
     */
    public static boolean isValidCedula(String cedula) {
        return cedula != null && cedula.matches("^[0-9]{6,12}$");
    }
    
    /**
     * Valida que un RIF tenga formato válido
     */
    public static boolean isValidRIF(String rif) {
        return rif != null && rif.matches("^[0-9]{8,12}$");
    }
    
    /**
     * Valida que un número de teléfono tenga formato válido
     */
    public static boolean isValidTelefono(String telefono) {
        return telefono != null && telefono.matches("^[0-9+\\-\\s()]{7,}$");
    }
    
    /**
     * Valida que una cantidad sea positiva
     */
    public static boolean isPositive(Number number) {
        return number != null && number.doubleValue() > 0;
    }
    
    /**
     * Valida que un valor esté dentro de un rango
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null || min == null || max == null) {
            return false;
        }
        
        double v = value.doubleValue();
        double minVal = min.doubleValue();
        double maxVal = max.doubleValue();
        
        return v >= minVal && v <= maxVal;
    }
}
