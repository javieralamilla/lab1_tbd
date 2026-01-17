<template>
  <div class="reportes-container">
    <div class="reportes-header">
      <h1>Reportes</h1>
      <p class="subtitle">Panel integral de inteligencia de negocios y estadísticas urbanas</p>
    </div>

    <!-- Navegación por Pestañas -->
    <div class="tabs-nav">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        class="tab-btn"
        :class="{ active: currentTab === tab.id }"
        @click="currentTab = tab.id"
      >
        {{ tab.label }}
      </button>
    </div>

    <div class="reportes-content">

      <!-- TAB 1: Reporte General (Existente) -->
      <div v-if="currentTab === 'general'" class="tab-pane fade-in">
        <div class="filters-section">
          <h2>Generador de Reportes</h2>
          <div class="filters-grid">
            <div class="filter-group">
              <label>Fecha Inicio:</label>
              <input v-model="filtros.fechaInicio" type="date" class="form-input">
            </div>
            <div class="filter-group">
              <label>Fecha Fin:</label>
              <input v-model="filtros.fechaFin" type="date" class="form-input">
            </div>
            <div class="filter-group">
              <label>Tipo:</label>
              <select v-model="filtros.tipoReporte" class="form-input">
                <option value="">Todos</option>
                <option value="proyectos">Proyectos</option>
                <option value="puntos">Puntos de Interés</option>
                <option value="zonas">Zonas</option>
              </select>
            </div>
          </div>
          <div class="filter-actions">
            <button @click="generarReporte" class="btn btn-primary" :disabled="loading">
              {{ loading ? 'Generando...' : 'Generar Reporte' }}
            </button>
            <button @click="limpiarFiltros" class="btn btn-secondary">Limpiar</button>
          </div>
        </div>

        <div v-if="reporteGenerado" class="results-section">
           <div class="report-summary" v-if="resumen.total >= 0">
            <div class="summary-header">
              <h3>Resumen Ejecutivo</h3>
              <p>Total registros: <strong>{{ resumen.total }}</strong></p>
            </div>
            <div class="summary-grid">
              <div class="summary-block">
                <h4>Por Estado</h4>
                <ul>
                  <li v-for="(count, estado) in resumen.porEstado" :key="estado">{{ estado }}: {{ count }}</li>
                </ul>
              </div>
              <div class="summary-block">
                <h4>Por Tipo</h4>
                <ul>
                  <li v-for="(count, tipo) in resumen.porTipo" :key="tipo">{{ tipo }}: {{ count }}</li>
                </ul>
              </div>
            </div>
          </div>

          <div class="table-wrapper">
             <table class="data-table">
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Nombre</th>
                  <th>Tipo</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="item in datosReporte" :key="item.id">
                  <td>{{ item.id }}</td>
                  <td>{{ item.nombre }}</td>
                  <td>{{ item.tipo }}</td>
                  <td>{{ formatearFecha(item.fecha) }}</td>
                  <td>
                    <span :class="['badge', `badge-${(item.estado || '').toLowerCase().replace(/ /g, '-')}`]">
                      {{ item.estado }}
                    </span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <div class="export-section">
            <button @click="exportarPDF" class="btn btn-success">Exportar PDF</button>
            <button @click="exportarExcel" class="btn btn-success" style="margin-left: 10px;">Exportar Excel</button>
          </div>
        </div>
      </div>

      <!-- TAB 1.5: Densidad Poblacional (Q1) -->
      <div v-if="currentTab === 'densidad'" class="tab-pane fade-in">
        <div class="analytics-card">
          <div class="card-header">
            <h2>Densidad Poblacional</h2>
            <p>Análisis de densidad poblacional por zona urbana</p>
          </div>

          <div v-if="error" class="error-banner" style="background-color: #fee; border: 1px solid #fcc; padding: 12px; margin: 12px 0; border-radius: 4px; color: #c33;">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="display: inline; vertical-align: middle; margin-right: 8px;">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="8" x2="12" y2="12"></line>
              <line x1="12" y1="16" x2="12.01" y2="16"></line>
            </svg>
            <span>{{ error }}</span>
          </div>

          <div v-if="loading" class="loading-message" style="text-align: center; padding: 20px;">
            Cargando datos de densidad...
          </div>

          <div class="info-banner">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="12" cy="12" r="10"></circle>
              <line x1="12" y1="16" x2="12" y2="12"></line>
              <line x1="12" y1="8" x2="12.01" y2="8"></line>
            </svg>
            <span>Esta tabla muestra la densidad poblacional calculada para cada zona urbana según su área y población.</span>
          </div>

          <div class="metric-highlight" v-if="zonasDensidad.length">
            <div class="metric-row">
              <div class="metric-item">
                <div class="metric-value">{{ zonasDensidad.length }}</div>
                <div class="metric-label">Zonas Analizadas</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ formatNumber(Math.max(...zonasDensidad.map(z => z.densidad_poblacion_km2 || 0))) }}</div>
                <div class="metric-label">Densidad Máxima (hab/km²)</div>
              </div>
              <div class="metric-item">
                <div class="metric-value">{{ formatNumber(Math.round(zonasDensidad.reduce((sum, z) => sum + (z.densidad_poblacion_km2 || 0), 0) / zonasDensidad.length)) }}</div>
                <div class="metric-label">Densidad Promedio (hab/km²)</div>
              </div>
            </div>
          </div>

          <table class="data-table">
            <thead>
              <tr>
                <th>Zona Urbana</th>
                <th>Población</th>
                <th>Área Real (km²)</th>
                <th>Densidad (hab/km²)</th>
                <th>Clasificación</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loading">
                <td colspan="5" class="text-center" style="padding: 30px;">
                  <div style="display: flex; align-items: center; justify-content: center; gap: 10px;">
                    <div style="width: 20px; height: 20px; border: 3px solid #f3f3f3; border-top: 3px solid #3498db; border-radius: 50%; animation: spin 1s linear infinite;"></div>
                    <span>Cargando datos de densidad...</span>
                  </div>
                </td>
              </tr>
              <tr v-else-if="!zonasDensidad.length">
                <td colspan="5" class="text-center" style="padding: 30px; color: #666;">
                  <div style="display: flex; flex-direction: column; align-items: center; gap: 10px;">
                    <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"></circle>
                      <line x1="12" y1="8" x2="12" y2="12"></line>
                      <line x1="12" y1="16" x2="12.01" y2="16"></line>
                    </svg>
                    <strong>No hay datos de densidad disponibles</strong>
                    <p style="margin: 0; font-size: 14px;">Verifica que existan zonas urbanas con datos demográficos en la base de datos.</p>
                  </div>
                </td>
              </tr>
              <tr v-else v-for="(zona, index) in zonasDensidad" :key="index">
                <td class="font-bold">{{ zona.zona }}</td>
                <td class="text-right">{{ formatNumber(zona.poblacion) }}</td>
                <td class="text-right">{{ zona.area_real_km2 }} km²</td>
                <td class="text-right font-bold">{{ formatNumber(zona.densidad_poblacion_km2) }}</td>
                <td>
                  <span v-if="zona.densidad_poblacion_km2 >= 10000" class="badge badge-error">Muy Alta</span>
                  <span v-else-if="zona.densidad_poblacion_km2 >= 5000" class="badge badge-warning">Alta</span>
                  <span v-else-if="zona.densidad_poblacion_km2 >= 1000" class="badge badge-info">Media</span>
                  <span v-else class="badge badge-success">Baja</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 2: Escasez Hospitales (Q2) -->
      <div v-if="currentTab === 'escasez'" class="tab-pane fade-in">
        <div class="analytics-card">
          <div class="card-header">
            <h2>Zonas con Escasez de Servicios</h2>
            <p>Top 5 zonas con alta población y baja cobertura hospitalaria</p>
          </div>

          <div class="card-controls">
            <select v-model="añoEscasez" class="form-select">
              <option v-for="year in [2024, 2023, 2022, 2021, 2020]" :key="year" :value="year">{{ year }}</option>
            </select>
            <button @click="cargarEscasezHospitales" class="btn btn-primary">Actualizar</button>
          </div>

          <table class="data-table">
            <thead>
              <tr>
                <th>Zona Urbana</th>
                <th>Población</th>
                <th>Hospitales</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(zona, index) in zonasEscasez" :key="index">
                <td>{{ zona.zona }}</td>
                <td>{{ formatNumber(zona.poblacion) }} Hab.</td>
                <td class="text-center"><strong>{{ zona.cantidad_hospitales }}</strong></td>
                <td><span class="badge badge-error">Crítico</span></td>
              </tr>
              <tr v-if="!zonasEscasez.length"><td colspan="4" class="text-center">No hay datos disponibles</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 3: Proximidad (Q3) -->
      <div v-if="currentTab === 'proximidad'" class="tab-pane fade-in">
        <div class="analytics-card">
          <div class="card-header">
            <h2>Proximidad Escuelas - Proyectos</h2>
            <p>Escuelas a menos de 500m de proyectos en curso</p>
          </div>

          <div class="metric-highlight" v-if="escuelasCerca.length">
            <div class="metric-value">{{ escuelasCerca.length }}</div>
            <div class="metric-label">Escuelas Impactadas</div>
          </div>

          <table class="data-table">
            <thead>
              <tr>
                <th>Escuela</th>
                <th>Proyecto Cercano</th>
                <th>Distancia</th>
                <th>Alerta</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in escuelasCerca" :key="index">
                <td>{{ item.nombre_escuela }}</td>
                <td>{{ item.nombre_proyecto }}</td>
                <td>{{ item.distancia_metros }} m</td>
                <td>
                  <span v-if="item.distancia_metros < 100" class="badge badge-error">Muy cerca</span>
                  <span v-else class="badge badge-warning">Cerca</span>
                </td>
              </tr>
               <tr v-if="!escuelasCerca.length"><td colspan="4" class="text-center">No se encontraron escuelas cerca de proyectos activos</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- TAB 4: Crecimiento (Q4) -->
      <div v-if="currentTab === 'crecimiento'" class="tab-pane fade-in">
        <div class="analytics-card">
          <div class="card-header">
            <h2>Zonas de Rápido Crecimiento</h2>
            <p>Zonas con crecimiento poblacional > 10% en últimos 5 años</p>
          </div>

          <div class="growth-grid">
            <div v-for="(zona, idx) in zonasCrecimiento" :key="idx" class="growth-card">
              <h3>{{ zona.zona }}</h3>
              <div class="growth-stat">
                <span class="growth-percent">+{{ zona.porcentaje_crecimiento }}%</span>
                <span class="growth-label">Crecimiento Total</span>
              </div>
              <div class="growth-details">
                <p><strong>Actual:</strong> {{ formatNumber(zona.poblacion_actual) }}</p>
                <p><strong>Anterior:</strong> {{ formatNumber(zona.poblacion_anterior) }}</p>
              </div>
            </div>
             <div v-if="!zonasCrecimiento.length" class="no-data-msg">No se detectaron zonas con crecimiento acelerado.</div>
          </div>
        </div>
      </div>

      <!-- TAB 5: Resumen Proyectos (Q10) -->
      <div v-if="currentTab === 'resumen'" class="tab-pane fade-in">
        <div class="analytics-card">
          <div class="card-header">
            <h2>Resumen de Proyectos</h2>
            <p>Matriz de estado de proyectos por tipo de zona</p>
          </div>

          <div class="card-controls">
            <select v-model="filtroResumen.tipoZona" class="form-select" @change="cargarResumenProyectos">
              <option value="todos">Todas las Zonas</option>
              <option value="Residencial">Residencial</option>
              <option value="Comercial">Comercial</option>
              <option value="Industrial">Industrial</option>
            </select>
             <select v-model="filtroResumen.estado" class="form-select" @change="cargarResumenProyectos">
              <option value="todos">Todos los Estados</option>
              <option value="En Curso">En Curso</option>
              <option value="Planeado">Planeado</option>
              <option value="Completado">Completado</option>
            </select>
          </div>

          <table class="data-table matrix-table">
            <thead>
              <tr>
                <th>Tipo Zona</th>
                <th>Estado</th>
                <th>Cantidad</th>
                <th>Presupuesto Total</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(row, index) in resumenProyectos" :key="index">
                <td class="font-bold">{{ row.tipo_zona }}</td>
                <td>
                   <span :class="['badge', `badge-${(row.estado_proyecto || '').toLowerCase().replace(/ /g, '-')}`]">
                      {{ row.estado_proyecto }}
                    </span>
                </td>
                <td class="text-right">{{ row.cantidad_proyectos }}</td>
                <td class="text-right">{{ formatCurrency(row.presupuesto_total) }}</td>
              </tr>
               <tr v-if="!resumenProyectos.length"><td colspan="4" class="text-center">No hay datos para mostrar</td></tr>
            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import reportesService from '@/services/reportesService'
// Importamos directamente los servicios faltantes para Q3, Q4, Q10
import puntosInteresService from '@/services/puntosInteresService.js' // Para Q3
import zonasService from '@/services/zonasService.js'         // Para Q4
import proyectosService from '@/services/proyectosService.js' // Para Q10

// Estado General
const currentTab = ref('general')
const loading = ref(false)
const error = ref(null)

// Configuración Tabs
const tabs = [
  { id: 'general', label: 'General' },
  { id: 'densidad', label: 'Densidad' },
  { id: 'escasez', label: 'Escasez' },
  { id: 'proximidad', label: 'Proximidad' },
  { id: 'crecimiento', label: 'Crecimiento' },
  { id: 'resumen', label: 'Resumen' }
]

// --- Lógica Tab General ---
const reporteGenerado = ref(false)
const datosReporte = ref([])
const filtros = reactive({ fechaInicio: '', fechaFin: '', tipoReporte: '' })

// --- Lógica Q1 Densidad ---
const zonasDensidad = ref([])

// --- Lógica Q2 Escasez ---
const zonasEscasez = ref([])
const añoEscasez = ref(2024)

// --- Lógica Q3 Proximidad ---
const escuelasCerca = ref([])

// --- Lógica Q4 Crecimiento ---
const zonasCrecimiento = ref([])

// --- Lógica Q10 Resumen ---
const resumenProyectos = ref([])
const filtroResumen = reactive({ tipoZona: 'todos', estado: 'todos' })

// MÉTODOS DE CARGA

// Cargar Tab General
const generarReporte = async () => {
  try {
    loading.value = true
    const data = await reportesService.generarReporte(filtros)
    datosReporte.value = data
    reporteGenerado.value = true
  } catch (err) {
    console.error(err)
    alert('Error generando reporte')
  } finally { loading.value = false }
}

const limpiarFiltros = () => {
  filtros.fechaInicio = ''
  filtros.fechaFin = ''
  filtros.tipoReporte = ''
  datosReporte.value = []
  reporteGenerado.value = false
}

// Cargar Q1 Densidad
const cargarDensidadPoblacion = async () => {
  try {
    loading.value = true
    error.value = null
    console.log('[ReportesView] Cargando densidad poblacional...')
    const data = await zonasService.getDensidadPoblacion()
    console.log('[ReportesView] Datos de densidad recibidos:', data)

    if (!data || data.length === 0) {
      console.warn('[ReportesView] No se recibieron datos de densidad')
      error.value = 'No hay datos de densidad disponibles'
    }

    zonasDensidad.value = data || []
  } catch (e) {
    console.error('[ReportesView] Error completo al cargar densidad:', e)
    console.error('[ReportesView] Mensaje de error:', e.message)
    if (e.response) {
      console.error('[ReportesView] Respuesta del servidor:', e.response)
    }
    error.value = 'Error al cargar datos de densidad: ' + (e.message || 'Error desconocido')
    zonasDensidad.value = []
  } finally {
    loading.value = false
  }
}

// Cargar Q2
const cargarEscasezHospitales = async () => {
  try {
    const data = await reportesService.obtenerZonasEscasezHospitales(añoEscasez.value)
    zonasEscasez.value = data
  } catch (e) {
    console.error('Q2 Error', e)
  }
}

// Cargar Q3
const cargarProximidad = async () => {
  try {
    // Usamos el servicio de puntos de interés directamente
    if (puntosInteresService.getEscuelasCercanas) {
       const data = await puntosInteresService.getEscuelasCercanas()
       escuelasCerca.value = data
    } else {
       console.warn('Servicio getEscuelasCercanas no encontrado en frontend. Pendiente actualización.')
    }
  } catch (e) { console.error('Q3 Error', e) }
}

// Cargar Q4
const cargarCrecimiento = async () => {
  try {
    const data = await zonasService.getZonasRapidoCrecimiento()
    zonasCrecimiento.value = data
  } catch (e) { console.error('Q4 Error', e) }
}

// Cargar Q10
const cargarResumenProyectos = async () => {
  try {
    const data = await proyectosService.getResumenEstadoZona(filtroResumen.tipoZona, filtroResumen.estado)
    resumenProyectos.value = data
  } catch (e) { console.error('Q10 Error', e) }
}

// Watchers para cargar datos al cambiar de tab
watch(currentTab, (newTab) => {
  if (newTab === 'densidad') cargarDensidadPoblacion()
  if (newTab === 'escasez') cargarEscasezHospitales()
  if (newTab === 'proximidad') cargarProximidad()
  if (newTab === 'crecimiento') cargarCrecimiento()
  if (newTab === 'resumen') cargarResumenProyectos()
})

// Computados
const resumen = computed(() => {
  const total = datosReporte.value.length
  const porEstado = {}
  const porTipo = {}
  datosReporte.value.forEach(item => {
    const est = item.estado || 'Desconocido'
    const tip = item.tipo || 'Desconocido'
    porEstado[est] = (porEstado[est] || 0) + 1
    porTipo[tip] = (porTipo[tip] || 0) + 1
  })
  return { total, porEstado, porTipo }
})

// Utilidades
const formatearFecha = (f) => f ? new Date(f).toLocaleDateString() : '-'
const formatNumber = (n) => new Intl.NumberFormat('es-CL').format(n)
const formatCurrency = (amount) => {
  if (!amount) return '$0';
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0
  }).format(amount);
};
const exportarPDF = () => reportesService.exportarPDF(datosReporte.value)
const exportarExcel = () => reportesService.exportarExcel(datosReporte.value)

// Init
onMounted(() => {
  // Carga inicial según el tab actual
  if (currentTab.value === 'densidad') cargarDensidadPoblacion()
  if (currentTab.value === 'escasez') cargarEscasezHospitales()
  if (currentTab.value === 'proximidad') cargarProximidad()
  if (currentTab.value === 'crecimiento') cargarCrecimiento()
  if (currentTab.value === 'resumen') cargarResumenProyectos()
})
</script>

<style scoped>
.reportes-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100vh;
  background: var(--bg-primary);
}

.reportes-header h1 {
  font-size: 2rem;
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}
.subtitle { color: var(--text-secondary); }

/* Tabs */
.tabs-nav {
  display: flex;
  gap: 10px;
  margin-top: 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
  overflow-x: auto;
  padding-bottom: 2px;
}

.tab-btn {
  padding: 10px 20px;
  background: transparent;
  border: none;
  font-weight: 600;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
  white-space: nowrap;
}

.tab-btn:hover { color: var(--accent-primary); background: var(--bg-secondary); border-radius: 8px 8px 0 0; }
.tab-btn.active { color: var(--accent-primary); border-bottom-color: var(--accent-primary); }

/* Cards & Sections */
.analytics-card {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.card-header h2 { margin: 0 0 8px 0; color: var(--text-primary); }
.card-header p { margin: 0 0 24px 0; color: var(--text-secondary); font-size: 0.9em; }

.card-controls {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

/* Tables */
.data-table { width: 100%; border-collapse: collapse; margin-top: 10px; }
.data-table th { text-align: left; padding: 12px; background: var(--bg-primary); border-bottom: 2px solid var(--border-color); color: var(--text-secondary); font-size: 0.85rem; text-transform: uppercase; }
.data-table td { padding: 12px; border-bottom: 1px solid var(--border-color); color: var(--text-primary); }
.data-table tr:hover { background: rgba(0,0,0,0.02); }
.text-center { text-align: center; }
.text-right { text-align: right; }
.font-bold { font-weight: 600; }

/* Badges */
.badge { padding: 4px 8px; border-radius: 6px; font-size: 0.75rem; font-weight: 600; }
.badge-planeado { background: #e0f2fe; color: #0284c7; }
.badge-en-curso { background: #fef3c7; color: #d97706; }
.badge-completado { background: #dcfce7; color: #16a34a; }
.badge-retrasado, .badge-error { background: #fee2e2; color: #dc2626; }
.badge-cancelado { background: #f3f4f6; color: #4b5563; }
.badge-warning { background: #ffedd5; color: #c2410c; }

/* Growth Grid (Q4) */
.growth-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.growth-card {
  background: linear-gradient(135deg, var(--bg-primary) 0%, var(--bg-secondary) 100%);
  border: 1px solid var(--border-color); padding: 20px; border-radius: 12px; text-align: center;
}
.growth-icon { font-size: 2rem; margin-bottom: 10px; }
.growth-stat { margin: 15px 0; }
.growth-percent { display: block; font-size: 2rem; font-weight: 800; color: #16a34a; }
.growth-label { font-size: 0.8rem; text-transform: uppercase; color: var(--text-secondary); letter-spacing: 1px; }
.growth-details { font-size: 0.9rem; color: var(--text-secondary); border-top: 1px solid var(--border-color); padding-top: 10px; margin-top: 10px; }

/* Metric Highlight */
.metric-highlight {
  background: var(--bg-primary);
  border: 1px solid var(--border-color);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.metric-row {
  display: flex;
  justify-content: space-around;
  gap: 20px;
  flex-wrap: wrap;
}
.metric-item {
  text-align: center;
  flex: 1;
  min-width: 150px;
}
.metric-value { font-size: 1.8rem; font-weight: bold; color: var(--accent-primary); }
.metric-label { font-size: 0.85rem; color: var(--text-secondary); margin-top: 4px; }

/* Info Banner */
.info-banner {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #dbeafe;
  color: #1e40af;
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 0.9rem;
}
.info-banner svg {
  flex-shrink: 0;
}

/* Badges adicionales para densidad */
.badge-info { background: #dbeafe; color: #1e40af; }
.badge-success { background: #dcfce7; color: #16a34a; }

/* Filters Section (Tab General) */
.filters-section { background: var(--bg-secondary); padding: 20px; border-radius: 12px; margin-bottom: 24px; border: 1px solid var(--border-color); }
.filters-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 16px; }
.filter-group { display: flex; flex-direction: column; gap: 6px; }
.form-input, .form-select { padding: 8px 12px; border: 1px solid var(--border-color); border-radius: 6px; background: var(--bg-primary); color: var(--text-primary); }
.filter-actions { display: flex; gap: 10px; }
.btn { padding: 10px 20px; border-radius: 6px; font-weight: 500; cursor: pointer; border: none; }
.btn-primary { background: var(--accent-primary); color: white; }
.btn-secondary { background: var(--border-color); color: var(--text-primary); }
.btn-success { background: #16a34a; color: white; }

/* Summary (Tab General) */
.report-summary { margin-bottom: 20px; background: var(--bg-secondary); padding: 20px; border-radius: 12px; border: 1px solid var(--border-color); }
.summary-grid { display: flex; gap: 40px; }
.summary-block ul { padding-left: 20px; margin: 0; color: var(--text-secondary); }

/* Animations */
.fade-in { animation: fadeIn 0.3s ease-in-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

</style>


