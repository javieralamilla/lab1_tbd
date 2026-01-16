<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Zonas Urbanas</h1>
        <p>Gestión de zonas geográficas de la ciudad</p>
      </div>
      
      <!-- Botón solo visible si tiene permiso de crear -->
      <button
        v-if="permissions.canCreate"
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

    <!-- Mensaje para usuarios con solo lectura -->
    <div v-if="permissions.isReadOnly" class="alert alert-info">
      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="16" x2="12" y2="12"></line>
        <line x1="12" y1="8" x2="12.01" y2="8"></line>
      </svg>
      Solo tienes permisos de lectura en este módulo
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
          <th @click="sortBy('zona_urbana_id')" class="sortable">
            ID
            <span class="sort-icon" :class="{ active: sortColumn === 'zona_urbana_id' }">
              {{ sortColumn === 'zona_urbana_id' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('nombre')" class="sortable">
            Nombre
            <span class="sort-icon" :class="{ active: sortColumn === 'nombre' }">
              {{ sortColumn === 'nombre' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('tipo_zona')" class="sortable">
            Tipo
            <span class="sort-icon" :class="{ active: sortColumn === 'tipo_zona' }">
              {{ sortColumn === 'tipo_zona' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th @click="sortBy('area_km2')" class="sortable">
            Área (km²)
            <span class="sort-icon" :class="{ active: sortColumn === 'area_km2' }">
              {{ sortColumn === 'area_km2' && sortDirection === 'desc' ? '↓' : '↑' }}
            </span>
          </th>
          <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="zona in filteredZonas" :key="zona.zona_urbana_id">
          <td>{{ zona.zona_urbana_id }}</td>
          <td class="zona-name">
            {{ zona.nombre }}
          </td>
          <td>{{ zona.tipo_zona || 'Sin definir' }}</td>
          <td>{{ formatNumber(zona.area_km2) }}</td>
          <td>
            <div class="action-buttons">
              <button @click="viewDetails(zona)" class="btn-icon" title="Ver detalles">
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path>
                  <circle cx="12" cy="12" r="3"></circle>
                </svg>
              </button>
              
              <!-- Botón editar - Solo si tiene permiso de actualizar -->
              <button
                v-if="permissions.canUpdate"
                @click="editZona(zona)"
                class="btn-icon btn-edit"
                title="Editar"
              >
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"></path>
                </svg>
              </button>
              
              <!-- Botón eliminar - Solo si tiene permiso de eliminar -->
              <button
                v-if="permissions.canDelete"
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

    <!-- Modal de Edición -->
    <div v-if="showEditModal" class="modal-overlay" @click.self="cancelEdit">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Editar Zona Urbana</h2>
          <button @click="cancelEdit" class="btn-close">×</button>
        </div>
        
        <div class="modal-body" v-if="editingZona">
          <div class="form-group">
            <label>ID</label>
            <input type="text" :value="editingZona.zona_urbana_id" disabled class="form-control-disabled" />
          </div>

          <div class="form-group">
            <label>Nombre</label>
            <input type="text" v-model="editingZona.nombre" class="form-control" />
          </div>

          <div class="form-group">
            <label>Tipo de Zona</label>
            <select v-model="editingZona.tipo_zona" class="form-control">
              <option value="Residencial">Residencial</option>
              <option value="Comercial">Comercial</option>
              <option value="Industrial">Industrial</option>
              <option value="Mixto">Mixto</option>
            </select>
          </div>

          <div class="form-group">
            <label>Área (km²)</label>
            <input type="number" step="0.1" v-model="editingZona.area_km2" class="form-control" />
          </div>
        </div>

        <div class="modal-footer">
          <button @click="cancelEdit" class="btn-secondary">Cancelar</button>
          <button @click="saveZona" class="btn-primary">Guardar Cambios</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { usePermissions } from '@/composables/usePermissions';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import MapaZonas from '@/components/common/MapaZonas.vue';
import zonasService from '@/services/zonasService';

// Permisos y autenticación
const authStore = useAuthStore();
const { permissions, user } = usePermissions('zonas');

// ... (el resto de tu <script setup> es idéntico y correcto)
const loading = ref(true);
const error = ref(null);
const zonas = ref([]);
const searchQuery = ref('');
const filterTipo = ref('');
const showCreateModal = ref(false);
const sortColumn = ref('nombre');
const sortDirection = ref('asc');
const formatNumber = (num) => {
  return new Intl.NumberFormat('es-CL').format(num);
};

const getTipoClass = (tipoZona) => {
  if (!tipoZona) return '';
  return tipoZona.toLowerCase().trim().replace(/\s+/g, '-');
};

const getBadgeStyle = (tipoZona) => {
  const baseStyle = {
    display: 'inline-block',
    padding: '6px 14px',
    fontSize: '12px',
    fontWeight: '600',
    textTransform: 'capitalize'
  };
  
  const tipo = tipoZona?.toLowerCase().trim();
  
  if (tipo === 'residencial') {
    return { ...baseStyle, backgroundColor: '#dbeafe', color: '#1e40af' };
  } else if (tipo === 'comercial') {
    return { ...baseStyle, backgroundColor: '#fef3c7', color: '#92400e' };
  } else if (tipo === 'industrial') {
    return { ...baseStyle, backgroundColor: '#fee2e2', color: '#991b1b' };
  } else if (tipo === 'mixto') {
    return { ...baseStyle, backgroundColor: '#8b5cf6', color: 'white' };
  }
  
  return baseStyle;
};

const sortBy = (column) => {
  if (sortColumn.value === column) {
    sortDirection.value = sortDirection.value === 'asc' ? 'desc' : 'asc';
  } else {
    sortColumn.value = column;
    sortDirection.value = 'asc';
  }
};

const filteredZonas = computed(() => {
  const filtered = zonas.value.filter(zona => {
    const matchesSearch = zona.nombre.toLowerCase().includes(searchQuery.value.toLowerCase());
    const matchesTipo = !filterTipo.value || zona.tipo_zona === filterTipo.value;
    return matchesSearch && matchesTipo;
  });

  // Ordenar
  return filtered.sort((a, b) => {
    let aVal = a[sortColumn.value];
    let bVal = b[sortColumn.value];

    // Manejar valores nulos
    if (aVal === null || aVal === undefined) aVal = 0;
    if (bVal === null || bVal === undefined) bVal = 0;

    // Comparar strings (para nombre y tipo_zona)
    if (typeof aVal === 'string' && typeof bVal === 'string') {
      aVal = aVal.toLowerCase();
      bVal = bVal.toLowerCase();
      if (sortDirection.value === 'asc') {
        return aVal.localeCompare(bVal);
      } else {
        return bVal.localeCompare(aVal);
      }
    }

    // Comparar números
    if (sortDirection.value === 'asc') {
      return aVal - bVal;
    } else {
      return bVal - aVal;
    }
  });
});
const totalArea = computed(() => {
  return zonas.value.reduce((sum, zona) => sum + (zona.area_km2 || 0), 0);
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
  // Mostrar detalles en el mapa o en un modal
  console.log('Ver detalles de:', zona);
};

const showEditModal = ref(false);
const editingZona = ref(null);

const editZona = (zona) => {
  editingZona.value = { ...zona };
  showEditModal.value = true;
};

const saveZona = async () => {
  if (!editingZona.value) return;
  
  try {
    // Preparar datos para enviar al backend
    const zonaData = {
      zona_urbana_id: editingZona.value.zona_urbana_id,
      nombre: editingZona.value.nombre,
      tipo_zona: editingZona.value.tipo_zona,
      area_km2: parseFloat(editingZona.value.area_km2)
    };
    
    console.log('Enviando datos al backend:', zonaData);
    await zonasService.update(editingZona.value.zona_urbana_id, zonaData);
    
    showEditModal.value = false;
    editingZona.value = null;
    
    // Recargar datos
    await loadZonas();
    console.log('Zona actualizada exitosamente');
  } catch (err) {
    console.error('Error al actualizar zona:', err);
    error.value = err.message || 'Error al actualizar zona';
  }
};

const cancelEdit = () => {
  showEditModal.value = false;
  editingZona.value = null;
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
</style>

<style>
/* Estilos sin scope para los badges */
.data-table .type-badge {
  display: inline-block !important;
  padding: 6px 14px !important;
  border-radius: 16px !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: capitalize !important;
}

.data-table .type-badge.residencial {
  background-color: #dbeafe !important;
  color: #1e40af !important;
}

.data-table .type-badge.comercial {
  background-color: #fef3c7 !important;
  color: #92400e !important;
}

.data-table .type-badge.industrial {
  background-color: #fee2e2 !important;
  color: #991b1b !important;
}

.data-table .type-badge.mixto {
  background-color: #8b5cf6 !important;
  color: white !important;
}
</style>

<style scoped>

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
  border-top: 1px solid var(--border-color); /* CAMBIADO */
  font-size: 14px;
  color: var(--text-primary); /* CAMBIADO */
}

.data-table .type-badge {
  display: inline-block !important;
  padding: 6px 14px !important;
  border-radius: 16px !important;
  font-size: 12px !important;
  font-weight: 600 !important;
  text-transform: capitalize !important;
  box-sizing: border-box !important;
}

.zona-name {
  font-weight: 600;
}

.type-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 16px;
  font-size: 12px;
  font-weight: 600;
  text-transform: capitalize;
}

/* Mapeo a colores de estado */
.data-table .type-badge.residencial,
.type-badge.residencial {
  background-color: var(--status-en-curso-bg) !important;
  color: var(--status-en-curso-border) !important;
  border-radius: 16px !important;
}

.data-table .type-badge.comercial,
.type-badge.comercial {
  background-color: var(--status-planeado-bg) !important;
  color: var(--status-planeado-border) !important;
  border-radius: 16px !important;
}

.data-table .type-badge.industrial,
.type-badge.industrial {
  background-color: var(--status-retrasado-bg) !important;
  color: var(--status-retrasado-border) !important;
  border-radius: 16px !important;
}

.data-table .type-badge.mixto,
.type-badge.mixto {
  background-color: var(--icon-zonas-bg) !important;
  color: white !important;
  border-radius: 16px !important;
  display: inline-block !important;
  padding: 6px 14px !important;
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

/* Modal Styles */
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
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.form-control-disabled {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background-color: var(--bg-tertiary);
  color: var(--text-secondary);
  font-size: 14px;
  cursor: not-allowed;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 24px;
  border-top: 1px solid var(--border-color);
}

.btn-secondary {
  padding: 10px 20px;
  border: 1px solid var(--border-color);
  background-color: var(--bg-primary);
  color: var(--text-primary);
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background-color: var(--border-color);
}

/* Alert de solo lectura */
.alert {
  padding: 14px 18px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.alert-info {
  background-color: #dbeafe;
  color: #1e40af;
  border: 1px solid #93c5fd;
}

.alert svg {
  flex-shrink: 0;
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

  .modal-content {
    width: 95%;
    max-height: 95vh;
  }
}
</style>
