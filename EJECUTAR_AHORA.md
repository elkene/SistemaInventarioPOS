# ✅ PROYECTO COMPLETO - GUÍA DE EJECUCIÓN

## 📋 ESTADO DEL PROYECTO

El **Sistema de Inventario y Punto de Ventas (POS)** está **COMPLETAMENTE IMPLEMENTADO** con todos los módulos:

- ✅ Módulo de Inventario
- ✅ Módulo de Ventas
- ✅ Módulo de Clientes
- ✅ Módulo de Proveedores
- ✅ Módulo de Reportes

---

## 🚀 CÓMO EJECUTAR AHORA

### **OPCIÓN 1: Usar el Script (MÁS FÁCIL)**

#### Windows - Command Prompt:
```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
ejecutar.bat
```

#### Windows - PowerShell:
```powershell
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
.\ejecutar.ps1
```

---

### **OPCIÓN 2: Desde Terminal de IntelliJ**

1. **En IntelliJ IDEA**
   - Terminal → New Terminal
   - Asegúrate de estar en: `C:\Users\Kenne\Idea\SistemaInventarioPOS`

2. **Ejecuta:**
```bash
mvn clean javafx:run
```

---

### **OPCIÓN 3: Desde Maven en IntelliJ**

1. **Abre Maven:**
   - View → Tool Windows → Maven

2. **Expande el árbol:**
   - SistemaInventarioPOS
   - Lifecycle

3. **Double-click en "clean"**
   - Espera a que termine

4. **Luego:**
   - Plugins
   - javafx
   - Double-click en "javafx:run"

---

### **OPCIÓN 4: Línea de Comandos Directa**

Abre **PowerShell** o **Command Prompt** y ejecuta:

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
mvn clean javafx:run
```

---

## 📋 ANTES DE EJECUTAR

### ✅ Verificar que tienes:

1. **Java instalado:**
```bash
java -version
```
Debería mostrar: `Java 11` o superior

2. **Maven instalado:**
```bash
mvn -version
```
Debería mostrar: `Maven 3.9` o superior

3. **PostgreSQL corriendo:**
   - Abre pgAdmin
   - O Services de Windows
   - Verifica que PostgreSQL está "Running"

4. **Base de datos creada:**
   - Abre SQL Shell o pgAdmin
   - Ejecuta: `CREATE DATABASE inventario_pos;`
   - O copia el contenido de `schema.sql` y ejecútalo

---

## 🔐 CREDENCIALES DE LOGIN

Una vez que se abra la aplicación:

- **Usuario:** `admin`
- **Contraseña:** `admin123`

---

## 📂 MÓDULOS IMPLEMENTADOS

| Módulo | Descripción |
|--------|-------------|
| **Inventario** | CRUD productos, categorías, ajuste stock, historial |
| **Ventas** | Carrito, búsqueda productos, métodos pago |
| **Clientes** | CRUD clientes, búsqueda, crédito |
| **Proveedores** | CRUD proveedores, búsqueda, condiciones pago |
| **Reportes** | Ventas, inventario, movimientos, ganancias |

---

## 📂 ARCHIVOS IMPORTANTES

| Archivo | Propósito |
|---------|-----------|
| `pom.xml` | Configuración Maven |
| `ejecutar.bat` | Script para Windows CMD |
| `ejecutar.ps1` | Script para PowerShell |
| `src/main/resources/schema.sql` | Script para crear BD |
| `src/main/resources/database.properties` | Configuración BD |
| `src/main/resources/data_inicial.sql` | Datos de prueba |

---

## 🎯 PASO A PASO FINAL

### 1️⃣ Abre PowerShell/CMD

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
```

### 2️⃣ Ejecuta la aplicación

```bash
mvn clean javafx:run
```

### 3️⃣ Espera a que se compile

- Verás muchas líneas de compilación
- Esto puede tomar 30-60 segundos la primera vez

### 4️⃣ Se abrirá una ventana

- La pantalla de login de la aplicación

### 5️⃣ Login

- Usuario: `admin`
- Contraseña: `admin123`

### 6️⃣ ¡Listo! 🎉

- Verás el dashboard principal con los 5 módulos

---

## ❌ SI TIENES UN ERROR

### Error: "Command not found: mvn"
**Solución:** Maven no está en PATH
```bash
# Descarga Maven desde: https://maven.apache.org/download.cgi
# O reinstálalo y asegúrate de agregarlo a PATH
```

### Error: "Cannot find Java"
**Solución:** Java no está en PATH
```bash
# Instala JDK 11+ desde: https://www.oracle.com/java/technologies/javase-jdk11-downloads.html
```

### Error: "Could not find or load main class"
**Solución:** Compila primero
```bash
mvn clean compile
mvn javafx:run
```

### Error: "JavaFX runtime components are missing"
**Solución:** Limpiar cache
```bash
mvn clean
rmdir /s /q target
mvn javafx:run
```

### Error: "Connection refused" o "No se puede conectar a la BD"
**Solución:**
1. Verifica que PostgreSQL está corriendo
2. Verifica las credenciales en `database.properties`
3. Verifica que la base de datos `inventario_pos` existe

---

## 📚 DOCUMENTACIÓN

Para más información, consulta:
- **README.md** - Documentación general
- **INSTRUCCIONES.md** - Guía completa
- **RESUMEN_PROYECTO.md** - Resumen ejecutivo
- **CHECKLIST_VERIFICACION.md** - Verificación completa
- **SOLUCION_JAVAFX.md** - Soluciones errores JavaFX
- **GUIA_INTELLIJ.md** - Si usas IntelliJ

---

## ✨ MÓDULOS IMPLEMENTADOS

### 📦 Inventario
- Lista de productos con búsqueda
- CRUD de productos
- Gestión de categorías
- Ajuste de stock (entradas/salidas)
- Historial de movimientos
- Productos bajo stock

### 💳 Ventas
- Punto de ventas completo
- Carrito de compras
- Búsqueda de productos por código/nombre
- Múltiples métodos de pago
- Selector de cliente
- Descuentos
- Completar/anular ventas

### 👥 Clientes
- Lista de clientes
- CRUD de clientes
- Búsqueda por nombre
- Información de crédito

### 🏭 Proveedores
- Lista de proveedores
- CRUD de proveedores
- Búsqueda por nombre
- Condiciones de pago

### 📊 Reportes
- Reporte de Ventas (por rango de fechas)
- Reporte de Inventario (estado actual, stock bajo, valor)
- Reporte de Movimientos (entradas, salidas, ajustes)
- Reporte de Ganancias (costos, ganancias, porcentaje)

---

## 🎓 RESUMEN

| Paso | Comando |
|------|---------|
| 1. Ir a carpeta | `cd C:\Users\Kenne\Idea\SistemaInventarioPOS` |
| 2. Ejecutar | `mvn clean javafx:run` |
| 3. Login | Usuario: `admin` / Contraseña: `admin123` |

---

**¡La aplicación está lista para usarse!** ✅

**Versión**: 1.0.0
**Fecha**: Abril 2026
**Estado**: COMPLETADO
