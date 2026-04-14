# INSTRUCCIONES DE EJECUCIÓN - Sistema de Inventario POS

## 📋 RESUMEN DEL PROYECTO COMPLETADO

El proyecto **Sistema de Inventario y Punto de Ventas (POS)** ha sido completamente creado con todas las funcionalidades, arquitectura profesional y mejores prácticas de desarrollo Java.

---

## 🚀 PASOS PARA EJECUTAR

### 1. CONFIGURAR LA BASE DE DATOS PostgreSQL

#### A. Crear la base de datos
```sql
-- Conectarse como usuario postgres
psql -U postgres

-- Crear la base de datos
CREATE DATABASE inventario_pos;

-- Conectarse a la base de datos
\c inventario_pos

-- Ejecutar el script de schema
\i 'C:/Users/Kenne/Idea/SistemaInventarioPOS/src/main/resources/schema.sql'

-- Ejecutar el script de datos iniciales (opcional)
\i 'C:/Users/Kenne/Idea/SistemaInventarioPOS/src/main/resources/data_inicial.sql'
```

#### B. Verificar que la conexión sea correcta
- Abrir archivo: `src/main/resources/database.properties`
- Verificar que los datos de conexión sean correctos:
  ```
  db.url=jdbc:postgresql://localhost:5432/inventario_pos
  db.username=postgres
  db.password=tu_contraseña
  ```

### 2. COMPILAR EL PROYECTO

Abrirse en la terminal/PowerShell en la carpeta del proyecto:

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS

# Limpiar y compilar
mvn clean compile
```

### 3. DESCARGAR DEPENDENCIAS

Las dependencias se descargarán automáticamente al compilar. Si deseas descargarlas explícitamente:

```bash
mvn dependency:resolve
```

### 4. EJECUTAR LA APLICACIÓN

#### Opción A: Desde la línea de comandos
```bash
mvn clean javafx:run
```

#### Opción B: Usar scripts de ejecución
```bash
# Windows CMD
ejecutar.bat

# Windows PowerShell
.\ejecutar.ps1
```

#### Opción C: Desde IDE (IntelliJ IDEA)
1. Click derecho en `Main.java`
2. Seleccionar "Run 'Main.main()'"

#### Opción D: Desde IDE (Eclipse)
1. Click derecho en el proyecto
2. Run As → Java Application
3. Seleccionar `Main.java`

### 5. CREDENCIALES DE INICIO

**Usuario**: `admin`
**Contraseña**: `admin123`

O

**Usuario**: `vendedor`
**Contraseña**: `vendedor123`

---

## 📁 ESTRUCTURA DE ARCHIVOS CREADOS

```
C:\Users\Kenne\Idea\SistemaInventarioPOS\
│
├── pom.xml                                 (Configuración Maven)
├── README.md                               (Documentación)
├── LICENSE                                 (MIT License)
├── .gitignore                              (Git ignore rules)
│
├── src/main/java/com/inventario/
│   ├── Main.java                          (Punto de entrada)
│   │
│   ├── controllers/
│   │   ├── LoginController.java           (Autenticación)
│   │   ├── MainController.java            (Pantalla principal)
│   │   ├── InventarioController.java      (Gestión inventario)
│   │   ├── ProductoDialogController.java   (Diálogo producto)
│   │   ├── AjusteStockDialogController.java (Ajuste stock)
│   │   ├── CategoriasDialogController.java (Gestión categorías)
│   │   ├── VentasController.java          (Punto de ventas)
│   │   ├── BuscarProductoController.java  (Búsqueda productos)
│   │   ├── ClientesController.java        (Gestión clientes)
│   │   ├── ClienteDialogController.java   (Diálogo cliente)
│   │   ├── ProveedoresController.java     (Gestión proveedores)
│   │   ├── ProveedorDialogController.java (Diálogo proveedor)
│   │   ├── HistorialMovimientosController.java (Historial)
│   │   ├── ReportesController.java        (Módulo reportes)
│   │   ├── ReporteVentasController.java   (Reporte ventas)
│   │   ├── ReporteInventarioController.java (Reporte inventario)
│   │   ├── ReporteMovimientosController.java (Reporte movimientos)
│   │   └── ReporteGananciasController.java (Reporte ganancias)
│   │
│   ├── models/
│   │   ├── Entidad.java                   (Clase base)
│   │   ├── Usuario.java
│   │   ├── Producto.java
│   │   ├── Cliente.java
│   │   ├── Proveedor.java
│   │   ├── Venta.java
│   │   ├── DetalleVenta.java
│   │   ├── Categoria.java
│   │   ├── MovimientoInventario.java
│   │   └── ItemVenta.java
│   │
│   ├── services/
│   │   ├── AuthenticationService.java     (Login)
│   │   ├── ProductoService.java           (Gestión productos)
│   │   ├── CategoriaService.java         (Gestión categorías)
│   │   ├── ClienteService.java            (Gestión clientes)
│   │   ├── ProveedorService.java         (Gestión proveedores)
│   │   ├── VentaService.java             (Gestión ventas)
│   │   └── MovimientoInventarioService.java (Movimientos)
│   │
│   ├── dao/
│   │   ├── IGenericDAO.java              (Interfaz genérica)
│   │   ├── IUsuarioDAO.java
│   │   ├── IProductoDAO.java
│   │   ├── ICategoriaDAO.java
│   │   ├── IClienteDAO.java
│   │   ├── IProveedorDAO.java
│   │   ├── IVentaDAO.java
│   │   ├── IDetalleVentaDAO.java
│   │   ├── IMovimientoInventarioDAO.java
│   │   ├── impl/
│   │   │   ├── GenericDAOImpl.java
│   │   │   ├── UsuarioDAOImpl.java
│   │   │   ├── ProductoDAOImpl.java
│   │   │   ├── CategoriaDAOImpl.java
│   │   │   ├── ClienteDAOImpl.java
│   │   │   ├── ProveedorDAOImpl.java
│   │   │   ├── VentaDAOImpl.java
│   │   │   ├── DetalleVentaDAOImpl.java
│   │   │   └── MovimientoInventarioDAOImpl.java
│   │   └── factory/
│   │       └── DAOFactory.java
│   │
│   ├── database/
│   │   └── ConnectionFactory.java         (Gestión conexiones)
│   │
│   ├── utils/
│   │   ├── PasswordHasher.java            (Hash BCrypt)
│   │   ├── ValidationUtil.java            (Validaciones)
│   │   └── AlertUtil.java                (Diálogos)
│   │
│   ├── exceptions/
│   │   ├── ApplicationException.java
│   │   ├── DataAccessException.java
│   │   ├── ValidationException.java
│   │   └── AuthenticationException.java
│   │
│   └── config/
│       └── SessionManager.java            (Gestión sesiones)
│
├── src/main/resources/
│   ├── fxml/
│   │   ├── login.fxml                    (Pantalla login)
│   │   ├── main.fxml                     (Pantalla principal)
│   │   ├── inventario.fxml               (Módulo inventario)
│   │   ├── producto_dialog.fxml          (Diálogo producto)
│   │   ├── ajuste_stock_dialog.fxml      (Ajuste stock)
│   │   ├── categorias_dialog.fxml        (Diálogo categorías)
│   │   ├── ventas.fxml                   (Punto de ventas)
│   │   ├── buscar_producto_dialog.fxml   (Búsqueda productos)
│   │   ├── clientes.fxml                 (Módulo clientes)
│   │   ├── cliente_dialog.fxml           (Diálogo cliente)
│   │   ├── proveedores.fxml              (Módulo proveedores)
│   │   ├── proveedor_dialog.fxml         (Diálogo proveedor)
│   │   ├── historial_movimientos.fxml    (Historial)
│   │   ├── reportes.fxml                 (Módulo reportes)
│   │   ├── reporte_ventas.fxml           (Reporte ventas)
│   │   ├── reporte_inventario.fxml       (Reporte inventario)
│   │   ├── reporte_movimientos.fxml      (Reporte movimientos)
│   │   └── reporte_ganancias.fxml        (Reporte ganancias)
│   │
│   ├── css/
│   │   └── styles.css                    (Estilos profesionales)
│   │
│   ├── schema.sql                        (Script base de datos)
│   ├── data_inicial.sql                  (Datos de prueba)
│   ├── application.properties
│   ├── database.properties
│   └── log4j2.xml                        (Configuración logging)
│
└── src/test/                             (Tests unitarios)
```

---

## 🔧 PROBLEMAS COMUNES Y SOLUCIONES

### Error: "No se puede conectar a la base de datos"
**Solución**: 
1. Verificar que PostgreSQL está ejecutándose
2. Verificar credenciales en `database.properties`
3. Verificar que la base de datos `inventario_pos` existe

### Error: "Maven command not found"
**Solución**:
1. Instalar Maven desde [maven.apache.org](https://maven.apache.org)
2. Agregar a variables de entorno PATH

### Error: "JavaFX not found"
**Solución**:
1. Maven descargará automáticamente JavaFX
2. Si no funciona, ejecutar: `mvn dependency:resolve`

### Error: "Cannot find symbol"
**Solución**:
1. Ejecutar: `mvn clean`
2. Luego: `mvn compile`

---

## 📊 CARACTERÍSTICAS IMPLEMENTADAS

### ✅ Completadas

**Módulo de Inventario**
- [x] CRUD de productos
- [x] Gestión de categorías
- [x] Ajuste de stock (entradas/salidas)
- [x] Historial de movimientos
- [x] Búsqueda y filtros
- [x] Productos bajo stock

**Módulo de Ventas**
- [x] Punto de ventas completo
- [x] Carrito de compras
- [x] Búsqueda de productos
- [x] Métodos de pago
- [x] Selector de cliente
- [x] Aplicación de descuentos
- [x] Completar/anular ventas

**Módulo de Clientes**
- [x] CRUD de clientes
- [x] Búsqueda por nombre
- [x] Información de crédito
- [x] Estado de cuenta

**Módulo de Proveedores**
- [x] CRUD de proveedores
- [x] Búsqueda por nombre
- [x] Información de contacto
- [x] Condiciones de pago

**Módulo de Reportes**
- [x] Reporte de Ventas (por rango de fechas)
- [x] Reporte de Inventario (estado actual, stock bajo)
- [x] Reporte de Movimientos (entradas, salidas, ajustes)
- [x] Reporte de Ganancias (costos, ganancias, porcentaje)

**Seguridad**
- [x] Autenticación segura con BCrypt
- [x] Gestión de sesiones con timeout
- [x] PreparedStatements (sin SQL Injection)
- [x] Logging completo

---

## 📚 TECNOLOGÍAS UTILIZADAS

| Componente | Versión | Propósito |
|---|---|---|
| Java | 11+ | Lenguaje de programación |
| JavaFX | 21.0.2 | Interfaz gráfica |
| PostgreSQL | 14+ | Base de datos |
| Maven | 3.9+ | Gestor de dependencias |
| HikariCP | 5.1.0 | Connection pooling |
| Spring Security | 6.1.5 | Hashing de contraseñas |
| SLF4J/Log4j2 | 2.0.11/2.21.1 | Logging |

---

## 🔒 SEGURIDAD IMPLEMENTADA

1. **Autenticación**
   - Hash BCrypt con work factor 10
   - Mensajes de error genéricos
   - Rate limiting (5 intentos)

2. **Sesiones**
   - Session ID de 128 bits
   - Timeout por inactividad: 30 min
   - Timeout absoluto: 8 horas

3. **Base de Datos**
   - Prepared Statements
   - Sin SQL Injection
   - Índices optimizados

4. **Logging**
   - Sin contraseñas en logs
   - Auditoría de cambios
   - Trazabilidad completa

---

## 🎯 MÓDULOS DEL SISTEMA

### Inventario
- Lista de productos con búsqueda
- Filtros por categoría y stock
- CRUD de productos
- Ajuste de stock
- Historial de movimientos
- Gestión de categorías

### Ventas
- Carrito de compras
- Búsqueda de productos
- Métodos de pago
- Selector de cliente
- Descuentos
- Anulación de ventas

### Clientes
- Lista de clientes
- Búsqueda por nombre
- CRUD de clientes
- Crédito

### Proveedores
- Lista de proveedores
- Búsqueda por nombre
- CRUD de proveedores

### Reportes
- Ventas por fechas
- Inventario actual
- Movimientos de inventario
- Ganancias

---

## 📞 SOPORTE

Para más información o ayuda:
1. Consultar el README.md
2. Revisar logs en `logs/application.log`
3. Consultar código fuente (bien comentado)

---

## 📋 DOCUMENTACIÓN ADICIONAL

- **EJECUTAR_AHORA.md** - Guía rápida de ejecución
- **GUIA_INTELLIJ.md** - Configuración para IntelliJ
- **SOLUCION_JAVAFX.md** - Solución de errores JavaFX
- **CHECKLIST_VERIFICACION.md** - Lista de verificación
- **RESUMEN_PROYECTO.md** - Resumen ejecutivo

---

**¡Proyecto completado exitosamente!** 🎉

Versión: 1.0.0
Fecha: Abril 2026
