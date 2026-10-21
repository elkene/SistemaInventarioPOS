package com.inventario.dao;

import com.inventario.exceptions.DataAccessException;
import com.inventario.models.CorteCaja;

import java.util.List;

public interface ICorteCajaDAO {

    void create(CorteCaja corte) throws DataAccessException;

    List<CorteCaja> findRecientes(int limit) throws DataAccessException;
}
