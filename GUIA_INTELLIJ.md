# Instrucciones para ejecutar desde IntelliJ IDEA

## OPCIÓN 1: Usar Maven desde IntelliJ

1. **Abrir el proyecto en IntelliJ**
   - File → Open
   - Seleccionar: C:\Users\Kenne\Idea\SistemaInventarioPOS
   - Click OK

2. **Configurar el SDK de Java**
   - File → Project Structure → Project
   - SDK: Seleccionar Java 11 o superior
   - Language level: 11
   - Click OK

3. **Ejecutar con Maven**
   - View → Tool Windows → Maven
   - En el panel Maven, expandir:
     - SistemaInventarioPOS
     - Lifecycle
   - Double-click en "clean"
   - Esperar a que termine
   - Double-click en "javafx:run"

## OPCIÓN 2: Configurar una Run Configuration

1. **Crear una nueva Run Configuration**
   - Run → Edit Configurations
   - Click en "+"
   - Seleccionar "Maven"
   - Nombre: "Run POS"
   - Working directory: C:\Users\Kenne\Idea\SistemaInventarioPOS
   - Command line: clean javafx:run
   - Click OK

2. **Ejecutar**
   - Run → Run 'Run POS'
   - O presionar Shift + F10

## OPCIÓN 3: Ejecutar Main.java directamente (No recomendado)

1. **Asegúrate de agregar VM options**
   - Run → Edit Configurations
   - Click en "+"
   - Seleccionar "Application"
   - Main class: com.inventario.Main
   - VM options: 
     ```
     --module-path C:\Users\Kenne\.m2\repository\org\openjfx\javafx-base\21.0.2\javafx-base-21.0.2.jar:C:\Users\Kenne\.m2\repository\org\openjfx\javafx-controls\21.0.2\javafx-controls-21.0.2.jar:C:\Users\Kenne\.m2\repository\org\openjfx\javafx-fxml\21.0.2\javafx-fxml-21.0.2.jar:C:\Users\Kenne\.m2\repository\org\openjfx\javafx-graphics\21.0.2\javafx-graphics-21.0.2.jar
     --add-modules javafx.controls,javafx.fxml
     ```

## SOLUCIÓN DE PROBLEMAS

### Error: "JavaFX runtime components are missing"

**Solución:**
1. Ejecutar: `mvn clean install`
2. Ir a: File → Invalidate Caches / Restart
3. Seleccionar "Invalidate and Restart"
4. Intentar nuevamente con Maven

### Error: "Cannot find symbol"

**Solución:**
1. File → Project Structure → Modules
2. Seleccionar el módulo "SistemaInventarioPOS"
3. Click en "Dependencies"
4. Verificar que todas las dependencias están OK
5. File → Invalidate Caches / Restart

### Error: "Module not found"

**Solución:**
1. Abrir terminal en el proyecto
2. Ejecutar: `mvn dependency:resolve`
3. Ejecutar: `mvn clean install`

## MÉTODO RECOMENDADO

**Usar Maven desde línea de comandos:**

```bash
cd C:\Users\Kenne\Idea\SistemaInventarioPOS
mvn clean javafx:run
```

O usar el script:

```bash
# PowerShell
.\ejecutar.ps1

# Command Prompt
ejecutar.bat
```

---

Si tienes problemas, verifica:
1. Java está instalado: `java -version`
2. Maven está instalado: `mvn -version`
3. PostgreSQL está corriendo
4. Base de datos está creada: `inventario_pos`
