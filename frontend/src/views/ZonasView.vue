<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Zonas Urbanas</h1>
        <p>Gestión de zonas geográficas de la ciudad</p>
      </div>
      <button
        v-if="authStore.isAdmin || authStore.isPlanificador"
        @click="showCreateModal = true"
        class="btn-primary"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        Nueva Zona
      </button>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando zonas..." />

    <ErrorAlert
      v-if="error"
      :message="error"
      type="error"
      @close="error = null"
    />

    <div v-if="!loading" class="filters">
      <div class="search-box">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="11" cy="11" r="8"></circle>
          <path d="m21 21-4.35-4.35"></path>
        </svg>
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Buscar zona por nombre..."
        />
      </div>

      <select v-model="filterTipo" class="filter-select">
        <option value="">Todos los tipos</option>
        <option value="Residencial">Residencial</option>
        <option value="Comercial">Comercial</option>
        <option value="Industrial">Industrial</option>
        <option value="Mixto">Mixto</option>
      </select>
    </div>

    <!-- Mapa Interactivo con Leaflet -->
    <div v-if="!loading && !error" class="mapa-container">
      <MapaZonas
        :zonas="zonas"
        :selected-tipo="filterTipo"
        @zona-selected="viewDetails"
      />
      <div class="mapa-leyenda">
        <h4>Tipos de Zonas</h4>
        <div class="leyenda-item residencial">
          <span class="leyenda-color"></span>
          <label>Residencial</label>
        </div>
        <div class="leyenda-item comercial">
          <span class="leyenda-color"></span>
          <label>Comercial</label>
        </div>
        <div class="leyenda-item industrial">
          <span class="leyenda-color"></span>
          <label>Industrial</label>
        </div>
        <div class="leyenda-item mixto">
          <span class="leyenda-color"></span>
          <label>Mixto</label>
        </div>

        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color);">
          <p style="font-size: 12px; color: var(--text-secondary); margin: 0;">
            Haz clic en una zona para ver más detalles
          </p>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error" class="table-container">
      <table class="data-table">
        <thead>
        <tr>
          <th>ID</th>
          <th>Nombre</th>
          <th>Tipo</th>
          <th>Área (km²)</th>
          <th>Población (2025)</th>
          <th>Densidad</th>
          <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="zona in filteredZonas" :key="zona.zona_urbana_id">
          <td>{{ zona.zona_urbana_id }}</td>
          <td class="zona-name">
            {{ zona.nombre }}
          </td>
          <td>
                <span class="type-badge" :class="zona.tipo_zona ? zona.tipo_zona.toLowerCase() : ''">
                  {{ zona.tipo_zona || 'Sin definir' }}
                </span>
          </td>
          <td>{{ formatNumber(zona.area_km2) }}</td>
          <td>{{ formatNumber(zona.poblacion || 0) }}</td>
          <td>{{ formatNumber(zona.densidad_poblacion || 0) }} hab/km²</td>
          <td>
            <div class="action-buttons">
              <button @click="viewDetails(zona)" class="btn-icon" title="Ver detalles">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
              <button
                v-if="authStore.isAdmin || authStore.isPlanificador"
                @click="editZona(zona)"
                class="btn-icon btn-edit"
                title="Editar"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"></path>
                </svg>
              </button>
              <button
                v-if="authStore.isAdmin"
                @click="deleteZona(zona)"
                class="btn-icon btn-delete"
                title="Eliminar"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6"></polyline>
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                </svg>
              </button>
            </div>
          </td>
        </tr>
        </tbody>
      </table>

    </div>

    <div v-if="!loading && zonas.length > 0" class="stats-section">
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import MapaZonas from '@/components/common/MapaZonas.vue';
import zonasService from '@/services/zonasService';

// ... (el resto de tu <script setup> es idéntico y correcto)
const authStore = useAuthStore();
const loading = ref(true);
const error = ref(null);
const zonas = ref([]);
const searchQuery = ref('');
const filterTipo = ref('');
const showCreateModal = ref(false);
const formatNumber = (num) => {
  return new Intl.NumberFormat('es-CL').format(num);
};
const filteredZonas = computed(() => {
  return zonas.value.filter(zona => {
    const matchesSearch = zona.nombre.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesTipo = !filterTipo.value || zona.tipo_zona === filterTipo.value;
    return matchesSearch && matchesTipo;
  });
});
const totalArea = computed(() => {
  return zonas.value.reduce((sum, zona) => sum + (zona.area_km2 || 0), 0);
});
const totalPoblacion = computed(() => {
  return zonas.value.reduce((sum, zona) => sum + (zona.poblacion || 0), 0);
});
const loadZonas = async () => {
  loading.value = true;
  error.value = null;
  try {
    const data = await zonasService.getAll();
    zonas.value = data;
  } catch (err) {
    error.value = err.message || 'Error al cargar zonas';
  } finally {
    loading.value = false;
  }
};
const viewDetails = (zona) => {
  alert(`Ver detalles de: ${zona.nombre}\n\nID: ${zona.zona_urbana_id}\nTipo: ${zona.tipo_zona}\nÁrea: ${zona.area_km2} km²`);
};
const editZona = (zona) => {
  alert(`Editar zona: ${zona.nombre}`);
};
const deleteZona = async (zona) => {
  if (confirm(`¿Estás seguro de eliminar la zona "${zona.nombre}"?`)) {
    try {
      await zonasService.delete(zona.zona_urbana_id);
      await loadZonas();
    } catch (err) {
      error.value = err.message || 'Error al eliminar zona';
    }
  }
};
onMounted(() => {
  loadZonas();
});
</script>

<style scoped>
/* .zonas-page { ... } <-- Eliminado */

.page-container {
  max-width: 1400px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary); /* CAMBIADO */
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 16px;
  color: var(--text-secondary); /* CAMBIADO */
  margin: 0;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: var(--accent-primary); /* CAMBIADO */
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  transform: translateY(-2px);
  background: var(--accent-primary-hover); /* CAMBIADO */
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
}

.search-box {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: var(--bg-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 8px;
}

.search-box svg {
  color: var(--text-secondary); /* CAMBIADO */
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent; /* CAMBIADO */
  color: var(--text-primary); /* CAMBIADO */
}

.search-box input::placeholder {
  color: var(--text-secondary);
}

.filter-select {
  padding: 12px 16px;
  background: var(--bg-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  color: var(--text-primary); /* CAMBIADO */
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.filter-select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

/* Contenedor del Mapa */
.mapa-container {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  margin-bottom: 24px;
}

.mapa-placeholder {
  background: var(--bg-secondary);
  border: 2px dashed var(--border-color);
  border-radius: 12px;
  padding: 60px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  min-height: 400px;
}

.mapa-placeholder svg {
  color: var(--text-secondary);
  margin-bottom: 20px;
  opacity: 0.5;
}

.mapa-placeholder h3 {
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.mapa-placeholder p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0 0 8px 0;
}

.mapa-placeholder .nota-futura {
  font-size: 12px;
  font-style: italic;
  color: var(--accent-primary);
  margin-top: 16px;
}

.mapa-leyenda {
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  height: fit-content;
}

.mapa-leyenda h4 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 16px 0;
}

.leyenda-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.leyenda-color {
  width: 20px;
  height: 20px;
  border-radius: 4px;
  border: 2px solid var(--border-color);
}

.leyenda-item.residencial .leyenda-color { background-color: #3b82f6; }
.leyenda-item.comercial .leyenda-color { background-color: #f59e0b; }
.leyenda-item.industrial .leyenda-color { background-color: #ef4444; }
.leyenda-item.mixto .leyenda-color { background-color: #8b5cf6; }

.leyenda-item input[type="checkbox"] {
  cursor: pointer;
}

.leyenda-item label {
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
}

.table-container {
  background: var(--bg-secondary); /* CAMBIADO */
  border-radius: 12px;
  box-shadow: none; /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  overflow: hidden;
  margin-bottom: 24px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background-color: var(--bg-primary); /* CAMBIADO */
}

.data-table th {
  padding: 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary); /* CAMBIADO */
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.data-table td {
  padding: 16px;
  border-top: 1px solid var(--border-color); /* CAMBIADO */
  font-size: 14px;
  color: var(--text-primary); /* CAMBIADO */
}

.zona-name {
  font-weight: 600;
}

.type-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* Mapeo a colores de estado */
.type-badge.residencial {
  background-color: var(--status-en-curso-bg);
  color: var(--status-en-curso-border);
}

.type-badge.comercial {
  background-color: var(--status-planeado-bg);
  color: var(--status-planeado-border);
}

.type-badge.industrial {
  background-color: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
}

.type-badge.mixto {
  background-color: var(--icon-zonas-bg);
  color: white;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-icon {
  padding: 8px;
  border: none;
  background-color: var(--bg-primary); /* CAMBIADO */
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary); /* CAMBIADO */
  transition: all 0.2s;
}

.btn-icon:hover {
  background-color: var(--border-color); /* CAMBIADO */
  color: var(--text-primary);
}

.btn-icon.btn-edit:hover {
  background-color: var(--status-en-curso-bg); /* CAMBIADO */
  color: var(--status-en-curso-border);
}

.btn-icon.btn-delete:hover {
  background-color: var(--accent-danger-bg); /* CAMBIADO */
  color: var(--accent-danger);
}

.empty-state {
  padding: 64px 24px;
  text-align: center;
  color: var(--text-secondary); /* CAMBIADO */
}

/* ... */

.stats-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.stat-box {
  background: var(--bg-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 12px;
  padding: 20px;
  box-shadow: none; /* CAMBIADO */
}

.stat-label {
  font-size: 12px;
  color: var(--text-secondary); /* CAMBIADO */
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary); /* CAMBIADO */
}

@media (max-width: 1024px) {
  .mapa-container {
    grid-template-columns: 1fr;
  }

  .mapa-leyenda {
    order: -1;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .filters {
    flex-direction: column;
  }

  .mapa-placeholder {
    padding: 40px 20px;
    min-height: 300px;
  }

  .data-table {
    font-size: 12px;
  }

  .data-table th,
  .data-table td {
    padding: 12px 8px;
  }
}
</style>
