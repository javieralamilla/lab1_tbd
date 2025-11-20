# 📋 PLAN DE TRABAJO - PLATAFORMA DE URBANISMO

**Fecha de última actualización:** 20 de Noviembre, 2025  
**Estado General:** 80% Completado

---

## 📂 ÍNDICE
1. [Base de Datos (PostgreSQL)](#1-base-de-datos-postgresql)
2. [Backend (Spring Boot + Java)](#2-backend-spring-boot--java)
3. [Frontend (Vue.js 3)](#3-frontend-vuejs-3)
4. [Resumen de Progreso](#-resumen-de-progreso)
5. [Próximos Pasos](#-próximos-pasos-prioritarios)

---

## 1. BASE DE DATOS (PostgreSQL)

### 📊 Diseño y Creación del Esquema

| Tarea | Estado | Detalles |
|-------|--------|----------|
| Diseñar Modelo Entidad-Relación | ✅ COMPLETO | Relaciones entre usuarios, zonas, puntos, proyectos, demografía |
| Crear Tabla `usuarios` | ✅ COMPLETO | Email único, contraseña BCrypt, roles (planificador, administrador, autoridad_municipal) |
| Crear Tabla `zonas_urbanas` | ✅ COMPLETO | GEOMETRY(POLYGON, 4326), tipos validados, área en km² |
| Crear Tabla `puntos_interes` | ✅ COMPLETO | GEOMETRY(POINT, 4326), 6 tipos: Hospital, Escuela, Parque, Centro Comercial, Transporte, Centro Cultural |
| Crear Tabla `datos_demograficos` | ✅ COMPLETO | Población, densidad, edad promedio, viviendas, factor personas/vivienda |
| Crear Tabla `proyectos_urbanos` | ✅ COMPLETO | Estado (5 valores), fechas, geometría GEOMETRY, presupuesto |
| Crear Tabla `proyectos_zonas` | ✅ COMPLETO | Relación N:M entre proyectos y zonas, área de superposición |

### ⚙️ Objetos de Base de Datos (Lógica de Negocio)

| Objeto | Estado | Descripción |
|--------|--------|-------------|
| **TRIGGERS** | | |
| `trigger_validar_proyecto` | ✅ COMPLETO | Valida que fecha_termino > fecha_inicio |
| `trigger_actualizar_densidad` | ✅ COMPLETO | Calcula densidad automáticamente al insertar/actualizar |
| **PROCEDIMIENTOS** | | |
| `simular_crecimiento_poblacion` | ✅ COMPLETO | Parámetros: (zona_id, nuevas_viviendas), Lógica: +3 personas/vivienda |
| `actualizar_proyectos_retrasados` | ✅ COMPLETO | Cambia estado a 'Retrasado' si fecha_termino < CURRENT_DATE |
| **VISTAS MATERIALIZADAS** | | |
| `cobertura_infraestructura` | ✅ COMPLETO | Conteo de parques, escuelas, hospitales por zona. Actualización: Semanal |
| `resumen_proyectos_estado_zona` | ✅ COMPLETO | Resumen por tipo_zona y estado, incluye presupuesto total |
| **ÍNDICES** | | |
| Índices Espaciales GIST | ✅ COMPLETO | 3 índices: zonas_geometria, puntos_coordenadas, proyectos_geometria |
| Índices B-tree | ✅ COMPLETO | 8 índices en campos frecuentes: email, tipo, zona_id, anio, estado, usuario_id, fecha |

### 📝 Consultas SQL Específicas (10 Requeridas)

| # | Consulta | Estado | Notas |
|---|----------|--------|-------|
| 1 | Calculadora de densidad de población por zona (ordenada mayor a menor) | ⚠️ PENDIENTE | SQL nativo requerido |
| 2 | Identificar zonas con mucha población y pocos hospitales (Top 5) | ⚠️ PENDIENTE | Cálculo de ratio población/hospitales |
| 3 | Análisis de proximidad: Escuelas a < 500m de proyectos "En Curso" | ⚠️ PENDIENTE | Usar ST_DWithin() de PostGIS |
| 4 | Detección de zonas con crecimiento > 10% en 5 años | ⚠️ PENDIENTE | Comparar datos_demograficos entre años |
| 5 | Vista Materializada: Conteo infraestructura por zona | ✅ COMPLETO | `cobertura_infraestructura` |
| 6 | Procedimiento: simular_crecimiento_poblacion | ✅ COMPLETO | INPUT: zona_id, nro_viviendas |
| 7 | Procedimiento: Actualizar proyectos retrasados | ✅ COMPLETO | Estado → 'Retrasado' |
| 8 | Listar zonas sin proyectos en los últimos 2 años | ⚠️ PENDIENTE | LEFT JOIN con filtro temporal |
| 9 | Detectar superposición geográfica de proyectos (área en m²) | ⚠️ PENDIENTE | ST_Intersection() + ST_Area() |
| 10 | Vista Materializada: Resumen proyectos por estado y tipo zona | ✅ COMPLETO | `resumen_proyectos_estado_zona` |

---

## 2. BACKEND (Spring Boot + Java)

### 🔐 Configuración y Seguridad

| Componente | Estado | Implementación |
|------------|--------|----------------|
| Configurar Spring Boot | ✅ COMPLETO | Estructura Maven con Java 17 |
| Implementar JWT | ✅ COMPLETO | `JwtService.java` - Genera/valida tokens con clave secreta |
| Endpoint de Login | ✅ COMPLETO | `POST /api/auth/login` - Retorna token JWT |
| Middleware de Seguridad | ✅ COMPLETO | `JwtAuthenticationFilter` + `SecurityConfig` - Protege rutas |
| Encriptación de Contraseñas | ✅ COMPLETO | BCrypt para hash de contraseñas |

### 🔌 Desarrollo de Endpoints (API)

| Módulo | Estado | Endpoints Implementados |
|--------|--------|------------------------|
| **Conexión JDBC** | ✅ COMPLETO | JdbcTemplate, sin JPA/Hibernate |
| **Usuarios** | ✅ COMPLETO | `UsuarioRepository`, `AuthController` |
| - Registro | ✅ | `POST /api/auth/register` |
| - Login | ✅ | `POST /api/auth/login` |
| **Zonas Urbanas** | ✅ COMPLETO | `ZonaUrbanaController`, `ZonaUrbanaRepository` |
| - Listar zonas | ✅ | `GET /api/zonas` - Incluye geometría GeoJSON y datos demográficos |
| - Obtener por ID | ✅ | `GET /api/zonas/{id}` |
| - Crear zona | ✅ | `POST /api/zonas` |
| - Actualizar zona | ✅ | `PUT /api/zonas/{id}` |
| - Eliminar zona | ✅ | `DELETE /api/zonas/{id}` |
| **Puntos de Interés** | ✅ COMPLETO | `PuntoInteresController`, `PuntoInteresRepository` |
| - Listar puntos | ✅ | `GET /api/puntos-interes` |
| - Filtrar por tipo | ✅ | `GET /api/puntos-interes?tipo=Hospital` |
| - Obtener por ID | ✅ | `GET /api/puntos-interes/{id}` |
| - Crear punto | ✅ | `POST /api/puntos-interes` |
| - Actualizar punto | ✅ | `PUT /api/puntos-interes/{id}` |
| - Eliminar punto | ✅ | `DELETE /api/puntos-interes/{id}` |
| **Proyectos Urbanos** | ✅ COMPLETO | `ProyectoController`, `ProyectoRepository` |
| - Listar proyectos | ✅ | `GET /api/proyectos` |
| - Filtrar por estado | ✅ | `GET /api/proyectos?estado=En Curso` |
| - Obtener por ID | ✅ | `GET /api/proyectos/{id}` |
| - Crear proyecto | ✅ | `POST /api/proyectos` |
| - Actualizar proyecto | ✅ | `PUT /api/proyectos/{id}` |
| - Eliminar proyecto | ✅ | `DELETE /api/proyectos/{id}` |
| **Reportes y Análisis** | ⚠️ PARCIAL | `ReporteController`, `ReporteService` |
| - Estructura base | ✅ | Controllers y Services creados |
| - 6 consultas SQL | ⚠️ | **PENDIENTE:** Implementar en ReporteRepository |

### 📦 Modelos y DTOs

| Tipo | Estado | Archivos |
|------|--------|----------|
| Entities | ✅ COMPLETO | Usuario, ZonaUrbana, PuntoInteres, ProyectoUrbano, DatoDemografico |
| DTOs | ✅ COMPLETO | AuthResponse, LoginRequest, EstadisticasDTO, ReporteDTO |
| Anotaciones | ✅ COMPLETO | @JsonProperty para mapeo correcto de campos (ej: geometria_geojson) |

### 💾 Datos Iniciales

| Dataset | Estado | Cantidad |
|---------|--------|----------|
| Usuarios | ✅ COMPLETO | 4 usuarios con contraseñas BCrypt |
| Zonas Urbanas (RM) | ✅ COMPLETO | **52 comunas** con límites geográficos reales (POLYGON) |
| Puntos de Interés | ✅ COMPLETO | **560+ puntos** georeferenciados (coordenadas corregidas PostGIS) |
| Datos Demográficos | ✅ COMPLETO | Población, densidad, viviendas por comuna (año 2025) |
| Proyectos Urbanos | ✅ COMPLETO | Proyectos de ejemplo con diferentes estados |

---

## 3. FRONTEND (Vue.js 3)

### 🎨 Estructura y Autenticación

| Componente | Estado | Detalles |
|------------|--------|----------|
| Configurar Vue.js 3 | ✅ COMPLETO | Proyecto Vite con TypeScript |
| Configurar Axios | ✅ COMPLETO | `api.js` con interceptores para JWT |
| Store de Autenticación | ✅ COMPLETO | Pinia store (`auth.js`) |
| Página de Login | ✅ COMPLETO | `LoginView.vue` - Almacena token en localStorage |
| AuthService | ✅ COMPLETO | `authService.js` - login/logout/verificación |
| Router Guards | ✅ COMPLETO | Protección de rutas autenticadas |

### 🗺️ Visualización y Mapa

| Componente | Estado | Funcionalidad |
|------------|--------|---------------|
| **Diseño de UI** | ✅ COMPLETO | AppLayout con Sidebar, tema consistente |
| **Integración Leaflet** | ✅ COMPLETO | Leaflet.js v1.9+ con OpenStreetMap |
| **Componentes de Mapa** | | |
| `MapaZonas.vue` | ✅ COMPLETO | Visualiza 52 comunas RM con POLYGON, colores por tipo (Residencial, Comercial, Industrial, Mixto) |
| `MapaPuntos.vue` | ✅ COMPLETO | 560+ puntos con iconos SVG personalizados por tipo |
| `MapaProyectos.vue` | ✅ COMPLETO | Proyectos urbanos con geometrías y estados |
| `MapaIntegrado.vue` | ✅ COMPLETO | Todas las capas combinadas con filtros interactivos |
| `MapaAnalisisEspacial.vue` | ⚠️ PARCIAL | Componente base creado |
| `MapaSimulacionProyectos.vue` | ⚠️ PARCIAL | Componente base creado |
| **Controles de Capas** | ✅ COMPLETO | Activar/desactivar capas dinámicamente |
| **Popups Interactivos** | ✅ COMPLETO | Información detallada al hacer clic |
| **Filtros por Tipo** | ✅ COMPLETO | Filtrar zonas y puntos por categoría |

### 📄 Vistas Implementadas

| Vista | Ruta | Estado | Descripción |
|-------|------|--------|-------------|
| LoginView | `/login` | ✅ COMPLETO | Autenticación JWT |
| DashboardView | `/` | ✅ COMPLETO | Panel principal con estadísticas |
| ZonasView | `/zonas` | ✅ COMPLETO | Gestión de zonas urbanas + mapa |
| PuntosInteresView | `/puntos-interes` | ✅ COMPLETO | CRUD de puntos + visualización |
| ProyectosView | `/proyectos` | ✅ COMPLETO | Gestión de proyectos urbanos |
| MapaIntegradoView | `/mapa-integrado` | ✅ COMPLETO | Mapa con todas las capas |
| ReportesView | `/reportes` | ✅ COMPLETO | Visualización de reportes y estadísticas |
| AnalisisEspacialView | `/analisis-espacial` | ⚠️ PARCIAL | **PENDIENTE:** Conectar con endpoints backend |
| SimulacionProyectosView | `/simulacion` | ⚠️ PARCIAL | **PENDIENTE:** Conectar con procedimiento almacenado |

### 🎯 Funcionalidades de Análisis

| Funcionalidad | Estado | Implementación |
|---------------|--------|----------------|
| Selección de Área | ⚠️ PARCIAL | UI preparada, falta lógica backend |
| Estadísticas Resumidas | ⚠️ PARCIAL | Diseño listo, falta integración API |
| Simulación de Crecimiento | ⚠️ PARCIAL | UI creada, falta llamar a `simular_crecimiento_poblacion()` |
| Dibujar Nuevas Zonas | ⚠️ PARCIAL | Componente preparado, falta persistencia |

---

## 📊 RESUMEN DE PROGRESO

### ✅ COMPLETADO (80%)

#### Base de Datos
- ✅ Esquema completo normalizado (6 tablas)
- ✅ 2 Triggers automáticos (validación + cálculo densidad)
- ✅ 2 Procedimientos almacenados (simulación + actualización estado)
- ✅ 2 Vistas materializadas (infraestructura + resumen proyectos)
- ✅ 11 Índices optimizados (3 espaciales GIST + 8 B-tree)
- ✅ 52 comunas RM con geometrías reales
- ✅ 560+ puntos de interés georeferenciados

#### Backend
- ✅ Autenticación JWT completa
- ✅ 4 módulos CRUD (Usuarios, Zonas, Puntos, Proyectos)
- ✅ Conexión JDBC nativa (sin JPA)
- ✅ Serialización correcta de GeoJSON con ST_AsGeoJSON
- ✅ Seguridad con Spring Security + BCrypt

#### Frontend
- ✅ UI completa con Vue 3 + Vite
- ✅ 8 vistas implementadas
- ✅ Integración Leaflet.js con 6 componentes de mapa
- ✅ Visualización de 52 comunas + 560 puntos
- ✅ Sistema de autenticación con token
- ✅ Filtros interactivos y popups

### ⚠️ EN PROGRESO (15%)

#### Consultas SQL Pendientes (6/10)
1. ⚠️ Densidad de población ordenada
2. ⚠️ Zonas con mucha población y pocos hospitales
3. ⚠️ Escuelas cerca de proyectos en curso (< 500m)
4. ⚠️ Zonas con crecimiento > 10% en 5 años
5. ⚠️ Zonas sin proyectos en 2 años
6. ⚠️ Superposición geográfica de proyectos

#### Integraciones Pendientes
- ⚠️ Conectar AnalisisEspacialView con backend
- ⚠️ Conectar SimulacionProyectosView con procedimiento almacenado
- ⚠️ Endpoints en ReporteController para las 6 consultas

### ❌ PENDIENTE (5%)

- ❌ Testing de integración completo
- ❌ Optimización de consultas geoespaciales complejas
- ❌ Documentación API (Swagger/OpenAPI)
- ❌ Validaciones de datos más robustas

---

## 🎯 PRÓXIMOS PASOS PRIORITARIOS

### 🔥 CRÍTICO (Completar antes de entrega)

1. **Implementar 6 Consultas SQL Faltantes**
   - Crear métodos en `ReporteRepository.java`
   - SQL nativo con JdbcTemplate
   - Archivos: `backend/...//repository/ReporteRepository.java`

2. **Crear Endpoints en ReporteController**
   ```java
   GET /api/reportes/densidad-poblacion
   GET /api/reportes/zonas-sin-hospitales
   GET /api/reportes/escuelas-proximidad?distancia=500
   GET /api/reportes/crecimiento-poblacional?anios=5
   GET /api/reportes/zonas-sin-proyectos?anios=2
   GET /api/reportes/superposicion-proyectos
   ```

3. **Integrar Frontend con Nuevos Endpoints**
   - Actualizar `reportesService.js`
   - Conectar `AnalisisEspacialView.vue`
   - Mostrar resultados en tablas y mapas

4. **Conectar Simulación de Crecimiento**
   - Endpoint: `POST /api/reportes/simular-crecimiento`
   - Body: `{ zona_id: 1, nuevas_viviendas: 100 }`
   - Actualizar `SimulacionProyectosView.vue`

### 📝 IMPORTANTE (Mejorar calidad)

5. **Testing**
   - Unit tests para servicios críticos
   - Integration tests para endpoints
   - Tests de consultas SQL con datos reales

6. **Documentación**
   - Swagger/OpenAPI para API REST
   - README.md con instrucciones de instalación
   - Comentarios en código complejo

7. **Optimización**
   - Revisar performance de consultas espaciales
   - Añadir paginación en listados grandes
   - Caché para vistas materializadas

---

## 📁 ESTRUCTURA DEL PROYECTO

```
lab1_tbd/
├── backend/
│   └── lab1_tbd/
│       ├── src/main/
│       │   ├── java/.../
│       │   │   ├── config/          ✅ SecurityConfig, JwtFilter, DatabaseInitializer
│       │   │   ├── controller/      ✅ Auth, Zona, Punto, Proyecto, Reporte (parcial)
│       │   │   ├── dto/             ✅ AuthResponse, LoginRequest, Estadisticas, Reporte
│       │   │   ├── model/           ✅ Usuario, ZonaUrbana, PuntoInteres, Proyecto, Demografico
│       │   │   ├── repository/      ✅ Usuario, Zona, Punto, Proyecto, Reporte (parcial)
│       │   │   └── service/         ✅ Auth, Jwt, Zona, Punto, Proyecto, Reporte (parcial)
│       │   └── resources/
│       │       ├── schema.sql       ✅ DDL completo
│       │       ├── datos.sql        ✅ 52 comunas + 560 puntos
│       │       └── application.properties ✅ Config DB + JWT
│       └── pom.xml                  ✅ Maven dependencies
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   └── common/              ✅ 6 mapas + Sidebar + Spinner + ErrorAlert
│   │   ├── layouts/                 ✅ AppLayout
│   │   ├── router/                  ✅ Router con guards
│   │   ├── services/                ✅ api, auth, zonas, puntos, proyectos, reportes
│   │   ├── stores/                  ✅ auth (Pinia)
│   │   ├── views/                   ✅ 8 vistas (2 parciales)
│   │   └── utils/                   ✅ constants
│   ├── package.json                 ✅ Dependencias (Vue 3, Leaflet, Axios)
│   └── vite.config.ts               ✅ Config Vite
│
├── datos_fuente/                    ⚠️ Shapefiles originales (considerar eliminar)
├── scripts/                         ✅ Scripts auxiliares
└── PLAN_TRABAJO.md                  ✅ Este archivo

```

---

## 🔧 COMANDOS ÚTILES

### Backend
```bash
# Iniciar backend (Spring Boot)
cd backend/lab1_tbd
.\mvnw.cmd spring-boot:run

# Puerto: 8080
# API Base: http://localhost:8080/api
```

### Frontend
```bash
# Iniciar frontend (Vite)
cd frontend
npm run dev

# Puerto: 5173
# URL: http://localhost:5173
```

### Base de Datos
```bash
# Conectar a PostgreSQL
psql -U postgres -d lab1_tbd

# Refrescar vistas materializadas
REFRESH MATERIALIZED VIEW CONCURRENTLY cobertura_infraestructura;
REFRESH MATERIALIZED VIEW CONCURRENTLY resumen_proyectos_estado_zona;
```

---

## 📞 CONTACTO Y RECURSOS

- **Repositorio:** javieralamilla/lab1_tbd
- **Branch:** main
- **Tecnologías:** PostgreSQL 14+ (PostGIS), Java 17, Spring Boot 3, Vue.js 3, Leaflet.js
- **Coordinador:** Javier Alamilla
- **Última actualización:** 20 de Noviembre, 2025

---

**Estado: EN DESARROLLO - 80% COMPLETO** 🚀
