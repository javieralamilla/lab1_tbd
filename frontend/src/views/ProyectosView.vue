<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Proyectos Urbanos</h1>
        <p>Gestión de proyectos de desarrollo de la ciudad</p>
      </div>
      <button
        v-if="authStore.isAdmin || authStore.isPlanificador"
        @click="createProject"
        class="btn-primary"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"></line>
          <line x1="5" y1="12" x2="19" y2="12"></line>
        </svg>
        Nuevo Proyecto
      </button>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando proyectos..." />

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
          placeholder="Buscar proyecto..."
        />
      </div>

      <select v-model="filterEstado" class="filter-select">
        <option value="">Todos los estados</option>
        <option value="Planeado">Planeado</option>
        <option value="En Curso">En Curso</option>
        <option value="Completado">Completado</option>
        <option value="Retrasado">Retrasado</option>
        <option value="Cancelado">Cancelado</option>
      </select>
    </div>

    <!-- Mapa Interactivo de Proyectos -->
    <div v-if="!loading && !error" class="mapa-container">
      <MapaProyectos
        :proyectos="proyectos"
        :selected-estado="filterEstado"
        @proyecto-selected="viewProject"
      />
      <div class="mapa-leyenda">
        <h4>Estados de Proyectos</h4>
        <div class="leyenda-item planeado">
          <span class="leyenda-color">📋</span>
          <label>Planeado</label>
        </div>
        <div class="leyenda-item en-curso">
          <span class="leyenda-color">🚧</span>
          <label>En Curso</label>
        </div>
        <div class="leyenda-item completado">
          <span class="leyenda-color">✅</span>
          <label>Completado</label>
        </div>
        <div class="leyenda-item retrasado">
          <span class="leyenda-color">⚠️</span>
          <label>Retrasado</label>
        </div>
        <div class="leyenda-item cancelado">
          <span class="leyenda-color">❌</span>
          <label>Cancelado</label>
        </div>

        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color);">
          <p style="font-size: 12px; color: var(--text-secondary); margin: 0;">
            Haz clic en un proyecto en el mapa para ver más detalles
          </p>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error" class="projects-grid">
      <div
        v-for="proyecto in filteredProyectos"
        :key="proyecto.proyecto_urbano_id"
        class="project-card"
      >
        <div class="project-header">
          <h3>{{ proyecto.nombre }}</h3>
          <span class="status-badge" :class="getStatusClass(proyecto.estado)">
              {{ proyecto.estado }}
            </span>
        </div>

        <p class="project-description">{{ proyecto.descripcion }}</p>

        <div class="project-details">
          <div class="detail-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
              <line x1="16" y1="2" x2="16" y2="6"></line>
              <line x1="8" y1="2" x2="8" y2="6"></line>
              <line x1="3" y1="10" x2="21" y2="10"></line>
            </svg>
            <span>{{ formatDate(proyecto.fecha_inicio) }}</span>
          </div>

          <div class="detail-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="12" y1="1" x2="12" y2="23"></line>
              <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"></path>
            </svg>
            <span>{{ formatCurrency(proyecto.presupuesto) }}</span>
          </div>

          <div v-if="proyecto.tipo_proyecto" class="detail-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            </svg>
            <span>{{ proyecto.tipo_proyecto }}</span>
          </div>
        </div>

        <div class="project-actions">
          <button @click="viewProject(proyecto)" class="btn-secondary">
            Ver Detalles
          </button>
          <button
            v-if="authStore.isAdmin || authStore.isPlanificador"
            @click="editProject(proyecto)"
            class="btn-icon"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 3a2.828 2.828 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5L17 3z"></path>
            </svg>
          </button>
        </div>
      </div>

      <div v-if="filteredProyectos.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
          <polyline points="14 2 14 8 20 8"></polyline>
        </svg>
        <h3>No se encontraron proyectos</h3>
        <p>Intenta ajustar los filtros de búsqueda</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import MapaProyectos from '@/components/common/MapaProyectos.vue';
import proyectosService from '@/services/proyectosService';

// ... (el resto de tu <script setup> es idéntico y correcto)
const authStore = useAuthStore();
const loading = ref(true);
const error = ref(null);
const proyectos = ref([]);
const searchQuery = ref('');
const filterEstado = ref('');
const filteredProyectos = computed(() => {
  return proyectos.value.filter(proyecto => {
    const matchesSearch = proyecto.nombre.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
      (proyecto.descripcion && proyecto.descripcion.toLowerCase().includes(searchQuery.value.toLowerCase()));
    const matchesEstado = !filterEstado.value || proyecto.estado === filterEstado.value;
    return matchesSearch && matchesEstado;
  });
});
const getStatusClass = (estado) => {
  const classes = {
    'Planeado': 'planeado',
    'En Curso': 'en-curso',
    'Completado': 'completado',
    'Retrasado': 'retrasado',
    'Cancelado': 'cancelado'
  };
  return classes[estado] || '';
};
const formatDate = (dateString) => {
  if (!dateString) return 'Sin fecha';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-CL', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};
const formatCurrency = (amount) => {
  if (!amount) return 'Sin presupuesto';
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0
  }).format(amount);
};
const loadProyectos = async () => {
  loading.value = true;
  error.value = null;

  try {
    const data = await proyectosService.getAll();
    proyectos.value = data;
  } catch (err) {
    error.value = err.message || 'Error al cargar proyectos';
  } finally {
    loading.value = false;
  }
};
const viewProject = (proyecto) => {
  alert(`Ver detalles de: ${proyecto.nombre}`);
};
const editProject = (proyecto) => {
  alert(`Editar proyecto: ${proyecto.nombre}`);
};
const createProject = () => {
  alert('Crear nuevo proyecto');
};
onMounted(() => {
  loadProyectos();
});
</script>

<style scoped>
/* .proyectos-page { ... } <-- Eliminado, no es necesario */

.page-container {
  /* max-width: 1400px;
  /* margin: 0 auto; <-- Esto ya lo maneja AppLayout.vue */
  /* padding: 32px 24px; <-- Esto ya lo maneja AppLayout.vue */
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

/* Contenedor del Mapa de Proyectos */
.mapa-container {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 20px;
  margin-bottom: 24px;
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
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.leyenda-item label {
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
}

/* Colores de estados para la leyenda */
.leyenda-item.planeado label { color: #3b82f6; }
.leyenda-item.en-curso label { color: #f59e0b; }
.leyenda-item.completado label { color: #10b981; }
.leyenda-item.retrasado label { color: #ef4444; }
.leyenda-item.cancelado label { color: #6b7280; }

.projects-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
}

.project-card {
  background: var(--bg-secondary); /* CAMBIADO */
  border-radius: 12px;
  padding: 24px;
  box-shadow: none; /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  transition: transform 0.2s, box-shadow 0.2s;
}

.project-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 12px;
}

.project-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary); /* CAMBIADO */
  margin: 0;
  flex: 1;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  white-space: nowrap;
  color: var(--text-primary); /* CAMBIADO */
}

.status-badge.planeado {
  background-color: var(--status-planeado-bg); /* CAMBIADO */
  color: var(--status-planeado-border);
}

.status-badge.en-curso {
  background-color: var(--status-en-curso-bg); /* CAMBIADO */
  color: var(--status-en-curso-border);
}

.status-badge.completado {
  background-color: var(--status-completado-bg); /* CAMBIADO */
  color: var(--status-completado-border);
}

.status-badge.retrasado {
  background-color: var(--status-retrasado-bg); /* CAMBIADO */
  color: var(--status-retrasado-border);
}

.status-badge.cancelado {
  background-color: var(--border-color); /* CAMBIADO */
  color: var(--text-secondary);
}

.project-description {
  font-size: 14px;
  color: var(--text-secondary); /* CAMBIADO */
  margin-bottom: 16px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-details {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color); /* CAMBIADO */
}

.detail-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary); /* CAMBIADO */
}

.detail-item svg {
  flex-shrink: 0;
}

.project-actions {
  display: flex;
  gap: 8px;
}

.btn-secondary {
  flex: 1;
  padding: 10px 16px;
  background-color: var(--bg-primary); /* CAMBIADO */
  color: var(--text-secondary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background-color: var(--border-color); /* CAMBIADO */
  color: var(--text-primary);
}

.btn-icon {
  padding: 10px;
  background-color: var(--bg-primary); /* CAMBIADO */
  border: 1px solid var(--border-color); /* CAMBIADO */
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary); /* CAMBIADO */
  transition: all 0.2s;
}

.btn-icon:hover {
  background-color: var(--status-en-curso-bg); /* CAMBIADO */
  border-color: var(--status-en-curso-border); /* CAMBIADO */
  color: var(--status-en-curso-border); /* CAMBIADO */
}

.empty-state {
  grid-column: 1 / -1;
  padding: 64px 24px;
  text-align: center;
  color: var(--text-secondary); /* CAMBIADO */
}

/* Responsive */
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

  .projects-grid {
    grid-template-columns: 1fr;
  }

  .project-card {
    padding: 20px;
  }
}

/* ... (el resto de tus estilos es correcto) ... */
</style>
