package com.inventario.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Carpetas donde la aplicación guarda sus datos.
 * Por defecto: %USERPROFILE%\SistemaInventarioPOS (se puede cambiar con -Dinventario.home=...)
 */
public final class AppPaths {

    public static final Path HOME = Paths.get(System.getProperty("inventario.home",
            Paths.get(System.getProperty("user.home"), "SistemaInventarioPOS").toString()));
    public static final Path DATOS = HOME.resolve("datos");
    public static final Path RESPALDOS = HOME.resolve("respaldos");
    public static final Path TICKETS = HOME.resolve("tickets");
    public static final Path LOGS = HOME.resolve("logs");
    public static final Path NEGOCIO = HOME.resolve("negocio.properties");

    /** Nombre de la base de datos H2 dentro de DATOS (archivo inventario.mv.db) */
    public static final String DB_NOMBRE = "inventario";

    private AppPaths() {
    }

    public static Path carpeta(Path carpeta) throws IOException {
        return Files.createDirectories(carpeta);
    }
}
