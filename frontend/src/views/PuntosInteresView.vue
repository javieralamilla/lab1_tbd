<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h1>Puntos de Interés</h1>
        <p>Ubicaciones de infraestructura clave en la ciudad</p>
      </div>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando puntos de interés..." />

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
          placeholder="Buscar punto de interés..."
        />
      </div>

      <select v-model="filterTipo" class="filter-select">
        <option value="">Todos los tipos</option>
        <option value="Hospital">Hospital</option>
        <option value="Escuela">Escuela</option>
        <option value="Parque">Parque</option>
        <option value="Centro Comercial">Centro Comercial</option>
        <option value="Transporte">Transporte</option>
        <option value="Centro Cultural">Centro Cultural</option>
      </select>
    </div>

    <div v-if="!loading && !error" class="mapa-container">
      <MapaPuntos
        :puntos="puntos"
        :selected-tipo="filterTipo"
        @punto-selected="handlePuntoSelected"
      />
      <div class="mapa-leyenda">
        <h4>Tipos de Puntos</h4>
        <div class="leyenda-item hospital">
          <span class="leyenda-color"></span>
          <label>Hospitales</label>
        </div>
        <div class="leyenda-item escuela">
          <span class="leyenda-color"></span>
          <label>Escuelas</label>
        </div>
        <div class="leyenda-item parque">
          <span class="leyenda-color"></span>
          <label>Parques</label>
        </div>
        <div class="leyenda-item centro-comercial">
          <span class="leyenda-color"></span>
          <label>Centros Comerciales</label>
        </div>
        <div class="leyenda-item transporte">
          <span class="leyenda-color"></span>
          <label>Transporte</label>
        </div>
        <div class="leyenda-item centro-cultural">
          <span class="leyenda-color"></span>
          <label>Centros Culturales</label>
        </div>

        <div style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color);">
          <p style="font-size: 12px; color: var(--text-secondary); margin: 0;">
            Haz clic en un marcador para ver más detalles
          </p>
        </div>
      </div>
    </div>

    <div v-if="!loading && !error" class="points-grid">
      <div
        v-for="punto in filteredPuntos"
        :key="punto.punto_interes_id"
        class="point-card"
        :class="getTipoString(punto.tipo).toLowerCase().replace(' ', '-')"
      >
        <div class="point-icon">
          <component :is="getIconForType(getTipoString(punto.tipo))" />
        </div>

        <div class="point-info">
          <h3>{{ punto.nombre }}</h3>
          <div class="point-type">
            <span class="type-badge" :class="getTipoString(punto.tipo).toLowerCase().replace(' ', '-')">
              {{ getTipoString(punto.tipo) }}
            </span>
          </div>
          <p v-if="punto.direccion" class="point-address">
            <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            {{ punto.direccion }}
          </p>
        </div>
      </div>

      <div v-if="filteredPuntos.length === 0" class="empty-state">
        <svg xmlns="http://www.w3.org/2000/svg" width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
          <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
          <circle cx="12" cy="10" r="3"></circle>
        </svg>
        <h3>No se encontraron puntos de interés</h3>
        <p>Intenta ajustar los filtros de búsqueda</p>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, h } from 'vue';
import { useAuthStore } from '@/stores/auth';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import MapaPuntos from '@/components/common/MapaPuntos.vue';
import puntosInteresService from '@/services/puntosInteresService';

const authStore = useAuthStore();
const loading = ref(true);
const error = ref(null);
const puntos = ref([]);
const searchQuery = ref('');
const filterTipo = ref('');

// Función helper para obtener el string del tipo
const getTipoString = (tipo) => {
  if (!tipo) return 'Desconocido';

  if (typeof tipo === 'string') {
    const enumMap = {
      'HOSPITAL': 'Hospital',
      'ESCUELA': 'Escuela',
      'PARQUE': 'Parque',
      'CENTRO_COMERCIAL': 'Centro Comercial',
      'TRANSPORTE': 'Transporte',
      'CENTRO_CULTURAL': 'Centro Cultural'
    };

    if (enumMap[tipo]) {
      return enumMap[tipo];
    }

    return tipo;
  }

  if (typeof tipo === 'object') {
    if (tipo.valor) return tipo.valor;
    if (tipo.name) return tipo.name;

    if (tipo.toString && tipo.toString() !== '[object Object]') {
      return tipo.toString();
    }
  }

  return String(tipo);
};

const filteredPuntos = computed(() => {
  return puntos.value.filter(punto => {
    const nombreMatch = punto.nombre.toLowerCase().includes(searchQuery.value.toLowerCase());
    const tipoString = getTipoString(punto.tipo);
    const tipoMatch = !filterTipo.value || tipoString === filterTipo.value;

    return nombreMatch && tipoMatch;
  });
});

const getIconForType = (tipo) => {
  const icons = {
    'Hospital': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '28',
      height: '28',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M12 2v20' }),
      h('path', { d: 'M2 12h20' })
    ]),
    'Escuela': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '28',
      height: '28',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M22 10v6' }),
      h('path', { d: 'M2 10l10-5 10 5-10 5z' }),
      h('path', { d: 'M6 12v5c3 3 9 3 12 0v-5' })
    ]),
    'Parque': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '28',
      height: '28',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M12 10v12m-6-6h12M8 10c0-3.3 2.7-6 6-6s6 2.7 6 6-2.7 6-6 6H8z' }),
      h('path', { d: 'M8 10c0 3.3-2.7 6-6 6' })
    ]),
    'Centro Comercial': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '28',
      height: '28',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('circle', { cx: '9', cy: '21', r: '1' }),
      h('circle', { cx: '20', cy: '21', r: '1' }),
      h('path', { d: 'M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6' })
    ]),
    'Transporte': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '28',
      height: '28',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M19 17h2c.6 0 1-.4 1-1v-3c0-.9-.7-1.7-1.5-1.9C18.7 10.6 16 10 16 10s-1.3-1.4-2.2-2.3c-.5-.4-1.1-.7-1.8-.7H5c-.6 0-1.1.4-1.4.9l-1.4 2.9A3.7 3.7 0 0 0 2 12v4c0 .6.4 1 1 1h2' }),
      h('circle', { cx: '7', cy: '17', r: '2' }),
      h('path', { d: 'M9 17h6' }),
      h('circle', { cx: '17', cy: '17', r: '2' })
    ]),
    'Centro Cultural': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '28',
      height: '28',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M9 18V5l12-2v13' }),
      h('circle', { cx: '6', cy: '18', r: '3' }),
      h('circle', { cx: '18', cy: '16', r: '3' })
    ])
  };
  return icons[tipo] || icons['Parque'];
};

const handlePuntoSelected = (punto) => {
  console.log('Punto seleccionado:', punto);
};

const loadPuntos = async () => {
  loading.value = true;
  error.value = null;

  try {
    const data = await puntosInteresService.getAll();
    puntos.value = data;
  } catch (err) {
    error.value = err.message || 'Error al cargar puntos de interés';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadPuntos();
});
</script>

<style scoped>
.page-container {
  max-width: 1400px;
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
  margin: 0;
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
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 8px;
}

.search-box svg {
  color: var(--text-secondary);
}

.search-box input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
  color: var(--text-primary);
}

.search-box input::placeholder {
  color: var(--text-secondary);
}

.filter-select {
  padding: 12px 16px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  color: var(--text-primary);
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}

.filter-select option {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

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
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
}

.leyenda-item.hospital .leyenda-color { background-color: #ef4444; }
.leyenda-item.escuela .leyenda-color { background-color: #3b82f6; }
.leyenda-item.parque .leyenda-color { background-color: #10b981; }
.leyenda-item.centro-comercial .leyenda-color { background-color: #f59e0b; }
.leyenda-item.transporte .leyenda-color { background-color: #8b5cf6; }
.leyenda-item.centro-cultural .leyenda-color { background-color: #ec4899; }

.leyenda-item label {
  font-size: 14px;
  color: var(--text-secondary);
}

.points-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

.point-card {
  background: var(--bg-secondary);
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  box-shadow: none;
  border: 1px solid var(--border-color);
  transition: transform 0.2s, box-shadow 0.2s;
  border-left: 4px solid;
}

.point-card.hospital {
  border-color: var(--status-retrasado-border);
}
.point-card.escuela {
  border-color: var(--status-en-curso-border);
}
.point-card.parque {
  border-color: var(--status-completado-border);
}
.point-card.centro-comercial {
  border-color: var(--status-planeado-border);
}
.point-card.transporte {
  border-color: var(--accent-primary);
}
.point-card.centro-cultural {
  border-color: #ec4899;
}

.point-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.point-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.hospital .point-icon { background-color: #dc2626; }
.escuela .point-icon { background-color: #3b82f6; }
.parque .point-icon { background-color: #059669; }
.centro-comercial .point-icon { background-color: #f59e0b; }
.transporte .point-icon { background-color: #8b5cf6; }
.centro-cultural .point-icon { background-color: #ec4899; }

.point-info {
  flex: 1;
  min-width: 0;
}

.point-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.point-type {
  margin-bottom: 8px;
}

.type-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: 600;
}

.type-badge.hospital {
  background-color: var(--status-retrasado-bg);
  color: var(--status-retrasado-border);
}
.type-badge.escuela {
  background-color: var(--status-en-curso-bg);
  color: var(--status-en-curso-border);
}
.type-badge.parque {
  background-color: var(--status-completado-bg);
  color: var(--status-completado-border);
}
.type-badge.centro-comercial {
  background-color: var(--status-planeado-bg);
  color: var(--status-planeado-border);
}
.type-badge.transporte {
  background-color: var(--icon-zonas-bg);
  color: white;
}
.type-badge.centro-cultural {
  background-color: #fce7f3;
  color: #ec4899;
}

.point-address {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--text-secondary);
  margin: 0;
}

.empty-state {
  grid-column: 1 / -1;
  padding: 64px 24px;
  text-align: center;
  color: var(--text-secondary);
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

.empty-state p {
  margin: 0;
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

  .points-grid {
    grid-template-columns: 1fr;
  }
}
</style>
