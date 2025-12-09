<template>
  <div class="mapa-wrapper">
    <div ref="mapContainer" class="mapa-leaflet"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

const props = defineProps({
  zonas: {
    type: Array,
    default: () => []
  },
  selectedTipo: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['zona-selected']);

const mapContainer = ref(null);
let map = null;
const zonasLayers = ref([]);

// Configuración de colores por tipo de zona
const tipoConfig = {
  'Residencial': {
    color: '#3b82f6',
    fillColor: '#3b82f6',
    fillOpacity: 0.3
  },
  'Comercial': {
    color: '#f59e0b',
    fillColor: '#f59e0b',
    fillOpacity: 0.3
  },
  'Industrial': {
    color: '#ef4444',
    fillColor: '#ef4444',
    fillOpacity: 0.3
  },
  'Mixto': {
    color: '#8b5cf6',
    fillColor: '#8b5cf6',
    fillOpacity: 0.3
  }
};

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
};

const parseGeoJSON = (geojson) => {
  if (!geojson) return null;

  try {
    // Si es string, parsearlo
    const geo = typeof geojson === 'string' ? JSON.parse(geojson) : geojson;

    // GeoJSON Polygon tiene formato: { "type": "Polygon", "coordinates": [[[lng, lat], ...]] }
    if (geo.type === 'Polygon' && geo.coordinates) {
      // coordinates[0] es el anillo exterior del polígono
      const ring = geo.coordinates[0];
      return ring.map(coord => [coord[1], coord[0]]); // Convertir [lng, lat] a [lat, lng] para Leaflet
    }

    // GeoJSON MultiPolygon tiene formato: { "type": "MultiPolygon", "coordinates": [[[[lng, lat], ...]], ...] }
    if (geo.type === 'MultiPolygon' && geo.coordinates) {
      return geo.coordinates.map(polygon => {
        // polygon[0] es el anillo exterior de cada polígono
        const ring = polygon[0];
        return ring.map(coord => [coord[1], coord[0]]); // Convertir [lng, lat] a [lat, lng]
      });
    }

    return null;
  } catch (error) {
    console.error('Error parsing GeoJSON:', error);
    return null;
  }
};

const getTipoString = (tipo) => {
  if (!tipo) return 'Desconocido';
  if (typeof tipo === 'string') return tipo;
  if (typeof tipo === 'object' && tipo.valor) return tipo.valor;
  return String(tipo);
};

const loadZonas = () => {
  if (!map) return;

  // Limpiar capas existentes de forma segura
  zonasLayers.value.forEach(layer => {
    try {
      if (map && layer) {
        layer.remove();
      }
    } catch (e) {
      console.warn('Error removing layer:', e);
    }
  });
  zonasLayers.value = [];

  // Filtrar zonas según tipo seleccionado
  let zonasToShow = props.zonas;

  if (props.selectedTipo) {
    zonasToShow = zonasToShow.filter(z => getTipoString(z.tipo_zona) === props.selectedTipo);
  }

  if (zonasToShow.length === 0) return;

  const bounds = [];

  zonasToShow.forEach(zona => {
    const tipoString = getTipoString(zona.tipo_zona);
    // El backend devuelve 'geometria_geojson' o 'geometriaPoligono'
    const coords = parseGeoJSON(zona.geometria_geojson || zona.geometriaPoligono);

    if (!coords) {
      console.warn(`No se pudieron parsear las coordenadas de la zona: ${zona.nombre}`);
      return;
    }

    const config = tipoConfig[tipoString] || {
      color: '#6b7280',
      fillColor: '#6b7280',
      fillOpacity: 0.3
    };

    try {
      let layer;

      // Si coords es un array de arrays (multipolygon), crear un polígono para cada uno
      if (Array.isArray(coords[0][0])) {
        // Es un multipolygon
        layer = L.polygon(coords, {
          color: config.color,
          fillColor: config.fillColor,
          fillOpacity: config.fillOpacity,
          weight: 2
        }).addTo(map);
      } else {
        // Es un polígono simple
        layer = L.polygon(coords, {
          color: config.color,
          fillColor: config.fillColor,
          fillOpacity: config.fillOpacity,
          weight: 2
        }).addTo(map);
      }

      // Popup con información
      layer.bindPopup(`
        <div style="min-width: 250px;">
          <h3 style="margin: 0 0 12px 0; font-size: 16px; font-weight: 600;">${zona.nombre}</h3>
          <p style="margin: 4px 0; font-size: 13px;"><strong>Tipo:</strong> ${tipoString}</p>
          ${zona.area_km2 ? `<p style="margin: 4px 0; font-size: 13px;"><strong>Área:</strong> ${zona.area_km2.toFixed(2)} km²</p>` : ''}
          ${zona.poblacion ? `<p style="margin: 4px 0; font-size: 13px;"><strong>Población Total:</strong> ${zona.poblacion.toLocaleString('es-CL')}</p>` : ''}
        </div>
      `);

      // Efecto hover
      layer.on('mouseover', function() {
        this.setStyle({
          fillOpacity: 0.5,
          weight: 3
        });
      });

      layer.on('mouseout', function() {
        this.setStyle({
          fillOpacity: config.fillOpacity,
          weight: 2
        });
      });

      // Evento click
      layer.on('click', () => {
        emit('zona-selected', zona);
      });

      zonasLayers.value.push(layer);

      // Agregar bounds
      if (Array.isArray(coords[0][0])) {
        coords.forEach(polygon => bounds.push(...polygon));
      } else {
        bounds.push(...coords);
      }
    } catch (e) {
      console.error('Error creando capa para zona:', zona.nombre, e);
    }
  });

  // Ajustar la vista del mapa solo si hay bounds válidos
  if (bounds.length > 0 && map) {
    try {
      setTimeout(() => {
        if (map) {
          map.fitBounds(bounds, {
            padding: [50, 50],
            maxZoom: 13,
            animate: false // Deshabilitar animación para evitar errores
          });
        }
      }, 100);
    } catch (e) {
      console.warn('Error ajustando bounds del mapa:', e);
    }
  }
};

onMounted(() => {
  initMap();
  loadZonas();
});

onBeforeUnmount(() => {
  // Limpiar capas
  zonasLayers.value.forEach(layer => {
    try {
      if (layer) {
        layer.remove();
      }
    } catch (e) {
      // Silenciar errores al desmontar
    }
  });
  zonasLayers.value = [];

  // Destruir el mapa
  if (map) {
    try {
      map.remove();
      map = null;
    } catch (e) {
      // Silenciar errores al desmontar
    }
  }
});

watch(() => props.zonas, () => {
  loadZonas();
}, { deep: true });

watch(() => props.selectedTipo, () => {
  loadZonas();
});
</script>

<style scoped>
.mapa-wrapper {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border-color);
}

.mapa-leaflet {
  width: 100%;
  height: 100%;
  min-height: 400px;
  background: var(--bg-secondary);
}
</style>

<style>
/* Estilos globales para Leaflet */
.leaflet-popup-content-wrapper {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.leaflet-popup-content {
  margin: 12px;
}
</style>

