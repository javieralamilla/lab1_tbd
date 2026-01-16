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
  proyectos: {
    type: Array,
    default: () => []
  },
  selectedEstado: {
    type: String,
    default: ''
  },
  selectedTipoZona: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['proyecto-selected']);

const mapContainer = ref(null);
let map = null;
const proyectosLayers = ref([]);

// Configuración de colores por estado de proyecto
const estadoConfig = {
  'Planeado': {
    color: '#3b82f6',
    fillColor: '#3b82f6',
    fillOpacity: 0.5,
    icon: ''
  },
  'En Curso': {
    color: '#f59e0b',
    fillColor: '#f59e0b',
    fillOpacity: 0.5,
    icon: '🚧'
  },
  'Completado': {
    color: '#10b981',
    fillColor: '#10b981',
    fillOpacity: 0.5,
    icon: ''
  },
  'Retrasado': {
    color: '#ef4444',
    fillColor: '#ef4444',
    fillOpacity: 0.5,
    icon: ''
  },
  'Cancelado': {
    color: '#6b7280',
    fillColor: '#6b7280',
    fillOpacity: 0.3,
    icon: ''
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

    // GeoJSON Point
    if (geo.type === 'Point' && geo.coordinates) {
      // Convertir [lng, lat] a [lat, lng] para Leaflet
      return [geo.coordinates[1], geo.coordinates[0]];
    }

    // GeoJSON Polygon
    if (geo.type === 'Polygon' && geo.coordinates) {
      const ring = geo.coordinates[0];
      return ring.map(coord => [coord[1], coord[0]]);
    }

    // GeoJSON MultiPolygon
    if (geo.type === 'MultiPolygon' && geo.coordinates) {
      return geo.coordinates.map(polygon => {
        const ring = polygon[0];
        return ring.map(coord => [coord[1], coord[0]]);
      });
    }

    // GeoJSON LineString (para proyectos lineales como carreteras)
    if (geo.type === 'LineString' && geo.coordinates) {
      return geo.coordinates.map(coord => [coord[1], coord[0]]);
    }

    return null;
  } catch (error) {
    console.error('Error parsing GeoJSON:', error);
    return null;
  }
};

const getEstadoString = (estado) => {
  if (!estado) return 'Planeado';
  if (typeof estado === 'string') return estado;
  if (typeof estado === 'object' && estado.valor) return estado.valor;
  return String(estado);
};

const createCustomIcon = (estado) => {
  const estadoStr = getEstadoString(estado);
  const config = estadoConfig[estadoStr] || estadoConfig['Planeado'];

  const iconHtml = `
    <div style="
      background-color: ${config.fillColor};
      border: 3px solid ${config.color};
      border-radius: 50%;
      width: 32px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.3);
    ">
      ${config.icon}
    </div>
  `;

  return L.divIcon({
    html: iconHtml,
    className: 'custom-proyecto-icon',
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -16]
  });
};

const addProyectosToMap = () => {
  if (!map) return;

  // Limpiar capas anteriores de forma segura
  proyectosLayers.value.forEach(layer => {
    try {
      if (map && layer) {
        layer.remove();
      }
    } catch (e) {
      console.warn('Error removing layer:', e);
    }
  });
  proyectosLayers.value = [];

  // Filtrar proyectos solo por estado (el filtro de zona ya se aplicó en el backend)
  let proyectosToShow = props.proyectos;

  if (props.selectedEstado) {
    proyectosToShow = proyectosToShow.filter(p => getEstadoString(p.estado) === props.selectedEstado);
  }

  if (proyectosToShow.length === 0) return;

  const bounds = [];

  proyectosToShow.forEach(proyecto => {
    const coords = parseGeoJSON(proyecto.geometria);
    if (!coords) return;

    const estadoStr = getEstadoString(proyecto.estado);
    const config = estadoConfig[estadoStr] || estadoConfig['Planeado'];

    try {
      let layer;
      let iconMarker = null;
      let addedIcon = false;

      // Determinar tipo de geometría
      if (Array.isArray(coords[0]) && Array.isArray(coords[0][0])) {
        // MultiPolygon
        coords.forEach(polygon => {
          layer = L.polygon(polygon, {
            color: config.color,
            fillColor: config.fillColor,
            fillOpacity: config.fillOpacity,
            weight: 3
          }).addTo(map);

          bounds.push(...polygon);
          proyectosLayers.value.push(layer);
          addProyectoInteraction(layer, proyecto);

          // Añadir un marcador con emoji en el centro del primer polígono
          if (!addedIcon) {
            try {
              const center = layer.getBounds().getCenter();
              iconMarker = L.marker([center.lat, center.lng], {
                icon: createCustomIcon(proyecto.estado),
                zIndexOffset: 1000
              }).addTo(map);
              proyectosLayers.value.push(iconMarker);
              addProyectoInteraction(iconMarker, proyecto);
              addedIcon = true;
            } catch (e) {
              // Silenciar errores al calcular centro
            }
          }
        });
      } else if (Array.isArray(coords[0]) && typeof coords[0][0] === 'number') {
        // Polygon o LineString
        if (coords.length > 2) {
          layer = L.polygon(coords, {
            color: config.color,
            fillColor: config.fillColor,
            fillOpacity: config.fillOpacity,
            weight: 3
          }).addTo(map);

          bounds.push(...coords);
          proyectosLayers.value.push(layer);
          addProyectoInteraction(layer, proyecto);

          // Añadir marcador con emoji en el centro del polígono/linea
          try {
            const center = layer.getBounds().getCenter();
            const markerIcon = L.marker([center.lat, center.lng], {
              icon: createCustomIcon(proyecto.estado),
              zIndexOffset: 1000
            }).addTo(map);
            proyectosLayers.value.push(markerIcon);
            addProyectoInteraction(markerIcon, proyecto);
          } catch (e) {
            // Silenciar errores
          }
        }
      } else if (typeof coords[0] === 'number') {
        // Point
        layer = L.marker(coords, {
          icon: createCustomIcon(proyecto.estado)
        }).addTo(map);

        bounds.push(coords);
        proyectosLayers.value.push(layer);
        addProyectoInteraction(layer, proyecto);
      }
    } catch (e) {
      console.error('Error creando capa para proyecto:', proyecto.nombre, e);
    }
  });

  // Ajustar zoom solo si hay bounds válidos
  if (bounds.length > 0 && map) {
    try {
      setTimeout(() => {
        if (map) {
          map.fitBounds(bounds, {
            padding: [50, 50],
            animate: false // Deshabilitar animación para evitar errores
          });
        }
      }, 100);
    } catch (e) {
      console.warn('Error ajustando bounds del mapa:', e);
    }
  }
};

const addProyectoInteraction = (layer, proyecto) => {
  const estadoStr = getEstadoString(proyecto.estado);
  const config = estadoConfig[estadoStr] || estadoConfig['Planeado'];

  // Popup con información del proyecto
  const popupContent = `
    <div style="min-width: 200px;">
      <h3 style="margin: 0 0 8px 0; color: ${config.color}; font-size: 16px;">
        ${config.icon} ${proyecto.nombre}
      </h3>
      <p style="margin: 4px 0; font-size: 13px; color: #666;">
        <strong>Estado:</strong> ${estadoStr}
      </p>
      ${(proyecto.tipoProyecto || proyecto.tipo_proyecto) ? `
        <p style="margin: 4px 0; font-size: 13px; color: #666;">
          <strong>Tipo:</strong> ${proyecto.tipoProyecto || proyecto.tipo_proyecto}
        </p>
      ` : ''}
      ${proyecto.presupuesto ? `
        <p style="margin: 4px 0; font-size: 13px; color: #666;">
          <strong>Presupuesto:</strong> ${formatCurrency(proyecto.presupuesto)}
        </p>
      ` : ''}
      ${(proyecto.fechaInicio || proyecto.fecha_inicio) ? `
        <p style="margin: 4px 0; font-size: 13px; color: #666;">
          <strong>Inicio:</strong> ${formatDate(proyecto.fechaInicio || proyecto.fecha_inicio)}
        </p>
      ` : ''}
      ${proyecto.descripcion ? `
        <p style="margin: 8px 0 4px 0; font-size: 12px; color: #888; font-style: italic;">
          ${proyecto.descripcion.substring(0, 100)}${proyecto.descripcion.length > 100 ? '...' : ''}
        </p>
      ` : ''}
    </div>
  `;

  layer.bindPopup(popupContent);

  // Efectos hover (solo para polígonos)
  if (layer instanceof L.Polygon) {
    layer.on('mouseover', () => {
      layer.setStyle({
        fillOpacity: config.fillOpacity + 0.2,
        weight: 4
      });
    });

    layer.on('mouseout', () => {
      layer.setStyle({
        fillOpacity: config.fillOpacity,
        weight: 3
      });
    });
  }

  // Click event
  layer.on('click', () => {
    emit('proyecto-selected', proyecto);
  });
};

const formatCurrency = (amount) => {
  if (!amount) return 'N/A';
  return new Intl.NumberFormat('es-CL', {
    style: 'currency',
    currency: 'CLP',
    minimumFractionDigits: 0
  }).format(amount);
};

const formatDate = (dateString) => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
  return date.toLocaleDateString('es-CL', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });
};

// Watchers
watch(() => props.proyectos, () => {
  if (map) {
    addProyectosToMap();
  }
}, { deep: true });

watch(() => props.selectedEstado, () => {
  if (map) {
    addProyectosToMap();
  }
});

onMounted(() => {
  initMap();
  if (props.proyectos.length > 0) {
    addProyectosToMap();
  }
});

onBeforeUnmount(() => {
  // Limpiar capas
  proyectosLayers.value.forEach(layer => {
    try {
      if (layer) {
        layer.remove();
      }
    } catch (e) {
      // Silenciar errores al desmontar
    }
  });
  proyectosLayers.value = [];

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
</script>

<style scoped>
.mapa-wrapper {
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.mapa-leaflet {
  width: 100%;
  height: 100%;
  min-height: 500px;
  border-radius: 12px;
}

/* Estilos globales para Leaflet (no scoped) */
:global(.custom-proyecto-icon) {
  background: transparent !important;
  border: none !important;
}

:global(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:global(.leaflet-popup-content) {
  margin: 12px;
}
</style>

