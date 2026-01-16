<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Mapa Integrado</h1>
        <p>Visualización de múltiples capas de información superpuestas</p>
      </div>
      <div class="header-info">
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="16" x2="12" y2="12"></line>
          <line x1="12" y1="8" x2="12.01" y2="8"></line>
        </svg>
        <span>Activa/desactiva capas para personalizar tu visualización</span>
      </div>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando datos del mapa..." />

    <ErrorAlert
      v-if="error"
      :message="error"
      type="error"
      @close="error = null"
    />

    <div v-if="!loading && !error" class="mapa-section">
      <MapaIntegrado
        :proyectos="proyectos"
        :puntos="puntos"
        :zonas="zonas"
        @item-selected="handleItemSelected"
      />
    </div>

    <!-- Modal de detalles del item seleccionado -->
    <div v-if="itemSeleccionado" class="modal-overlay" @click="itemSeleccionado = null">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h2>
            <span v-if="itemSeleccionado.tipo === 'proyecto'">Proyecto</span>
            <span v-else-if="itemSeleccionado.tipo === 'punto'">Punto de Interés</span>
            <span v-else>🏘️ Zona Urbana</span>
          </h2>
          <button class="btn-close" @click="itemSeleccionado = null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>
        <div class="modal-body">
          <!-- Detalles Proyecto -->
          <div v-if="itemSeleccionado.tipo === 'proyecto'">
            <h3>{{ itemSeleccionado.data.nombre }}</h3>
            <div class="detail-row">
              <span class="label">Estado:</span>
              <span class="status-badge" :class="getStatusClass(itemSeleccionado.data.estado)">
                {{ itemSeleccionado.data.estado }}
              </span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.descripcion">
              <span class="label">Descripción:</span>
              <span>{{ itemSeleccionado.data.descripcion }}</span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.presupuesto">
              <span class="label">Presupuesto:</span>
              <span>${{ itemSeleccionado.data.presupuesto.toLocaleString() }}</span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.fechaInicio || itemSeleccionado.data.fecha_inicio">
              <span class="label">Fecha Inicio:</span>
              <span>{{ formatDate(itemSeleccionado.data.fechaInicio || itemSeleccionado.data.fecha_inicio) }}</span>
            </div>
          </div>

          <!-- Detalles Punto -->
          <div v-else-if="itemSeleccionado.tipo === 'punto'">
            <h3>{{ itemSeleccionado.data.nombre }}</h3>
            <div class="detail-row">
              <span class="label">Tipo:</span>
              <span class="type-badge">{{ getTipoString(itemSeleccionado.data.tipo) }}</span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.direccion">
              <span class="label">Dirección:</span>
              <span>{{ itemSeleccionado.data.direccion }}</span>
            </div>
          </div>

          <!-- Detalles Zona -->
          <div v-else>
            <h3>{{ itemSeleccionado.data.nombre }}</h3>
            <div class="detail-row">
              <span class="label">Tipo:</span>
              <span class="type-badge">{{ getTipoString(itemSeleccionado.data.tipo) }}</span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.area">
              <span class="label">Área:</span>
              <span>{{ itemSeleccionado.data.area.toFixed(2) }} km²</span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.poblacion_estimada_2025">
              <span class="label">Población (2025):</span>
              <span>{{ itemSeleccionado.data.poblacion_estimada_2025.toLocaleString() }}</span>
            </div>
            <div class="detail-row" v-if="itemSeleccionado.data.densidad_poblacional">
              <span class="label">Densidad:</span>
              <span>{{ itemSeleccionado.data.densidad_poblacional.toFixed(0) }} hab/km²</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import MapaIntegrado from '@/components/common/MapaIntegrado.vue';
import proyectosService from '@/services/proyectosService';
import puntosInteresService from '@/services/puntosInteresService';
import zonasService from '@/services/zonasService';

const loading = ref(true);
const error = ref(null);
const proyectos = ref([]);
const puntos = ref([]);
const zonas = ref([]);
const itemSeleccionado = ref(null);

const loadAllData = async () => {
  loading.value = true;
  error.value = null;

  try {
    console.log('[MapaIntegrado] Iniciando carga de datos...');
    console.log('[MapaIntegrado] Token:', localStorage.getItem('auth_token') ? 'Presente' : 'Ausente');

    const [proyectosData, puntosData, zonasData] = await Promise.all([
      proyectosService.getAll().catch(err => {
        console.error('[MapaIntegrado] Error al cargar proyectos:', err);
        throw new Error('Error al cargar proyectos: ' + (err.message || 'Error desconocido'));
      }),
      puntosInteresService.getAll().catch(err => {
        console.error('[MapaIntegrado] Error al cargar puntos:', err);
        throw new Error('Error al cargar puntos de interés: ' + (err.message || 'Error desconocido'));
      }),
      zonasService.getAll().catch(err => {
        console.error('[MapaIntegrado] Error al cargar zonas:', err);
        throw new Error('Error al cargar zonas: ' + (err.message || 'Error desconocido'));
      })
    ]);

    console.log('[MapaIntegrado] Datos cargados exitosamente:');
    console.log('- Proyectos:', proyectosData?.length || 0, proyectosData);
    console.log('- Puntos:', puntosData?.length || 0, puntosData);
    console.log('- Zonas:', zonasData?.length || 0, zonasData);

    proyectos.value = Array.isArray(proyectosData) ? proyectosData : [];
    puntos.value = Array.isArray(puntosData) ? puntosData : [];
    zonas.value = Array.isArray(zonasData) ? zonasData : [];

    console.log('[MapaIntegrado] Datos asignados a refs:', {
      proyectos: proyectos.value.length,
      puntos: puntos.value.length,
      zonas: zonas.value.length
    });

    if (proyectos.value.length === 0 && puntos.value.length === 0 && zonas.value.length === 0) {
      error.value = 'No hay datos disponibles para mostrar en el mapa. Verifica que el backend tenga datos cargados.';
    }
  } catch (err) {
    console.error('[MapaIntegrado] Error al cargar datos:', err);
    error.value = err.message || 'Error al cargar los datos del mapa. Verifica que estés autenticado y el backend esté funcionando.';
  } finally {
    loading.value = false;
  }
};

const handleItemSelected = (item) => {
  itemSeleccionado.value = item;
};

const getStatusClass = (estado) => {
  const map = {
    'Planeado': 'planeado',
    'En Curso': 'en-curso',
    'Completado': 'completado',
    'Retrasado': 'retrasado',
    'Cancelado': 'cancelado'
  };
  return map[estado] || 'planeado';
};

const getTipoString = (tipo) => {
  if (!tipo) return 'Desconocido';

  if (typeof tipo === 'string') {
    const enumMap = {
      'HOSPITAL': 'Hospital',
      'ESCUELA': 'Escuela',
      'PARQUE': 'Parque',
      'CENTRO_COMERCIAL': 'Centro Comercial',
      'TRANSPORTE': 'Transporte',
      'RESIDENCIAL': 'Residencial',
      'COMERCIAL': 'Comercial',
      'INDUSTRIAL': 'Industrial',
      'MIXTO': 'Mixto'
    };

    if (enumMap[tipo]) return enumMap[tipo];
    return tipo;
  }

  if (typeof tipo === 'object') {
    if (tipo.valor) return tipo.valor;
    if (tipo.name) return tipo.name;
  }

  return String(tipo);
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-ES', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

onMounted(() => {
  loadAllData();
});
</script>

<style scoped>
.page-container {
  max-width: 1600px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.page-header p {
  font-size: 16px;
  color: var(--text-secondary);
  margin: 0 0 12px 0;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: var(--icon-zonas-bg);
  border-radius: 8px;
  font-size: 14px;
  color: var(--text-primary);
  margin-top: 16px;
}

.header-info svg {
  flex-shrink: 0;
  color: var(--accent-primary);
}

.mapa-section {
  margin-bottom: 32px;
}

.info-section {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.info-card {
  display: flex;
  gap: 16px;
  padding: 24px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.info-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: var(--icon-zonas-bg);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--accent-primary);
}

.info-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.info-content p {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
  line-height: 1.6;
}

/* Modal */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-content {
  background: var(--bg-secondary);
  border-radius: 16px;
  max-width: 600px;
  width: 100%;
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
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
}

.btn-close {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s, color 0.2s;
}

.btn-close:hover {
  background: var(--bg-primary);
  color: var(--text-primary);
}

.modal-body {
  padding: 24px;
}

.modal-body h3 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid var(--border-color);
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-row .label {
  font-weight: 600;
  color: var(--text-secondary);
  flex-shrink: 0;
  margin-right: 16px;
}

.status-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
}

.status-badge.planeado {
  background-color: var(--status-planeado-bg);
  color: var(--status-planeado-border);
}

.status-badge.en-curso {
  background-color: var(--status-en-curso-bg);
  color: var(--status-en-curso-border);
}

.status-badge.completado {
  background-color: var(--status-completado-bg);
  color: var(--status-completado-border);
}

.status-badge.retrasado {
  background-color: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
}

.status-badge.cancelado {
  background-color: #f3f4f6;
  color: #6b7280;
}

.type-badge {
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  background-color: var(--icon-zonas-bg);
  color: var(--accent-primary);
}

@media (max-width: 768px) {
  .info-section {
    grid-template-columns: 1fr;
  }

  .modal-content {
    max-width: 100%;
  }

  .detail-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>

