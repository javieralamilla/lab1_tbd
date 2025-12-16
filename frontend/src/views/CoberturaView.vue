<template>
  <div class="cobertura-container">
    <div class="header">
      <div>
        <h1>🏢 Cobertura de Infraestructura</h1>
        <p class="subtitle">
          Análisis semanal de equipamiento urbano (Hospitales, Escuelas, Parques) por zona.
        </p>
      </div>
      <button @click="refrescarDatos" class="btn-refresh" :disabled="loadingRefresco">
        <span v-if="loadingRefresco">⏳ Actualizando...</span>
        <span v-else>🔄 Actualizar Vista Materializada</span>
      </button>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando reporte de cobertura..." />

    <ErrorAlert v-if="error" :message="error" @close="error = null" />

    <div v-if="!loading && !error" class="content">
      <div v-if="datos.length === 0" class="empty-state">
        <p>No hay datos de cobertura disponibles.</p>
      </div>

      <div v-else class="table-card">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID Zona</th>
              <th>Zona Urbana</th>
              <th class="text-center">🌳 Parques</th>
              <th class="text-center">🏫 Escuelas</th>
              <th class="text-center">🏥 Hospitales</th>
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
</template>

<script setup>
import { ref, onMounted } from 'vue';
import zonasService from '@/services/zonasService';
import api from '@/services/api'; // Necesario para llamar al endpoint de refresh si no está en service
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';

const loading = ref(false);
const loadingRefresco = ref(false);
const error = ref(null);
const datos = ref([]);

const cargarDatos = async () => {
  loading.value = true;
  error.value = null;
  try {
    datos.value = await zonasService.getCoberturaInfraestructura();
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
    // Llamada directa al endpoint de refresco administrativo (o vía servicio si existiera)
    // Asumiendo endpoint existente en ZonaUrbanaController: /api/zonas/admin/refrescar-vistas
    await api.post('/zonas/admin/refrescar-vistas');
    await cargarDatos(); // Recargar la tabla
    alert('Vista actualizada correctamente');
  } catch (err) {
    console.error('Error al refrescar vista:', err);
    error.value = 'Error al actualizar la vista materializada. Intente más tarde.';
  } finally {
    loadingRefresco.value = false;
  }
};

onMounted(() => {
  cargarDatos();
});
</script>

<style scoped>
.cobertura-container {
  padding: 1.5rem;
  max-width: 1200px;
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

.table-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color);
  color: var(--text-primary);
}

.data-table th {
  background: var(--bg-primary);
  font-weight: 600;
  color: var(--text-secondary);
  font-size: 0.9rem;
  text-transform: uppercase;
}

.data-table tr:hover {
  background: rgba(0,0,0,0.02);
}

.text-center { text-align: center; }
.font-medium { font-weight: 500; }
.font-bold { font-weight: 700; }

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border-radius: 8px;
}
</style>
