package com.inventario.utils;

import com.inventario.config.AppPaths;
import com.inventario.database.ConnectionFactory;
import org.h2.tools.DeleteDbFiles;
import org.h2.tools.Restore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

/**
 * Respaldos de la base de datos: archivos .zip que se pueden copiar a una USB o a la nube.
 */
public final class RespaldoUtil {

    private static final Logger logger = LoggerFactory.getLogger(RespaldoUtil.class);
    private static final DateTimeFormatter SELLO = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    /** Respaldos automáticos que se conservan en la carpeta respaldos */
    private static final int AUTOMATICOS_A_CONSERVAR = 30;

    private RespaldoUtil() {
    }

    public static String nombreSugerido(String prefijo) {
        return prefijo + "_" + LocalDateTime.now().format(SELLO) + ".zip";
    }

    /** Copia la base de datos abierta a un .zip (se puede hacer mientras la aplicación se usa). */
    public static void crearRespaldo(Path destino) throws SQLException, IOException {
        if (destino.getParent() != null) {
            Files.createDirectories(destino.getParent());
        }
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement("BACKUP TO ?")) {
            stmt.setString(1, destino.toAbsolutePath().toString());
            stmt.execute();
        }
        logger.info("Respaldo creado: {}", destino);
    }

    /** Respaldo automático en la carpeta respaldos; borra los más viejos. */
    public static void respaldoAutomatico() {
        try {
            crearRespaldo(AppPaths.carpeta(AppPaths.RESPALDOS).resolve(nombreSugerido("auto")));
            List<Path> automaticos;
            try (Stream<Path> archivos = Files.list(AppPaths.RESPALDOS)) {
                automaticos = archivos
                        .filter(p -> p.getFileName().toString().startsWith("auto_"))
                        .sorted()
                        .collect(Collectors.toList());
            }
            for (int i = 0; i < automaticos.size() - AUTOMATICOS_A_CONSERVAR; i++) {
                Files.deleteIfExists(automaticos.get(i));
            }
        } catch (SQLException | IOException e) {
            logger.error("Error en respaldo automático", e);
        }
    }

    /**
     * Reemplaza la base de datos actual por la del respaldo. Antes guarda la actual en
     * respaldos/antes_de_restaurar_*.zip. La base queda cerrada: hay que reiniciar la aplicación.
     */
    public static Path restaurar(Path respaldo) throws SQLException, IOException {
        try (ZipFile zip = new ZipFile(respaldo.toFile())) {
            if (zip.getEntry(AppPaths.DB_NOMBRE + ".mv.db") == null) {
                throw new IOException("El archivo no es un respaldo de este sistema");
            }
        }
        Path copiaActual = AppPaths.carpeta(AppPaths.RESPALDOS).resolve(nombreSugerido("antes_de_restaurar"));
        crearRespaldo(copiaActual);
        ConnectionFactory.closeDataSource();

        String carpeta = AppPaths.DATOS.toAbsolutePath().toString();
        DeleteDbFiles.execute(carpeta, AppPaths.DB_NOMBRE, true);
        Restore.execute(respaldo.toAbsolutePath().toString(), carpeta, AppPaths.DB_NOMBRE);
        logger.info("Base de datos restaurada desde {}", respaldo);
        return copiaActual;
    }
}
