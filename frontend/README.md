# 🌐 Frontend - Plataforma de Urbanismo y Planificación

Aplicación web moderna desarrollada con Vue 3, TypeScript y Vite para la gestión visual e interactiva de proyectos urbanos, zonas y análisis espacial.

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Tecnologías y Dependencias](#tecnologías-y-dependencias)
- [Características Principales](#características-principales)
- [Requisitos Previos](#requisitos-previos)
- [Instalación y Configuración](#instalación-y-configuración)
- [Ejecución del Proyecto](#ejecución-del-proyecto)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Vistas y Componentes](#vistas-y-componentes)
- [Sistema de Mapas](#sistema-de-mapas)
- [Servicios y API](#servicios-y-api)
- [Estado Global (Stores)](#estado-global-stores)
- [Rutas y Navegación](#rutas-y-navegación)
- [Estilos y Temas](#estilos-y-temas)
- [Build y Deployment](#build-y-deployment)
- [Testing](#testing)
- [Solución de Problemas](#solución-de-problemas)
- [Mejores Prácticas](#mejores-prácticas)

---

##  Descripción General

Plataforma web interactiva para la gestión de urbanismo y planificación territorial que permite:

- **Visualización de Mapas Interactivos**: Mapas con Leaflet para proyectos, puntos de interés y zonas
- **Dashboard Analítico**: Panel de control con estadísticas y métricas
- **Gestión de Proyectos**: CRUD completo con visualización geoespacial
- **Puntos de Interés**: Administración de POIs con marcadores en mapa
- **Zonas Urbanas**: Creación y edición de polígonos con dibujo interactivo
- **Reportes y Estadísticas**: Generación y exportación de reportes (PDF/Excel)
- **Análisis Espacial**: Consultas espaciales y visualización de resultados
- **Simulación de Proyectos**: Herramienta para simular nuevos desarrollos urbanos
- **Mapa Integrado**: Vista consolidada con capas superpuestas
- **Autenticación**: Sistema de login con JWT

### Características Técnicas

- **Vue 3 Composition API**: Última versión de Vue con script setup
- **TypeScript**: Tipado estático para mayor robustez
- **Vite**: Build tool ultrarrápido
- **Pinia**: State management moderno
- **Vue Router**: Navegación SPA
- **Axios**: Cliente HTTP
- **Leaflet**: Mapas interactivos
- **Responsive Design**: Adaptable a todos los dispositivos

---

## Tecnologías y Dependencias

### Framework y Core

- **Vue 3.5.22**: Framework progresivo de JavaScript
- **TypeScript 5.9.0**: Superset tipado de JavaScript
- **Vite 7.1.11**: Build tool y dev server

### State Management y Routing

- **Pinia 3.0.3**: Store de estado oficial de Vue
- **Vue Router 4.6.3**: Enrutamiento oficial de Vue
- **@vueuse/core 14.0.0**: Composables de utilidad

### Mapas y Visualización

- **Leaflet 1.9.4**: Biblioteca de mapas interactivos
- **Leaflet Draw 1.0.4**: Plugin para dibujar formas
- **Vue Leaflet 0.1.0**: Integración de Leaflet con Vue

### HTTP y Comunicación

- **Axios 1.13.1**: Cliente HTTP basado en promesas

### Reportes y Exportación

- **jsPDF 3.0.3**: Generación de documentos PDF
- **jsPDF-AutoTable 5.0.2**: Tablas para jsPDF
- **XLSX 0.18.5**: Exportación a Excel

### Herramientas de Desarrollo

- **ESLint 9.37.0**: Linter de código
- **Vue TSC 3.1.1**: Compilador TypeScript para Vue
- **Vite Plugin Vue DevTools 8.0.3**: Herramientas de desarrollo

---

##  Características Principales

### 1. Sistema de Mapas Avanzado

- **Múltiples Capas**: Proyectos, puntos de interés, zonas urbanas
- **Interactividad**: Click, hover, popups informativos
- **Dibujo**: Herramientas para crear polígonos y marcadores
- **Leyendas**: Identificación visual de elementos
- **Búsqueda Geográfica**: Localizar elementos por coordenadas
- **Filtros**: Por tipo, estado, fecha

### 2. Gestión de Datos

- **CRUD Completo**: Crear, leer, actualizar, eliminar
- **Formularios Validados**: Validación en tiempo real
- **Confirmaciones**: Diálogos de confirmación para acciones destructivas
- **Feedback Visual**: Mensajes de éxito/error

### 3. Reportes y Análisis

- **Filtros Avanzados**: Por fecha, tipo, estado
- **Visualización de Datos**: Tablas responsivas
- **Exportación**: PDF y Excel
- **Estadísticas**: Métricas calculadas en tiempo real

### 4. UX/UI Moderna

- **Dark Theme**: Esquema de colores oscuro elegante
- **Responsive**: Adaptable a móviles, tablets y desktop
- **Animaciones**: Transiciones suaves
- **Icons**: Iconografía consistente
- **Loading States**: Indicadores de carga

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

1. **Node.js 20.19.0+ o 22.12.0+**
   ```bash
   node --version
   ```

2. **npm 10+ o pnpm 8+**
   ```bash
   npm --version
   ```

3. **Git**
   ```bash
   git --version
   ```

### Configuración de Node.js (Windows)

1. Descargar desde [nodejs.org](https://nodejs.org/)
2. Instalar versión LTS recomendada
3. Verificar instalación:
   ```cmd
   node --version
   npm --version
   ```

---

## ⚙️ Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <url-del-repositorio>
cd TBD/frontend
```

### 2. Instalar Dependencias

```bash
npm install
```

o con pnpm (más rápido):

```bash
pnpm install
```

### 3. Configurar Variables de Entorno

Crea un archivo `.env` en la raíz del frontend:

```env
# URL de la API Backend
VITE_API_BASE_URL=http://localhost:8080/api

# Configuración de Mapas
VITE_MAP_CENTER_LAT=-33.4489
VITE_MAP_CENTER_LNG=-70.6693
VITE_MAP_ZOOM=12

# Otras configuraciones
VITE_APP_TITLE=Plataforma de Urbanismo
```

### 4. Configurar API Base URL

Edita `src/services/api.js`:

```javascript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
```

---

##  Ejecución del Proyecto

### Modo Desarrollo

```bash
npm run dev
```

La aplicación estará disponible en: `http://localhost:5173`

### Compilación de Producción

```bash
npm run build
```

Los archivos compilados estarán en el directorio `dist/`

### Preview de Build de Producción

```bash
npm run preview
```

### Verificación de Tipos TypeScript

```bash
npm run type-check
```

### Linting

```bash
npm run lint
```

### Script de Diagnóstico (Windows)

```bash
diagnostico.bat
```

Este script verifica:
- Versión de Node.js
- Dependencias instaladas
- Configuración del proyecto

---

##  Estructura del Proyecto

```
frontend/
│
├── public/                          # Archivos estáticos
│   └── favicon.ico
│
├── src/                             # Código fuente
│   ├── main.ts                      # Punto de entrada
│   ├── App.vue                      # Componente raíz
│   │
│   ├── assets/                      # Recursos estáticos
│   │   ├── base.css                # Estilos base
│   │   ├── main.css                # Estilos principales
│   │   ├── css/                    # Hojas de estilo
│   │   ├── images/                 # Imágenes
│   │   └── styles/                 # Estilos modulares
│   │
│   ├── components/                  # Componentes reutilizables
│   │   └── common/                 # Componentes comunes
│   │
│   ├── layouts/                     # Layouts de página
│   │   └── AppLayout.vue           # Layout principal
│   │
│   ├── router/                      # Configuración de rutas
│   │   └── index.js                # Definición de rutas
│   │
│   ├── services/                    # Servicios de API
│   │   ├── api.js                  # Cliente HTTP base
│   │   ├── authService.js          # Servicio de autenticación
│   │   ├── proyectosService.js     # Servicio de proyectos
│   │   ├── puntosInteresService.js # Servicio de puntos
│   │   ├── zonasService.js         # Servicio de zonas
│   │   └── reportesService.js      # Servicio de reportes
│   │
│   ├── stores/                      # Stores de Pinia
│   │   ├── auth.js                 # Store de autenticación
│   │   └── counter.ts              # Store de ejemplo
│   │
│   ├── utils/                       # Utilidades
│   │   └── constants.js            # Constantes globales
│   │
│   └── views/                       # Vistas/Páginas
│       ├── LoginView.vue           # Vista de login
│       ├── DashboardView.vue       # Panel principal
│       ├── ProyectosView.vue       # Gestión de proyectos
│       ├── PuntosInteresView.vue   # Gestión de puntos
│       ├── ZonasView.vue           # Gestión de zonas
│       ├── ReportesView.vue        # Reportes y estadísticas
│       ├── AnalisisEspacialView.vue # Análisis espacial
│       ├── SimulacionProyectosView.vue # Simulación
│       ├── MapaIntegradoView.vue   # Mapa consolidado
│       └── NotFoundView.vue        # Página 404
│
├── index.html                       # HTML principal
├── vite.config.ts                   # Configuración de Vite
├── tsconfig.json                    # Configuración TypeScript
├── eslint.config.ts                 # Configuración ESLint
├── package.json                     # Dependencias y scripts
└── README.md                        # Este archivo
```

---

## 📱 Vistas y Componentes

###  LoginView.vue

**Ruta**: `/login`

**Funcionalidad**:
- Formulario de autenticación
- Validación de credenciales
- Redirección al dashboard tras login exitoso
- Almacenamiento de token JWT

**Características**:
```vue
- Input de usuario y contraseña
- Validación en tiempo real
- Mensajes de error
- Loading state durante autenticación
```

### DashboardView.vue

**Ruta**: `/` (ruta principal)

**Funcionalidad**:
- Vista general del sistema
- Estadísticas principales
- Gráficos y métricas
- Accesos rápidos a secciones

**Widgets**:
- Total de proyectos
- Puntos de interés registrados
- Zonas urbanas activas
- Proyectos por estado
- Actividad reciente

### ProyectosView.vue

**Ruta**: `/proyectos`

**Funcionalidad**:
- Lista de proyectos urbanos
- Crear nuevo proyecto
- Editar proyecto existente
- Eliminar proyecto
- Ver detalles en mapa

**Formulario de Proyecto**:
```javascript
{
  nombre: String,
  descripcion: String,
  tipo: String,
  estado: String,
  presupuesto: Number,
  fechaInicio: Date,
  fechaFin: Date,
  latitud: Number,
  longitud: Number
}
```

### PuntosInteresView.vue

**Ruta**: `/puntos-interes`

**Funcionalidad**:
- Gestión de puntos de interés (POIs)
- Mapa interactivo con marcadores
- Click en mapa para agregar punto
- Categorización por tipo
- Filtros y búsqueda

**Tipos de Puntos**:
- Hospitales
- Escuelas
- Parques
- Comercios
- Servicios públicos
- Transporte

### ZonasView.vue

**Ruta**: `/zonas`

**Funcionalidad**:
- Gestión de zonas urbanas
- Dibujo de polígonos en mapa
- Edición de geometrías
- Datos demográficos
- Uso de suelo

**Herramientas de Dibujo**:
- Polígono libre
- Rectángulo
- Círculo
- Editar vértices
- Eliminar formas

### ReportesView.vue

**Ruta**: `/reportes`

**Funcionalidad**:
- Generación de reportes personalizados
- Filtros por fecha y tipo
- Visualización en tablas
- Exportación a PDF y Excel

**Filtros Disponibles**:
```javascript
{
  fechaInicio: Date,
  fechaFin: Date,
  tipoReporte: String ('proyectos' | 'puntos' | 'zonas')
}
```

**Exportación**:
- **PDF**: Usa jsPDF con autoTable
- **Excel**: Usa XLSX para generar archivos .xlsx

### AnalisisEspacialView.vue

**Ruta**: `/analisis-espacial`

**Funcionalidad**:
- Consultas espaciales avanzadas
- Búsqueda de proximidad
- Intersección de capas
- Análisis de densidad
- Visualización de resultados

**Tipos de Análisis**:
- Puntos dentro de zona
- Proyectos cercanos a punto
- Zonas que intersectan
- Densidad poblacional
- Cobertura de servicios

### SimulacionProyectosView.vue

**Ruta**: `/simulacion-proyectos`

**Funcionalidad**:
- Simular nuevos proyectos
- Análisis de impacto
- Visualización temporal
- Comparación de escenarios
- Validación de factibilidad

**Características**:
- Dibujar proyecto en mapa
- Configurar parámetros
- Ver impacto en zonas cercanas
- Generar reportes de simulación

### MapaIntegradoView.vue

**Ruta**: `/mapa-integrado`

**Funcionalidad**:
- Vista consolidada de todos los elementos
- Capas superpuestas
- Control de visibilidad de capas
- Leyenda unificada
- Filtros globales

**Capas Disponibles**:
- ✅ Proyectos urbanos
- ✅ Puntos de interés
- ✅ Zonas urbanas
- ✅ Áreas de influencia
- ✅ Datos demográficos

###  NotFoundView.vue

**Ruta**: `*` (404)

**Funcionalidad**:
- Página de error 404
- Redirección al inicio
- Diseño amigable

---

## Sistema de Mapas

### Configuración de Leaflet

```javascript
// Inicialización básica
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import 'leaflet-draw/dist/leaflet.draw.css'

const map = L.map('map').setView([-33.4489, -70.6693], 12)

// Tile layer (OpenStreetMap)
L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
  attribution: '© OpenStreetMap contributors',
  maxZoom: 19
}).addTo(map)
```

### Marcadores Personalizados

```javascript
// Icono personalizado
const iconoProyecto = L.icon({
  iconUrl: '/icons/proyecto.png',
  iconSize: [32, 32],
  iconAnchor: [16, 32],
  popupAnchor: [0, -32]
})

// Agregar marcador
L.marker([lat, lng], { icon: iconoProyecto })
  .addTo(map)
  .bindPopup('Información del proyecto')
```

### Polígonos y Zonas

```javascript
// Crear polígono
const poligono = L.polygon(coordenadas, {
  color: 'blue',
  fillColor: '#3b82f6',
  fillOpacity: 0.3,
  weight: 2
}).addTo(map)

// Evento de click
poligono.on('click', (e) => {
  mostrarDetallesZona(e.target)
})
```

### Herramientas de Dibujo

```javascript
import 'leaflet-draw'

// Configurar controles de dibujo
const drawControl = new L.Control.Draw({
  draw: {
    polygon: true,
    rectangle: true,
    circle: false,
    marker: true,
    polyline: false,
    circlemarker: false
  },
  edit: {
    featureGroup: drawnItems,
    remove: true
  }
})

map.addControl(drawControl)

// Evento de creación
map.on(L.Draw.Event.CREATED, (e) => {
  const layer = e.layer
  drawnItems.addLayer(layer)
  guardarGeometria(layer.toGeoJSON())
})
```

### Control de Capas

```javascript
// Definir capas
const capas = {
  'Proyectos': layerProyectos,
  'Puntos de Interés': layerPuntos,
  'Zonas Urbanas': layerZonas
}

// Control de capas
L.control.layers(null, capas, { collapsed: false }).addTo(map)
```

---

## Servicios y API

### Cliente HTTP Base (api.js)

```javascript
import axios from 'axios'

const API_BASE_URL = 'http://localhost:8080/api'

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json'
  }
})

// Interceptor para agregar token
apiClient.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default apiClient
```

### Auth Service (authService.js)

```javascript
import apiClient from './api'

export default {
  // Login
  async login(credentials) {
    const response = await apiClient.post('/auth/login', credentials)
    const { token, username, rol } = response.data
    
    // Guardar en localStorage
    localStorage.setItem('token', token)
    localStorage.setItem('username', username)
    localStorage.setItem('rol', rol)
    
    return response.data
  },
  
  // Logout
  logout() {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('rol')
  },
  
  // Verificar autenticación
  isAuthenticated() {
    return !!localStorage.getItem('token')
  }
}
```

### Proyectos Service (proyectosService.js)

```javascript
import apiClient from './api'

export default {
  // Obtener todos
  async getProyectos() {
    const response = await apiClient.get('/proyectos')
    return response.data
  },
  
  // Obtener por ID
  async getProyecto(id) {
    const response = await apiClient.get(`/proyectos/${id}`)
    return response.data
  },
  
  // Crear
  async crearProyecto(proyecto) {
    const response = await apiClient.post('/proyectos', proyecto)
    return response.data
  },
  
  // Actualizar
  async actualizarProyecto(id, proyecto) {
    const response = await apiClient.put(`/proyectos/${id}`, proyecto)
    return response.data
  },
  
  // Eliminar
  async eliminarProyecto(id) {
    await apiClient.delete(`/proyectos/${id}`)
  },
  
  // Por estado
  async getProyectosPorEstado(estado) {
    const response = await apiClient.get(`/proyectos/estado/${estado}`)
    return response.data
  }
}
```

### Reportes Service (reportesService.js)

```javascript
import apiClient from './api'
import jsPDF from 'jspdf'
import autoTable from 'jspdf-autotable'
import * as XLSX from 'xlsx'

export default {
  // Generar reporte
  async generarReporte(filtros) {
    const params = new URLSearchParams()
    if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio)
    if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin)
    if (filtros.tipoReporte) params.append('tipo', filtros.tipoReporte)
    
    const response = await apiClient.get(`/reportes?${params}`)
    return response.data
  },
  
  // Exportar a PDF
  async exportarPDF(datos, titulo) {
    const doc = new jsPDF()
    
    doc.setFontSize(18)
    doc.text(titulo, 14, 20)
    
    const columns = ['ID', 'Nombre', 'Tipo', 'Fecha', 'Estado']
    const rows = datos.map(item => [
      item.id,
      item.nombre,
      item.tipo,
      new Date(item.fecha).toLocaleDateString(),
      item.estado
    ])
    
    autoTable(doc, {
      head: [columns],
      body: rows,
      startY: 30
    })
    
    doc.save(`${titulo}.pdf`)
  },
  
  // Exportar a Excel
  async exportarExcel(datos, nombreArchivo) {
    const worksheet = XLSX.utils.json_to_sheet(datos)
    const workbook = XLSX.utils.book_new()
    XLSX.utils.book_append_sheet(workbook, worksheet, 'Reporte')
    XLSX.writeFile(workbook, `${nombreArchivo}.xlsx`)
  }
}
```

---

##  Estado Global (Stores)

### Auth Store (stores/auth.js)

```javascript
import { defineStore } from 'pinia'
import authService from '@/services/authService'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || null,
    username: localStorage.getItem('username') || null,
    rol: localStorage.getItem('rol') || null,
    isAuthenticated: !!localStorage.getItem('token')
  }),
  
  actions: {
    async login(credentials) {
      try {
        const data = await authService.login(credentials)
        this.token = data.token
        this.username = data.username
        this.rol = data.rol
        this.isAuthenticated = true
        return true
      } catch (error) {
        console.error('Error en login:', error)
        return false
      }
    },
    
    logout() {
      authService.logout()
      this.token = null
      this.username = null
      this.rol = null
      this.isAuthenticated = false
    }
  },
  
  getters: {
    isAdmin: (state) => state.rol === 'ADMIN',
    getUsername: (state) => state.username
  }
})
```

---

##  Rutas y Navegación

### Configuración de Router (router/index.js)

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layouts/AppLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue')
      },
      {
        path: 'proyectos',
        name: 'Proyectos',
        component: () => import('@/views/ProyectosView.vue')
      },
      {
        path: 'puntos-interes',
        name: 'PuntosInteres',
        component: () => import('@/views/PuntosInteresView.vue')
      },
      {
        path: 'zonas',
        name: 'Zonas',
        component: () => import('@/views/ZonasView.vue')
      },
      {
        path: 'reportes',
        name: 'Reportes',
        component: () => import('@/views/ReportesView.vue')
      },
      {
        path: 'analisis-espacial',
        name: 'AnalisisEspacial',
        component: () => import('@/views/AnalisisEspacialView.vue')
      },
      {
        path: 'simulacion-proyectos',
        name: 'SimulacionProyectos',
        component: () => import('@/views/SimulacionProyectosView.vue')
      },
      {
        path: 'mapa-integrado',
        name: 'MapaIntegrado',
        component: () => import('@/views/MapaIntegradoView.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// Guard de navegación
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login')
  } else if (to.path === '/login' && authStore.isAuthenticated) {
    next('/')
  } else {
    next()
  }
})

export default router
```

---

##  Estilos y Temas

### Variables CSS (assets/base.css)

```css
:root {
  /* Colores principales */
  --bg-primary: #0f172a;
  --bg-secondary: #1e293b;
  --bg-tertiary: #334155;
  
  --text-primary: #f1f5f9;
  --text-secondary: #94a3b8;
  --text-tertiary: #64748b;
  
  --accent-primary: #6366f1;
  --accent-primary-hover: #4f46e5;
  --accent-secondary: #8b5cf6;
  
  --success: #10b981;
  --warning: #f59e0b;
  --error: #ef4444;
  --info: #3b82f6;
  
  --border-color: #334155;
  
  /* Sombras */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.3);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.4);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.5);
  
  /* Transiciones */
  --transition-fast: 0.15s ease;
  --transition-normal: 0.3s ease;
  --transition-slow: 0.5s ease;
}
