<template>
  <div class="mapa-proximidad-wrapper">
    <div ref="mapContainer" class="mapa-leaflet"></div>

    <!-- Leyenda simplificada -->
    <div class="leyenda-container">
      <h4>📍 Leyenda</h4>
      <p class="leyenda-descripcion">Escuelas y proyectos cercanos (< 500m)</p>
      <div class="leyenda-divider"></div>
      <div class="leyenda-item">
        <span class="marker-icon">🏫</span>
        <span>Escuela</span>
      </div>
      <div class="leyenda-item">
        <span class="marker-icon">🚧</span>
        <span>Proyecto en Curso</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import api from '@/services/api';

const props = defineProps({
  resultados: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits(['error']);

const mapContainer = ref(null);
let map = null;
let escuelasLayer = null;
let proyectosLayer = null;
let lineasLayer = null;

const escuelasCache = new Map();
const proyectosCache = new Map();

const initMap = () => {
  if (map) return;

  map = L.map(mapContainer.value, {
    center: [-33.4489, -70.6693],
    zoom: 12,
    zoomControl: true,
    attributionControl: false
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '',
    maxZoom: 18
  }).addTo(map);

  escuelasLayer = L.layerGroup().addTo(map);
  proyectosLayer = L.layerGroup().addTo(map);
  lineasLayer = L.layerGroup().addTo(map);
};

const cargarDatosYVisualizarProximidad = async () => {
  if (!map || props.resultados.length === 0) return;

  escuelasLayer.clearLayers();
  proyectosLayer.clearLayers();
  lineasLayer.clearLayers();

  try {
    console.log(' Datos recibidos desde backend:', props.resultados.length, 'registros');
    if (props.resultados.length > 0) {
      console.log(' Primer registro:', props.resultados[0]);
    }

    const escuelasUnicas = [...new Set(props.resultados.map(r => r.nombre_escuela))];
    const proyectosUnicos = [...new Set(props.resultados.map(r => r.nombre_proyecto))];

    console.log(' Cargando', escuelasUnicas.length, 'escuelas y', proyectosUnicos.length, 'proyectos');
    console.log(' Nombres de escuelas:', escuelasUnicas);
    console.log(' Nombres de proyectos:', proyectosUnicos);

    const escuelasPromises = escuelasUnicas.map(async (nombreEscuela) => {
      if (escuelasCache.has(nombreEscuela)) {
        return escuelasCache.get(nombreEscuela);
      }

      try {
        const response = await api.get('/puntos-interes', { params: { size: -1 } });
        const escuela = response.data.find(p =>
          p.nombre === nombreEscuela && p.tipo === 'ESCUELA'
        );
        if (escuela) {
          escuelasCache.set(nombreEscuela, escuela);
        }
        return escuela;
      } catch (error) {
        console.error(`Error cargando escuela ${nombreEscuela}:`, error);
        return null;
      }
    });

    const proyectosPromises = proyectosUnicos.map(async (nombreProyecto) => {
      if (proyectosCache.has(nombreProyecto)) {
        console.log(` Proyecto en caché: ${nombreProyecto}`);
        return proyectosCache.get(nombreProyecto);
      }

      try {
        // Obtener todos los proyectos con size=-1 para que devuelva un array
        const response = await api.get('/proyectos', { params: { size: -1 } });

        // Verificar si la respuesta es un array o un objeto paginado
        const proyectos = Array.isArray(response.data) ? response.data : response.data.content || [];

        console.log(` Buscando proyecto: "${nombreProyecto}" entre ${proyectos.length} proyectos`);

        // Listar algunos nombres de proyectos disponibles para comparar
        console.log(`📋Primeros proyectos disponibles:`, proyectos.slice(0, 5).map(p => `"${p.nombre}"`));

        // Buscar el proyecto SIN filtrar por estado (la consulta SQL ya lo hace)
        const proyecto = proyectos.find(p => p.nombre === nombreProyecto);

        if (proyecto) {
          console.log(`Proyecto encontrado: "${nombreProyecto}"`, {
            estado: proyecto.estado,
            tipo: proyecto.tipoProyecto,
            tieneGeometria: !!proyecto.geometria
          });
          proyectosCache.set(nombreProyecto, proyecto);
        } else {
          console.log(`Proyecto NO encontrado: "${nombreProyecto}"`);
          console.log(`   Intentando búsqueda case-insensitive...`);

          // Intentar búsqueda case-insensitive por si hay diferencias
          const proyectoInsensitive = proyectos.find(p =>
            p.nombre.toLowerCase().trim() === nombreProyecto.toLowerCase().trim()
          );

          if (proyectoInsensitive) {
            console.log(` Proyecto encontrado (insensitive): "${proyectoInsensitive.nombre}"`);
            proyectosCache.set(nombreProyecto, proyectoInsensitive);
            return proyectoInsensitive;
          }
        }
        return proyecto;
      } catch (error) {
        console.error(` Error cargando proyecto ${nombreProyecto}:`, error);
        return null;
      }
    });

    const escuelas = await Promise.all(escuelasPromises);
    const proyectos = await Promise.all(proyectosPromises);

    const escuelasValidas = escuelas.filter(e => e !== null);
    const proyectosValidos = proyectos.filter(p => p !== null);

    console.log(' Escuelas válidas:', escuelasValidas.length);
    console.log(' Proyectos válidos:', proyectosValidos.length);

    if (proyectosValidos.length > 0) {
      console.log(' Proyectos a mostrar:');
      proyectosValidos.forEach(p => {
        console.log(`  - "${p.nombre}" (Estado: ${p.estado}, Tipo: ${p.tipoProyecto})`);
      });
    } else {
      console.warn(' No hay proyectos válidos para mostrar');
      console.log(' Nombres esperados desde resultados:', proyectosUnicos);
    }

    // Agregar escuelas
    escuelasValidas.forEach(escuela => {
      try {
        const geojson = JSON.parse(escuela.coordenadasPunto);
        const [lng, lat] = geojson.coordinates;

        const icon = L.divIcon({
          html: '<div class="custom-marker escuela">🏫</div>',
          className: 'custom-marker-wrapper',
          iconSize: [50, 50],
          iconAnchor: [25, 50],
          popupAnchor: [0, -50]
        });

        const marker = L.marker([lat, lng], { icon })
          .bindPopup(`
            <div class="popup-content">
              <h3>🏫 ${escuela.nombre}</h3>
              <p><strong>Tipo:</strong> Escuela</p>
              <p><strong>Dirección:</strong> ${escuela.direccion || 'No especificada'}</p>
            </div>
          `);

        escuelasLayer.addLayer(marker);
      } catch (error) {
        console.error('Error procesando escuela:', error);
      }
    });

    // Agregar proyectos
    proyectosValidos.forEach(proyecto => {
      try {
        const geojson = JSON.parse(proyecto.geometria);
        console.log(' Proyecto:', proyecto.nombre, 'Tipo:', geojson.type);

        let layer;
        let centroide = null; // Para calcular el centroide y poner el marcador

        if (geojson.type === 'Point') {
          const [lng, lat] = geojson.coordinates;
          centroide = [lat, lng];

          const icon = L.divIcon({
            html: '<div class="custom-marker proyecto">🚧</div>',
            className: 'custom-marker-wrapper',
            iconSize: [50, 50],
            iconAnchor: [25, 50],
            popupAnchor: [0, -50]
          });

          layer = L.marker([lat, lng], { icon });
        } else if (geojson.type === 'Polygon' || geojson.type === 'MultiPolygon') {
          // Dibujar el polígono
          layer = L.geoJSON(geojson, {
            style: {
              color: '#f59e0b',
              weight: 4,
              fillColor: '#fef3c7',
              fillOpacity: 0.5
            }
          });

          // Calcular centroide del polígono para el marcador
          const coordinates = geojson.type === 'Polygon'
            ? geojson.coordinates[0]
            : geojson.coordinates[0][0];
          const centroidCoords = getCentroid(coordinates);
          centroide = [centroidCoords[1], centroidCoords[0]];

        } else if (geojson.type === 'LineString' || geojson.type === 'MultiLineString') {
          // Dibujar la línea
          layer = L.geoJSON(geojson, {
            style: {
              color: '#f59e0b',
              weight: 5
            }
          });

          // Calcular punto medio de la línea para el marcador
          const coordinates = geojson.type === 'LineString'
            ? geojson.coordinates
            : geojson.coordinates[0];
          const midIndex = Math.floor(coordinates.length / 2);
          const [lng, lat] = coordinates[midIndex];
          centroide = [lat, lng];
        }

        if (layer) {
          const popupContent = `
            <div class="popup-content">
              <h3>🚧 ${proyecto.nombre}</h3>
              <p><strong>Estado:</strong> ${proyecto.estado}</p>
              <p><strong>Tipo:</strong> ${proyecto.tipoProyecto}</p>
              <p><strong>Descripción:</strong> ${proyecto.descripcion || 'No especificada'}</p>
            </div>
          `;

          layer.bindPopup(popupContent);
          proyectosLayer.addLayer(layer);

          // Agregar marcador 🚧 en el centroide para LineString y Polygon
          if (centroide && geojson.type !== 'Point') {
            const icon = L.divIcon({
              html: '<div class="custom-marker proyecto">🚧</div>',
              className: 'custom-marker-wrapper',
              iconSize: [50, 50],
              iconAnchor: [25, 50],
              popupAnchor: [0, -50]
            });

            const marker = L.marker(centroide, { icon, zIndexOffset: 1000 })
              .bindPopup(popupContent);

            proyectosLayer.addLayer(marker);
            console.log(' Marcador agregado en centroide para:', proyecto.nombre);
          }

          console.log(' Proyecto agregado:', proyecto.nombre);
        }
      } catch (error) {
        console.error('Error procesando proyecto:', error);
      }
    });

    // Dibujar líneas - DESHABILITADO para simplificar visualización
    /*
    console.log('🔗 Dibujando', props.resultados.length, 'líneas');
    let lineasDibujadas = 0;

    props.resultados.forEach(resultado => {
      const escuela = escuelasValidas.find(e => e.nombre === resultado.nombre_escuela);
      const proyecto = proyectosValidos.find(p => p.nombre === resultado.nombre_proyecto);

      if (escuela && proyecto) {
        try {
          const escuelaGeojson = JSON.parse(escuela.coordenadasPunto);
          const proyectoGeojson = JSON.parse(proyecto.geometria);

          let escuelaCoords = escuelaGeojson.coordinates;
          let proyectoCoords;

          if (proyectoGeojson.type === 'Point') {
            proyectoCoords = proyectoGeojson.coordinates;
          } else if (proyectoGeojson.type === 'Polygon') {
            proyectoCoords = getCentroid(proyectoGeojson.coordinates[0]);
          } else if (proyectoGeojson.type === 'LineString') {
            proyectoCoords = proyectoGeojson.coordinates[0];
          } else {
            return;
          }

          const distancia = resultado.distancia_metros;
          let color, weight;

          if (distancia < 250) {
            color = '#dc2626';
            weight = 5;
          } else if (distancia < 400) {
            color = '#f59e0b';
            weight = 5;
          } else {
            color = '#10b981';
            weight = 5;
          }

          const line = L.polyline(
            [
              [escuelaCoords[1], escuelaCoords[0]],
              [proyectoCoords[1], proyectoCoords[0]]
            ],
            {
              color: color,
              weight: weight,
              opacity: 0.95,
              dashArray: '12, 6'
            }
          ).bindPopup(`
            <div class="popup-content">
              <h3>📏 Conexión</h3>
              <p><strong>Escuela:</strong> ${resultado.nombre_escuela}</p>
              <p><strong>Proyecto:</strong> ${resultado.nombre_proyecto}</p>
              <p><strong>Distancia:</strong> ${resultado.distancia_metros} m</p>
            </div>
          `);

          lineasLayer.addLayer(line);
          lineasDibujadas++;
          console.log(` Línea ${lineasDibujadas}: ${resultado.nombre_escuela} ↔ ${resultado.nombre_proyecto}`);
        } catch (error) {
          console.error('Error dibujando línea:', error);
        }
      }
    });

    console.log(` Total líneas dibujadas: ${lineasDibujadas}`);
    */

    console.log(' Mapa cargado: escuelas y proyectos mostrados (sin líneas de conexión)');

    const allLayers = [...escuelasLayer.getLayers(), ...proyectosLayer.getLayers()];
    if (allLayers.length > 0) {
      const group = L.featureGroup(allLayers);
      map.fitBounds(group.getBounds().pad(0.1));
    }

  } catch (error) {
    console.error('Error cargando datos:', error);
    emit('error', 'Error al cargar los datos en el mapa');
  }
};

const getCentroid = (coords) => {
  let x = 0, y = 0;
  coords.forEach(coord => {
    x += coord[0];
    y += coord[1];
  });
  return [x / coords.length, y / coords.length];
};

watch(() => props.resultados, () => {
  if (map) {
    cargarDatosYVisualizarProximidad();
  }
}, { deep: true });

onMounted(() => {
  initMap();
  if (props.resultados.length > 0) {
    cargarDatosYVisualizarProximidad();
  }
});

onBeforeUnmount(() => {
  if (map) {
    map.remove();
    map = null;
  }
  escuelasCache.clear();
  proyectosCache.clear();
});
</script>

<style scoped>
.mapa-proximidad-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
}

.mapa-leaflet {
  width: 100%;
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.leyenda-container {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.98);
  padding: 0.5rem 0.6rem;
  border-radius: 4px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.25);
  z-index: 1000;
  max-width: 160px;
  backdrop-filter: blur(4px);
}

.leyenda-container h4 {
  margin: 0 0 0.4rem 0;
  font-size: 0.8rem;
  color: #1f2937;
  font-weight: 700;
  text-align: center;
}

.leyenda-descripcion {
  margin: 0 0 0.4rem 0;
  font-size: 0.65rem;
  color: #6b7280;
  line-height: 1.2;
  font-style: italic;
  text-align: center;
}

.leyenda-subtitle {
  margin: 0.4rem 0 0.3rem 0;
  font-size: 0.7rem;
  color: #374151;
  font-weight: 600;
  text-align: center;
}

.leyenda-item {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.3rem;
  font-size: 0.7rem;
  color: #374151;
  font-weight: 500;
}

.leyenda-item:last-child {
  margin-bottom: 0;
}

.marker-icon {
  font-size: 1rem;
  width: 18px;
  text-align: center;
  flex-shrink: 0;
}

/* Estilos de líneas - NO UTILIZADOS (líneas deshabilitadas)
.line {
  width: 20px;
  height: 3px;
  border-radius: 1px;
  flex-shrink: 0;
}

.line.muy-cerca {
  background: #dc2626;
  box-shadow: 0 0 3px rgba(220, 38, 38, 0.6);
}

.line.cerca {
  background: #f59e0b;
  box-shadow: 0 0 3px rgba(245, 158, 11, 0.6);
}

.line.moderada {
  background: #10b981;
  box-shadow: 0 0 3px rgba(16, 185, 129, 0.6);
}

.leyenda-subtitle {
  margin: 0.4rem 0 0.3rem 0;
  font-size: 0.7rem;
  color: #374151;
  font-weight: 600;
  text-align: center;
}
*/

.leyenda-divider {
  height: 1px;
  background: #d1d5db;
  margin: 0.3rem 0;
}

:deep(.custom-marker-wrapper) {
  background: transparent;
  border: none;
}

:deep(.custom-marker) {
  font-size: 2.5rem;
  text-shadow: 0 3px 6px rgba(0,0,0,0.4);
  cursor: pointer;
  transition: transform 0.2s;
  filter: drop-shadow(0 0 3px rgba(255,255,255,0.5));
}

:deep(.custom-marker:hover) {
  transform: scale(1.3);
}

:deep(.custom-marker.proyecto) {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

:deep(.popup-content) {
  font-family: inherit;
  min-width: 200px;
  background: white;
  padding: 0.5rem;
}

:deep(.popup-content h3) {
  margin: 0 0 0.75rem 0;
  font-size: 1.1rem;
  color: #2563eb;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

:deep(.popup-content p) {
  margin: 0.5rem 0;
  font-size: 0.9rem;
  color: #1f2937;
}

:deep(.popup-content p strong) {
  color: #4b5563;
  font-weight: 600;
}

@media (max-width: 768px) {
  .leyenda-container {
    bottom: 10px;
    right: 10px;
    left: 10px;
    min-width: auto;
    padding: 0.75rem;
    max-width: none;
  }
}
</style>

