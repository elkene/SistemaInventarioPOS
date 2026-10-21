-- Datos iniciales. La aplicación los carga una sola vez, al crear la base de datos.
-- Contraseñas iniciales (cámbialas desde el botón "Cambiar contraseña"):
--   admin    / admin123
--   vendedor / vendedor123

INSERT INTO usuarios (username, email, password_hash, rol, activo)
VALUES ('admin', 'admin@sucursal.local',
        '$2a$10$RS//flY4fqeD/5y9FDeSIecum9oxrNDHeMnb0s0aOuBwuU1ymT5..', 'ADMIN', TRUE);

INSERT INTO usuarios (username, email, password_hash, rol, activo)
VALUES ('vendedor', 'vendedor@sucursal.local',
        '$2a$10$fhjvxXuVUcnBGILW8RBSeOB9OcEEUe9tplGVRQApsvpgm3GG8djbC', 'VENDEDOR', TRUE);

INSERT INTO categorias (nombre, descripcion)
VALUES ('General', 'Productos sin categoría específica');
