-- Script de datos iniciales para el Sistema de Inventario POS
-- Ejecutar después de crear el schema


-- Insertar empresa
INSERT INTO empresas (nombre, rif, direccion, telefono, email, ciudad, activa)
VALUES ('Mi Empresa PYME', '12345678', 'Avenida Principal 123', '+58-212-1234567', 
        'info@miempresa.com', 'Caracas', true);

-- Insertar usuario admin (contraseña: admin123 hasheada con BCrypt)
INSERT INTO usuarios (username, email, password_hash, rol, activo)
VALUES ('admin', 'admin@miempresa.com', 
        '$2a$10$slYQmyNdGzin7olVN3p5Be9DlH.PKZbv5H8KnzzVgXXbVxzy0YSOK', 
        'ADMIN', true);

-- Insertar usuario vendedor (contraseña: vendedor123)
INSERT INTO usuarios (username, email, password_hash, rol, activo)
VALUES ('vendedor', 'vendedor@miempresa.com', 
        '$2a$10$LQv3c1yqBWVHxkd0LHAkCOYvxH1nJcQq3x3Pg5cUGXwPQpM5h1pui', 
        'VENDEDOR', true);

-- Insertar categorías
INSERT INTO categorias (nombre, descripcion)
VALUES ('Electronica', 'Productos electronicos en general');

INSERT INTO categorias (nombre, descripcion)
VALUES ('Ropa', 'Prendas de vestir');

INSERT INTO categorias (nombre, descripcion)
VALUES ('Alimentos', 'Productos alimenticios');

INSERT INTO categorias (nombre, descripcion)
VALUES ('Accesorios', 'Accesorios y complementos');

-- Insertar productos de prueba
INSERT INTO productos (codigo, nombre, descripcion, precio_compra, precio_venta, 
                      stock, stock_minimo, categoria_id)
VALUES ('ELEC-001', 'Laptop Dell', 'Laptop Dell Inspiron 15', 400.00, 599.99, 10, 2);

INSERT INTO productos (codigo, nombre, descripcion, precio_compra, precio_venta, 
                      stock, stock_minimo, categoria_id)
VALUES ('ELEC-002', 'Mouse Logitech', 'Mouse inalámbrico Logitech', 15.00, 25.99, 50, 5);

INSERT INTO productos (codigo, nombre, descripcion, precio_compra, precio_venta, 
                      stock, stock_minimo, categoria_id)
VALUES ('ROPA-001', 'Camiseta Básica', 'Camiseta 100% algodón', 5.00, 12.99, 100, 10);

INSERT INTO productos (codigo, nombre, descripcion, precio_compra, precio_venta, 
                      stock, stock_minimo, categoria_id)
VALUES ('ROPA-002', 'Pantalón Jeans', 'Pantalón jean clásico', 20.00, 49.99, 50, 8);

INSERT INTO productos (codigo, nombre, descripcion, precio_compra, precio_venta, 
                      stock, stock_minimo, categoria_id)
VALUES ('ALIM-001', 'Aceite de oliva', 'Aceite de oliva virgen extra', 8.00, 15.99, 25, 5);

-- Insertar clientes
INSERT INTO clientes (nombre, cedula, email, telefono, direccion, ciudad, estado, saldo_credito)
VALUES ('Juan Pérez', '12345678', 'juan.perez@email.com', '+58-212-1111111', 
        'Calle 1 #100', 'Caracas', 'Distrito Capital', 0.00);

INSERT INTO clientes (nombre, cedula, email, telefono, direccion, ciudad, estado, saldo_credito)
VALUES ('María García', '87654321', 'maria.garcia@email.com', '+58-212-2222222', 
        'Calle 2 #200', 'Maracaibo', 'Zulia', 150.00);

INSERT INTO clientes (nombre, cedula, email, telefono, direccion, ciudad, estado, saldo_credito)
VALUES ('Carlos López', '11111111', 'carlos.lopez@email.com', '+58-212-3333333', 
        'Calle 3 #300', 'Valencia', 'Carabobo', 75.00);

-- Insertar proveedores
INSERT INTO proveedores (nombre, rif, email, telefono, direccion, ciudad, condiciones_pago)
VALUES ('Distribuidor Tech', '99999999', 'ventas@distribuidortech.com', '+58-212-5555555', 
        'Av. Tecnológica 456', 'Caracas', '30 días');

INSERT INTO proveedores (nombre, rif, email, telefono, direccion, ciudad, condiciones_pago)
VALUES ('Textiles Global', '88888888', 'ventas@textilesglobal.com', '+58-212-6666666', 
        'Av. Industrial 789', 'Maracaibo', '15 días');

-- Insertar venta de ejemplo
INSERT INTO ventas (usuario_id, cliente_id, total, metodo_pago, estado, observaciones)
VALUES (1, 1, 612.98, 'EFECTIVO', 'COMPLETADA', 'Venta de prueba');

-- Insertar detalle de venta
INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES (1, 1, 1, 599.99, 599.99);

INSERT INTO detalles_venta (venta_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES (1, 2, 1, 12.99, 12.99);

-- Notas:
-- 1. Las contraseñas están hasheadas con BCrypt
--    admin123: $2a$10$slYQmyNdGzin7olVN3p5Be9DlH.PKZbv5H8KnzzVgXXbVxzy0YSOK
--    vendedor123: $2a$10$LQv3c1yqBWVHxkd0LHAkCOYvxH1nJcQq3x3Pg5cUGXwPQpM5h1pui
--
-- 2. Puedes cambiar estos valores según necesites
-- 3. Se recomienda usar contraseñas fuertes en producción
