# Manual de uso

Guía para el personal de la sucursal. Para instalar el sistema o hacer cambios al código, ve el [README](README.md).

## 1. Primer día

1. Abre el sistema y entra con `admin` / `admin123`.
2. Pulsa **Cambiar contraseña** (arriba a la derecha) y pon una contraseña nueva. Haz lo mismo con el usuario `vendedor` (contraseña inicial `vendedor123`).
3. Edita los datos que salen en el ticket. Abre con el Bloc de notas el archivo
   `%USERPROFILE%\SistemaInventarioPOS\negocio.properties`
   (se crea solo la primera vez que abres el sistema). Ejemplo:
   ```
   nombre=Abarrotes La Esquina
   direccion=Av. Reforma 123, Mexicali, B.C.
   telefono=686 123 4567
   rfc=XAXX010101000
   mensaje=¡Gracias por su compra!
   ```
4. Da de alta tus proveedores en **Proveedores**, y tus categorías y productos en **Inventario**. El stock que captures al crear un producto queda registrado como "Inventario inicial".

## 2. Qué puede hacer cada usuario

| Función | Administrador | Vendedor |
|---------|:---:|:---:|
| Punto de venta, tickets, reimpresión y corte de caja | ✔ | ✔ |
| Clientes | ✔ | ✔ |
| Inventario (productos, entradas, salidas, ajustes) | ✔ | — |
| Proveedores | ✔ | — |
| Reportes y anulación de ventas | ✔ | — |
| Usuarios y respaldos | ✔ | — |

## 3. Vender

1. Entra a **Punto de Venta**.
2. Escanea o escribe el código del producto y pulsa Enter o **Buscar**. Con el botón **…** buscas por nombre.
3. Ajusta las cantidades con **+** y **−**, y quita productos con **X**. El sistema no deja vender más unidades de las que hay en stock.
4. El cliente es **Público en general** si no eliges otro.
5. Elige el **método de pago**. Si es en efectivo, escribe en **Paga con** el billete que te dieron y el sistema muestra el **Cambio**.
6. Si hay descuento, escribe el **monto** (en pesos, no en porcentaje).
7. Pulsa **COBRAR**. En la confirmación puedes pulsar **Imprimir ticket**: se abre el PDF del ticket para mandarlo a la impresora. Los tickets quedan guardados en la carpeta `tickets`.

## 4. Corte de caja

Al cerrar el turno, en **Punto de Venta**:

1. Pulsa **Corte de caja**.
2. Captura el **fondo inicial** (el efectivo con el que abriste la caja) y el **efectivo contado** (lo que hay en la caja ahora).
3. El corte se **guarda en el sistema** y se abre un PDF con:
   - las ventas del día por método de pago,
   - el efectivo que debería haber,
   - la **diferencia** (faltante si es negativa, sobrante si es positiva),
   - un espacio para la firma.

   Imprímelo o guárdalo. El administrador ve todos los cortes en **Reportes → Cortes de caja**; los faltantes aparecen en rojo.

### Reimprimir un ticket

En **Punto de Venta**, pulsa **Reimprimir ticket** y escribe el número de venta (el "Ticket #"). Ya viene escrito el de la última venta. La copia dice "REIMPRESIÓN" y, si la venta fue anulada, "VENTA ANULADA". La copia no trae el "Paga con" ni el cambio, porque esos datos no se guardan.

## 5. Inventario (administrador)

En **Inventario**, selecciona un producto y usa **Ajustar Stock**:

| Opción | Para qué |
|--------|----------|
| **Entrada** | Llegó mercancía. Puedes elegir el proveedor que la surtió. |
| **Salida** | Merma, producto caducado, uso interno… No puede quedar stock negativo. |
| **Ajuste (conteo físico)** | Contaste el anaquel y la cantidad no coincide. Escribe **cuántas piezas hay realmente**; el sistema corrige el stock y registra la diferencia. |

Todo queda registrado (botón **Ver Movimientos** y **Reportes → Movimientos**), junto con las ventas y anulaciones. Para editar nombre, precios o categoría usa **Editar**. El stock de un producto existente solo se cambia con **Ajustar Stock**, para que quede registrado.

### Dejar de vender un producto

Para eliminar un producto, su stock debe estar en 0 (usa **Ajustar Stock**). Después pulsa **Eliminar**:

- Si el producto **nunca** tuvo ventas ni movimientos, se borra.
- Si **sí** tiene historial, se **desactiva**: ya no aparece en las listas ni se puede vender, pero sus ventas y movimientos siguen en los reportes.

Para recuperar un producto desactivado, pulsa **+ Nuevo Producto** y escribe el mismo código. El sistema te ofrece reactivarlo.

## 6. Anular una venta (administrador)

1. Ve a **Reportes → Ventas**.
2. Selecciona la venta en la tabla y pulsa **Anular venta seleccionada** (abajo de la tabla).

Los productos regresan al inventario y la venta queda marcada como ANULADA. Ya no cuenta en los totales ni en las ganancias.

## 7. Reportes (administrador)

- **Ventas**: totales, número de ventas y promedio por periodo.
- **Ganancias**: venta menos costo. El costo es el precio de compra que tenía el producto **el día de la venta**.
- **Inventario** y **Movimientos**: existencias, productos con stock bajo y todo lo que entró o salió.
- **Cortes de caja**: los últimos 200 cortes, con su fondo, el efectivo esperado, el contado y la diferencia.

Todos menos los cortes se pueden exportar a PDF (cada corte ya tiene su propio PDF en la carpeta `tickets`).

## 8. Usuarios (administrador)

En **👤 Usuarios** puedes:

| Botón | Para qué |
|-------|----------|
| **+ Nuevo Usuario** | Dar de alta a un cajero (rol VENDEDOR) o a otro administrador (ADMIN). |
| **Cambiar rol** | Pasar a alguien de VENDEDOR a ADMIN o al revés. |
| **Activar / Desactivar** | Quitar el acceso a alguien que ya no trabaja ahí. Sus ventas se conservan. |
| **Restablecer contraseña** | El usuario olvidó su contraseña: le asignas una nueva y él la cambia al entrar con **Cambiar contraseña**. |

No puedes desactivarte ni cambiarte el rol a ti mismo, y siempre debe quedar al menos un administrador activo.

**Recomendación:** ten dos administradores (por ejemplo, el dueño y el encargado) para que uno pueda restablecer la contraseña del otro.

### Si el único administrador olvidó su contraseña

1. Cierra el sistema.
2. Abre una ventana de comandos (`cmd`) en la carpeta donde está instalado el sistema.
3. Ejecuta:
   ```
   "Iniciar sistema.cmd" --restablecer-admin
   ```
4. El sistema avisa que el usuario `admin` quedó activo con la contraseña `admin123`. Entra y cámbiala de inmediato.

Cualquiera con acceso a la laptop puede hacer esto. Protege la sesión de Windows con contraseña.

## 9. Respaldos (¡importante!)

Si la laptop se descompone o la roban, **solo podrás recuperar la información si tienes un respaldo fuera de ella.**

- **Automático:** cada vez que se cierra el sistema se guarda un respaldo en `%USERPROFILE%\SistemaInventarioPOS\respaldos`. Se conservan los últimos 30.
- **Manual:** en **💾 Respaldos → Crear respaldo**, elige dónde guardarlo (por ejemplo, una USB).
- **Recomendación:** al menos una vez por semana copia el respaldo más reciente a una USB o a la nube (Google Drive, OneDrive…).

### Restaurar un respaldo

1. Ve a **💾 Respaldos → Restaurar respaldo** y elige el archivo `.zip`.
2. Confirma. Los datos actuales se reemplazan por los del respaldo. Antes de reemplazarlos, el sistema guarda una copia de los datos actuales (`antes_de_restaurar_*.zip`) por si te equivocaste de archivo.
3. El sistema se cierra. Vuelve a abrirlo.

### Pasar el sistema a otra laptop

1. Instala el sistema en la laptop nueva.
2. Ábrelo una vez.
3. Restaura el respaldo más reciente de la laptop anterior.

## 10. Problemas comunes

| Mensaje | Qué hacer |
|---------|-----------|
| "El sistema ya está abierto en otra ventana" | Solo puede haber una ventana del sistema abierta. Búscala en la barra de tareas. |
| "La sesión expiró…" | Por seguridad, la sesión se cierra tras 30 minutos sin actividad o tras 8 horas. Pulsa **Cerrar sesión** y vuelve a entrar. |
| "Demasiados intentos fallidos. Espere…" | Tras 5 contraseñas incorrectas seguidas, el inicio de sesión se bloquea 5 minutos. Espera y vuelve a intentarlo. |
| "Credenciales inválidas" y estás seguro de la contraseña | Puede que tu usuario esté desactivado. Pide a un administrador que lo revise en **Usuarios**. |
| "Stock insuficiente" | No hay suficientes piezas registradas. Si físicamente sí hay, haz un **Ajuste (conteo físico)**. |
| Windows no deja abrir `SistemaInventarioPOS.exe` | Abre el sistema con **Iniciar sistema.cmd** (en la misma carpeta). O mejor, dale doble clic a **Crear acceso directo.cmd** y usa el ícono **Sistema POS** del escritorio. |
| Cualquier otro error | Revisa o envía el archivo `%USERPROFILE%\SistemaInventarioPOS\logs\aplicacion.log` a soporte. |
