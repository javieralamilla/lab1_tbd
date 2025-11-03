# 🏙️ Backend - Plataforma de Urbanismo y Planificación

API RESTful desarrollada con Spring Boot para la gestión de proyectos urbanos, zonas, puntos de interés y análisis espacial.

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Tecnologías y Dependencias](#tecnologías-y-dependencias)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Configuración de Base de Datos](#configuración-de-base-de-datos)
- [Ejecución del Proyecto](#ejecución-del-proyecto)
- [Endpoints de la API](#endpoints-de-la-api)
- [Autenticación y Seguridad](#autenticación-y-seguridad)
- [Modelos de Datos](#modelos-de-datos)
- [Testing](#testing)
- [Solución de Problemas](#solución-de-problemas)
- [Contribución](#contribución)

---

## 🎯 Descripción General

Esta API proporciona servicios backend para una plataforma de urbanismo que permite:

- **Gestión de Usuarios**: Registro, autenticación y autorización con JWT
- **Proyectos Urbanos**: CRUD completo de proyectos con información geoespacial
- **Puntos de Interés**: Gestión de POIs con coordenadas geográficas
- **Zonas Urbanas**: Administración de polígonos y áreas con geometrías complejas
- **Reportes y Estadísticas**: Generación de reportes con filtros avanzados
- **Análisis Espacial**: Consultas espaciales usando PostGIS
- **Simulación de Proyectos**: Sistema para simular nuevos proyectos urbanos

### Características Principales

- **API RESTful**: Arquitectura REST completa con mejores prácticas
- **Seguridad JWT**: Autenticación basada en tokens
- **Base de Datos Geoespacial**: PostgreSQL con extensión PostGIS
- **CORS Configurado**: Listo para trabajar con frontend
- **Validación de Datos**: DTOs y validación de entrada
- **Logging**: Sistema de logs configurado

---

## 🛠️ Tecnologías y Dependencias

### Framework Principal

- **Spring Boot 3.5.7**
  - spring-boot-starter-web
  - spring-boot-starter-data-jdbc
  - spring-boot-starter-security

### Base de Datos

- **PostgreSQL** (con extensión PostGIS)
- **Driver JDBC**: org.postgresql:postgresql

### Seguridad

- **JSON Web Tokens (JWT)**
  - io.jsonwebtoken:jjwt-api (0.11.5)
  - io.jsonwebtoken:jjwt-impl (0.11.5)
  - io.jsonwebtoken:jjwt-jackson (0.11.5)
- **Spring Security**

### Utilidades

- **Lombok**: Reducción de código boilerplate
- **HikariCP**: Connection pooling

### Testing

- spring-boot-starter-test
- spring-security-test
- JUnit 5
- Mockito

### Build Tool

- **Apache Maven 3.x**
- Java 17

---

## 🏗️ Arquitectura del Proyecto

```
backend/lab1_tbd/src/main/java/cl/usach/tbd/grupo4/plataforma_urbanismo/
│
├── PlataformaUrbanismoApplication.java    # Clase principal
│
├── config/                                 # Configuraciones
│   ├── SecurityConfig.java                # Configuración de seguridad
│   ├── JwtAuthenticationFilter.java       # Filtro JWT
│   └── DatabaseInitializer.java           # Inicialización de BD
│
├── controller/                            # Controladores REST
│   ├── AuthController.java               # Autenticación
│   ├── ProyectoController.java           # Proyectos urbanos
│   ├── PuntoInteresController.java       # Puntos de interés
│   ├── ZonaUrbanaController.java         # Zonas urbanas
│   └── ReporteController.java            # Reportes
│
├── service/                               # Lógica de negocio
│   ├── AuthService.java                  # Servicio de autenticación
│   ├── JwtService.java                   # Manejo de JWT
│   ├── ProyectoService.java              # Lógica de proyectos
│   ├── PuntoInteresService.java          # Lógica de POIs
│   ├── ZonaUrbanaService.java            # Lógica de zonas
│   └── ReporteService.java               # Generación de reportes
│
├── repository/                            # Acceso a datos
│   ├── UsuarioRepository.java
│   ├── ProyectoRepository.java
│   ├── PuntoInteresRepository.java
│   └── ZonaUrbanaRepository.java
│
├── model/                                 # Entidades del dominio
│   ├── Usuario.java
│   ├── ProyectoUrbano.java
│   ├── PuntoInteres.java
│   ├── ZonaUrbana.java
│   └── DatoDemografico.java
│
└── dto/                                   # Data Transfer Objects
    ├── LoginRequest.java
    ├── AuthResponse.java
    ├── ReporteDTO.java
    └── EstadisticasDTO.java
```

### Patrones de Diseño Implementados

1. **MVC (Model-View-Controller)**: Separación de responsabilidades
2. **Repository Pattern**: Abstracción del acceso a datos
3. **Service Layer Pattern**: Lógica de negocio centralizada
4. **DTO Pattern**: Transferencia de datos controlada
5. **Dependency Injection**: Inversión de control con Spring

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

1. **Java Development Kit (JDK) 17 o superior**
   ```bash
   java -version
   ```

2. **Apache Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **PostgreSQL 12+ con PostGIS 3.x**
   ```bash
   psql --version
   ```

4. **Git** (para clonar el repositorio)
   ```bash
   git --version
   ```

### Variables de Entorno Recomendadas

```bash
JAVA_HOME=C:\Program Files\Java\jdk-17
MAVEN_HOME=C:\Program Files\Apache\maven
PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%
```

---

## ⚙️ Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd TBD/backend/lab1_tbd
```

### 2. Configurar `application.properties`

Edita el archivo `src/main/resources/application.properties`:

```properties
# Nombre de la aplicación
spring.application.name=plataforma-urbanismo

# ========== CONFIGURACIÓN DE BASE DE DATOS ==========
spring.datasource.url=jdbc:postgresql://localhost:5432/urbanismo_db
spring.datasource.username=postgres
spring.datasource.password=TU_PASSWORD_AQUI
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000

# ========== JPA/HIBERNATE ==========
spring.jpa.hibernate.ddl-auto=none
spring.sql.init.mode=never

# ========== JWT CONFIGURACIÓN ==========
jwt.secret=MiClaveSecretaSuperSeguraParaJWTDebeSerLargaYCompleja2024!
jwt.expiration=86400000

# ========== LOGGING ==========
logging.level.cl.usach.tbd.grupo4=INFO
logging.level.org.springframework.security=DEBUG
```

### 3. Instalar Dependencias

```bash
mvn clean install
```

---

## 🗄️ Configuración de Base de Datos

### 1. Crear Base de Datos PostgreSQL

```sql
-- Conectar a PostgreSQL
psql -U postgres

-- Crear base de datos
CREATE DATABASE urbanismo_db;

-- Conectar a la base de datos
\c urbanismo_db

-- Habilitar extensión PostGIS
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 2. Crear Tablas

```sql
-- Tabla de Usuarios
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    rol VARCHAR(50) DEFAULT 'USER',
    activo BOOLEAN DEFAULT true,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Proyectos Urbanos
CREATE TABLE proyectos_urbanos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    tipo VARCHAR(100),
    estado VARCHAR(50) DEFAULT 'Planificado',
    presupuesto DECIMAL(15,2),
    fecha_inicio DATE,
    fecha_fin DATE,
    responsable_id INTEGER REFERENCES usuarios(id),
    ubicacion GEOMETRY(Point, 4326),
    area_influencia GEOMETRY(Polygon, 4326),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Puntos de Interés
CREATE TABLE puntos_interes (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(100),
    descripcion TEXT,
    coordenadas GEOMETRY(Point, 4326) NOT NULL,
    direccion VARCHAR(300),
    calificacion DECIMAL(3,2),
    horario VARCHAR(200),
    contacto VARCHAR(100),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Zonas Urbanas
CREATE TABLE zonas_urbanas (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(200) NOT NULL,
    tipo VARCHAR(100),
    descripcion TEXT,
    poligono GEOMETRY(Polygon, 4326) NOT NULL,
    area_m2 DECIMAL(15,2),
    poblacion INTEGER,
    densidad_poblacional DECIMAL(10,2),
    uso_suelo VARCHAR(100),
    zonificacion VARCHAR(100),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de Datos Demográficos
CREATE TABLE datos_demograficos (
    id SERIAL PRIMARY KEY,
    zona_id INTEGER REFERENCES zonas_urbanas(id) ON DELETE CASCADE,
    poblacion_total INTEGER,
    poblacion_hombres INTEGER,
    poblacion_mujeres INTEGER,
    edad_promedio DECIMAL(5,2),
    densidad_habitacional DECIMAL(10,2),
    hogares INTEGER,
    ingreso_promedio DECIMAL(12,2),
    nivel_educacion VARCHAR(100),
    fecha_censo DATE,
    fuente VARCHAR(200)
);

-- Índices espaciales para mejor rendimiento
CREATE INDEX idx_proyectos_ubicacion ON proyectos_urbanos USING GIST(ubicacion);
CREATE INDEX idx_proyectos_area ON proyectos_urbanos USING GIST(area_influencia);
CREATE INDEX idx_puntos_coordenadas ON puntos_interes USING GIST(coordenadas);
CREATE INDEX idx_zonas_poligono ON zonas_urbanas USING GIST(poligono);

-- Índices adicionales
CREATE INDEX idx_usuarios_username ON usuarios(username);
CREATE INDEX idx_proyectos_estado ON proyectos_urbanos(estado);
CREATE INDEX idx_puntos_tipo ON puntos_interes(tipo);
CREATE INDEX idx_zonas_tipo ON zonas_urbanas(tipo);
```

### 3. Datos de Ejemplo (Opcional)

```sql
-- Insertar usuario administrador (password: admin123)
INSERT INTO usuarios (username, password, email, rol) 
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'admin@urbanismo.cl', 'ADMIN');

-- Insertar usuario regular (password: user123)
INSERT INTO usuarios (username, password, email, rol) 
VALUES ('usuario', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'user@urbanismo.cl', 'USER');
```

---

## 🚀 Ejecución del Proyecto

### Modo Desarrollo

#### Opción 1: Maven

```bash
mvn spring-boot:run
```

#### Opción 2: Maven Wrapper (si está disponible)

```bash
# En Windows
.\mvnw.cmd spring-boot:run

# En Linux/Mac
./mvnw spring-boot:run
```

#### Opción 3: IDE (IntelliJ IDEA / Eclipse)

1. Importar el proyecto como proyecto Maven
2. Esperar a que se resuelvan las dependencias
3. Ejecutar `PlataformaUrbanismoApplication.java`

### Compilar y Generar JAR

```bash
mvn clean package
```

El archivo JAR se generará en: `target/plataforma-urbanismo-0.0.1-SNAPSHOT.jar`

### Ejecutar JAR

```bash
java -jar target/plataforma-urbanismo-0.0.1-SNAPSHOT.jar
```

### Verificar que está Funcionando

Una vez iniciada la aplicación, deberías ver:

```
Started PlataformaUrbanismoApplication in X.XXX seconds
```

La API estará disponible en: `http://localhost:8080`

---

## 🌐 Endpoints de la API

###  Autenticación

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin",
  "rol": "ADMIN"
}
```

#### Registro
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "nuevoUsuario",
  "password": "password123",
  "email": "nuevo@email.com"
}
```

###  Proyectos Urbanos

#### Listar todos los proyectos
```http
GET /api/proyectos
Authorization: Bearer {token}

Response: Array<ProyectoUrbano>
```

#### Obtener proyecto por ID
```http
GET /api/proyectos/{id}
Authorization: Bearer {token}
```

#### Crear proyecto
```http
POST /api/proyectos
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Parque Central",
  "descripcion": "Nuevo espacio verde",
  "tipo": "Área Verde",
  "estado": "Planificado",
  "presupuesto": 500000.00,
  "fechaInicio": "2025-01-15",
  "fechaFin": "2025-12-31",
  "latitud": -33.4489,
  "longitud": -70.6693
}
```

#### Actualizar proyecto
```http
PUT /api/proyectos/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

#### Eliminar proyecto
```http
DELETE /api/proyectos/{id}
Authorization: Bearer {token}
```

#### Proyectos por estado
```http
GET /api/proyectos/estado/{estado}
Authorization: Bearer {token}
```

###  Puntos de Interés

#### Listar todos los puntos
```http
GET /api/puntos
Authorization: Bearer {token}
```

#### Crear punto de interés
```http
POST /api/puntos
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Plaza de Armas",
  "tipo": "Plaza",
  "descripcion": "Plaza principal",
  "latitud": -33.4372,
  "longitud": -70.6506,
  "direccion": "Plaza de Armas S/N",
  "calificacion": 4.5
}
```

#### Puntos por tipo
```http
GET /api/puntos/tipo/{tipo}
Authorization: Bearer {token}
```

#### Puntos cercanos (radio en metros)
```http
GET /api/puntos/cercanos?lat=-33.4489&lng=-70.6693&radio=1000
Authorization: Bearer {token}
```

### Zonas Urbanas

#### Listar todas las zonas
```http
GET /api/zonas
Authorization: Bearer {token}
```

#### Crear zona
```http
POST /api/zonas
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "Zona Residencial Norte",
  "tipo": "Residencial",
  "descripcion": "Área residencial",
  "coordenadas": [
    [-33.4, -70.6],
    [-33.4, -70.5],
    [-33.3, -70.5],
    [-33.3, -70.6],
    [-33.4, -70.6]
  ],
  "poblacion": 5000,
  "usoSuelo": "Residencial"
}
```

#### Zonas que contienen un punto
```http
GET /api/zonas/contiene?lat=-33.4489&lng=-70.6693
Authorization: Bearer {token}
```

### Reportes y Estadísticas

#### Generar reporte
```http
GET /api/reportes?fechaInicio=2025-01-01&fechaFin=2025-12-31&tipo=proyectos
Authorization: Bearer {token}
```

#### Estadísticas generales
```http
GET /api/reportes/estadisticas
Authorization: Bearer {token}

Response:
{
  "totalProyectos": 150,
  "totalPuntos": 320,
  "totalZonas": 45,
  "proyectosActivos": 78,
  "proyectosCompletados": 42,
  "presupuestoTotal": 15000000.00
}
```

#### Análisis espacial
```http
GET /api/reportes/analisis-espacial
Authorization: Bearer {token}
```

---

##  Autenticación y Seguridad

### Sistema de Autenticación JWT

1. **Login**: El usuario envía credenciales y recibe un token JWT
2. **Token**: Se incluye en el header `Authorization: Bearer {token}`
3. **Validación**: Cada request valida el token
4. **Expiración**: Token válido por 24 horas (configurable)

### Configuración de Seguridad

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    // CORS habilitado para http://localhost:5173
    // Endpoints públicos: /api/auth/**
    // Endpoints protegidos: /api/** (requieren autenticación)
}
```

### Roles de Usuario

- **USER**: Usuario estándar (puede ver y crear)
- **ADMIN**: Administrador (todos los permisos)

### Password Encoding

- Utiliza **BCrypt** para encriptar contraseñas
- Nunca se almacenan contraseñas en texto plano

---

##  Modelos de Datos

### Usuario
```java
{
  id: Long
  username: String
  password: String (encrypted)
  email: String
  rol: String
  activo: Boolean
  fechaRegistro: LocalDateTime
}
```

### ProyectoUrbano
```java
{
  id: Long
  nombre: String
  descripcion: String
  tipo: String
  estado: String
  presupuesto: BigDecimal
  fechaInicio: LocalDate
  fechaFin: LocalDate
  responsableId: Long
  latitud: Double
  longitud: Double
  areaInfluencia: String (WKT)
  fechaCreacion: LocalDateTime
}
```

### PuntoInteres
```java
{
  id: Long
  nombre: String
  tipo: String
  descripcion: String
  latitud: Double
  longitud: Double
  direccion: String
  calificacion: Double
  horario: String
  contacto: String
}
```

### ZonaUrbana
```java
{
  id: Long
  nombre: String
  tipo: String
  descripcion: String
  coordenadas: List<List<Double>>
  areaM2: Double
  poblacion: Integer
  densidadPoblacional: Double
  usoSuelo: String
  zonificacion: String
}
