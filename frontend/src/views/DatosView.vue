<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Datos Demográficos</h1>
        <p>Gestión de estadísticas poblacionales por zona urbana</p>
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
        Nuevo Dato Demográfico
      </button>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando datos demográficos..." />

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
          placeholder="Buscar por zona..."
        />
      </div>
      <div class="filter-group">
        <label for="yearFilter">Año:</label>
        <select id="yearFilter" v-model="selectedYear" class="form-select">
          <option value="">Todos los años</option>
          <option v-for="year in availableYears" :key="year" :value="year">
            {{ year }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="!loading && !error" class="table-container">
      <table class="data-table">
        <thead>
        <tr>
          <th @click="sortBy('zona_urbana_id')" class="sortable">
            ID Zona
            <span class="sort-icon" :class="{ active: sortColumn === 'zona_urbana_id' }">
              {{ sortColumn === 'zona_urbana_id' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('año')" class="sortable">
            Año
            <span class="sort-icon" :class="{ active: sortColumn === 'año' }">
              {{ sortColumn === 'año' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('zona_nombre')" class="sortable">
            Nombre
            <span class="sort-icon" :class="{ active: sortColumn === 'zona_nombre' }">
              {{ sortColumn === 'zona_nombre' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('zona_tipo')" class="sortable">
            Tipo
            <span class="sort-icon" :class="{ active: sortColumn === 'zona_tipo' }">
              {{ sortColumn === 'zona_tipo' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('zona_area_km2')" class="sortable">
            Área (km²)
            <span class="sort-icon" :class="{ active: sortColumn === 'zona_area_km2' }">
              {{ sortColumn === 'zona_area_km2' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('poblacion')" class="sortable">
            Población
            <span class="sort-icon" :class="{ active: sortColumn === 'poblacion' }">
              {{ sortColumn === 'poblacion' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('densidad_poblacion')" class="sortable">
            Densidad (hab/km²)
            <span class="sort-icon" :class="{ active: sortColumn === 'densidad_poblacion' }">
              {{ sortColumn === 'densidad_poblacion' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('edad_promedio')" class="sortable">
            Edad Promedio
            <span class="sort-icon" :class="{ active: sortColumn === 'edad_promedio' }">
              {{ sortColumn === 'edad_promedio' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('numero_viviendas')" class="sortable">
            Viviendas
            <span class="sort-icon" :class="{ active: sortColumn === 'numero_viviendas' }">
              {{ sortColumn === 'numero_viviendas' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('factor_personas_vivienda')" class="sortable">
            Factor Pers/Viv
            <span class="sort-icon" :class="{ active: sortColumn === 'factor_personas_vivienda' }">
              {{ sortColumn === 'factor_personas_vivienda' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="dato in filteredDatos" :key="dato.dato_demografico_id">
          <td>{{ dato.zona_urbana_id }}</td>
          <td><strong>{{ dato.año || 2024 }}</strong></td>
          <td>{{ dato.zona_nombre || 'Sin zona' }}</td>
          <td>{{ dato.zona_tipo || 'N/A' }}</td>
          <td>{{ dato.zona_area_km2 ? formatNumber(dato.zona_area_km2) : 'N/A' }}</td>
          <td>{{ formatNumber(dato.poblacion) }}</td>
          <td>{{ dato.densidad_poblacion ? formatNumber(parseFloat(dato.densidad_poblacion).toFixed(2)) : (dato.poblacion && dato.zona_area_km2 ? formatNumber((dato.poblacion / dato.zona_area_km2).toFixed(2)) : 'N/A') }}</td>
          <td>{{ dato.edad_promedio ? parseFloat(dato.edad_promedio).toFixed(1) : 'N/A' }} años</td>
          <td>{{ formatNumber(dato.numero_viviendas) }}</td>
          <td>{{ dato.factor_personas_vivienda ? parseFloat(dato.factor_personas_vivienda).toFixed(2) : 'N/A' }}</td>
          <td>
            <div class="action-buttons">
              <button
                v-if="authStore.isAdmin || authStore.isPlanificador"
                @click="editDato(dato)"
                class="btn-icon btn-edit"
                title="Editar"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"></path>
                  <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"></path>
                </svg>
              </button>
              <button
                v-if="authStore.isAdmin"
                @click="deleteDato(dato)"
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

      <div v-if="filteredDatos.length === 0" class="empty-state">
        <p>No se encontraron datos demográficos</p>
      </div>
    </div>

    <!-- Modal Crear/Editar -->
    <div v-if="showCreateModal || showEditModal" class="modal-overlay" @click.self="cancelEdit">
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ showEditModal ? 'Editar Dato Demográfico' : 'Nuevo Dato Demográfico' }}</h2>
          <button @click="cancelEdit" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="saveDato">
            <div class="form-group" v-if="!showEditModal">
              <label>Zona Urbana *</label>
              <select v-model="editingDato.zona_urbana_id" class="form-control" required>
                <option value="">Seleccione una zona</option>
                <option v-for="zona in zonas" :key="zona.zona_urbana_id" :value="zona.zona_urbana_id">
                  {{ zona.nombre }}
                </option>
              </select>
            </div>

            <div class="form-group">
              <label>Año *</label>
              <input
                v-model.number="editingDato.año"
                type="number"
                class="form-control"
                min="2019"
                max="2030"
                :required="!showEditModal"
                :disabled="showEditModal"
              />
            </div>

            <div class="form-group">
              <label>Población *</label>
              <input
                v-model.number="editingDato.poblacion"
                type="number"
                class="form-control"
                min="0"
                required
              />
            </div>

            <div class="form-group">
              <label>Edad Promedio</label>
              <input
                v-model.number="editingDato.edad_promedio"
                type="number"
                class="form-control"
                step="0.01"
                min="0"
                max="120"
              />
            </div>

            <div class="form-group">
              <label>Número de Viviendas</label>
              <input
                v-model.number="editingDato.numero_viviendas"
                type="number"
                class="form-control"
                min="0"
              />
            </div>

            <div class="form-group">
              <label>Factor Personas por Vivienda</label>
              <input
                v-model.number="editingDato.factor_personas_vivienda"
                type="number"
                class="form-control"
                step="0.01"
                min="0"
              />
            </div>

            <div class="modal-actions">
              <button type="button" @click="cancelEdit" class="btn-secondary">
                Cancelar
              </button>
              <button type="submit" class="btn-primary">
                {{ showEditModal ? 'Guardar Cambios' : 'Crear' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import datosService from '@/services/datosService';
import zonasService from '@/services/zonasService';

const authStore = useAuthStore();
const loading = ref(true);
const error = ref(null);
const datos = ref([]);
const zonas = ref([]);
const searchQuery = ref('');
const selectedYear = ref('');
const showCreateModal = ref(false);
const showEditModal = ref(false);
const editingDato = ref(null);
const sortColumn = ref('zona_nombre');
const sortDirection = ref('asc');

const availableYears = computed(() => {
  const years = [...new Set(datos.value.map(d => d.año).filter(y => y != null))];
  return years.sort((a, b) => b - a); // Ordenar descendente
});

const formatNumber = (num) => {
  return new Intl.NumberFormat('es-CL').format(num);
};

const sortBy = (column) => {
  if (sortColumn.value === column) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortColumn.value = column;
    sortDirection.value = 'asc';
  }
};

const filteredDatos = computed(() => {
  let filtered = datos.value.filter(dato => {
    const matchesSearch = !searchQuery.value || 
      (dato.zona_nombre && dato.zona_nombre.toLowerCase().includes(searchQuery.value.toLowerCase()));
    const matchesYear = !selectedYear.value || dato.año == selectedYear.value;
    return matchesSearch && matchesYear;
  });

  return filtered.sort((a, b) => {
    let aVal = a[sortColumn.value];
    let bVal = b[sortColumn.value];

    if (aVal === null || aVal === undefined) aVal = 0;
    if (bVal === null || bVal === undefined) bVal = 0;

    if (typeof aVal === 'string' && typeof bVal === 'string') {
      aVal = aVal.toLowerCase();
      bVal = bVal.toLowerCase();
      if (sortDirection.value === 'asc') {
        return aVal.localeCompare(bVal);
      } else {
        return bVal.localeCompare(aVal);
      }
    }

    if (sortDirection.value === 'asc') {
      return aVal - bVal;
    } else {
      return bVal - aVal;
    }
  });
});

const loadDatos = async () => {
  loading.value = true;
  error.value = null;
  console.log('[DatosView] Iniciando carga de datos...');
  try {
    datos.value = await datosService.getAll();
    console.log('[DatosView] Datos cargados:', datos.value.length, 'registros');
  } catch (err) {
    console.error('[DatosView] Error al cargar datos:', err);
    error.value = err.message || 'Error al cargar datos demográficos';
  } finally {
    loading.value = false;
  }
};

const loadZonas = async () => {
  try {
    zonas.value = await zonasService.getAll();
  } catch (err) {
    console.error('Error al cargar zonas:', err);
  }
};

const editDato = (dato) => {
  editingDato.value = { ...dato };
  showEditModal.value = true;
};

const saveDato = async () => {
  if (!editingDato.value) return;

  try {
    if (showEditModal.value) {
      // Actualizar
      await datosService.update(editingDato.value.dato_demografico_id, {
        poblacion: editingDato.value.poblacion,
        edad_promedio: editingDato.value.edad_promedio,
        numero_viviendas: editingDato.value.numero_viviendas,
        factor_personas_vivienda: editingDato.value.factor_personas_vivienda
      });
    } else {
      // Crear
      await datosService.create({
        zona_urbana_id: editingDato.value.zona_urbana_id,
        año: editingDato.value.año,
        poblacion: editingDato.value.poblacion,
        edad_promedio: editingDato.value.edad_promedio,
        numero_viviendas: editingDato.value.numero_viviendas,
        factor_personas_vivienda: editingDato.value.factor_personas_vivienda
      });
    }

    showEditModal.value = false;
    showCreateModal.value = false;
    editingDato.value = null;
    await loadDatos();
  } catch (err) {
    error.value = err.message || 'Error al guardar dato demográfico';
  }
};

const cancelEdit = () => {
  showEditModal.value = false;
  showCreateModal.value = false;
  editingDato.value = null;
};

const deleteDato = async (dato) => {
  if (confirm(`¿Estás seguro de eliminar el dato demográfico de ${dato.zona_nombre}?`)) {
    try {
      await datosService.delete(dato.dato_demografico_id);
      await loadDatos();
    } catch (err) {
      error.value = err.message || 'Error al eliminar dato demográfico';
    }
  }
};

onMounted(async () => {
  await Promise.all([loadDatos(), loadZonas()]);
});

// Watch para crear nuevo dato
import { watch } from 'vue';
watch(showCreateModal, (newVal) => {
  if (newVal) {
    editingDato.value = {
      zona_urbana_id: '',
      año: new Date().getFullYear(),
      poblacion: 0,
      edad_promedio: null,
      numero_viviendas: 0,
      factor_personas_vivienda: 3.0
    };
  }
});
</script>

<style scoped>
.page-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
}

.page-header p {
  margin: 0;
  color: var(--text-secondary);
  font-size: 14px;
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  background-color: var(--primary-color);
  color: white;
  border: none;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover {
  background-color: var(--primary-hover);
  transform: translateY(-1px);
}

.filters {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: center;
}

.search-box {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}

.search-box svg {
  position: absolute;
  left: 12px;
  color: var(--text-secondary);
}

.search-box input {
  width: 100%;
  padding: 10px 12px 10px 40px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
}

.filter-group {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 200px;
}

.filter-group label {
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  font-size: 14px;
}

.form-select {
  flex: 1;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.form-select:hover {
  border-color: var(--primary-color);
}

.form-select:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.filter-select {
  padding: 10px 16px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background-color: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 14px;
  cursor: pointer;
  min-width: 200px;
}

.table-container {
  background: var(--bg-secondary);
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
  margin-bottom: 24px;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table thead {
  background-color: var(--bg-primary);
}

.data-table th {
  padding: 16px;
  text-align: left;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.data-table th.sortable {
  cursor: pointer;
  user-select: none;
  transition: all 0.2s;
}

.data-table th.sortable:hover {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.data-table th .sort-icon {
  margin-left: 8px;
  font-size: 16px;
  color: var(--text-secondary);
  font-weight: bold;
  opacity: 0.4;
  transition: all 0.2s;
}

.data-table th .sort-icon.active {
  color: var(--primary-color);
  opacity: 1;
}

.data-table th.sortable:hover .sort-icon {
  opacity: 0.7;
}

.data-table td {
  padding: 16px;
  border-top: 1px solid var(--border-color);
  font-size: 14px;
  color: var(--text-primary);
}

.zona-name {
  font-weight: 600;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-icon {
  padding: 8px;
  border: none;
  background-color: var(--bg-primary);
  border-radius: 6px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
}

.btn-icon:hover {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.btn-icon.btn-edit:hover {
  background-color: var(--status-en-curso-bg);
  color: var(--status-en-curso-border);
}

.btn-icon.btn-delete:hover {
  background-color: var(--accent-danger-bg);
  color: var(--accent-danger);
}

.empty-state {
  padding: 64px 24px;
  text-align: center;
  color: var(--text-secondary);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: var(--bg-secondary);
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-bottom: 1px solid var(--border-color);
}

.modal-header h2 {
  margin: 0;
  font-size: 24px;
  color: var(--text-primary);
}

.btn-close {
  background: none;
  border: none;
  font-size: 32px;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  transition: all 0.2s;
}

.btn-close:hover {
  background-color: var(--border-color);
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 14px;
}

.form-control {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  font-size: 14px;
  transition: all 0.2s;
}

.form-control:focus {
  outline: none;
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid var(--border-color);
}

.btn-secondary {
  padding: 10px 20px;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background-color: var(--border-color);
}
</style>
