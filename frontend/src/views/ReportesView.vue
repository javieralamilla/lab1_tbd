<template>
  <div class="reportes-container">
    <div class="reportes-header">
      <h1>Reportes y Estadísticas</h1>
      <p class="subtitle">Visualiza y genera reportes del sistema</p>
    </div>

    <div class="reportes-content">
      <!-- Sección de filtros -->
      <div class="filters-section">
        <h2>Filtros</h2>
        <div class="filters-grid">
          <div class="filter-group">
            <label for="fecha-inicio">Fecha Inicio:</label>
            <input
              id="fecha-inicio"
              v-model="filtros.fechaInicio"
              type="date"
              class="form-input"
            >
          </div>

          <div class="filter-group">
            <label for="fecha-fin">Fecha Fin:</label>
            <input
              id="fecha-fin"
              v-model="filtros.fechaFin"
              type="date"
              class="form-input"
            >
          </div>

          <div class="filter-group">
            <label for="tipo-reporte">Tipo de Reporte:</label>
            <select
              id="tipo-reporte"
              v-model="filtros.tipoReporte"
              class="form-input"
            >
              <option value="">Todos</option>
              <option value="proyectos">Proyectos</option>
              <option value="puntos">Puntos de Interés</option>
              <option value="zonas">Zonas</option>
            </select>
          </div>
        </div>

        <div class="filter-actions">
          <button @click="generarReporte" class="btn btn-primary">
            Generar Reporte
          </button>
          <button @click="limpiarFiltros" class="btn btn-secondary">
            Limpiar Filtros
          </button>
        </div>
      </div>


      <!-- Sección de resultados -->
      <div v-if="reporteGenerado" class="results-section">
        <ErrorAlert v-if="error" :message="error" type="error" @close="error = null" />
        <h2>Resultados del Reporte</h2>
        <div class="report-summary" v-if="resumen.total >= 0">
          <p><strong>Total registros:</strong> {{ resumen.total }}</p>
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
        <div class="results-table">
          <p v-if="loading" class="loading-message">Cargando datos...</p>
          <p v-else-if="!datosReporte.length" class="no-data-message">
            No hay datos disponibles para los filtros seleccionados
          </p>
          <table v-else class="data-table">
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
                  <span :class="['badge', `badge-${item.estado.toLowerCase()}`]">
                    {{ item.estado }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="export-section">
          <button @click="exportarPDF" class="btn btn-success" :disabled="loading || !reporteGenerado || datosReporte.length === 0">
            <span v-if="loading">Procesando...</span>
            <span v-else>Exportar a PDF</span>
          </button>
          <button @click="exportarExcel" class="btn btn-success" :disabled="loading || !reporteGenerado || datosReporte.length === 0">
            <span v-if="loading">Procesando...</span>
            <span v-else>Exportar a Excel</span>
          </button>
        </div>
      </div>

      <!-- Sección de Escasez de Hospitales -->
      <div class="escasez-hospitales-section">
        <h2>Identificación de Zonas con Escasez de Servicios Hospitalarios</h2>
        <p class="section-subtitle">Top 5 zonas con mayor población pero menor cantidad de hospitales</p>
        
        <div class="escasez-filters">
          <div class="filter-group">
            <label for="año-escasez">Año de Análisis:</label>
            <select
              id="año-escasez"
              v-model="añoEscasez"
              class="form-input"
            >
              <option value="2024">2024</option>
              <option value="2023">2023</option>
              <option value="2022">2022</option>
              <option value="2021">2021</option>
              <option value="2020">2020</option>
              <option value="2019">2019</option>
            </select>
          </div>

          <button @click="cargarEscasezHospitales" class="btn btn-primary" :disabled="loadingEscasez">
            <span v-if="loadingEscasez">Cargando...</span>
            <span v-else>Actualizar Análisis</span>
          </button>
        </div>

        <div v-if="zonasEscasez.length > 0" class="escasez-results">
          <table class="data-table">
            <thead>
              <tr>
                <th>Zona Urbana</th>
                <th>Población</th>
                <th>Cantidad de Hospitales</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(zona, index) in zonasEscasez" :key="index">
                <td>{{ zona.zona }}</td>
                <td>{{ formatNumber(zona.poblacion) }}</td>
                <td class="hospital-count">{{ zona.cantidad_hospitales }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        
        <p v-else-if="!loadingEscasez" class="no-data-message">
          No hay datos disponibles. Haz clic en "Actualizar Análisis" para cargar los datos.
        </p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import reportesService from '@/services/reportesService'
import ErrorAlert from '@/components/common/ErrorAlert.vue'
import LoadingSpinner from '@/components/common/LoadingSpinner.vue'

// Estado reactivo
const loading = ref(false)
const reporteGenerado = ref(false)
const datosReporte = ref([])
const error = ref(null)
const loadingEscasez = ref(false)
const zonasEscasez = ref([])
const añoEscasez = ref(2024)

const filtros = reactive({
  fechaInicio: '',
  fechaFin: '',
  tipoReporte: ''
})


// Métodos

const generarReporte = async () => {
  try {
    loading.value = true
    error.value = null
    // Marcar como no generado hasta que la carga finalice correctamente
    reporteGenerado.value = false

    // Llamar al servicio con los filtros
    const data = await reportesService.generarReporte(filtros)
    datosReporte.value = data
    // Marcar que se generó el reporte solo si la carga fue exitosa
    reporteGenerado.value = true

    if (data.length === 0) {
      console.info('No se encontraron datos para los filtros seleccionados')
    }
  } catch (err) {
    console.error('Error al generar reporte:', err)
    error.value = err.message || 'Error al generar el reporte. Intente nuevamente.'
    datosReporte.value = []
  } finally {
    loading.value = false
  }
}

// Conteos y resumen del reporte
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

const limpiarFiltros = () => {
  filtros.fechaInicio = ''
  filtros.fechaFin = ''
  filtros.tipoReporte = ''
  reporteGenerado.value = false
  datosReporte.value = []
  error.value = null
}

const formatearFecha = (fecha) => {
  if (!fecha) return '-'
  return new Date(fecha).toLocaleDateString('es-ES')
}

const formatNumber = (num) => {
  if (!num && num !== 0) return '-'
  return new Intl.NumberFormat('es-ES').format(num)
}

const cargarEscasezHospitales = async () => {
  try {
    loadingEscasez.value = true
    error.value = null
    const data = await reportesService.obtenerZonasEscasezHospitales(añoEscasez.value)
    zonasEscasez.value = data
  } catch (err) {
    console.error('Error al cargar zonas con escasez de hospitales:', err)
    error.value = err.message || 'Error al cargar el análisis. Intente nuevamente.'
    zonasEscasez.value = []
  } finally {
    loadingEscasez.value = false
  }
}

const exportarPDF = async () => {
  loading.value = true
  error.value = null
  let backendErr = null

  try {
    const titulo = filtros.tipoReporte
      ? `Reporte de ${filtros.tipoReporte}`
      : 'Reporte General'

    // Intentar descarga desde backend primero
    try {
      await reportesService.exportarDesdeBackend(filtros, 'pdf')
      console.log('Reporte PDF descargado desde backend')
      return
    } catch (e) {
      backendErr = e
      console.warn('Export desde backend falló, intentando generación local:', e)
    }

    // Si no hay datos locales, informar y mostrar detalle del fallo backend
    if (!datosReporte.value || datosReporte.value.length === 0) {
      error.value = backendErr && backendErr.message
        ? `Error al exportar desde backend: ${backendErr.message}. Además no hay datos locales para generar el PDF.`
        : 'No hay datos cargados para exportar a PDF.'
      return
    }

    // Generar PDF en cliente como fallback
    try {
      await reportesService.exportarPDF(datosReporte.value, titulo)
      console.log('Reporte PDF generado en cliente exitosamente')
    } catch (clientErr) {
      console.error('Error generando PDF en cliente:', clientErr)
      const backendMsg = backendErr && backendErr.message ? `Backend: ${backendErr.message}. ` : ''
      const clientMsg = clientErr && clientErr.message ? clientErr.message : String(clientErr)
      error.value = `Error al exportar a PDF. ${backendMsg}Generación local falló: ${clientMsg}`
    }
  } catch (err) {
    console.error('Error inesperado en exportarPDF:', err)
    error.value = err && err.message ? `Error al exportar a PDF: ${err.message}` : 'Error al exportar a PDF.'
  } finally {
    loading.value = false
  }
}

const exportarExcel = async () => {
  loading.value = true
  error.value = null
  let backendErr = null

  try {
    const nombreArchivo = filtros.tipoReporte
      ? `reporte_${filtros.tipoReporte}`
      : 'reporte_general'

    // Intentar descargar desde backend primero
    try {
      await reportesService.exportarDesdeBackend(filtros, 'xlsx')
      console.log('Reporte Excel descargado desde backend')
      return
    } catch (e) {
      backendErr = e
      console.warn('Export desde backend falló, intentando generación local:', e)
    }

    if (!datosReporte.value || datosReporte.value.length === 0) {
      error.value = backendErr && backendErr.message
        ? `Error al exportar desde backend: ${backendErr.message}. Además no hay datos locales para generar el Excel.`
        : 'No hay datos cargados para exportar a Excel.'
      return
    }

    try {
      await reportesService.exportarExcel(datosReporte.value, nombreArchivo)
      console.log('Reporte Excel generado en cliente exitosamente')
    } catch (clientErr) {
      console.error('Error generando Excel en cliente:', clientErr)
      const backendMsg = backendErr && backendErr.message ? `Backend: ${backendErr.message}. ` : ''
      const clientMsg = clientErr && clientErr.message ? clientErr.message : String(clientErr)
      error.value = `Error al exportar a Excel. ${backendMsg}Generación local falló: ${clientMsg}`
    }
  } catch (err) {
    console.error('Error inesperado en exportarExcel:', err)
    error.value = err && err.message ? `Error al exportar a Excel: ${err.message}` : 'Error al exportar a Excel.'
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  cargarEscasezHospitales()
})
</script>

<style scoped>
.reportes-container {
  padding: 2rem;
  max-width: 1400px;
  margin: 0 auto;
  background-color: var(--bg-primary);
  min-height: 100vh;
}

.reportes-header {
  margin-bottom: 2rem;
}

.reportes-header h1 {
  color: var(--text-primary);
  margin-bottom: 0.5rem;
}

.subtitle {
  color: var(--text-secondary);
  font-size: 1.1rem;
}

.reportes-content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.report-summary { padding: 12px; background: var(--bg-primary); border: 1px solid var(--border-color); border-radius: 8px; }
.summary-grid { display:flex; gap:16px; margin-top:8px; }
.summary-block h4 { margin:0 0 6px 0; font-size:14px; }
.summary-block ul { margin:0; padding-left: 18px; color: var(--text-secondary); }

/* Filtros */
.filters-section {
  background: var(--bg-secondary);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-color);
}

.filters-section h2 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.filters-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.filter-group {
  display: flex;
  flex-direction: column;
}

.filter-group label {
  margin-bottom: 0.5rem;
  color: var(--text-secondary);
  font-weight: 500;
}

.form-input {
  padding: 0.75rem;
  border: 1px solid var(--border-color);
  border-radius: 4px;
  font-size: 1rem;
  transition: border-color 0.3s;
  background-color: var(--bg-primary);
  color: var(--text-primary);
}

.form-input:focus {
  outline: none;
  border-color: var(--accent-primary);
}

.filter-actions {
  display: flex;
  gap: 1rem;
}


/* Resultados */
.results-section {
  background: var(--bg-secondary);
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  border: 1px solid var(--border-color);
}

.results-section h2 {
  color: var(--text-primary);
  margin-bottom: 1rem;
  font-size: 1.3rem;
}

.results-table {
  margin-bottom: 1.5rem;
  overflow-x: auto;
}

.loading-message,
.no-data-message {
  text-align: center;
  padding: 2rem;
  color: var(--text-secondary);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--bg-primary);
  border-radius: 8px;
  overflow: hidden;
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
}

.data-table th {
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-weight: 600;
}

.data-table td {
  color: var(--text-secondary);
}

.data-table tbody tr:hover {
  background: rgba(99, 102, 241, 0.1);
}

.badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 500;
}

.badge-activo {
  background: rgba(16, 185, 129, 0.2);
  color: #10b981;
  border: 1px solid #10b981;
}

.badge-en.proceso {
  background: rgba(245, 158, 11, 0.2);
  color: #f59e0b;
  border: 1px solid #f59e0b;
}

.badge-completado {
  background: rgba(59, 130, 246, 0.2);
  color: #3b82f6;
  border: 1px solid #3b82f6;
}

.export-section {
  display: flex;
  gap: 1rem;
}

/* Botones */
.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.btn-primary {
  background: var(--accent-primary);
  color: white;
}

.btn-primary:hover {
  background: var(--accent-primary-hover);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(99, 102, 241, 0.4);
}

.btn-secondary {
  background: var(--border-color);
  color: var(--text-primary);
}

.btn-secondary:hover {
  background: var(--text-secondary);
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(148, 163, 184, 0.3);
}

.btn-success {
  background: #10b981;
  color: white;
}

.btn-success:hover {
  background: #059669;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(16, 185, 129, 0.4);
}

/* Escasez de Hospitales Section */
.escasez-hospitales-section {
  background: var(--card-background);
  border-radius: 12px;
  padding: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 2rem;
}

.escasez-hospitales-section h2 {
  margin-bottom: 0.5rem;
  color: var(--text-primary);
  font-size: 1.5rem;
}

.section-subtitle {
  color: var(--text-secondary);
  margin-bottom: 1.5rem;
  font-size: 0.95rem;
}

.escasez-filters {
  display: flex;
  gap: 1rem;
  align-items: flex-end;
  margin-bottom: 1.5rem;
}

.escasez-filters .filter-group {
  flex: 0 0 200px;
}

.escasez-results {
  margin-top: 1.5rem;
}

.hospital-count {
  font-weight: 600;
  color: var(--accent-primary);
  text-align: center;
}

/* Responsive */
@media (max-width: 768px) {
  .reportes-container {
    padding: 1rem;
  }

  .filters-grid {
    grid-template-columns: 1fr;
  }


  .filter-actions,
  .export-section {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>


