# Sistema de Inventario y Punto de Ventas (POS)

## Análisis y Diseño de Software - Documento de Especificación de Requerimientos

**Versión:** 1.0  
**Fecha:** 10 de abril de 2026  
**Desarrollado por:** Kenneth Armenta Ibarra

---

## ÍNDICE

1. [INTRODUCCIÓN](#introducción)
2. [DESCRIPCIÓN DEL PROYECTO](#descripción-del-proyecto)
3. [ACTORES PRINCIPALES](#actores-principales)
4. [CONTEXTO OPERACIONAL](#contexto-operacional)
5. [REGLAS DE NEGOCIO](#reglas-de-negocio)
6. [GLOSARIO](#glosario)
7. [ALCANCE DEL PROYECTO](#alcance-del-proyecto)
8. [DEFINICIÓN DE SRS (FURPS)](#definición-de-srs-furps)
   - [Requerimientos Funcionales (F)](#requerimientos-funcionales-f)
   - [Requerimientos de Usabilidad (U)](#requerimientos-de-usabilidad-u)
   - [Requerimientos de Confiabilidad (R)](#requerimientos-de-confiabilidad-r)
   - [Requerimientos de Desempeño (P)](#requerimientos-de-desempeño-p)
   - [Requerimientos de Mantenibilidad (S)](#requerimientos-de-mantenibilidad-s)
9. [DIAGRAMAS DE CASO DE USO](#diagramas-de-caso-de-uso)
10. [DIAGRAMAS DE ACTIVIDADES](#diagramas-de-actividades)
11. [DIAGRAMAS DE CLASES](#diagramas-de-clases)
12. [DIAGRAMAS DE SECUENCIA](#diagramas-de-secuencia)
13. [DIAGRAMAS DE SUBSISTEMAS](#diagramas-de-subsistemas)
14. [PROTOTIPOS](#prototipos)

---

## INTRODUCCIÓN

El presente documento describe el Sistema de Inventario y Punto de Ventas (POS) desarrollado para gestionar de manera integral las operaciones de un negocio de venta de productos. El sistema tiene como propósito principal centralizar y automatizar los procesos de control de inventario, registro de ventas, gestión de clientes y proveedores, así como la generación de reportes administrativos.

La problemática identificada radica en la falta de un control eficiente del inventario: productos que se agotan sin previo aviso, ventas que no se registran correctamente, dificultades para conocer el estado financiero del negocio, y la ausencia de información histórica para la toma de decisiones. El sistema propuesto busca resolver estas deficiencias proporcionando una herramienta integral que permita gestionar el negocio de manera eficiente, con trazabilidad completa de las operaciones y generación de reportes precisos.

---

## DESCRIPCIÓN DEL PROYECTO

El Sistema de Inventario y Punto de Ventas es una aplicación de escritorio desarrollada en Java/JavaFX cuyo objetivo es facilitar la gestión integral de un negocio comercial. El sistema ofrecerá las siguientes funciones principales:

- **Gestión de Inventario:** Registro y actualización de productos con información de precios de compra y venta, control de stock, categorías y alertas de stock bajo.

- **Punto de Venta (POS):** Registro de ventas con carrito de compras, búsqueda de productos, aplicación de descuentos, selección de método de pago y cliente opcional.

- **Gestión de Clientes:** Registro de clientes con información de contacto, seguimiento de saldo de crédito.

- **Gestión de Proveedores:** Registro de proveedores con información de contacto y condiciones de pago.

- **Reportes y Análisis:** Generación de reportes de ventas, inventario, movimientos y ganancias con filtros por fecha y exportación a PDF.

- **Seguridad:** Autenticación de usuarios con roles diferenciados (Administrador y Vendedor), contraseñas cifradas con BCrypt.

---

## ACTORES PRINCIPALES

| Actor | Descripción |
|-------|-------------|
| **Administrador** | Usuario con acceso completo al sistema. Gestiona usuarios, productos, categorías, clientes, proveedores. Tiene acceso a todos los reportes y configuraciones del sistema. |
| **Vendedor** | Usuario con acceso limitado. Puede realizar ventas, buscar productos, consultar clientes. No tiene acceso a configuraciones ni a ciertos reportes. |

### Usuarios de Prueba

| Usuario | Contraseña | Rol |
|---------|------------|-----|
| admin | admin123 | ADMIN |
| vendedor | vendedor123 | VENDEDOR |

---

## CONTEXTO OPERACIONAL

El sistema operará en el entorno de un negocio comercial (tienda, almacén o similar), funcionando de manera independiente utilizando su propia base de datos PostgreSQL.

El usuario Administrador tendrá la responsabilidad de:
- Gestionar el catálogo de productos (crear, modificar, eliminar)
- Administrar categorías de productos
- Registrar y mantener clientes y proveedores
- Consultar reportes y métricas del negocio
- Gestionar usuarios del sistema

El usuario Vendedor utilizará el sistema para:
- Realizar ventas a clientes
- Buscar productos disponibles
- Consultar información de clientes

---

## REGLAS DE NEGOCIO

| # | Regla |
|---|-------|
| RN-01 | Solo usuarios autenticados pueden acceder al sistema |
| RN-02 | Cada usuario tiene un rol asignado: ADMIN o VENDEDOR |
| RN-03 | Los permisos de acción dependen exclusivamente del rol asignado |
| RN-04 | Las contraseñas deben almacenarse en formato BCrypt |
| RN-05 | Todo producto debe tener: código único, nombre, precio de compra, precio de venta, stock actual y stock mínimo |
| RN-06 | El stock de un producto no puede ser negativo |
| RN-07 | Cuando el stock sea menor o igual al stock mínimo, se debe mostrar alerta de stock bajo |
| RN-08 | Al realizar una venta, el stock del producto debe decrementarse automáticamente |
| RN-09 | Toda venta debe registrar: usuario que la realizó, cliente (opcional), total, método de pago y estado |
| RN-10 | Los métodos de pago válidos son: EFECTIVO, TARJETA, TRANSFERENCIA |
| RN-11 | Toda venta genera un movimiento de inventario de tipo SALIDA |
| RN-12 | Todo movimiento de inventario debe registrar: tipo (ENTRADA, SALIDA, AJUSTE), cantidad, descripción y usuario responsable |
| RN-13 | El administrador puede realizar ajustes de inventario que generan movimientos de tipo AJUSTE |
| RN-14 | Todo cliente debe tener un número de cédula único |
| RN-15 | Todo proveedor debe tener un RIF único |
| RN-16 | El sistema debe bloquear al usuario después de 5 intentos fallidos de login |

---

## GLOSARIO

| Término | Definición |
|---------|------------|
| **POS (Point of Sale)** | Sistema de punto de venta. Módulo del sistema donde se registran las ventas. |
| **Inventario** | Registro de todos los productos disponibles para la venta, incluyendo cantidades y valores. |
| **Stock** | Cantidad disponible de un producto en el inventario. |
| **Stock Mínimo** | Nivel mínimo de stock de un producto. Cuando se alcanza, se genera una alerta. |
| **Producto** | Artículo registrado en el sistema con código, nombre, descripción, precios y cantidad en stock. |
| **Categoría** | Clasificación que agrupa productos similares (ej: Electrónica, Alimentos, etc.). |
| **Cliente** | Persona que compra productos en el negocio. Puede tener saldo de crédito. |
| **Proveedor** | Empresa o persona que suministra productos al negocio. |
| **Venta** | Transacción comercial donde se registran los productos vendidos, total y método de pago. |
| **DetalleVenta** | Línea individual dentro de una venta que registra un producto específico, cantidad y precio. |
| **MovimientoInventario** | Registro de cualquier cambio en el stock de productos (entrada, salida o ajuste). |
| **Carrito de Compras** | Elemento temporal que acumula los productos seleccionados antes de confirmar la venta. |
| **Método de Pago** | Forma en que el cliente realiza el pago: EFECTIVO, TARJETA o TRANSFERENCIA. |
| **BCrypt** | Algoritmo de hash para almacenar contraseñas de forma segura. |
| **Sesión** | Estado temporal que mantiene al usuario autenticado durante su interacción con el sistema. |
| **Reporte** | Documento generado por el sistema que presenta información estructurada sobre ventas, inventario u otros aspectos del negocio. |
| **PDF** | Formato de documento portátil utilizado para exportar reportes. |
| **DAO (Data Access Object)** | Patrón de diseño que encapsula el acceso a la base de datos. |
| **Service** | Capa lógica que gestiona las reglas de negocio y coordina los DAOs. |
| **Entidad** | Clase base que contiene los campos comunes a todas las entidades del sistema (id, fechaCreacion, fechaModificacion). |
| **AlertUtil** | Utilitario que muestra mensajes al usuario (información, advertencias, errores). |
| **PDF Report Exporter** | Componente que genera reportes en formato PDF utilizando la librería iTextPDF. |

---

## ALCANCE DEL PROYECTO

### Funcionalidades Incluidas

El Sistema de Inventario y Punto de Ventas comprende:

1. **Autenticación y Seguridad**
   - Inicio de sesión con usuario y contraseña
   - Roles diferenciados (Administrador, Vendedor)
   - Contraseñas cifradas con BCrypt
   - Bloqueo por intentos fallidos
   - Gestión de sesiones con timeout

2. **Gestión de Inventario**
   - CRUD completo de productos
   - Gestión de categorías
   - Control de stock con alertas
   - Historial de movimientos de inventario
   - Búsqueda y filtrado de productos

3. **Punto de Venta**
   - Carrito de compras interactivo
   - Búsqueda de productos por código o nombre
   - Modificación de cantidades
   - Aplicación de descuentos
   - Selección de método de pago
   - Selección de cliente (opcional)
   - Finalización de ventas

4. **Gestión de Clientes**
   - Registro de clientes
   - Información de contacto
   - Seguimiento de crédito
   - Búsqueda de clientes

5. **Gestión de Proveedores**
   - Registro de proveedores
   - Información de contacto
   - Condiciones de pago

6. **Reportes**
   - Reporte de Ventas (por rango de fechas)
   - Reporte de Inventario (estado actual)
   - Reporte de Movimientos (entradas, salidas, ajustes)
   - Reporte de Ganancias (análisis de costos y márgenes)
   - Exportación a PDF

### Funcionalidades No Incluidas

El proyecto no contempla en esta versión:

- Integración con sistemas contables externos
- Aplicación web o móvil
- Facturación electrónica
- Gestión de caja chica
- Control de múltiples almacenes
- Módulos de importación/exportación masiva

---

## DEFINICIÓN DE SRS (FURPS)

### Requerimientos Funcionales (F)

| ID | Descripción |
|----|-------------|
| RF-01 | El sistema deberá permitir iniciar sesión con usuario y contraseña |
| RF-02 | El sistema deberá validar las credenciales contra la base de datos |
| RF-03 | El sistema deberá almacenar las contraseñas en formato BCrypt |
| RF-04 | El sistema deberá bloquear al usuario después de 5 intentos fallidos |
| RF-05 | El sistema deberá mostrar una interfaz diferente según el rol del usuario |
| RF-06 | El sistema deberá permitir al administrador gestionar productos (CRUD) |
| RF-07 | El sistema deberá permitir al administrador gestionar categorías (CRUD) |
| RF-08 | El sistema deberá asignar un código único a cada producto |
| RF-09 | El sistema deberá validar que el stock no sea negativo |
| RF-10 | El sistema deberá generar alerta cuando el stock sea menor o igual al stock mínimo |
| RF-11 | El sistema deberá permitir buscar productos por código o nombre |
| RF-12 | El sistema deberá permitir al vendedor realizar ventas |
| RF-13 | El sistema deberá permitir agregar productos al carrito de compras |
| RF-14 | El sistema deberá permitir modificar las cantidades en el carrito |
| RF-15 | El sistema deberá permitir eliminar productos del carrito |
| RF-16 | El sistema deberá permitir aplicar descuentos a la venta |
| RF-17 | El sistema deberá registrar el método de pago: EFECTIVO, TARJETA o TRANSFERENCIA |
| RF-18 | El sistema deberá permitir seleccionar un cliente para la venta (opcional) |
| RF-19 | El sistema deberá decrementar el stock automáticamente al completar una venta |
| RF-20 | El sistema deberá registrar automáticamente el movimiento de inventario |
| RF-21 | El sistema deberá permitir al administrador gestionar clientes (CRUD) |
| RF-22 | El sistema deberá validar que la cédula del cliente sea única |
| RF-23 | El sistema deberá permitir al administrador gestionar proveedores (CRUD) |
| RF-24 | El sistema deberá validar que el RIF del proveedor sea único |
| RF-25 | El sistema deberá permitir generar reporte de ventas por rango de fechas |
| RF-26 | El sistema deberá permitir generar reporte de inventario actual |
| RF-27 | El sistema deberá permitir generar reporte de movimientos de inventario |
| RF-28 | El sistema deberá permitir generar reporte de ganancias |
| RF-29 | El sistema deberá permitir exportar reportes a formato PDF |
| RF-30 | El sistema deberá registrar automáticamente la fecha y hora de creación de cada registro |
| RF-31 | El sistema deberá permitir al administrador gestionar usuarios del sistema |

### Requerimientos de Usabilidad (U)

| ID | Descripción |
|----|-------------|
| RU-01 | El sistema deberá mostrar una interfaz gráfica intuitiva y fácil de usar |
| RU-02 | El sistema deberá utilizar colores consistentes en toda la aplicación |
| RU-03 | El sistema deberá mostrar mensajes de confirmación al completar acciones importantes |
| RU-04 | El sistema deberá validar campos obligatorios en los formularios |
| RU-05 | El sistema deberá mostrar errores de forma clara y comprensible |
| RU-06 | El sistema deberá utilizar íconos representativos para las acciones principales |
| RU-07 | El sistema deberá permitir navegar fácilmente entre los diferentes módulos |
| RU-08 | El sistema deberá mostrar el nombre de usuario y rol en la interfaz principal |
| RU-09 | El sistema deberá incluir una barra de búsqueda en los listados |
| RU-10 | El sistema deberá mostrar tooltips en los botones para facilitar su comprensión |

### Requerimientos de Confiabilidad (R)

| ID | Descripción |
|----|-------------|
| RCN-01 | El sistema deberá mantener la integridad de los datos al realizar transacciones |
| RCN-02 | El sistema deberá generar mensajes claros ante errores de operación |
| RCN-03 | El sistema deberá realizar rollback de transacciones en caso de error |
| RCN-04 | El sistema deberá garantizar que no se registren ventas con stock negativo |
| RCN-05 | El sistema deberá registrar todas las operaciones importantes en logs |
| RCN-06 | El sistema deberá validar que los datos requeridos estén presentes antes de guardar |
| RCN-07 | El sistema deberá confirmar antes de eliminar registros importantes |

### Requerimientos de Desempeño (P)

| ID | Descripción |
|----|-------------|
| RDS-01 | La carga de cualquier pantalla deberá completarse en menos de 3 segundos |
| RDS-02 | El sistema deberá soportar al menos 10 usuarios conectados simultáneamente |
| RDS-03 | La búsqueda de productos deberá mostrar resultados en menos de 1 segundo |
| RDS-04 | El proceso de venta (incluyendo decremento de stock) deberá completarse en menos de 2 segundos |
| RDS-05 | La generación de reportes PDF deberá completarse en menos de 10 segundos |
| RDS-06 | El sistema deberá mantener tiempos de respuesta aceptables bajo carga normal |

### Requerimientos de Mantenibilidad (S)

| ID | Descripción |
|----|-------------|
| RRM-01 | El sistema deberá almacenar información en una base de datos PostgreSQL |
| RRM-02 | El código deberá seguir el patrón MVC (Model-View-Controller) |
| RRM-03 | El sistema deberá utilizar el patrón DAO para el acceso a datos |
| RRM-04 | El sistema deberá permitir agregar nuevos roles sin modificar módulos existentes |
| RRM-05 | El código deberá estar documentado con comentarios relevantes |
| RRM-06 | El sistema deberá utilizar convenciones de nomenclatura consistentes |
| RRM-07 | El sistema deberá ser desarrollado utilizando JavaFX con FXML para las vistas |

---

## DIAGRAMAS DE CASO DE USO

### Caso de Uso: Iniciar Sesión

| Campo | Valor |
|-------|-------|
| **ID** | CU-01 |
| **Nombre** | Iniciar Sesión |
| **Breve Descripción** | Permite a los usuarios autenticarse en el sistema mediante usuario y contraseña |
| **Actores Principales** | Administrador, Vendedor |
| **Precondiciones** | El usuario debe estar registrado en el sistema |

**Flujo Principal:**
1. El usuario ejecuta la aplicación
2. El sistema muestra la pantalla de login
3. El usuario ingresa su nombre de usuario y contraseña
4. El sistema valida las credenciales
5. Si son correctas, el sistema muestra el dashboard según el rol
6. El sistema registra el último login del usuario

**Flujos Alternativos:**
- A1: Si las credenciales son incorrectas, mostrar mensaje de error y contar el intento
- A2: Si se superan los 5 intentos fallidos, bloquear al usuario
- A3: Si el usuario está inactivo por 30 minutos, cerrar la sesión automáticamente

---

### Caso de Uso: Gestionar Productos

| Campo | Valor |
|-------|-------|
| **ID** | CU-02 |
| **Nombre** | Gestionar Productos |
| **Breve Descripción** | Permite al administrador crear, modificar, eliminar y consultar productos |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de productos
2. El sistema muestra la lista de productos existentes
3. El administrador puede:
   - Crear nuevo producto
   - Modificar producto existente
   - Eliminar producto
   - Buscar productos por código o nombre
4. El sistema valida y guarda los cambios

**Flujos Alternativos:**
- A1: Si se intenta eliminar un producto con stock mayor a 0, mostrar advertencia
- A2: Si el código del producto ya existe, mostrar mensaje de error

---

### Caso de Uso: Realizar Venta

| Campo | Valor |
|-------|-------|
| **ID** | CU-03 |
| **Nombre** | Realizar Venta |
| **Breve Descripción** | Permite registrar una venta con carrito de compras, descuentos y método de pago |
| **Actores Principales** | Vendedor, Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión |

**Flujo Principal:**
1. El usuario accede al módulo de Punto de Venta
2. El usuario busca productos por código o nombre
3. El usuario agrega productos al carrito
4. El usuario puede modificar cantidades o eliminar productos del carrito
5. El usuario puede aplicar un descuento (opcional)
6. El usuario selecciona el método de pago
7. El usuario selecciona un cliente (opcional)
8. El usuario confirma la venta
9. El sistema:
   - Registra la venta
   - Registra los detalles de venta
   - Decrementa el stock de cada producto
   - Genera movimientos de inventario
   - Muestra confirmación

**Flujos Alternativos:**
- A1: Si un producto no tiene stock suficiente, mostrar mensaje de error
- A2: Si se cancela la operación, no se realiza ningún cambio

---

### Caso de Uso: Gestionar Clientes

| Campo | Valor |
|-------|-------|
| **ID** | CU-04 |
| **Nombre** | Gestionar Clientes |
| **Breve Descripción** | Permite al administrador crear, modificar, eliminar y consultar clientes |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de clientes
2. El sistema muestra la lista de clientes
3. El administrador puede:
   - Crear nuevo cliente
   - Modificar cliente existente
   - Eliminar cliente
   - Buscar clientes por nombre o cédula
4. El sistema valida y guarda los cambios

**Flujos Alternativos:**
- A1: Si se intenta eliminar un cliente con saldo de crédito pendiente, mostrar advertencia

---

### Caso de Uso: Gestionar Proveedores

| Campo | Valor |
|-------|-------|
| **ID** | CU-05 |
| **Nombre** | Gestionar Proveedores |
| **Breve Descripción** | Permite al administrador crear, modificar, eliminar y consultar proveedores |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de proveedores
2. El sistema muestra la lista de proveedores
3. El administrador puede:
   - Crear nuevo proveedor
   - Modificar proveedor existente
   - Eliminar proveedor
   - Buscar proveedores por nombre o RIF
4. El sistema valida y guarda los cambios

---

### Caso de Uso: Gestionar Categorías

| Campo | Valor |
|-------|-------|
| **ID** | CU-06 |
| **Nombre** | Gestionar Categorías |
| **Breve Descripción** | Permite al administrador crear, modificar, eliminar y consultar categorías de productos |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de categorías
2. El sistema muestra la lista de categorías
3. El administrador puede:
   - Crear nueva categoría
   - Modificar categoría existente
   - Eliminar categoría
4. El sistema valida y guarda los cambios

---

### Caso de Uso: Gestionar Movimientos de Inventario

| Campo | Valor |
|-------|-------|
| **ID** | CU-07 |
| **Nombre** | Gestionar Movimientos de Inventario |
| **Breve Descripción** | Permite al administrador registrar entradas, salidas y ajustes de inventario |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de movimientos
2. El administrador selecciona el tipo de movimiento (ENTRADA, SALIDA, AJUSTE)
3. El administrador selecciona el producto
4. El administrador ingresa la cantidad y descripción
5. El sistema actualiza el stock del producto
6. El sistema registra el movimiento

---

### Caso de Uso: Generar Reporte de Ventas

| Campo | Valor |
|-------|-------|
| **ID** | CU-08 |
| **Nombre** | Generar Reporte de Ventas |
| **Breve Descripción** | Permite generar un reporte de ventas filtrado por rango de fechas |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de reportes
2. Selecciona "Reporte de Ventas"
3. El administrador selecciona el rango de fechas
4. El sistema muestra las ventas del período
5. El sistema muestra estadísticas (total, cantidad, promedio)
6. El administrador puede exportar a PDF

---

### Caso de Uso: Generar Reporte de Inventario

| Campo | Valor |
|-------|-------|
| **ID** | CU-09 |
| **Nombre** | Generar Reporte de Inventario |
| **Breve Descripción** | Permite generar un reporte del estado actual del inventario |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de reportes
2. Selecciona "Reporte de Inventario"
3. El sistema muestra todos los productos con su stock actual
4. El sistema muestra estadísticas (total productos, stock total, valor inventario, stock bajo)
5. El administrador puede buscar productos específicos
6. El administrador puede exportar a PDF

---

### Caso de Uso: Generar Reporte de Ganancias

| Campo | Valor |
|-------|-------|
| **ID** | CU-10 |
| **Nombre** | Generar Reporte de Ganancias |
| **Breve Descripción** | Permite generar un análisis de ganancias basado en costos y ventas |
| **Actores Principales** | Administrador |
| **Precondiciones** | El usuario debe haber iniciado sesión con rol Administrador |

**Flujo Principal:**
1. El administrador accede al módulo de reportes
2. Selecciona "Reporte de Ganancias"
3. El administrador selecciona el rango de fechas
4. El sistema calcula el costo y ganancia de cada venta
5. El sistema muestra totales y márgenes
6. El administrador puede exportar a PDF

---

### Caso de Uso: Cerrar Sesión

| Campo | Valor |
|-------|-------|
| **ID** | CU-11 |
| **Nombre** | Cerrar Sesión |
| **Breve Descripción** | Permite al usuario cerrar su sesión de forma segura |
| **Actores Principales** | Administrador, Vendedor |
| **Precondiciones** | El usuario debe haber iniciado sesión |

**Flujo Principal:**
1. El usuario selecciona "Cerrar Sesión" en el menú
2. El sistema cierra la sesión actual
3. El sistema regresa a la pantalla de login

---

## DIAGRAMAS DE ACTIVIDADES

### Actividad: Realizar Venta

```
[Inicio] 
    │
    ▼
¿Buscar producto? ──Sí──> [Ingresar código/nombre]
    │                          │
    No                         ▼
    │                    ¿Producto encontrado?
    │                    Sí ──> [Agregar al carrito]
    │                    No        │
    │                     │        ▼
    │                     │    [Mostrar mensaje error]
    │                     │        │
    ▼                     ◄────────┘
¿Modificar carrito?
    │
    ├──Sí──> [Modificar cantidad / Eliminar producto]
    │              │
    │              ▼
    │        ¿Continuar modificando?
    │        Sí ──> [Volver a modificar]
    │        No ──────────────────┐
    │                              │
    No                             │
    │                              ▼
    ▼                     ¿Aplicar descuento?
¿Confirmar venta?                  │
    │                              ├──Sí──> [Ingresar descuento]
    ├──Sí──> [Seleccionar método de pago]   │
    │              │                       No
    No             ▼                       │
    │        [Registrar venta]             │
    │              │                       │
    │              ▼                       │
    │        [Decrementar stock]           │
    │              │                       │
    │              ▼                       │
    │        [Registrar movimientos]       │
    │              │                       │
    │              ▼                       │
    │        [Mostrar confirmación]       │
    │              │                       │
    └──────────────┴───────────────────────┘
                                    │
                                    ▼
                              [Fin]
```

### Actividad: Gestionar Producto

```
[Inicio]
    │
    ▼
[Seleccionar acción: Crear/Modificar/Eliminar]
    │
    ├──Crear──> [Ingresar datos del producto]
    │              │
    │              ▼
    │        ¿Código ya existe?
    │        Sí ──> [Mostrar error]
    │        No ──> [Guardar producto]
    │
    ├──Modificar──> [Seleccionar producto]
    │              │
    │              ▼
    │        [Modificar datos]
    │              │
    │              ▼
    │        [Guardar cambios]
    │
    └──Eliminar──> [Seleccionar producto]
                    │
                    ▼
              ¿Stock > 0?
              Sí ──> [Mostrar advertencia]
              No ──> [Confirmar eliminación]
                    │
                    ▼
              [Eliminar producto]
                    │
                    ▼
                 [Fin]
```

---

## DIAGRAMAS DE CLASES

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           MODELO DE CLASES                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌──────────────────────┐                                                   │
│  │     Entidad          │                                                   │
│  ├──────────────────────┤                                                   │
│  │ - id: Long          │                                                   │
│  │ - fechaCreacion     │                                                   │
│  │ - fechaModificacion │                                                   │
│  └──────────┬───────────┘                                                   │
│             │ (herencia)                                                    │
│             │                                                               │
│  ┌──────────┴──────────┐    ┌─────────────┐    ┌────────────────┐          │
│  │     Producto         │    │   Usuario   │    │    Venta       │          │
│  ├──────────────────────┤    ├─────────────┤    ├────────────────┤          │
│  │ - codigo: String    │    │ - username  │    │ - total       │          │
│  │ - nombre: String    │    │ - email     │    │ - metodoPago  │          │
│  │ - descripcion       │◄───┤ - password  │    │ - estado      │          │
│  │ - precioCompra     │    │ - rol       │    │ - observaciones│         │
│  │ - precioVenta      │    └─────────────┘    └───────┬────────┘          │
│  │ - stock: Integer   │                               │                    │
│  │ - stockMinimo     │                               │ 1..*               │
│  │ - categoriaId     │                               ▼                    │
│  └───────────────────┘                    ┌─────────────────┐             │
│                                              │   DetalleVenta │             │
│  ┌──────────────────────┐                   ├─────────────────┤             │
│  │     Categoria        │                   │ - cantidad      │             │
│  ├──────────────────────┤                   │ - precioUnitario│             │
│  │ - nombre: String    │                   │ - subtotal      │             │
│  │ - descripcion       │                   └─────────────────┘             │
│  └──────────────────────┘                                                  │
│                                              ┌─────────────────┐           │
│  ┌──────────────────────┐                   │Cliente           │           │
│  │     Proveedor       │                   ├─────────────────┤           │
│  ├──────────────────────┤                   │ - nombre        │           │
│  │ - nombre: String    │                   │ - cedula        │◄──FK──────┤
│  │ - rif: String      │                   │ - email        │           │
│  │ - email            │                   │ - telefono     │           │
│  │ - telefono         │                   │ - saldoCredito │           │
│  │ - direccion        │                   └─────────────────┘           │
│  │ - condicionesPago  │                                                  │
│  └──────────────────────┘                                                  │
│                                                                              │
│  ┌──────────────────────────┐    ┌─────────────────┐                       │
│  │ MovimientoInventario      │    │  UsuarioSesion  │                       │
│  ├──────────────────────────┤    ├─────────────────┤                       │
│  │ - tipo: TipoMovimiento  │    │ - usuarioActual │                       │
│  │ - cantidad: Integer     │    │ - rolActual     │                       │
│  │ - descripcion           │    │ - sesionActiva  │                       │
│  │ - referenciaId          │    └─────────────────┘                       │
│  └──────────────────────────┘                                               │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                          PATRÓN DAO Y SERVICE                             │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────┐     ┌─────────────┐     ┌────────────────┐                  │
│  │ DAOFactory  │────▶│ ProductoDAO │────▶│ ProductoDAOImpl│                  │
│  └─────────────┘     └─────────────┘     └────────────────┘                  │
│         │                 │                                                   │
│         │           ┌─────┴─────┐                                             │
│         │           │  Service  │                                             │
│         │           └─────┬─────┘                                             │
│         │                 │                                                   │
│         ▼                 ▼                                                   │
│  ┌─────────────┐     ┌─────────────┐                                        │
│  │ VentaDAO    │     │VentaService │                                        │
│  └─────────────┘     └─────────────┘                                        │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## DIAGRAMAS DE SECUENCIA

### Diagrama de Secuencia: Realizar Venta

```
┌──────┐     ┌──────────────┐     ┌───────────────┐     ┌─────────────────┐
│Vendedor│     │  POSController │     │ VentaService │     │ ProductoDAOImpl │
└───┬────┘     └──────┬───────┘     └──────┬────────┘     └────────┬────────┘
    │                 │                       │                       │
    │ 1. agregarItem()│                       │                       │
    │────────────────▶│                       │                       │
    │                 │ 2. buscarProducto()   │                       │
    │                 │─────────────────────▶│                       │
    │                 │                       │ 3. SELECT producto   │
    │                 │                       │─────────────────────▶│
    │                 │                       │       4. Result      │
    │                 │                       │◀─────────────────────│
    │                 │      5. Producto     │                       │
    │     6. Item     │◀─────────────────────│                       │
    │◀────────────────│                       │                       │
    │                 │                       │                       │
    │ 7. confirmarVenta()│                    │                       │
    │────────────────▶│                       │                       │
    │                 │ 8. registrarVenta()  │                       │
    │                 │─────────────────────▶│                       │
    │                 │                       │ 9. INSERT venta      │
    │                 │                       │─────────────────────▶│
    │                 │                       │ 10. INSERT detalle   │
    │                 │                       │─────────────────────▶│
    │                 │                       │ 11. UPDATE stock     │
    │                 │                       │─────────────────────▶│
    │                 │                       │ 12. INSERT movimiento│
    │                 │                       │─────────────────────▶│
    │                 │      13. Venta OK    │                       │
    │     14. OK      │◀─────────────────────│                       │
    │◀────────────────│                       │                       │
    │                 │                       │                       │
```

### Diagrama de Secuencia: Iniciar Sesión

```
┌──────────┐     ┌────────────────┐     ┌─────────────────┐     ┌──────────────┐
│  Usuario │     │ LoginController │     │SessionManager  │     │ UsuarioDAO  │
└────┬─────┘     └───────┬────────┘     └───────┬─────────┘     └──────┬───────┘
     │                    │                       │                       │
     │ 1. login(user,pass)│                    │                       │
     │───────────────────▶│                       │                       │
     │                    │ 2. autenticar()     │                       │
     │                    │────────────────────▶│                       │
     │                    │                       │ 3. buscarUsuario()   │
     │                    │                       │─────────────────────▶│
     │                    │                       │       4. Usuario     │
     │                    │                       │◀─────────────────────│
     │                    │                       │ 5. validarPassword() │
     │                    │                       │─────────────────────▶│
     │                    │                       │     6. true/false    │
     │                    │                       │◀─────────────────────│
     │                    │      7. Resultado    │                       │
     │     8. Dashboard   │◀─────────────────────│                       │
     │◀──────────────────│                       │                       │
     │                    │                       │                       │
```

---

## DIAGRAMAS DE SUBSISTEMAS

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                        SISTEMA DE INVENTARIO POS                                │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        MÓDULO DE SEGURIDAD                                  │ │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────┐     │ │
│  │  │  Autenticación  │  │ SessionManager   │  │ PasswordHasher      │     │ │
│  │  │  - login()      │  │ - iniciarSesion()│  │ - hash()           │     │ │
│  │  │  - logout()     │  │ - cerrarSesion() │  │ - verify()         │     │ │
│  │  │  - validar()    │  │ - getUsuario()   │  │                     │     │ │
│  │  └─────────────────┘  └──────────────────┘  └─────────────────────┘     │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        MÓDULO DE INVENTARIO                                  │ │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────┐     │ │
│  │  │   Producto      │  │   Categoria     │  │ MovimientoInventario│     │ │
│  │  │   - CRUD       │  │   - CRUD        │  │   - registrar()    │     │ │
│  │  │   - buscar()   │  │   - listar()    │  │   - listar()       │     │ │
│  │  │   - stockOK()  │  │                 │  │   - porProducto()  │     │ │
│  │  └─────────────────┘  └──────────────────┘  └─────────────────────┘     │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        MÓDULO DE VENTAS (POS)                               │ │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────┐     │ │
│  │  │     Venta      │  │   DetalleVenta   │  │    ItemVenta        │     │ │
│  │  │   - registrar()│  │   - crear()     │  │   - agregar()      │     │ │
│  │  │   - listar()   │  │   - listar()    │  │   - quitar()      │     │ │
│  │  │   - porFecha() │  │                 │  │   - modificar()    │     │ │
│  │  └─────────────────┘  └──────────────────┘  └─────────────────────┘     │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        MÓDULO DE CLIENTES Y PROVEEDORES                     │ │
│  │  ┌─────────────────┐  ┌──────────────────┐                                  │ │
│  │  │    Cliente     │  │   Proveedor      │                                  │ │
│  │  │   - CRUD       │  │   - CRUD        │                                  │ │
│  │  │   - buscar()   │  │   - buscar()    │                                  │ │
│  │  │   - porCedula()│  │   - porRif()    │                                  │ │
│  │  └─────────────────┘  └──────────────────┘                                  │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────────┐ │
│  │                        MÓDULO DE REPORTES                                    │ │
│  │  ┌─────────────────┐  ┌──────────────────┐  ┌─────────────────────┐     │ │
│  │  │ReportesController│ │PdfReportExporter│  │   ReporteService    │     │ │
│  │  │ - ventas()     │  │ - exportVentas()│  │   - generarVentas()│     │ │
│  │  │ - inventario() │  │ - exportInvent()│  │   - generarInvent()│     │ │
│  │  │ - movimientos()│  │ - exportMovims()│  │   - generarGanancias│    │ │
│  │  │ - ganancias()  │  │ - exportGanancias│ │                     │     │ │
│  │  └─────────────────┘  └──────────────────┘  └─────────────────────┘     │ │
│  └─────────────────────────────────────────────────────────────────────────────┘ │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

---

## PROTOTIPOS

### Pantalla de Login

```
┌─────────────────────────────────────────┐
│                                         │
│           [Logo del Sistema]            │
│                                         │
│      SISTEMA DE INVENTARIO Y POS        │
│                                         │
│  ┌─────────────────────────────────┐    │
│  │  Usuario:                       │    │
│  │  [________________________]      │    │
│  │                                 │    │
│  │  Contraseña:                   │    │
│  │  [________________________]      │    │
│  │                                 │    │
│  │  [        INGRESAR        ]    │    │
│  │                                 │    │
│  │  Intentos restantes: 5         │    │
│  └─────────────────────────────────┘    │
│                                         │
└─────────────────────────────────────────┘
```

### Dashboard Administrador

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │  [Logo] SISTEMA POS          Usuario: admin (ADMIN)    [Cerrar Sesión] │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│                                                                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐                │
│  │ PRODUCTOS  │ │ CATEGORÍAS │ │   VENTAS   │ │  CLIENTES  │                │
│  │   [📦]     │ │   [🏷️]     │ │   [💰]     │ │   [👤]     │                │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘                │
│                                                                                  │
│  ┌────────────┐ ┌────────────┐ ┌────────────┐ ┌────────────┐                │
│  │PROVEEDORES │ │MOVIMIENTOS│ │  REPORTES  │ │  USUARIOS │                │
│  │   [🏢]     │ │   [📋]     │ │   [📊]     │ │   [👥]     │                │
│  └────────────┘ └────────────┘ └────────────┘ └────────────┘                │
│                                                                                  │
│  ┌─────────────────────────────────────────────────────────────────────────┐    │
│  │                         ESTADÍSTICAS RÁPIDAS                           │    │
│  │  ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐           │    │
│  │  │ Total Productos │ │ Ventas Hoy      │ │ Stock Bajo      │           │    │
│  │  │     156        │ │    $12,450.00   │ │      8         │           │    │
│  │  └─────────────────┘ └─────────────────┘ └─────────────────┘           │    │
│  └─────────────────────────────────────────────────────────────────────────┘    │
│                                                                                  │
└─────────────────────────────────────────────────────────────────────────────────┘
```

### Módulo de Punto de Venta

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              PUNTO DE VENTA                                     │
├───────────────────────────────────┬─────────────────────────────────────────────┤
│                                   │                                             │
│  ┌─────────────────────────────┐ │                                             │
│  │ Buscar producto (código/nombre)│ │                                             │
│  │ [___________________][🔍]  │ │                                             │
│  └─────────────────────────────┘ │                                             │
│                                   │                                             │
│  ┌─────────────────────────────┐ │  ┌───────────────────────────────────────┐│
│  │ PRODUCTOS ENCONTRADOS       │ │  │         CARRITO DE COMPRAS           ││
│  │ ┌─────┬──────────┬──────┐ │ │  │                                       ││
│  │ │Código│ Nombre   │Stock │ │ │  │  #  Producto        Cant.    Subtotal││
│  │ ├─────┼──────────┼──────┤ │ │  │  ─────────────────────────────────────││
│  │ │001  │Arroz 1kg │  45  │ │ │  │  1  Refresco Coca    3 x $1.50  $4.50││
│  │ │002  │Frijoles  │  32  │ │ │  │  2  Pan Bimbo        2 x $2.00  $4.00││
│  │ │003  │Aceite    │  28  │ │ │  │                                       ││
│  │ │004  │Azúcar    │  50  │ │ │  │                                       ││
│  │ └─────┴──────────┴──────┘ │ │  │                                       ││
│  └─────────────────────────────┘ │  │                                       ││
│                                   │  │  Subtotal:              $8.50      ││
│  [Agregar al carrito]            │  │  Descuento:              $0.00      ││
│                                   │  │  ─────────────────────────────────    ││
│                                   │  │  TOTAL:                $8.50      ││
│                                   │  │                                       ││
│                                   │  │  Cliente: [Sin cliente      ▼]      ││
│                                   │  │                                       ││
│                                   │  │  Método Pago: [Efectivo ▼]          ││
│                                   │  │                                       ││
│                                   │  │  Descuento ($): [____] [%:____]      ││
│                                   │  │                                       ││
│                                   │  │  [    COBRAR Y FINALIZAR    ]        ││
│                                   │  │  [        CANCELAR         ]        ││
│                                   │  └───────────────────────────────────────┘│
└───────────────────────────────────┴─────────────────────────────────────────────┘
```

---

## TECNOLOGÍAS UTILIZADAS

| Componente | Tecnología |
|------------|------------|
| **Lenguaje** | Java 11+ |
| **Framework UI** | JavaFX 21.0.2 |
| **Base de Datos** | PostgreSQL 14+ |
| **Driver JDBC** | PostgreSQL JDBC 42.7.1 |
| **Pool de Conexiones** | HikariCP 5.1.0 |
| **Cifrado** | Spring Security BCrypt |
| **Generación PDF** | iTextPDF 5.5.13.3 |
| **Logging** | Log4j2 |
| **Construcción** | Maven |
| **Arquitectura** | MVC con DAO/Service |

---

**Documento generado para el Proyecto de Análisis y Diseño de Software**
