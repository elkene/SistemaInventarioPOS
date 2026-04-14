# SOLUCIÓN RÁPIDA - Error de JavaFX en Windows

## ⚡ SOLUCIÓN INMEDIATA

El error "JavaFX runtime components are missing" ocurre cuando Java no puede encontrar las librerías de JavaFX. Aquí está la solución:

---

## **OPCIÓN 1: Ejecutar con Maven (RECOMENDADO)**

### En Terminal/PowerShell:

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
mvn clean javafx:run
```

### O usar el script batch:

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
ejecutar.bat
```

### O usar el script PowerShell:

```powershell
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
.\ejecutar.ps1
```

---

## **OPCIÓN 2: Si ejecutas desde IntelliJ IDEA**

### Paso 1: Invalidar Cache
1. File → Invalidate Caches / Restart
2. Click en "Invalidate and Restart"
3. Esperar a que reinicie

### Paso 2: Compilar nuevamente
```
View → Tool Windows → Maven
→ SistemaInventarioPOS → Lifecycle → Double-click "clean"
→ SistemaInventarioPOS → Lifecycle → Double-click "compile"
```

### Paso 3: Ejecutar
```
View → Tool Windows → Maven
→ SistemaInventarioPOS → Plugins → javafx → Double-click "javafx:run"
```

---

## **OPCIÓN 3: Configurar Run Configuration en IntelliJ**

1. **Run → Edit Configurations**
2. **Click en "+"**
3. **Seleccionar "Maven"**
4. **Llenar:**
   - Name: `Run POS`
   - Working directory: `C:\Users\Kenne\Idea\SistemaInventarioPOS`
   - Command line: `clean javafx:run`
5. **Click OK**
6. **Run → Run 'Run POS'**

---

## **OPCIÓN 4: Ejecutar desde Eclipse**

1. Click derecho en el proyecto
2. Run As → Maven clean
3. Click derecho en el proyecto
4. Run As → Maven build
5. Goals: `javafx:run`
6. Click Run

---

## **OPCIÓN 5: Descargar dependencias manualmente**

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
mvn dependency:resolve
mvn dependency:resolve-plugins
mvn clean compile
```

---

## ✅ VERIFICACIÓN

Antes de ejecutar, verifica:

```bash
# Verificar Java
java -version

# Verificar Maven
mvn -version

# Verificar que PostgreSQL está corriendo
# (Si tienes PostgreSQL instalado)
```

---

## 🎯 PASOS PARA EJECUTAR LA APLICACIÓN

### 1. Preparar Base de Datos (Primera vez)

```sql
-- En pgAdmin o SQL Shell de PostgreSQL
CREATE DATABASE inventario_pos;

-- Ejecutar el script SQL
-- C:\Users\Kenne\Idea\SistemaInventarioPOS\src\main\resources\schema.sql
```

### 2. Compilar y Ejecutar

**Opción A - Desde línea de comandos:**
```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
mvn clean javafx:run
```

**Opción B - Desde script:**
```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
ejecutar.bat
```

### 3. Login

- Usuario: `admin`
- Contraseña: `admin123`

---

## ❌ SI AÚN TIENES PROBLEMAS

### Error persiste después de ejecutar `mvn clean javafx:run`

**Solución:**

```bash
# Eliminar cache de Maven
rmdir /s /q C:\Users\Kenne\.m2\repository\org\openjfx
rmdir /s /q C:\Users\Kenne\.m2\repository\org\springframework\security

# Limpiar y recompilar
mvn clean
mvn dependency:resolve
mvn compile
mvn javafx:run
```

### JavaFX sigue sin funcionar en IntelliJ

**Solución alternativa:**

1. Abre Terminal en IntelliJ
2. Ejecuta:
```bash
mvn clean javafx:run
```

Esto evita los problemas de configuración de IntelliJ.

---

## 📝 NOTAS IMPORTANTES

- **Maven es la forma más confiable de ejecutar JavaFX** en Windows
- No intentes ejecutar `java -jar` directamente, no funcionará con JavaFX
- Si IntelliJ te pide descargar dependencias, déjalo completar
- Si tienes firewall, asegúrate de permitir que Maven descargue dependencias

---

## 🆘 ÚLTIMA OPCIÓN - Limpiar todo

Si nada funciona:

```bash
# Eliminar directorio target
rmdir /s /q C:\Users\Kenne\Idea\SistemaInventarioPOS\target

# Limpiar cache Maven
rmdir /s /q C:\Users\Kenne\.m2\repository

# En IntelliJ: File → Invalidate Caches / Restart

# Recompilar
mvn clean install

# Ejecutar
mvn javafx:run
```

---

**¡La aplicación debería ejecutarse correctamente ahora!** ✅

Si aún tienes problemas, asegúrate de:
1. ✅ Java 11+ instalado
2. ✅ Maven instalado
3. ✅ PostgreSQL corriendo (para la BD)
4. ✅ Conexión a Internet (para descargar dependencias)

