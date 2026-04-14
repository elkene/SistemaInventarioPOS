# Sistema de Inventario y Punto de Ventas (POS)

## Descripción
Sistema completo de gestión de inventario y punto de ventas para pequeñas y medianas empresas (PYMEs), desarrollado en Java con JavaFX como interfaz gráfica y PostgreSQL como base de datos.

## Características Principales

### Módulos Implementados
- **Autenticación Segura**: Login con hashing BCrypt y gestión de sesiones
- **Gestión de Inventario**: CRUD de productos, categorías, ajuste de stock e historial de movimientos
- **Sistema de Punto de Ventas**: Registro de ventas con carrito de compras, búsqueda de productos y múltiples métodos de pago
- **Gestión de Clientes**: Registro, seguimiento y crédito de clientes
- **Gestión de Proveedores**: Registro y contacto de proveedores
- **Reportes**: Análisis de ventas, inventario, movimientos y ganancias
- **Control de Sesiones**: Manejo automático de sesiones con timeout

## Tecnologías Utilizadas

### Backend
- **Java 11+**: Lenguaje de programación
- **Maven 3.9+**: Gestor de dependencias
- **PostgreSQL 14+**: Base de datos relacional
- **HikariCP 5.1.0**: Connection pooling
- **Spring Security Crypto**: Hashing de contraseñas
- **SLF4J / Log4j2**: Logging

### Frontend
- **JavaFX 21.0.2**: Framework de interfaz gráfica
- **FXML**: XML para definir interfaces
- **CSS**: Estilos profesionales

## Estructura del Proyecto

```
SistemaInventarioPOS/
├── src/
│   ├── main/
│   │   ├── java/com/inventario/
│   │   │   ├── controllers/      # Controladores JavaFX (12)
│   │   │   ├── models/           # Clases de modelo (10)
│   │   │   ├── services/         # Servicios de negocio (7)
│   │   │   ├── dao/              # Data Access Objects (16)
│   │   │   ├── database/         # Configuración de BD
│   │   │   ├── utils/            # Utilidades
│   │   │   ├── exceptions/       # Excepciones personalizadas
│   │   │   ├── config/           # Configuración
│   │   │   └── Main.java         # Punto de entrada
│   │   └── resources/
│   │       ├── fxml/             # Archivos FXML (18)
│   │       ├── css/              # Estilos CSS
│   │       ├── schema.sql        # Script de BD
│   │       ├── data_inicial.sql  # Datos de prueba
│   │       ├── application.properties
│   │       ├── database.properties
│   │       └── log4j2.xml
│   └── test/                     # Tests unitarios
├── pom.xml                       # Configuración Maven
├── README.md                     # Este archivo
├── .gitignore
└── LICENSE
```

## Requisitos Previos

1. **Java Development Kit (JDK) 11 o superior**
   - Descargar desde [oracle.com](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html)
   - Configurar variable de entorno JAVA_HOME

2. **Maven 3.9 o superior**
   - Descargar desde [maven.apache.org](https://maven.apache.org/download.cgi)
   - Configurar variable de entorno MAVEN_HOME

3. **PostgreSQL 14 o superior**
   - Descargar desde [postgresql.org](https://www.postgresql.org/download/)
   - Crear base de datos vacía llamada `inventario_pos`

4. **IDE Recomendado**
   - IntelliJ IDEA Community Edition
   - Eclipse IDE
   - VS Code con extensiones Java

## Instalación y Configuración

### 1. Clonar/Descargar el Proyecto
```bash
cd C:\Users\Kenne
# El proyecto ya está en: SistemaInventarioPOS
```

### 2. Configurar Base de Datos
```bash
# Conectarse a PostgreSQL
psql -U postgres

# Crear base de datos
CREATE DATABASE inventario_pos;

# Usar la base de datos
\c inventario_pos

# Ejecutar script de schema
\i 'C:/Users/Kenne/Idea/SistemaInventarioPOS/src/main/resources/schema.sql'

# Ejecutar script de datos iniciales
\i 'C:/Users/Kenne/Idea/SistemaInventarioPOS/src/main/resources/data_inicial.sql'
```

### 3. Configurar Propiedades
Editar `src/main/resources/database.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/inventario_pos
db.username=postgres
db.password=tu_contraseña
```

### 4. Compilar el Proyecto
```bash
mvn clean compile
```

### 5. Ejecutar la Aplicación
```bash
mvn clean javafx:run
```

## Credenciales de Prueba

**Usuario**: `admin`
**Contraseña**: `admin123`

*(Nota: Las contraseñas de prueba deben ser hasheadas con BCrypt)*

## Uso de la Aplicación

### Pantalla de Login
1. Ingresar nombre de usuario
2. Ingresar contraseña
3. Hacer clic en "ENTRAR"

### Menú Principal
- **Inventario**: Gestionar productos, categorías y ajustes de stock
- **Punto de Ventas**: Registrar ventas con carrito de compras
- **Clientes**: Gestionar clientes con información de crédito
- **Proveedores**: Gestionar proveedores
- **Reportes**: Ver reportes de ventas, inventario, movimientos y ganancias

### Módulo de Inventario
- Agregar, editar y eliminar productos
- Gestionar categorías
- Ajustar stock (entradas/salidas)
- Ver historial de movimientos
- Buscar y filtrar productos

### Módulo de Ventas
- Agregar productos al carrito
- Buscar productos por código o nombre
- Aplicar descuentos
- Seleccionar cliente
- Elegir método de pago
- Completar venta

### Módulo de Clientes
- Agregar, editar y eliminar clientes
- Buscar clientes por nombre
- Registrar información de crédito

### Módulo de Proveedores
- Agregar, editar y eliminar proveedores
- Buscar proveedores por nombre
- Registrar información de contacto

### Módulo de Reportes
- **Reporte de Ventas**: Filtrar por rango de fechas, ver estadísticas
- **Reporte de Inventario**: Estado actual, stock bajo, valor total
- **Reporte de Movimientos**: Entradas, salidas y ajustes
- **Reporte de Ganancias**: Costos, ganancias y porcentaje

## Arquitectura

### Patrón MVC
- **Models**: Clases que representan entidades del negocio
- **Views**: Archivos FXML para la interfaz gráfica
- **Controllers**: Lógica de presentación y manejo de eventos

### Patrón DAO
- Abstracción de acceso a datos
- Separación entre lógica de negocio y persistencia
- Interfaces genéricas y específicas

### Patrón Service
- Lógica de negocio centralizada
- Validaciones de datos
- Manejo de transacciones

## Seguridad

### Autenticación
- Hashing de contraseñas con BCrypt (work factor: 10)
- Mensajes de error genéricos (previene enumeración de usuarios)
- Rate limiting (máximo 5 intentos fallidos)

### Sesiones
- Session ID de 128 bits generado con SecureRandom
- Timeout por inactividad: 30 minutos
- Timeout absoluto: 8 horas
- Renovación de sesión post-autenticación

### Base de Datos
- Prepared Statements para prevenir SQL Injection
- Transacciones ACID
- Índices en columnas frecuentes
- Auditoría de cambios

## Logging

Los logs se guardan en `logs/application.log` con la siguiente configuración:
- **INFO**: Eventos importantes
- **WARN**: Advertencias
- **ERROR**: Errores de aplicación
- **DEBUG**: Información de debugging (deshabilitado en producción)

## Testing

Para ejecutar tests unitarios:
```bash
mvn test
```

## Roadmap

- [x] Módulo de Inventario completo
- [x] Módulo de Ventas completo
- [x] Módulo de Clientes completo
- [x] Módulo de Proveedores completo
- [x] Módulo de Reportes completo
- [ ] Exportación de reportes a PDF
- [ ] Exportación de datos a Excel
- [ ] Sistema de códigos de barras
- [ ] Integración con sistemas de pago
- [ ] Aplicación móvil
- [ ] API REST
- [ ] Sincronización en la nube
- [ ] Multi-sucursales

## Contribuciones

Las contribuciones son bienvenidas. Por favor:
1. Fork el proyecto
2. Crear una rama para tu feature (`git checkout -b feature/AmazingFeature`)
3. Commit tus cambios (`git commit -m 'Add some AmazingFeature'`)
4. Push a la rama (`git push origin feature/AmazingFeature`)
5. Abrir un Pull Request

## Licencia

Este proyecto está bajo licencia MIT. Ver archivo LICENSE para más detalles.

## Soporte

Para reportar bugs o solicitar features, abrir un issue en el repositorio.

## Autores

Sistema desarrollado como proyecto de gestión para PYMEs.

---

**Versión**: 1.0.0  
**Última actualización**: Abril 2026
