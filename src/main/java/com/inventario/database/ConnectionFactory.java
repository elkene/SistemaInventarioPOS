package com.inventario.database;

import com.inventario.config.AppPaths;
import com.inventario.exceptions.ApplicationException;
import com.inventario.exceptions.DataAccessException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexiones a la base de datos H2 embebida (archivo local, sin servidor).
 * La primera vez que se abre crea las tablas y los usuarios iniciales.
 */
public class ConnectionFactory {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
    private static HikariDataSource dataSource;

    /** Conexión de la transacción en curso en este hilo (ver {@link #enTransaccion}) */
    private static final ThreadLocal<Connection> transaccion = new ThreadLocal<>();

    public interface Trabajo<T> {
        T ejecutar() throws ApplicationException;
    }

    /**
     * Abre la base de datos. Lanza SQLException si no se puede (por ejemplo,
     * si otra ventana de la aplicación ya la tiene abierta).
     */
    public static synchronized void inicializar() throws SQLException, IOException {
        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }
        AppPaths.carpeta(AppPaths.DATOS);

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(urlJdbc());
        config.setUsername("sa");
        config.setPassword("");
        config.setMaximumPoolSize(4);
        config.setMinimumIdle(1);
        config.setAutoCommit(true);
        config.setPoolName("InventarioPOSPool");
        // Falla al abrir en vez de reintentar 30 s si el archivo está bloqueado
        config.setInitializationFailTimeout(1);

        try {
            dataSource = new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw e.getCause() instanceof SQLException ? (SQLException) e.getCause() : new SQLException(e.getMessage(), e);
        }
        aplicarEsquema();
        logger.info("Base de datos abierta en {}", AppPaths.DATOS);
    }

    public static String urlJdbc() {
        return "jdbc:h2:file:" + AppPaths.DATOS.resolve(AppPaths.DB_NOMBRE).toAbsolutePath();
    }

    /**
     * schema.sql se aplica en cada arranque (es idempotente) para que una base creada con una
     * versión anterior reciba las tablas y columnas nuevas. Los datos iniciales solo van en una base nueva.
     */
    private static void aplicarEsquema() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            boolean nueva;
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'USUARIOS'")) {
                rs.next();
                nueva = rs.getInt(1) == 0;
            }
            stmt.execute("RUNSCRIPT FROM 'classpath:/schema.sql' CHARSET 'UTF-8'");
            if (nueva) {
                logger.info("Base de datos nueva: cargando datos iniciales");
                stmt.execute("RUNSCRIPT FROM 'classpath:/data_inicial.sql' CHARSET 'UTF-8'");
            }
        }
    }

    /**
     * Obtiene una conexión. Dentro de {@link #enTransaccion} devuelve la conexión
     * de la transacción; su close() no hace nada para que los DAOs la puedan
     * usar con try-with-resources sin cerrarla antes de tiempo.
     */
    public static Connection getConnection() throws SQLException {
        Connection tx = transaccion.get();
        if (tx != null) {
            return (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class}, (proxy, method, args) -> {
                        if ("close".equals(method.getName())) {
                            return null;
                        }
                        try {
                            return method.invoke(tx, args);
                        } catch (InvocationTargetException e) {
                            throw e.getCause();
                        }
                    });
        }
        if (dataSource == null || dataSource.isClosed()) {
            throw new SQLException("La base de datos no está abierta");
        }
        return dataSource.getConnection();
    }

    /**
     * Ejecuta el trabajo en una sola transacción: si algo falla se deshace todo.
     */
    public static <T> T enTransaccion(Trabajo<T> trabajo) throws ApplicationException {
        if (transaccion.get() != null) {
            return trabajo.ejecutar();
        }
        Connection conn;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            throw new DataAccessException("Error iniciando transacción", e);
        }
        transaccion.set(conn);
        try {
            T resultado = trabajo.ejecutar();
            conn.commit();
            return resultado;
        } catch (ApplicationException | RuntimeException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                logger.error("Error deshaciendo transacción", ex);
            }
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Error confirmando transacción", e);
        } finally {
            transaccion.remove();
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                logger.error("Error cerrando conexión", e);
            }
        }
    }

    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("DataSource cerrado");
        }
    }

    public static boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }
}
