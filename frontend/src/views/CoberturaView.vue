<template>
  <div class="cobertura-container">
    <div class="header">
      <div>
        <h1>Cobertura de Infraestructura</h1>
        <p class="subtitle">
          Análisis de equipamiento urbano y cobertura de servicios por zona.
        </p>
      </div>
      <button @click="refrescarDatos" class="btn-refresh" :disabled="loadingRefresco">
        <span v-if="loadingRefresco">Actualizando...</span>
        <span v-else>Actualizar Vista Materializada</span>
      </button>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando datos de cobertura..." />

    <ErrorAlert v-if="error" :message="error" @close="error = null" />

    <div v-if="!loading && !error" class="content">
      <!-- SECCIÓN 1: Cobertura de Hospitales con Buffer 1km (Consulta 4 - Enunciado 2) -->
      <div class="section">
        <div class="section-header">
          <h2>🏥 Cobertura de Hospitales (Buffer 1km)</h2>
          <p class="section-description">
            Porcentaje del área de cada zona urbana cubierta por el radio de servicio de 1km de los hospitales existentes.
          </p>
        </div>

        <div v-if="datosHospitales.length === 0" class="empty-state">
          <p>No hay datos de cobertura de hospitales disponibles.</p>
        </div>

        <div v-else class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Zona Urbana</th>
                <th>Tipo</th>
                <th class="text-center">Área Total (km²)</th>
                <th class="text-center">Área Cubierta (km²)</th>
                <th class="text-center">% Cobertura</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="zona in datosHospitales" :key="zona.zona_urbana_id">
                <td>{{ zona.zona_urbana_id }}</td>
                <td class="font-medium">{{ zona.zona }}</td>
                <td>{{ zona.tipo_zona }}</td>
                <td class="text-center">{{ zona.area_total_km2 }}</td>
                <td class="text-center">{{ zona.area_cubierta_km2 }}</td>
                <td class="text-center">
                  <span :class="getCoberturaClass(zona.porcentaje_cobertura)">
                    {{ zona.porcentaje_cobertura }}%
                  </span>
                </td>
                <td>
                  <span :class="['badge', getBadgeClass(zona.porcentaje_cobertura)]">
                    {{ getCoberturaLabel(zona.porcentaje_cobertura) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- SECCIÓN 2: Cobertura de Infraestructura General (Vista Materializada) -->
      <div class="section">
        <div class="section-header">
          <h2>📊 Infraestructura por Zona</h2>
          <p class="section-description">
            Conteo de parques, escuelas y hospitales por cada zona urbana (vista materializada).
          </p>
        </div>

        <div v-if="datos.length === 0" class="empty-state">
          <p>No hay datos de cobertura disponibles.</p>
        </div>

        <div v-else class="table-card">
          <table class="data-table">
            <thead>
              <tr>
                <th>ID Zona</th>
                <th>Zona Urbana</th>
                <th class="text-center">Parques</th>
                <th class="text-center">Escuelas</th>
                <th class="text-center">Hospitales</th>
                <th class="text-center">Total Puntos</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="zona in datos" :key="zona.zona_urbana_id">
                <td>{{ zona.zona_urbana_id }}</td>
                <td class="font-medium">{{ zona.nombre_zona }}</td>
                <td class="text-center">{{ zona.total_parques || 0 }}</td>
                <td class="text-center">{{ zona.total_escuelas || 0 }}</td>
                <td class="text-center">{{ zona.total_hospitales || 0 }}</td>
                <td class="text-center font-bold">{{ zona.total_puntos_interes || 0 }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import zonasService from '@/services/zonasService';
import api from '@/services/api';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';

const loading = ref(false);
const loadingRefresco = ref(false);
const error = ref(null);
const datos = ref([]);
const datosHospitales = ref([]);

const cargarDatos = async () => {
  loading.value = true;
  error.value = null;
  try {
    // Cargar ambos conjuntos de datos en paralelo
    const [coberturaGeneral, coberturaHospitales] = await Promise.all([
      zonasService.getCoberturaInfraestructura(),
      zonasService.getCoberturaHospitales()
    ]);
    datos.value = coberturaGeneral;
    datosHospitales.value = coberturaHospitales;
    console.log('[CoberturaView] Datos cargados:', {
      infraestructura: coberturaGeneral.length,
      hospitales: coberturaHospitales.length
    });
  } catch (err) {
    console.error('Error al cargar cobertura:', err);
    error.value = err.message || 'Error al obtener datos de cobertura.';
  } finally {
    loading.value = false;
  }
};

const refrescarDatos = async () => {
  loadingRefresco.value = true;
  try {
    await api.post('/zonas/admin/refrescar-vistas');
    await cargarDatos();
    alert('Vista actualizada correctamente');
  } catch (err) {
    console.error('Error al refrescar vista:', err);
    error.value = 'Error al actualizar la vista materializada. Intente más tarde.';
  } finally {
    loadingRefresco.value = false;
  }
};

const getCoberturaClass = (porcentaje) => {
  if (porcentaje >= 80) return 'cobertura-alta';
  if (porcentaje >= 50) return 'cobertura-media';
  if (porcentaje >= 20) return 'cobertura-baja';
  return 'cobertura-critica';
};

const getBadgeClass = (porcentaje) => {
  if (porcentaje >= 80) return 'badge-success';
  if (porcentaje >= 50) return 'badge-warning';
  if (porcentaje >= 20) return 'badge-caution';
  return 'badge-danger';
};

const getCoberturaLabel = (porcentaje) => {
  if (porcentaje >= 80) return 'Excelente';
  if (porcentaje >= 50) return 'Adecuada';
  if (porcentaje >= 20) return 'Limitada';
  return 'Crítica';
};

onMounted(() => {
  cargarDatos();
});
</script>

<style scoped>
.cobertura-container {
  padding: 1.5rem;
  max-width: 1400px;
  margin: 0 auto;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 1.8rem;
  color: var(--text-primary);
}

.subtitle {
  margin: 0;
  color: var(--text-secondary);
  font-size: 1rem;
}

.btn-refresh {
  background: var(--accent-primary);
  color: white;
  border: none;
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  transition: opacity 0.3s;
}

.btn-refresh:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.content {
  display: flex;
  flex-direction: column;
  gap: 2rem;
}

.section {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 1.5rem;
  border: 1px solid var(--border-color);
}

.section-header {
  margin-bottom: 1.5rem;
}

.section-header h2 {
  margin: 0 0 0.5rem 0;
  font-size: 1.3rem;
  color: var(--text-primary);
}

.section-description {
  margin: 0;
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.table-card {
  background: var(--bg-primary);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  border: 1px solid var(--border-color);
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  min-width: 700px;
}

.data-table th,
.data-table td {
  padding: 0.875rem 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-primary);
}

.data-table th {
  background: var(--bg-secondary);
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.85rem;
  text-transform: uppercase;
  white-space: nowrap;
}

.data-table tr:hover {
  background: rgba(0,0,0,0.02);
}

.text-center { text-align: center; }
.font-medium { font-weight: 500; }
.font-bold { font-weight: 700; }

/* Estilos de cobertura */
.cobertura-alta {
  color: #10b981;
  font-weight: 700;
}

.cobertura-media {
  color: #f59e0b;
  font-weight: 700;
}

.cobertura-baja {
  color: #f97316;
  font-weight: 700;
}

.cobertura-critica {
  color: #ef4444;
  font-weight: 700;
}

/* Badges */
.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  font-size: 0.75rem;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-success {
  background: rgba(16, 185, 129, 0.15);
  color: #10b981;
}

.badge-warning {
  background: rgba(245, 158, 11, 0.15);
  color: #f59e0b;
}

.badge-caution {
  background: rgba(249, 115, 22, 0.15);
  color: #f97316;
}

.badge-danger {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
  background: var(--bg-primary);
  border-radius: 8px;
}

/* Responsive */
@media (max-width: 768px) {
  .cobertura-container {
    padding: 1rem;
  }

  .header {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .section {
    padding: 1rem;
  }
}
</style>
