<template>
  <div class="mapa-integrado-wrapper">
    <!-- Panel de Control de Capas -->
    <div class="capas-control">
      <h3>
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z"/>
          <path d="m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65"/>
          <path d="m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65"/>
        </svg>
        Control de Capas
      </h3>

      <div class="capa-group">
        <div class="capa-header">
          <label class="capa-toggle">
            <input
              type="checkbox"
              v-model="capasActivas.proyectos"
              @change="toggleCapa('proyectos')"
            />
            <span class="toggle-slider"></span>
            <span class="capa-nombre">Proyectos Urbanos</span>
          </label>
        </div>
        <div v-if="capasActivas.proyectos" class="capa-filtros">
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosProyectos.planeado" @change="actualizarProyectos" />
            <span class="filtro-color planeado"></span>
            Planeado
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosProyectos.enCurso" @change="actualizarProyectos" />
            <span class="filtro-color en-curso"></span>
            En Curso
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosProyectos.completado" @change="actualizarProyectos" />
            <span class="filtro-color completado"></span>
            Completado
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosProyectos.retrasado" @change="actualizarProyectos" />
            <span class="filtro-color retrasado"></span>
            Retrasado
          </label>
        </div>
      </div>

      <div class="capa-group">
        <div class="capa-header">
          <label class="capa-toggle">
            <input
              type="checkbox"
              v-model="capasActivas.puntos"
              @change="toggleCapa('puntos')"
            />
            <span class="toggle-slider"></span>
            <span class="capa-nombre">Puntos de Interés</span>
          </label>
        </div>
        <div v-if="capasActivas.puntos" class="capa-filtros">
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosPuntos.hospital" @change="actualizarPuntos" />
            <span class="filtro-color hospital"></span>
            Hospitales
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosPuntos.escuela" @change="actualizarPuntos" />
            <span class="filtro-color escuela"></span>
            Escuelas
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosPuntos.parque" @change="actualizarPuntos" />
            <span class="filtro-color parque"></span>
            Parques
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosPuntos.centroComercial" @change="actualizarPuntos" />
            <span class="filtro-color centro-comercial"></span>
            Centros Comerciales
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosPuntos.transporte" @change="actualizarPuntos" />
            <span class="filtro-color transporte"></span>
            Transporte
          </label>
        </div>
      </div>

      <div class="capa-group">
        <div class="capa-header">
          <label class="capa-toggle">
            <input
              type="checkbox"
              v-model="capasActivas.zonas"
              @change="toggleCapa('zonas')"
            />
            <span class="toggle-slider"></span>
            <span class="capa-nombre">Zonas Urbanas</span>
          </label>
        </div>
        <div v-if="capasActivas.zonas" class="capa-filtros">
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosZonas.residencial" @change="actualizarZonas" />
            <span class="filtro-color zona-residencial"></span>
            Residencial
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosZonas.comercial" @change="actualizarZonas" />
            <span class="filtro-color zona-comercial"></span>
            Comercial
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosZonas.industrial" @change="actualizarZonas" />
            <span class="filtro-color zona-industrial"></span>
            Industrial
          </label>
          <label class="filtro-item">
            <input type="checkbox" v-model="filtrosZonas.mixto" @change="actualizarZonas" />
            <span class="filtro-color zona-mixto"></span>
            Mixto
          </label>
        </div>
      </div>

      <div class="estadisticas-capas">
        <div class="stat-item">
          <span class="stat-label">Proyectos visibles:</span>
          <span class="stat-value">{{ contadores.proyectos }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Puntos visibles:</span>
          <span class="stat-value">{{ contadores.puntos }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">Zonas visibles:</span>
          <span class="stat-value">{{ contadores.zonas }}</span>
        </div>
      </div>
    </div>

    <!-- Mapa -->
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
  puntos: {
    type: Array,
    default: () => []
  },
  zonas: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['item-selected']);

const mapContainer = ref(null);
let map = null;

// Capas activas - Todas activadas por defecto para ver los datos inmediatamente
const capasActivas = ref({
  proyectos: true,
  puntos: true,
  zonas: true
});

// Filtros de proyectos
const filtrosProyectos = ref({
  planeado: true,
  enCurso: true,
  completado: true,
  retrasado: true,
  cancelado: true
});

// Filtros de puntos
const filtrosPuntos = ref({
  hospital: true,
  escuela: true,
  parque: true,
  centroComercial: true,
  transporte: true
});

// Filtros de zonas
const filtrosZonas = ref({
  residencial: true,
  comercial: true,
  industrial: true,
  mixto: true
});

// Contadores
const contadores = ref({
  proyectos: 0,
  puntos: 0,
  zonas: 0
});

// Layer groups
const layerGroups = {
  proyectos: null,
  puntos: null,
  zonas: null
};

// Configuraciones de estilos
const estadoConfig = {
  'Planeado': { color: '#3b82f6', fillColor: '#3b82f6', fillOpacity: 0.5 },
  'En Curso': { color: '#f59e0b', fillColor: '#f59e0b', fillOpacity: 0.5 },
  'Completado': { color: '#10b981', fillColor: '#10b981', fillOpacity: 0.5 },
  'Retrasado': { color: '#ef4444', fillColor: '#ef4444', fillOpacity: 0.5 },
  'Cancelado': { color: '#6b7280', fillColor: '#6b7280', fillOpacity: 0.3 }
};

const tipoConfigPuntos = {
  'Hospital': { color: '#ef4444' },
  'Escuela': { color: '#3b82f6' },
  'Parque': { color: '#10b981' },
  'Centro Comercial': { color: '#f59e0b' },
  'Transporte': { color: '#8b5cf6' }
};

const tipoConfigZonas = {
  'Residencial': { color: '#3b82f6', fillColor: '#3b82f6', fillOpacity: 0.3 },
  'Comercial': { color: '#f59e0b', fillColor: '#f59e0b', fillOpacity: 0.3 },
  'Industrial': { color: '#ef4444', fillColor: '#ef4444', fillOpacity: 0.3 },
  'Mixto': { color: '#8b5cf6', fillColor: '#8b5cf6', fillOpacity: 0.3 }
};

const initMap = () => {
  if (map) return;

  console.log('[MapaIntegrado] Inicializando mapa...');

  map = L.map(mapContainer.value, {
    center: [-33.4489, -70.6693],
    zoom: 12,
    zoomControl: true
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(map);

  // Inicializar layer groups
  layerGroups.proyectos = L.layerGroup();
  layerGroups.puntos = L.layerGroup();
  layerGroups.zonas = L.layerGroup();

  // Añadir capas activas al mapa
  if (capasActivas.value.proyectos) layerGroups.proyectos.addTo(map);
  if (capasActivas.value.puntos) layerGroups.puntos.addTo(map);
  if (capasActivas.value.zonas) layerGroups.zonas.addTo(map);

  console.log('[MapaIntegrado] Mapa inicializado correctamente');
};

// FUNCIÓN CLAVE CORREGIDA: Obtener coordenadas desde los datos del backend
const getCoordenadas = (item) => {
  // Intentar múltiples propiedades comunes (camelCase y snake_case)
  const geojson = item.geometria ||
    item.ubicacion ||
    item.coordenadas ||
    item.coordenadas_punto ||
    item.coordenadasPunto ||  // camelCase
    item.geometria_poligono ||
    item.geometriaPoligono;    // camelCase

  if (!geojson) {
    console.warn('[MapaIntegrado] Item sin geometría:', item);
    console.warn('[MapaIntegrado] Propiedades disponibles:', Object.keys(item));
    return null;
  }

  return parseGeoJSON(geojson);
};

const parseGeoJSON = (geojson) => {
  if (!geojson) return null;

  try {
    const geo = typeof geojson === 'string' ? JSON.parse(geojson) : geojson;

    if (geo.type === 'Point' && geo.coordinates) {
      return [geo.coordinates[1], geo.coordinates[0]];
    }

    if (geo.type === 'Polygon' && geo.coordinates) {
      const ring = geo.coordinates[0];
      return ring.map(coord => [coord[1], coord[0]]);
    }

    if (geo.type === 'LineString' && geo.coordinates) {
      return geo.coordinates.map(coord => [coord[1], coord[0]]);
    }

    if (geo.type === 'MultiLineString' && geo.coordinates) {
      return geo.coordinates.map(line =>
        line.map(coord => [coord[1], coord[0]])
      );
    }

    if (geo.type === 'MultiPolygon' && geo.coordinates) {
      return geo.coordinates.map(polygon => {
        const ring = polygon[0];
        return ring.map(coord => [coord[1], coord[0]]);
      });
    }

    return null;
  } catch (error) {
    console.error('[MapaIntegrado] Error parsing GeoJSON:', error, geojson);
    return null;
  }
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

const actualizarProyectos = () => {
  console.log('[MapaIntegrado] actualizarProyectos() - Iniciando...');

  if (!layerGroups.proyectos) {
    console.warn('[MapaIntegrado] layerGroups.proyectos no está inicializado');
    return;
  }

  layerGroups.proyectos.clearLayers();
  let contador = 0;

  if (!capasActivas.value.proyectos) {
    console.log('[MapaIntegrado] Capa de proyectos desactivada');
    contadores.value.proyectos = 0;
    return;
  }

  console.log('[MapaIntegrado] Procesando', props.proyectos?.length || 0, 'proyectos...');

  props.proyectos.forEach((proyecto, index) => {
    const estado = proyecto.estado || 'Planeado';

    // Aplicar filtros
    const mostrarEstado = estado === 'Planeado' ? filtrosProyectos.value.planeado :
      estado === 'En Curso' ? filtrosProyectos.value.enCurso :
        estado === 'Completado' ? filtrosProyectos.value.completado :
          estado === 'Retrasado' ? filtrosProyectos.value.retrasado :
            filtrosProyectos.value.cancelado;

    if (!mostrarEstado) {
      return;
    }

    const coords = getCoordenadas(proyecto);
    if (!coords) {
      console.warn(`[MapaIntegrado] Proyecto #${index} "${proyecto.nombre}" sin coordenadas válidas`);
      return;
    }

    const config = estadoConfig[estado] || estadoConfig['Planeado'];

    let layer;
    if (Array.isArray(coords[0]) && Array.isArray(coords[0][0])) {
      // MultiPolygon o MultiLineString
      if (coords[0][0].length === 2 && typeof coords[0][0][0] === 'number') {
        // MultiLineString
        layer = L.polyline(coords, {
          ...config,
          weight: 3
        });
      } else {
        // MultiPolygon
        layer = L.polygon(coords, {
          ...config,
          weight: 2
        });
      }
    } else if (Array.isArray(coords[0]) && typeof coords[0][0] === 'number') {
      // LineString
      layer = L.polyline(coords, {
        ...config,
        weight: 3
      });
    } else if (Array.isArray(coords[0])) {
      // Polygon
      layer = L.polygon(coords, {
        ...config,
        weight: 2
      });
    } else {
      // Point
      layer = L.circleMarker(coords, {
        radius: 8,
        ...config,
        weight: 2
      });
    }

    layer.bindPopup(`
      <div class="popup-content">
        <h4>${proyecto.nombre}</h4>
        <p><strong>Estado:</strong> ${estado}</p>
        ${proyecto.descripcion ? `<p>${proyecto.descripcion}</p>` : ''}
        ${proyecto.presupuesto ? `<p><strong>Presupuesto:</strong> $${proyecto.presupuesto.toLocaleString()}</p>` : ''}
      </div>
    `);

    layer.on('click', () => {
      emit('item-selected', { tipo: 'proyecto', data: proyecto });
    });

    layer.addTo(layerGroups.proyectos);
    contador++;
  });

  contadores.value.proyectos = contador;
  console.log('[MapaIntegrado] Proyectos renderizados:', contador);
};

const actualizarPuntos = () => {
  console.log('[MapaIntegrado] actualizarPuntos() - Iniciando...');

  if (!layerGroups.puntos) {
    console.warn('[MapaIntegrado] layerGroups.puntos no está inicializado');
    return;
  }

  layerGroups.puntos.clearLayers();
  let contador = 0;

  if (!capasActivas.value.puntos) {
    console.log('[MapaIntegrado] Capa de puntos desactivada');
    contadores.value.puntos = 0;
    return;
  }

  console.log('[MapaIntegrado] Procesando', props.puntos?.length || 0, 'puntos...');

  props.puntos.forEach((punto, index) => {
    const tipoStr = getTipoString(punto.tipo);

    // Aplicar filtros
    const mostrarTipo = tipoStr === 'Hospital' ? filtrosPuntos.value.hospital :
      tipoStr === 'Escuela' ? filtrosPuntos.value.escuela :
        tipoStr === 'Parque' ? filtrosPuntos.value.parque :
          tipoStr === 'Centro Comercial' ? filtrosPuntos.value.centroComercial :
            filtrosPuntos.value.transporte;

    if (!mostrarTipo) {
      return;
    }

    const coords = getCoordenadas(punto);
    if (!coords) {
      console.warn(`[MapaIntegrado] Punto #${index} "${punto.nombre}" sin coordenadas válidas`);
      return;
    }

    const config = tipoConfigPuntos[tipoStr] || tipoConfigPuntos['Parque'];

    const icon = L.divIcon({
      className: 'custom-marker',
      html: `<div style="background-color: ${config.color}; width: 24px; height: 24px; border-radius: 50%; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"></div>`,
      iconSize: [24, 24],
      iconAnchor: [12, 12]
    });

    const marker = L.marker(coords, { icon });

    marker.bindPopup(`
      <div class="popup-content">
        <h4>${punto.nombre}</h4>
        <p><strong>Tipo:</strong> ${tipoStr}</p>
        ${punto.direccion ? `<p><strong>Dirección:</strong> ${punto.direccion}</p>` : ''}
      </div>
    `);

    marker.on('click', () => {
      emit('item-selected', { tipo: 'punto', data: punto });
    });

    marker.addTo(layerGroups.puntos);
    contador++;
  });

  contadores.value.puntos = contador;
  console.log('[MapaIntegrado] Puntos renderizados:', contador);
};

const actualizarZonas = () => {
  console.log('[MapaIntegrado] actualizarZonas() - Iniciando...');

  if (!layerGroups.zonas) {
    console.warn('[MapaIntegrado] layerGroups.zonas no está inicializado');
    return;
  }

  layerGroups.zonas.clearLayers();
  let contador = 0;

  if (!capasActivas.value.zonas) {
    console.log('[MapaIntegrado] Capa de zonas desactivada');
    contadores.value.zonas = 0;
    return;
  }

  console.log('[MapaIntegrado] Procesando', props.zonas?.length || 0, 'zonas...');

  props.zonas.forEach((zona, index) => {
    const tipoStr = getTipoString(zona.tipo);

    // Aplicar filtros
    const mostrarTipo = tipoStr === 'Residencial' ? filtrosZonas.value.residencial :
      tipoStr === 'Comercial' ? filtrosZonas.value.comercial :
        tipoStr === 'Industrial' ? filtrosZonas.value.industrial :
          filtrosZonas.value.mixto;

    if (!mostrarTipo) {
      return;
    }

    const coords = getCoordenadas(zona);
    if (!coords) {
      console.warn(`[MapaIntegrado] Zona #${index} "${zona.nombre}" sin coordenadas válidas`);
      return;
    }

    const config = tipoConfigZonas[tipoStr] || tipoConfigZonas['Residencial'];

    let layer;
    if (Array.isArray(coords[0]) && Array.isArray(coords[0][0])) {
      layer = L.polygon(coords, { ...config, weight: 2 });
    } else {
      layer = L.polygon(coords, { ...config, weight: 2 });
    }

    layer.bindPopup(`
      <div class="popup-content">
        <h4>${zona.nombre}</h4>
        <p><strong>Tipo:</strong> ${tipoStr}</p>
        ${zona.area ? `<p><strong>Área:</strong> ${zona.area.toFixed(2)} km²</p>` : ''}
        ${zona.poblacion_estimada_2025 ? `<p><strong>Población:</strong> ${zona.poblacion_estimada_2025.toLocaleString()}</p>` : ''}
      </div>
    `);

    layer.on('click', () => {
      emit('item-selected', { tipo: 'zona', data: zona });
    });

    layer.addTo(layerGroups.zonas);
    contador++;
  });

  contadores.value.zonas = contador;
  console.log('[MapaIntegrado] Zonas renderizadas:', contador);
};

const toggleCapa = (capa) => {
  console.log(`[MapaIntegrado] Toggle capa: ${capa}, activa: ${capasActivas.value[capa]}`);

  if (!map || !layerGroups[capa]) {
    console.warn('[MapaIntegrado] Mapa o layer group no inicializado');
    return;
  }

  if (capasActivas.value[capa]) {
    layerGroups[capa].addTo(map);

    if (capa === 'proyectos') actualizarProyectos();
    if (capa === 'puntos') actualizarPuntos();
    if (capa === 'zonas') actualizarZonas();
  } else {
    layerGroups[capa].clearLayers();
    map.removeLayer(layerGroups[capa]);
    contadores.value[capa] = 0;
  }
};

onMounted(() => {
  console.log('[MapaIntegrado] Componente montado');
  console.log('[MapaIntegrado] Datos recibidos:', {
    proyectos: props.proyectos?.length || 0,
    puntos: props.puntos?.length || 0,
    zonas: props.zonas?.length || 0
  });

  initMap();

  // Cargar datos iniciales si las capas están activas
  if (capasActivas.value.proyectos) actualizarProyectos();
  if (capasActivas.value.puntos) actualizarPuntos();
  if (capasActivas.value.zonas) actualizarZonas();
});

// Watchers
watch(() => props.proyectos, (newVal) => {
  console.log('[MapaIntegrado] Proyectos actualizados:', newVal?.length || 0);
  if (capasActivas.value.proyectos) {
    actualizarProyectos();
  }
}, { deep: true });

watch(() => props.puntos, (newVal) => {
  console.log('[MapaIntegrado] Puntos actualizados:', newVal?.length || 0);
  if (capasActivas.value.puntos) {
    actualizarPuntos();
  }
}, { deep: true });

watch(() => props.zonas, (newVal) => {
  console.log('[MapaIntegrado] Zonas actualizadas:', newVal?.length || 0);
  if (capasActivas.value.zonas) {
    actualizarZonas();
  }
}, { deep: true });

onBeforeUnmount(() => {
  if (map) {
    map.remove();
    map = null;
  }
});
</script>

<style scoped>
.mapa-integrado-wrapper {
  position: relative;
  display: flex;
  gap: 20px;
  height: 600px;
  border-radius: 12px;
  overflow: hidden;
}

.capas-control {
  width: 320px;
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 20px;
  overflow-y: auto;
  flex-shrink: 0;
}

.capas-control h3 {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 20px 0;
}

.capa-group {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.capa-group:last-of-type {
  border-bottom: none;
}

.capa-header {
  margin-bottom: 12px;
}

.capa-toggle {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
}

.capa-toggle input[type="checkbox"] {
  display: none;
}

.toggle-slider {
  position: relative;
  width: 44px;
  height: 24px;
  background-color: var(--border-color);
  border-radius: 24px;
  transition: background-color 0.3s;
  flex-shrink: 0;
}

.toggle-slider::before {
  content: '';
  position: absolute;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  top: 3px;
  left: 3px;
  background-color: white;
  transition: transform 0.3s;
}

.capa-toggle input[type="checkbox"]:checked + .toggle-slider {
  background-color: var(--accent-primary);
}

.capa-toggle input[type="checkbox"]:checked + .toggle-slider::before {
  transform: translateX(20px);
}

.capa-nombre {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.capa-filtros {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding-left: 12px;
}

.filtro-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  user-select: none;
  padding: 4px 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.filtro-item:hover {
  background-color: var(--bg-primary);
}

.filtro-item input[type="checkbox"] {
  cursor: pointer;
}

.filtro-color {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 2px solid var(--border-color);
  flex-shrink: 0;
}

/* Colores proyectos */
.filtro-color.planeado { background-color: #3b82f6; }
.filtro-color.en-curso { background-color: #f59e0b; }
.filtro-color.completado { background-color: #10b981; }
.filtro-color.retrasado { background-color: #ef4444; }

/* Colores puntos */
.filtro-color.hospital { background-color: #ef4444; }
.filtro-color.escuela { background-color: #3b82f6; }
.filtro-color.parque { background-color: #10b981; }
.filtro-color.centro-comercial { background-color: #f59e0b; }
.filtro-color.transporte { background-color: #8b5cf6; }

/* Colores zonas */
.filtro-color.zona-residencial { background-color: #3b82f6; }
.filtro-color.zona-comercial { background-color: #f59e0b; }
.filtro-color.zona-industrial { background-color: #ef4444; }
.filtro-color.zona-mixto { background-color: #8b5cf6; }

.estadisticas-capas {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
}

.stat-label {
  color: var(--text-secondary);
}

.stat-value {
  font-weight: 600;
  color: var(--accent-primary);
  background: var(--icon-zonas-bg);
  padding: 2px 8px;
  border-radius: 12px;
}

.mapa-leaflet {
  flex: 1;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  overflow: hidden;
}

:deep(.popup-content) {
  font-family: inherit;
}

:deep(.popup-content h4) {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin: 0 0 8px 0;
}

:deep(.popup-content p) {
  font-size: 13px;
  color: var(--text-secondary);
  margin: 4px 0;
}

:deep(.custom-marker) {
  background: transparent !important;
  border: none !important;
}

@media (max-width: 1024px) {
  .mapa-integrado-wrapper {
    flex-direction: column;
    height: auto;
  }

  .capas-control {
    width: 100%;
  }

  .mapa-leaflet {
    height: 500px;
  }
}
</style>
