<template>
  <div class="mapa-analisis-wrapper">
    <div ref="mapContainer" class="mapa-leaflet"></div>

    <!-- Panel de resultados -->
    <div v-if="estadisticas" class="panel-estadisticas">
      <div class="estadisticas-header">
        <h3> Estadísticas del Área Seleccionada</h3>
        <button @click="limpiarArea" class="btn-limpiar">
           Limpiar
        </button>
      </div>

      <div class="estadisticas-grid">
        <div class="stat-card">
          <div class="stat-icon">👥</div>
          <div class="stat-content">
            <div class="stat-label">Población Total</div>
            <div class="stat-value">{{ formatNumber(estadisticas.poblacion_total) }}</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🏫</div>
          <div class="stat-content">
            <div class="stat-label">Escuelas</div>
            <div class="stat-value">{{ estadisticas.total_escuelas }}</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🏥</div>
          <div class="stat-content">
            <div class="stat-label">Hospitales</div>
            <div class="stat-value">{{ estadisticas.total_hospitales }}</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon">🚧</div>
          <div class="stat-content">
            <div class="stat-label">Proyectos en Curso</div>
            <div class="stat-value">{{ estadisticas.proyectos_en_curso }}</div>
          </div>
        </div>

        <div class="stat-card full-width">
          <div class="stat-icon">📐</div>
          <div class="stat-content">
            <div class="stat-label">Área Total</div>
            <div class="stat-value">{{ estadisticas.area_km2 }} km²</div>
          </div>
        </div>

        <div v-if="estadisticas.zonas_por_tipo && estadisticas.zonas_por_tipo.length > 0" class="stat-card full-width">
          <div class="stat-content">
            <div class="stat-label">Zonas por Tipo</div>
            <div class="zonas-lista">
              <div v-for="zona in estadisticas.zonas_por_tipo" :key="zona.tipo_zona" class="zona-item">
                <span class="zona-tipo">{{ zona.tipo_zona }}</span>
                <span class="zona-cantidad">{{ zona.cantidad }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Indicador de carga -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>Calculando estadísticas...</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

// Fix para leaflet-draw con Vite
import 'leaflet-draw';
import 'leaflet-draw/dist/leaflet.draw.css';

import api from '@/services/api';

const emit = defineEmits(['error']);

const mapContainer = ref(null);
let map = null;
let drawnItems = null;
let drawControl = null;
const estadisticas = ref(null);
const loading = ref(false);

const initMap = () => {
  if (map) return;

  // Crear el mapa centrado en Santiago
  map = L.map(mapContainer.value, {
    center: [-33.4489, -70.6693],
    zoom: 12,
    zoomControl: true
  });

  // Agregar capa de mapa base
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(map);

  // Inicializar capa para elementos dibujados
  drawnItems = new L.FeatureGroup();
  map.addLayer(drawnItems);

  // Configurar controles de dibujo
  drawControl = new L.Control.Draw({
    position: 'topright',
    draw: {
      polygon: {
        allowIntersection: false,
        showArea: false,  // Deshabilitar para evitar error "type is not defined"
        metric: false,
        shapeOptions: {
          color: '#3b82f6',
          fillColor: '#3b82f6',
          fillOpacity: 0.3,
          weight: 3
        }
      },
      rectangle: {
        showArea: false,  // Deshabilitar para evitar error
        metric: false,
        shapeOptions: {
          color: '#3b82f6',
          fillColor: '#3b82f6',
          fillOpacity: 0.3,
          weight: 3
        }
      },
      circle: {
        shapeOptions: {
          color: '#3b82f6',
          fillColor: '#3b82f6',
          fillOpacity: 0.3,
          weight: 3
        }
      },
      polyline: false,
      marker: false,
      circlemarker: false
    },
    edit: {
      featureGroup: drawnItems,
      remove: true
    }
  });
  map.addControl(drawControl);

  // Eventos de dibujo
  map.on(L.Draw.Event.CREATED, async (e) => {
    const layer = e.layer;

    // Limpiar área anterior
    drawnItems.clearLayers();
    drawnItems.addLayer(layer);

    // Obtener estadísticas del área
    await obtenerEstadisticas(layer);
  });

  map.on(L.Draw.Event.DELETED, () => {
    estadisticas.value = null;
  });
};

const obtenerEstadisticas = async (layer) => {
  loading.value = true;
  try {
    // Convertir la capa a GeoJSON
    const geojson = layer.toGeoJSON();

    // Si es un círculo, necesitamos convertirlo a polígono
    let geometria = geojson.geometry;
    if (layer instanceof L.Circle) {
      // Convertir círculo a polígono aproximado
      const points = [];
      const center = layer.getLatLng();
      const radius = layer.getRadius();
      const numPoints = 64;

      for (let i = 0; i < numPoints; i++) {
        const angle = (i / numPoints) * 2 * Math.PI;
        const lat = center.lat + (radius / 111320) * Math.cos(angle);
        const lng = center.lng + (radius / (111320 * Math.cos(center.lat * Math.PI / 180))) * Math.sin(angle);
        points.push([lng, lat]);
      }
      points.push(points[0]); // Cerrar el polígono

      geometria = {
        type: 'Polygon',
        coordinates: [points]
      };
    }

    // Enviar al backend
    const response = await api.post('/zonas/analisis/area', {
      geojson: JSON.stringify(geometria)
    });

    estadisticas.value = response.data;
  } catch (error) {
    console.error('Error al obtener estadísticas:', error);
    emit('error', 'Error al calcular las estadísticas del área seleccionada');
    estadisticas.value = null;
  } finally {
    loading.value = false;
  }
};

const limpiarArea = () => {
  if (drawnItems) {
    drawnItems.clearLayers();
  }
  estadisticas.value = null;
};

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  return new Intl.NumberFormat('es-CL').format(num);
};

onMounted(() => {
  initMap();
});

onBeforeUnmount(() => {
  if (map) {
    map.remove();
    map = null;
  }
});
</script>

<style scoped>
.mapa-analisis-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  gap: 1rem;
}

.mapa-leaflet {
  flex: 1;
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 1;
}

.panel-estadisticas {
  width: 350px;
  background: var(--bg-primary);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 1.5rem;
  overflow-y: auto;
  max-height: 100%;
}

.estadisticas-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

.estadisticas-header h3 {
  margin: 0;
  font-size: 1.25rem;
  color: var(--text-primary);
}

.btn-limpiar {
  padding: 0.5rem 1rem;
  background: var(--accent-danger);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}

.btn-limpiar:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.estadisticas-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 1rem;
}

.stat-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 1rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  border: 1px solid var(--border-color);
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.stat-card.full-width {
  grid-column: 1 / -1;
}

.stat-icon {
  font-size: 2rem;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.85rem;
  color: var(--text-secondary);
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: var(--text-primary);
}

.zonas-lista {
  margin-top: 0.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.zona-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  background: var(--bg-primary);
  border-radius: 4px;
  border: 1px solid var(--border-color);
}

.zona-tipo {
  font-weight: 500;
  color: var(--text-primary);
}

.zona-cantidad {
  font-weight: bold;
  color: var(--accent-primary);
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  border-radius: 8px;
}

.loading-overlay p {
  color: white;
  font-size: 1.1rem;
  margin-top: 1rem;
}

.spinner {
  border: 4px solid rgba(255,255,255,0.3);
  border-top: 4px solid white;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* Responsive */
@media (max-width: 1024px) {
  .mapa-analisis-wrapper {
    flex-direction: column;
  }

  .panel-estadisticas {
    width: 100%;
    max-height: 400px;
  }

  .mapa-leaflet {
    height: 500px;
  }
}
</style>

