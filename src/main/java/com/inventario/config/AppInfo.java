package com.inventario.config;

/**
 * Nombre, autor y versión que se muestran en "Acerca de" y en el login.
 */
public final class AppInfo {

    public static final String NOMBRE = "Sistema de Inventario y Punto de Venta";
    public static final String AUTOR = "Kenneth Armenta Ibarra";
    public static final String CONTACTO = "armentakenneth00@gmail.com";
    public static final String COPYRIGHT = "© 2026 " + AUTOR + ". Todos los derechos reservados.";

    private AppInfo() {
    }

    /** Versión del pom.xml, tomada del manifiesto del .jar ("desarrollo" al correr desde el IDE o Maven). */
    public static String version() {
        String version = AppInfo.class.getPackage().getImplementationVersion();
        return version != null ? version : "desarrollo";
    }
}
