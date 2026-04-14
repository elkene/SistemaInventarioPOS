package com.inventario.dao.impl;

import com.inventario.dao.IClienteDAO;
import com.inventario.models.Cliente;
import com.inventario.exceptions.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl extends GenericDAOImpl<Cliente> implements IClienteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteDAOImpl.class);
    
    public ClienteDAOImpl() {
        super(Cliente.class);
    }
    
    @Override
    public void create(Cliente cliente) throws DataAccessException {
        String sql = "INSERT INTO clientes (nombre, cedula, email, telefono, direccion, ciudad, estado, saldo_credito) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getCedula());
            pstmt.setString(3, cliente.getEmail());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getDireccion());
            pstmt.setString(6, cliente.getCiudad());
            pstmt.setString(7, cliente.getEstado());
            pstmt.setDouble(8, cliente.getSaldoCredito() != null ? cliente.getSaldoCredito() : 0.0);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cliente.setId(generatedKeys.getLong(1));
                        logger.info("Cliente creado con ID: " + cliente.getId());
                    }
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando cliente", e);
            throw new DataAccessException("Error creando cliente", e);
        }
    }
    
    @Override
    public Cliente read(Long id) throws DataAccessException {
        String sql = "SELECT * FROM clientes WHERE cliente_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo cliente", e);
            throw new DataAccessException("Error leyendo cliente", e);
        }
        
        return null;
    }
    
    @Override
    public List<Cliente> readAll() throws DataAccessException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        } catch (SQLException e) {
            logger.error("Error leyendo todos los clientes", e);
            throw new DataAccessException("Error leyendo todos los clientes", e);
        }
        
        return clientes;
    }
    
    @Override
    public void update(Cliente cliente) throws DataAccessException {
        String sql = "UPDATE clientes SET nombre = ?, cedula = ?, email = ?, telefono = ?, " +
                     "direccion = ?, ciudad = ?, estado = ?, saldo_credito = ?, " +
                     "fecha_modificacion = CURRENT_TIMESTAMP WHERE cliente_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cliente.getNombre());
            pstmt.setString(2, cliente.getCedula());
            pstmt.setString(3, cliente.getEmail());
            pstmt.setString(4, cliente.getTelefono());
            pstmt.setString(5, cliente.getDireccion());
            pstmt.setString(6, cliente.getCiudad());
            pstmt.setString(7, cliente.getEstado());
            pstmt.setDouble(8, cliente.getSaldoCredito() != null ? cliente.getSaldoCredito() : 0.0);
            pstmt.setLong(9, cliente.getId());
            
            pstmt.executeUpdate();
            logger.info("Cliente actualizado: " + cliente.getId());
        } catch (SQLException e) {
            logger.error("Error actualizando cliente", e);
            throw new DataAccessException("Error actualizando cliente", e);
        }
    }
    
    @Override
    public void delete(Long id) throws DataAccessException {
        String sql = "DELETE FROM clientes WHERE cliente_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            logger.info("Cliente eliminado: " + id);
        } catch (SQLException e) {
            logger.error("Error eliminando cliente", e);
            throw new DataAccessException("Error eliminando cliente", e);
        }
    }
    
    @Override
    public long count() throws DataAccessException {
        String sql = "SELECT COUNT(*) FROM clientes";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error contando clientes", e);
            throw new DataAccessException("Error contando clientes", e);
        }
        
        return 0;
    }
    
    @Override
    public Cliente findByCedula(String cedula) throws DataAccessException {
        String sql = "SELECT * FROM clientes WHERE cedula = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cedula);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando cliente por cédula", e);
            throw new DataAccessException("Error buscando cliente por cédula", e);
        }
        
        return null;
    }
    
    @Override
    public List<Cliente> findByNombre(String nombre) throws DataAccessException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE LOWER(nombre) LIKE LOWER(?) ORDER BY nombre";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, "%" + nombre + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando clientes por nombre", e);
            throw new DataAccessException("Error buscando clientes por nombre", e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findByCiudad(String ciudad) throws DataAccessException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE ciudad = ? ORDER BY nombre";
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, ciudad);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error buscando clientes por ciudad", e);
            throw new DataAccessException("Error buscando clientes por ciudad", e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findConCredito() throws DataAccessException {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT * FROM clientes WHERE saldo_credito > 0 ORDER BY nombre";
        
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        } catch (SQLException e) {
            logger.error("Error buscando clientes con crédito", e);
            throw new DataAccessException("Error buscando clientes con crédito", e);
        }
        
        return clientes;
    }
    
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("cliente_id"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setCedula(rs.getString("cedula"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setCiudad(rs.getString("ciudad"));
        cliente.setEstado(rs.getString("estado"));
        cliente.setSaldoCredito(rs.getDouble("saldo_credito"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            cliente.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return cliente;
    }
}
