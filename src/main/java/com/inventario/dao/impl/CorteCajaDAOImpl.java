package com.inventario.dao.impl;

import com.inventario.dao.ICorteCajaDAO;
import com.inventario.database.ConnectionFactory;
import com.inventario.exceptions.DataAccessException;
import com.inventario.models.CorteCaja;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CorteCajaDAOImpl implements ICorteCajaDAO {

    private static final Logger logger = LoggerFactory.getLogger(CorteCajaDAOImpl.class);

    @Override
    public void create(CorteCaja corte) throws DataAccessException {
        String sql = "INSERT INTO cortes_caja (usuario_id, fecha, numero_ventas, total_vendido, ventas_efectivo, " +
                     "fondo_inicial, efectivo_contado, diferencia) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setLong(1, corte.getUsuarioId());
            pstmt.setDate(2, Date.valueOf(corte.getFecha()));
            pstmt.setInt(3, corte.getNumeroVentas());
            pstmt.setBigDecimal(4, corte.getTotalVendido());
            pstmt.setBigDecimal(5, corte.getVentasEfectivo());
            pstmt.setBigDecimal(6, corte.getFondoInicial());
            pstmt.setBigDecimal(7, corte.getEfectivoContado());
            pstmt.setBigDecimal(8, corte.getDiferencia());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    corte.setId(generatedKeys.getLong(1));
                    logger.info("Corte de caja creado con ID: " + corte.getId());
                }
            }
        } catch (SQLException e) {
            logger.error("Error creando corte de caja", e);
            throw new DataAccessException("Error creando corte de caja", e);
        }
    }

    @Override
    public List<CorteCaja> findRecientes(int limit) throws DataAccessException {
        List<CorteCaja> cortes = new ArrayList<>();
        String sql = "SELECT k.*, u.username AS usuario_nombre FROM cortes_caja k " +
                     "LEFT JOIN usuarios u ON k.usuario_id = u.usuario_id " +
                     "ORDER BY k.fecha_creacion DESC LIMIT ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    CorteCaja corte = new CorteCaja();
                    corte.setId(rs.getLong("corte_id"));
                    corte.setUsuarioId(rs.getLong("usuario_id"));
                    corte.setUsuarioNombre(rs.getString("usuario_nombre"));
                    corte.setFecha(rs.getDate("fecha").toLocalDate());
                    corte.setNumeroVentas(rs.getInt("numero_ventas"));
                    corte.setTotalVendido(rs.getBigDecimal("total_vendido"));
                    corte.setVentasEfectivo(rs.getBigDecimal("ventas_efectivo"));
                    corte.setFondoInicial(rs.getBigDecimal("fondo_inicial"));
                    corte.setEfectivoContado(rs.getBigDecimal("efectivo_contado"));
                    corte.setDiferencia(rs.getBigDecimal("diferencia"));
                    corte.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
                    cortes.add(corte);
                }
            }
        } catch (SQLException e) {
            logger.error("Error leyendo cortes de caja", e);
            throw new DataAccessException("Error leyendo cortes de caja", e);
        }

        return cortes;
    }
}
