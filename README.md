# Sistema de Inventario y Punto de Venta

Aplicación de escritorio (JavaFX) para controlar inventario, ventas, clientes y proveedores de **una sucursal en una sola computadora**. Los datos se guardan en una base de datos **H2 embebida**: es un archivo local, así que no hay que instalar ni administrar un servidor de base de datos.

Para el uso diario (vender, corte de caja, respaldos) consulta [MANUAL_DE_USO.md](MANUAL_DE_USO.md).

## Requisitos

| Para | Necesitas |
|------|-----------|
| Usar la aplicación empaquetada | Nada: el paquete trae su propio Java |
| Ejecutar el `.jar` | Java 17 o superior |
| Desarrollar o compilar | JDK 17 o superior y Maven 3.8+ |

## Dónde guarda los datos

Todo vive en la carpeta del usuario de Windows: `%USERPROFILE%\SistemaInventarioPOS\`

| Carpeta / archivo | Contenido |
|-------------------|-----------|
| `datos\inventario.mv.db` | La base de datos (no la abras ni la copies con el sistema abierto; usa los respaldos) |
| `respaldos\` | Respaldos automáticos (`auto_*.zip`, se guardan los últimos 30) y los previos a una restauración |
| `tickets\` | Tickets de venta y cortes de caja en PDF |
| `logs\aplicacion.log` | Registro de la aplicación, para diagnosticar errores |
| `negocio.properties` | Nombre, dirección, teléfono, RFC y mensaje que salen en los tickets |

La base de datos se crea sola la primera vez que se abre el sistema (`schema.sql` + `data_inicial.sql`). Para usar otra carpeta, por ejemplo en pruebas, arranca con `-Dinventario.home=C:\otra\carpeta`.

**Usuarios iniciales**

| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| `admin` | `admin123` | ADMIN: acceso completo |
| `vendedor` | `vendedor123` | VENDEDOR: punto de venta y clientes |

Cambia las dos contraseñas el primer día con el botón **Cambiar contraseña**.

## Ejecutar en desarrollo

```bat
mvn javafx:run
```

`ejecutar.bat` y `ejecutar.ps1` hacen lo mismo, con limpieza y compilación previas.

## Pruebas

```bat
mvn test
```

Las pruebas crean una base de datos temporal y cubren estos casos:

- Una venta descuenta el stock, guarda el subtotal, el descuento y el costo, y registra el movimiento.
- Una venta sin stock suficiente se rechaza y no deja nada guardado.
- Un descuento mayor al subtotal se rechaza.
- Anular una venta devuelve el stock.
- Un ajuste por conteo físico deja el stock contado.
- Una salida mayor al stock se rechaza.
- El respaldo se crea y se restaura de ida y vuelta, y un archivo ajeno se rechaza.
- El ticket, el corte de caja y la reimpresión de tickets se generan en PDF.
- Administración de usuarios: alta, contraseña restablecida, desactivación, duplicados, permisos del vendedor, el admin no puede desactivarse a sí mismo y la recuperación de emergencia.
- El login se bloquea tras 5 intentos fallidos y se libera al pasar el tiempo.
- El corte de caja se guarda con su diferencia y se rechazan montos negativos.
- Un producto con historial se desactiva (y se reactiva); uno sin historial se borra.

## Generar la versión para la laptop de la sucursal

```bat
empaquetar.bat
```

1. Compila, corre las pruebas y genera `target\sistema-inventario-pos.jar` con todas las dependencias.
2. Con `jpackage` (incluido en el JDK) crea `dist\SistemaInventarioPOS\`. Esa carpeta trae su propio Java, el ejecutable `SistemaInventarioPOS.exe` y el lanzador `Iniciar sistema.cmd`.
3. Copia la carpeta `dist\SistemaInventarioPOS` completa a la laptop y crea un acceso directo en el escritorio a `Iniciar sistema.cmd` (funciona siempre) o al `.exe`.

Para un instalador `.msi` con acceso directo en el menú Inicio, instala [WiX Toolset 3](https://wixtoolset.org/) y ejecuta `empaquetar.bat msi`.

> **Windows 11 y el Control inteligente de aplicaciones:** el `.exe` no está firmado digitalmente y esa función de Windows lo bloquea (verificado en Windows 11 con la función activada). `Iniciar sistema.cmd` evita el problema: arranca el sistema con `runtime\bin\javaw.exe`, que sí está firmado por el proveedor de Java. La otra opción es firmar el `.exe` con un certificado de firma de código.

## Estructura del código

```
src/main/java/com/inventario/
├── Main.java / Launcher.java   Arranque (Launcher es el punto de entrada del .jar empaquetado)
├── config/                     AppPaths (carpetas de datos), SessionManager (usuario actual y rol)
├── database/                   ConnectionFactory: pool H2, creación del esquema y transacciones
├── dao/ + dao/impl/            Acceso a datos con JDBC (una clase por tabla)
├── services/                   Reglas de negocio (ventas, inventario, productos, autenticación…)
├── controllers/                Pantallas JavaFX (cada una con su .fxml en resources/fxml)
├── models/                     Entidades
└── utils/                      Alertas, BCrypt, reportes PDF, TicketPdf, RespaldoUtil
```

### Reglas importantes del diseño

- **Transacciones.** Cada venta, anulación o movimiento de inventario se ejecuta dentro de `ConnectionFactory.enTransaccion(...)`: o se guarda todo, o nada. Los DAOs no cambian; dentro de una transacción reciben la misma conexión.
- **El stock solo cambia por movimientos.** Las ventas, anulaciones, entradas, salidas y ajustes pasan por `IProductoDAO.updateStock`, que rechaza dejar el stock negativo. Todos esos cambios quedan en `movimientos_inventario`. Al editar un producto existente no se puede cambiar su stock. La tabla tiene además `CHECK (stock >= 0)`.
- **Ganancias reales.** Cada detalle de venta guarda el `costo_unitario` del momento de la venta, así que cambiar después el precio de compra no altera los reportes históricos.
- **Roles.** El VENDEDOR solo ve Punto de Venta y Clientes. Inventario, Proveedores, Reportes, Usuarios y Respaldos son del ADMIN. Los servicios también verifican el rol al anular ventas, al mover inventario y al administrar usuarios (`UsuarioService`). Siempre queda al menos un ADMIN activo.
- **Recuperación del administrador.** Arrancar con el argumento `--restablecer-admin` deja al usuario `admin` activo, con rol ADMIN y contraseña `admin123` (`UsuarioService.restablecerAdminDeEmergencia`). No pide sesión: quien puede ejecutar el programa en la laptop ya tiene acceso al archivo de datos.
- **Bloqueo de login.** Tras 5 intentos fallidos seguidos se bloquea el inicio de sesión 5 minutos. El contador es estático para que recargar la pantalla de login no lo reinicie.
- **Productos desactivados.** `productos.activo = FALSE` oculta el producto de las listas y búsquedas por nombre e impide venderlo. `findByCodigo` y `read` sí lo devuelven, para conservar el historial y poder reactivarlo. Un producto se borra físicamente solo si no tiene movimientos.
- **Localización.** Las columnas `clientes.cedula` y `proveedores.rif` conservan su nombre, pero guardan el RFC, y la interfaz las muestra como "RFC".

### Base de datos

El esquema está en [`src/main/resources/schema.sql`](src/main/resources/schema.sql). Tablas: `usuarios`, `categorias`, `productos`, `clientes`, `proveedores`, `ventas`, `detalles_venta`, `movimientos_inventario`, `cortes_caja`.

Para revisar los datos a mano, cierra el sistema y abre la consola de H2:

```bat
java -cp %USERPROFILE%\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar org.h2.tools.Console
```

Conéctate con la URL `jdbc:h2:file:%USERPROFILE%\SistemaInventarioPOS\datos\inventario`, el usuario `sa` y la contraseña vacía.

**Cambios al esquema.** `schema.sql` se ejecuta en **cada arranque**, así que cada sentencia debe poder repetirse sin error. Para agregar algo a una base ya instalada:

- Las tablas nuevas van con `CREATE TABLE IF NOT EXISTS`.
- Las columnas nuevas van al final del archivo con `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`, como `productos.activo`.

Los datos iniciales (`data_inicial.sql`) solo se cargan en una base nueva. Si algún día necesitas transformar datos existentes (renombrar columnas, mover información), conviene adoptar una herramienta de migraciones con versiones, como Flyway.

## Licencia

Ver [LICENSE](LICENSE).
