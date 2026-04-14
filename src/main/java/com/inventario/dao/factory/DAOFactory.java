package com.inventario.dao.factory;

import com.inventario.dao.*;
import com.inventario.dao.impl.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DAOFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(DAOFactory.class);
    
    private static IUsuarioDAO usuarioDAOInstance;
    private static IProductoDAO productoDAOInstance;
    private static ICategoriaDAO categoriaDAOInstance;
    private static IMovimientoInventarioDAO movimientoInventarioDAOInstance;
    private static IVentaDAO ventaDAOInstance;
    private static IDetalleVentaDAO detalleVentaDAOInstance;
    private static IClienteDAO clienteDAOInstance;
    private static IProveedorDAO proveedorDAOInstance;
    
    private DAOFactory() {
    }
    
    public static IUsuarioDAO getUsuarioDAO() {
        if (usuarioDAOInstance == null) {
            usuarioDAOInstance = new UsuarioDAOImpl();
            logger.debug("UsuarioDAO creado");
        }
        return usuarioDAOInstance;
    }
    
    public static IProductoDAO getProductoDAO() {
        if (productoDAOInstance == null) {
            productoDAOInstance = new ProductoDAOImpl();
            logger.debug("ProductoDAO creado");
        }
        return productoDAOInstance;
    }
    
    public static ICategoriaDAO getCategoriaDAO() {
        if (categoriaDAOInstance == null) {
            categoriaDAOInstance = new CategoriaDAOImpl();
            logger.debug("CategoriaDAO creado");
        }
        return categoriaDAOInstance;
    }
    
    public static IMovimientoInventarioDAO getMovimientoInventarioDAO() {
        if (movimientoInventarioDAOInstance == null) {
            movimientoInventarioDAOInstance = new MovimientoInventarioDAOImpl();
            logger.debug("MovimientoInventarioDAO creado");
        }
        return movimientoInventarioDAOInstance;
    }
    
    public static IVentaDAO getVentaDAO() {
        if (ventaDAOInstance == null) {
            ventaDAOInstance = new VentaDAOImpl();
            logger.debug("VentaDAO creado");
        }
        return ventaDAOInstance;
    }
    
    public static IDetalleVentaDAO getDetalleVentaDAO() {
        if (detalleVentaDAOInstance == null) {
            detalleVentaDAOInstance = new DetalleVentaDAOImpl();
            logger.debug("DetalleVentaDAO creado");
        }
        return detalleVentaDAOInstance;
    }
    
    public static IClienteDAO getClienteDAO() {
        if (clienteDAOInstance == null) {
            clienteDAOInstance = new ClienteDAOImpl();
            logger.debug("ClienteDAO creado");
        }
        return clienteDAOInstance;
    }
    
    public static IProveedorDAO getProveedorDAO() {
        if (proveedorDAOInstance == null) {
            proveedorDAOInstance = new ProveedorDAOImpl();
            logger.debug("ProveedorDAO creado");
        }
        return proveedorDAOInstance;
    }
    
    public static void resetAll() {
        usuarioDAOInstance = null;
        productoDAOInstance = null;
        categoriaDAOInstance = null;
        movimientoInventarioDAOInstance = null;
        ventaDAOInstance = null;
        detalleVentaDAOInstance = null;
        clienteDAOInstance = null;
        proveedorDAOInstance = null;
        logger.info("Todas las instancias de DAOs fueron reiniciadas");
    }
}
