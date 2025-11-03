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
        <h2>Resultados del Reporte</h2>
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
          <button @click="exportarPDF" class="btn btn-success">
            Exportar a PDF
          </button>
          <button @click="exportarExcel" class="btn btn-success">
            Exportar a Excel
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import reportesService from '@/services/reportesService'

// Estado reactivo
const loading = ref(false)
const reporteGenerado = ref(false)
const datosReporte = ref([])
const error = ref(null)

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
    reporteGenerado.value = true

    // Llamar al servicio con los filtros
    const data = await reportesService.generarReporte(filtros)
    datosReporte.value = data

    if (data.length === 0) {
      console.info('No se encontraron datos para los filtros seleccionados')
    }
  } catch (err) {
    console.error('Error al generar reporte:', err)
    error.value = 'Error al generar el reporte. Intente nuevamente.'
    datosReporte.value = []
  } finally {
    loading.value = false
  }
}

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

const exportarPDF = async () => {
  try {
    loading.value = true

    const titulo = filtros.tipoReporte
      ? `Reporte de ${filtros.tipoReporte}`
      : 'Reporte General'

    await reportesService.exportarPDF(datosReporte.value, titulo)

    console.log('Reporte PDF generado exitosamente')
  } catch (err) {
    console.error('Error al exportar a PDF:', err)
    alert('Error al exportar a PDF. Asegúrese de que los datos del reporte estén cargados.')
  } finally {
    loading.value = false
  }
}

const exportarExcel = async () => {
  try {
    loading.value = true

    const nombreArchivo = filtros.tipoReporte
      ? `reporte_${filtros.tipoReporte}`
      : 'reporte_general'

    await reportesService.exportarExcel(datosReporte.value, nombreArchivo)

    console.log('Reporte Excel generado exitosamente')
  } catch (err) {
    console.error('Error al exportar a Excel:', err)
    alert('Error al exportar a Excel. Asegúrese de que los datos del reporte estén cargados.')
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(() => {
  // No es necesario cargar nada al montar
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

