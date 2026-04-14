package com.inventario.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Factory para manejar conexiones a la base de datos con HikariCP
 */
public class ConnectionFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);
    private static HikariDataSource dataSource;
    
    static {
        try {
            inicializarDataSource();
        } catch (IOException e) {
            logger.error("Error inicializando DataSource", e);
            throw new RuntimeException("Error inicializando DataSource", e);
        }
    }
    
    private static void inicializarDataSource() throws IOException {
        Properties props = new Properties();
        
        try (InputStream input = ConnectionFactory.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input == null) {
                throw new IOException("database.properties no encontrado");
            }
            
            props.load(input);
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(Integer.parseInt(
                    props.getProperty("db.pool.maximum", "20")));
            config.setMinimumIdle(Integer.parseInt(
                    props.getProperty("db.pool.size", "10")));
            config.setIdleTimeout(Long.parseLong(
                    props.getProperty("db.pool.idle.timeout", "300000")));
            config.setMaxLifetime(Long.parseLong(
                    props.getProperty("db.pool.max.lifetime", "1200000")));
            config.setAutoCommit(true);
            config.setPoolName("InventarioPOSPool");
            
            dataSource = new HikariDataSource(config);
            logger.info("DataSource inicializado exitosamente");
            
        }
    }
    
    /**
     * Obtiene una conexión del pool
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource no inicializado");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Cierra el DataSource y todas las conexiones
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("DataSource cerrado");
        }
    }
    
    /**
     * Verifica si el DataSource está disponible
     */
    public static boolean isAvailable() {
        return dataSource != null && !dataSource.isClosed();
    }
}
