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
  puntos: {
    type: Array,
    default: () => []
  },
  selectedTipo: {
    type: String,
    default: ''
  },
  activeLayers: {
    type: Object,
    default: () => ({
      hospital: true,
      escuela: true,
      parque: true,
      centro_comercial: true,
      transporte: true,
      centro_cultural: true
    })
  }
});

const emit = defineEmits(['punto-selected']);

const mapContainer = ref(null);
let map = null;
const puntosMarkers = ref([]);

// Configuración de íconos y colores por tipo
const tipoConfig = {
  'Hospital': {
    color: '#ef4444',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="white" stroke="white" stroke-width="2">
      <path d="M12 2v20M2 12h20"/>
    </svg>`
  },
  'Escuela': {
    color: '#3b82f6',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
      <path d="M22 10v6M2 10l10-5 10 5-10 5z"/>
      <path d="M6 12v5c3 3 9 3 12 0v-5"/>
    </svg>`
  },
  'Parque': {
    color: '#10b981',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
      <path d="M12 2c-1.5 0-2.5 1.5-2.5 3.5S10.5 9 12 9s2.5-1.5 2.5-3.5S13.5 2 12 2z"/>
      <path d="M8 9c-2 0-3 1.5-3 3.5S6 16 8 16h8c2 0 3-1.5 3-3.5S18 9 16 9"/>
      <path d="M12 16v6"/>
    </svg>`
  },
  'Centro Comercial': {
    color: '#f59e0b',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
      <circle cx="9" cy="21" r="1" fill="white"/>
      <circle cx="20" cy="21" r="1" fill="white"/>
      <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/>
    </svg>`
  },
  'Transporte': {
    color: '#8b5cf6',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
      <path d="M8 6v6M16 6v6"/>
      <path d="M2 12h19.6"/>
      <path d="M18 18h3s.5-1.7.8-2.8c.1-.4.2-.8.2-1.2 0-.4-.1-.8-.2-1.2l-1.4-5C20.1 6.8 19.1 6 18 6H6c-1.1 0-2.1.8-2.4 1.8l-1.4 5c-.1.4-.2.8-.2 1.2 0 .4.1.8.2 1.2.3 1.1.8 2.8.8 2.8h3"/>
      <circle cx="7" cy="18" r="2"/>
      <circle cx="17" cy="18" r="2"/>
    </svg>`
  },
  'Centro Cultural': {
    color: '#ec4899',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" stroke-width="2">
      <rect x="3" y="3" width="7" height="9"/>
      <rect x="14" y="3" width="7" height="5"/>
      <rect x="14" y="12" width="7" height="9"/>
      <rect x="3" y="16" width="7" height="5"/>
      <path d="M3 3L21 3M3 21h18"/>
    </svg>`
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

    // GeoJSON Point tiene formato: { "type": "Point", "coordinates": [lng, lat] }
    // PostGIS ST_AsGeoJSON devuelve: {"type":"Point","coordinates":[-70.6408,-33.4245]}
    // Donde el primer valor es LONGITUD (lng) y el segundo es LATITUD (lat)
    if (geo.type === 'Point' && geo.coordinates) {
      const [lng, lat] = geo.coordinates;

      // Validación: Santiago está en lng≈-70.6, lat≈-33.4
      // lng debe estar entre -71 y -70 (negativo, oeste)
      // lat debe estar entre -34 y -33 (negativo, sur)
      if (lng < -71 || lng > -70 || lat < -34 || lat > -33) {
        console.warn(`⚠️ Coordenadas fuera de rango para Santiago: [lng=${lng}, lat=${lat}]`);
      }

      // Convertir a formato Leaflet: [lat, lng]
      return [lat, lng];
    }

    return null;
  } catch (error) {
    console.error('Error parsing GeoJSON:', error);
    return null;
  }
};

const getTipoString = (tipo) => {
  if (!tipo) return 'Desconocido';

  // Si ya es string, puede ser el enum o el valor
  if (typeof tipo === 'string') {
    // Mapeo de valores del enum a valores legibles
    const enumMap = {
      'HOSPITAL': 'Hospital',
      'ESCUELA': 'Escuela',
      'PARQUE': 'Parque',
      'CENTRO_COMERCIAL': 'Centro Comercial',
      'TRANSPORTE': 'Transporte',
      'CENTRO_CULTURAL': 'Centro Cultural'
    };

    // Si es un valor del enum (mayúsculas), convertirlo
    if (enumMap[tipo]) {
      return enumMap[tipo];
    }

    // Si ya es el valor legible, devolverlo
    return tipo;
  }

  // Si es un objeto, intentar extraer el valor
  if (typeof tipo === 'object') {
    // Puede tener propiedad 'valor' o 'name'
    if (tipo.valor) return tipo.valor;
    if (tipo.name) return tipo.name;

    if (tipo.toString && tipo.toString() !== '[object Object]') {
      return tipo.toString();
    }
  }

  // Último recurso: convertir a string
  return String(tipo);
};

const createCustomIcon = (tipo) => {
  const tipoStr = getTipoString(tipo);
  const config = tipoConfig[tipoStr] || {
    color: '#6b7280',
    iconSvg: `<svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="white">
      <circle cx="12" cy="12" r="8"/>
    </svg>`
  };

  return L.divIcon({
    className: 'custom-marker',
    html: `
      <div style="
        background-color: ${config.color};
        width: 36px;
        height: 36px;
        border-radius: 50% 50% 50% 0;
        border: 3px solid white;
        transform: rotate(-45deg);
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 3px 10px rgba(0,0,0,0.4);
        position: relative;
      ">
        <div style="
          transform: rotate(45deg);
          display: flex;
          align-items: center;
          justify-content: center;
        ">${config.iconSvg}</div>
      </div>
    `,
    iconSize: [36, 36],
    iconAnchor: [18, 36],
    popupAnchor: [0, -36]
  });
};

const addPuntosToMap = () => {
  if (!map) return;

  // Limpiar marcadores existentes de forma segura
  puntosMarkers.value.forEach(marker => {
    try {
      if (map && marker) {
        marker.remove();
      }
    } catch (e) {
      console.warn('Error removing marker:', e);
    }
  });
  puntosMarkers.value = [];

  // DEBUG: Log para ver qué está pasando
  console.log('=== MapaPuntos - addPuntosToMap ===');
  console.log('Total puntos recibidos:', props.puntos.length);
  console.log('Filtro seleccionado:', props.selectedTipo);

  // Mostrar los tipos de los primeros puntos para debugging
  if (props.puntos.length > 0) {
    console.log('Tipos de puntos (primeros 3):');
    props.puntos.slice(0, 3).forEach(p => {
      const tipoString = getTipoString(p.tipo);
      console.log(`  - ${p.nombre}: tipo original =`, p.tipo, ', tipo normalizado =', tipoString);
    });
  }

  // Filtrar puntos según tipo seleccionado
  let puntosToShow = props.selectedTipo
    ? props.puntos.filter(p => {
      const tipoNormalizado = getTipoString(p.tipo);
      const match = tipoNormalizado === props.selectedTipo;
      if (!match && props.selectedTipo) {
        console.log(`  FILTRADO: "${p.nombre}" tipo="${tipoNormalizado}" NO coincide con filtro="${props.selectedTipo}"`);
      }
      return match;
    })
    : props.puntos;

  console.log('Puntos después del filtro:', puntosToShow.length);

  if (puntosToShow.length === 0) {
    console.warn('⚠️ No hay puntos para mostrar después del filtro');
    return;
  }

  const bounds = [];

  puntosToShow.forEach(punto => {
    const tipoString = getTipoString(punto.tipo);
    // El backend puede devolver diferentes nombres de campo
    const geojsonData = punto.coordenadas_geojson || punto.coordenadasPunto || punto.geometria;

    // DEBUG: Mostrar datos originales del primer punto
    if (bounds.length === 0) {
      console.log('📍 Ejemplo de punto original del backend:', {
        nombre: punto.nombre,
        geojson_original: geojsonData,
        geojson_tipo: typeof geojsonData
      });
    }

    const coords = parseGeoJSON(geojsonData);

    if (!coords) {
      console.warn('❌ No se pudieron parsear coordenadas para:', punto.nombre);
      return;
    }

    // DEBUG: Mostrar conversión del primer punto
    if (bounds.length === 0) {
      console.log('✅ Coordenadas después de parsear:', {
        nombre: punto.nombre,
        leaflet_coords: coords,
        formato: '[lat, lng]'
      });
    }

    // Validar coordenadas
    if (!Array.isArray(coords) || coords.length !== 2 ||
      isNaN(coords[0]) || isNaN(coords[1])) {
      console.warn('Coordenadas inválidas para:', punto.nombre, coords);
      return;
    }

    try {
      // Crear marcador con ícono personalizado
      const marker = L.marker(coords, {
        icon: createCustomIcon(punto.tipo)
      }).addTo(map);

      // Configuración de color para el popup
      const config = tipoConfig[tipoString] || { color: '#6b7280' };

      // Popup con información mejorada
      const popupContent = `
        <div style="min-width: 200px;">
          <h3 style="margin: 0 0 8px 0; color: ${config.color}; font-size: 16px; font-weight: 600;">
            ${punto.nombre}
          </h3>
          <p style="margin: 4px 0; font-size: 13px; color: #666;">
            <strong>Tipo:</strong> ${tipoString}
          </p>
          ${punto.direccion ? `
            <p style="margin: 4px 0; font-size: 13px; color: #666;">
              <strong>Dirección:</strong> ${punto.direccion}
            </p>
          ` : ''}
          ${punto.zona_urbana_id ? `
            <p style="margin: 4px 0; font-size: 13px; color: #666;">
              <strong>Zona:</strong> ID ${punto.zona_urbana_id}
            </p>
          ` : ''}
        </div>
      `;

      marker.bindPopup(popupContent);

      // Evento click
      marker.on('click', () => {
        emit('punto-selected', punto);
      });

      puntosMarkers.value.push(marker);
      bounds.push(coords);
    } catch (e) {
      console.error('Error creando marcador para:', punto.nombre, e);
    }
  });

  // Ajustar la vista del mapa solo si hay bounds válidos
  if (bounds.length > 0 && map) {
    try {
      // Usar setTimeout para evitar conflictos con animaciones
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
  if (props.puntos.length > 0) {
    addPuntosToMap();
  }
});

onBeforeUnmount(() => {
  // Limpiar marcadores
  puntosMarkers.value.forEach(marker => {
    try {
      if (marker) {
        marker.remove();
      }
    } catch (e) {
      // Silenciar errores al desmontar
    }
  });
  puntosMarkers.value = [];

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

watch(() => props.puntos, () => {
  if (map) {
    addPuntosToMap();
  }
}, { deep: true });

watch(() => props.selectedTipo, () => {
  if (map) {
    addPuntosToMap();
  }
});

watch(() => props.activeLayers, () => {
  if (map) {
    addPuntosToMap();
  }
}, { deep: true });
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
/* Estilos globales para los marcadores personalizados */
.custom-marker {
  background: transparent !important;
  border: none !important;
}

.leaflet-popup-content-wrapper {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
}

.leaflet-popup-content {
  margin: 12px;
}
</style>
