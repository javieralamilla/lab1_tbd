<template>
  <div class="mapa-simulacion-wrapper">
    <div ref="mapContainer" class="mapa-leaflet"></div>

    <!-- Panel de control de simulación -->
    <div class="panel-simulacion">
      <div class="simulacion-header">
        <h3>Simulación de Proyectos</h3>
        <button v-if="!modoSimulacion" @click="iniciarSimulacion" class="btn-iniciar">
          ✏️ Iniciar Simulación
        </button>
        <button v-else @click="cancelarSimulacion" class="btn-cancelar">
          Cancelar
        </button>
      </div>

      <!-- Formulario de simulación -->
      <div v-if="modoSimulacion && areaSimulada" class="formulario-simulacion">
        <h4>Configurar Proyecto</h4>

        <div class="form-group">
          <label>Tipo de Proyecto:</label>
          <select v-model="simulacion.tipoProyecto">
            <option value="RESIDENCIAL">🏘️ Residencial</option>
            <option value="COMERCIAL">🏬 Comercial</option>
            <option value="INDUSTRIAL">🏭 Industrial</option>
            <option value="EDUCATIVO">Educativo</option>
            <option value="SALUD">Salud</option>
            <option value="RECREATIVO">🎡 Recreativo</option>
            <option value="TRANSPORTE">🚇 Transporte</option>
          </select>
        </div>

        <div class="form-group" v-if="simulacion.tipoProyecto === 'RESIDENCIAL'">
          <label>Población Estimada:</label>
          <input
            type="number"
            v-model.number="simulacion.poblacionEstimada"
            placeholder="Ej: 5000"
            min="0"
          />
        </div>

        <div class="form-group" v-if="simulacion.tipoProyecto === 'EDUCATIVO'">
          <label>Número de Escuelas:</label>
          <input
            type="number"
            v-model.number="simulacion.numEscuelas"
            placeholder="Ej: 2"
            min="0"
          />
        </div>

        <div class="form-group" v-if="simulacion.tipoProyecto === 'SALUD'">
          <label>Número de Hospitales:</label>
          <input
            type="number"
            v-model.number="simulacion.numHospitales"
            placeholder="Ej: 1"
            min="0"
          />
        </div>

        <div class="form-group">
          <label>Impacto en Población (%):</label>
          <input
            type="number"
            v-model.number="simulacion.impactoPoblacion"
            placeholder="Ej: 10"
            min="-100"
            max="100"
          />
          <small>Incremento o reducción porcentual de población circundante</small>
        </div>

        <div class="form-group">
          <label>Área de Influencia (metros):</label>
          <input
            type="number"
            v-model.number="simulacion.areaInfluencia"
            placeholder="Ej: 1000"
            min="100"
            max="5000"
            step="100"
          />
          <small>Radio de impacto del proyecto en metros</small>
        </div>

        <div class="form-actions">
          <button @click="calcularImpacto" class="btn-calcular" :disabled="loading">
            Calcular Impacto
          </button>
          <button @click="mostrarModalGuardar" class="btn-guardar" :disabled="!resultadoImpacto || loading">
            💾 Guardar Simulación
          </button>
        </div>
      </div>

      <!-- Resultados del impacto -->
      <div v-if="resultadoImpacto" class="resultados-impacto">
        <h4>Impacto Proyectado</h4>

        <div class="comparacion-grid">
          <div class="comparacion-card">
            <h5>Estado Actual</h5>
            <div class="stat-item">
              <span class="label">👥 Población:</span>
              <span class="value">{{ formatNumber(resultadoImpacto.actual.poblacion) }}</span>
            </div>
            <div class="stat-item">
              <span class="label">🏫 Escuelas:</span>
              <span class="value">{{ resultadoImpacto.actual.escuelas }}</span>
            </div>
            <div class="stat-item">
              <span class="label">🏥 Hospitales:</span>
              <span class="value">{{ resultadoImpacto.actual.hospitales }}</span>
            </div>
            <div class="stat-item">
              <span class="label">🚧 Proyectos:</span>
              <span class="value">{{ resultadoImpacto.actual.proyectos }}</span>
            </div>
          </div>

          <div class="comparacion-card proyectado">
            <h5>Con Nuevo Proyecto</h5>
            <div class="stat-item">
              <span class="label">👥 Población:</span>
              <span class="value">{{ formatNumber(resultadoImpacto.proyectado.poblacion) }}</span>
              <span class="diferencia" :class="getDiferenciaClass(resultadoImpacto.diferencias.poblacion)">
                {{ formatDiferencia(resultadoImpacto.diferencias.poblacion) }}
              </span>
            </div>
            <div class="stat-item">
              <span class="label">🏫 Escuelas:</span>
              <span class="value">{{ resultadoImpacto.proyectado.escuelas }}</span>
              <span class="diferencia" :class="getDiferenciaClass(resultadoImpacto.diferencias.escuelas)">
                {{ formatDiferencia(resultadoImpacto.diferencias.escuelas) }}
              </span>
            </div>
            <div class="stat-item">
              <span class="label">🏥 Hospitales:</span>
              <span class="value">{{ resultadoImpacto.proyectado.hospitales }}</span>
              <span class="diferencia" :class="getDiferenciaClass(resultadoImpacto.diferencias.hospitales)">
                {{ formatDiferencia(resultadoImpacto.diferencias.hospitales) }}
              </span>
            </div>
            <div class="stat-item">
              <span class="label">🚧 Proyectos:</span>
              <span class="value">{{ resultadoImpacto.proyectado.proyectos }}</span>
              <span class="diferencia positivo">+1</span>
            </div>
          </div>
        </div>

        <!-- Métricas adicionales -->
        <div class="metricas-adicionales">
          <div class="metrica-card">
            <div class="metrica-icon">📏</div>
            <div class="metrica-content">
              <span class="metrica-label">Área del Proyecto</span>
              <span class="metrica-value">{{ resultadoImpacto.area_km2 }} km²</span>
            </div>
          </div>
          <div class="metrica-card">
            <div class="metrica-icon"></div>
            <div class="metrica-content">
              <span class="metrica-label">Área de Influencia</span>
              <span class="metrica-value">{{ simulacion.areaInfluencia }} m</span>
            </div>
          </div>
          <div class="metrica-card">
            <div class="metrica-icon"></div>
            <div class="metrica-content">
              <span class="metrica-label">Densidad Población</span>
              <span class="metrica-value">
                {{ calcularDensidad(resultadoImpacto.proyectado.poblacion, Math.PI * Math.pow(resultadoImpacto.area_influencia_m / 1000, 2)) }} hab/km²
              </span>
            </div>
            <div class="metrica-sublabel" style="font-size: 0.7rem; color: var(--text-secondary);">
              (Calculado sobre área de influencia)
            </div>
          </div>
        </div>
      </div>

      <!-- Lista de simulaciones guardadas -->
      <div v-if="simulacionesGuardadas.length > 0" class="simulaciones-guardadas">
        <h4>💾 Simulaciones Guardadas</h4>
        <div class="simulacion-item" v-for="(sim, index) in simulacionesGuardadas" :key="index">
          <div class="sim-header">
            <span class="sim-tipo">{{ getIconoTipo(sim.tipo) }} {{ sim.tipo }}</span>
            <button @click="eliminarSimulacion(index)" class="btn-eliminar-sim">🗑️</button>
          </div>
          <div class="sim-info">
            <small>Impacto población: {{ formatDiferencia(sim.impactoPoblacion) }}</small>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal para guardar simulación -->
    <div v-if="showGuardarModal" class="modal-overlay" @click.self="showGuardarModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>💾 Guardar Simulación</h3>
          <button @click="showGuardarModal = false" class="btn-close">✕</button>
        </div>
        <div class="modal-body">
          <p>Selecciona cómo deseas guardar esta simulación:</p>
          <div class="opciones-guardar">
            <button @click="guardarComoProyecto" class="btn-opcion-guardar" :disabled="guardando">
              <div class="opcion-icon">📋</div>
              <div class="opcion-info">
                <h4>Guardar como Proyecto</h4>
                <p>Crea un nuevo proyecto en el sistema con los parámetros de la simulación</p>
              </div>
            </button>
            <button @click="guardarComoDatoDemografico" class="btn-opcion-guardar" :disabled="guardando">
              <div class="opcion-icon">📊</div>
              <div class="opcion-info">
                <h4>Guardar como Dato Demográfico</h4>
                <p>Registra el impacto poblacional proyectado como dato demográfico</p>
              </div>
            </button>
          </div>
          <div class="modal-actions">
            <button @click="guardarSimulacion" class="btn-guardar-local" :disabled="guardando">
              💾 Solo guardar localmente
            </button>
          </div>
        </div>
      </div>
    </div>


    <!-- Indicador de carga -->
    <div v-if="loading" class="loading-overlay">
      <div class="spinner"></div>
      <p>Calculando impacto...</p>
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
import { useAuthStore } from '@/stores/auth';

const emit = defineEmits(['error', 'success']);
const authStore = useAuthStore();

const mapContainer = ref(null);
let map = null;
let drawnItems = null;
let drawControl = null;
let areaInfluenciaCircle = null;

const modoSimulacion = ref(false);
const areaSimulada = ref(null);
const loading = ref(false);
const resultadoImpacto = ref(null);
const simulacionesGuardadas = ref([]);
const showGuardarModal = ref(false);
const guardando = ref(false);

const simulacion = ref({
  tipoProyecto: 'RESIDENCIAL',
  poblacionEstimada: 0,
  numEscuelas: 0,
  numHospitales: 0,
  impactoPoblacion: 0,
  areaInfluencia: 1000
});

const initMap = () => {
  if (map) return;

  map = L.map(mapContainer.value, {
    center: [-33.4489, -70.6693],
    zoom: 12,
    zoomControl: true
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap contributors',
    maxZoom: 18
  }).addTo(map);

  drawnItems = new L.FeatureGroup();
  map.addLayer(drawnItems);
};

const iniciarSimulacion = () => {
  modoSimulacion.value = true;

  // Configurar controles de dibujo
  if (drawControl) {
    map.removeControl(drawControl);
  }

  drawControl = new L.Control.Draw({
    position: 'topright',
    draw: {
      polygon: {
        allowIntersection: false,
        showArea: false,
        shapeOptions: {
          color: '#f59e0b',
          fillColor: '#fbbf24',
          fillOpacity: 0.4,
          weight: 3
        }
      },
      rectangle: {
        showArea: false,
        shapeOptions: {
          color: '#f59e0b',
          fillColor: '#fbbf24',
          fillOpacity: 0.4,
          weight: 3
        }
      },
      circle: false,
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
  map.on(L.Draw.Event.CREATED, (e) => {
    drawnItems.clearLayers();
    areaSimulada.value = e.layer;
    drawnItems.addLayer(e.layer);
    resultadoImpacto.value = null;
  });

  map.on(L.Draw.Event.DELETED, () => {
    areaSimulada.value = null;
    resultadoImpacto.value = null;
    if (areaInfluenciaCircle) {
      map.removeLayer(areaInfluenciaCircle);
      areaInfluenciaCircle = null;
    }
  });
};

const cancelarSimulacion = () => {
  modoSimulacion.value = false;
  areaSimulada.value = null;
  resultadoImpacto.value = null;
  drawnItems.clearLayers();

  if (drawControl) {
    map.removeControl(drawControl);
    drawControl = null;
  }

  if (areaInfluenciaCircle) {
    map.removeLayer(areaInfluenciaCircle);
    areaInfluenciaCircle = null;
  }
};

const calcularImpacto = async () => {
  if (!areaSimulada.value) {
    emit('error', 'Debe dibujar un área en el mapa primero');
    return;
  }

  loading.value = true;

  try {
    // Obtener el centro del área para mostrar el área de influencia
    const bounds = areaSimulada.value.getBounds();
    const center = bounds.getCenter();

    // Mostrar área de influencia
    if (areaInfluenciaCircle) {
      map.removeLayer(areaInfluenciaCircle);
    }

    areaInfluenciaCircle = L.circle(center, {
      radius: simulacion.value.areaInfluencia,
      color: '#3b82f6',
      fillColor: '#60a5fa',
      fillOpacity: 0.15,
      weight: 2,
      dashArray: '5, 10'
    }).addTo(map);

    // Convertir área a GeoJSON
    const geojson = areaSimulada.value.toGeoJSON();

    // Preparar datos de simulación
    const datosSimulacion = {
      geojson: JSON.stringify(geojson.geometry),
      tipoProyecto: simulacion.value.tipoProyecto,
      poblacionEstimada: simulacion.value.poblacionEstimada || 0,
      numEscuelas: simulacion.value.numEscuelas || 0,
      numHospitales: simulacion.value.numHospitales || 0,
      impactoPoblacion: simulacion.value.impactoPoblacion || 0,
      areaInfluencia: simulacion.value.areaInfluencia
    };

    // Enviar al backend
    const response = await api.post('/zonas/simulacion/impacto', datosSimulacion);

    resultadoImpacto.value = response.data;
    emit('success', 'Impacto calculado exitosamente');

  } catch (error) {
    console.error('Error al calcular impacto:', error);
    emit('error', 'Error al calcular el impacto del proyecto');
  } finally {
    loading.value = false;
  }
};

const guardarSimulacion = () => {
  if (!resultadoImpacto.value) return;

  const simulacionGuardada = {
    tipo: simulacion.value.tipoProyecto,
    area: resultadoImpacto.value.area_km2,
    impactoPoblacion: resultadoImpacto.value.diferencias.poblacion,
    geometria: areaSimulada.value.toGeoJSON(),
    fecha: new Date().toISOString(),
    parametros: { ...simulacion.value }
  };

  simulacionesGuardadas.value.push(simulacionGuardada);

  // Guardar en localStorage
  localStorage.setItem('simulaciones', JSON.stringify(simulacionesGuardadas.value));

  showGuardarModal.value = false;
  emit('success', 'Simulación guardada localmente');
};

const mostrarModalGuardar = () => {
  if (!resultadoImpacto.value) {
    emit('error', 'Debes calcular el impacto antes de guardar');
    return;
  }
  showGuardarModal.value = true;
};

const guardarComoProyecto = async () => {
  try {
    guardando.value = true;

    // Obtener ID del usuario actual
    const usuarioId = authStore.user?.usuario_id || authStore.user?.usuarioId;
    
    if (!usuarioId) {
      emit('error', 'Debes iniciar sesión para crear proyectos');
      guardando.value = false;
      return;
    }

    // Preparar datos del proyecto basados en la simulación
    const geojson = areaSimulada.value.toGeoJSON();
    
    const proyectoData = {
      nombre: `Proyecto Simulado - ${simulacion.value.tipoProyecto}`,
      descripcion: `Proyecto generado desde simulación. Impacto población: ${formatDiferencia(resultadoImpacto.value.diferencias.poblacion)}`,
      tipoProyecto: simulacion.value.tipoProyecto,
      presupuesto: 0, // Se puede ajustar según necesidad
      estado: 'PLANIFICADO',
      fechaInicio: new Date().toISOString().split('T')[0],
      fechaFinEstimada: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // +1 año
      geometria: JSON.stringify(geojson.geometry),
      usuarioId: usuarioId,
      usuario_id: usuarioId,
      // Incluir parámetros adicionales según tipo
      poblacionEstimada: simulacion.value.poblacionEstimada || null,
      numEscuelas: simulacion.value.numEscuelas || null,
      numHospitales: simulacion.value.numHospitales || null
    };

    // Llamar al servicio de proyectos
    const proyectosService = await import('@/services/proyectosService');
    await proyectosService.default.create(proyectoData);

    // También guardar localmente
    guardarSimulacion();
    
    showGuardarModal.value = false;
    emit('success', '¡Proyecto creado exitosamente desde la simulación!');

  } catch (error) {
    console.error('Error al guardar proyecto:', error);
    emit('error', 'Error al crear el proyecto: ' + (error.response?.data?.mensaje || error.message));
  } finally {
    guardando.value = false;
  }
};

const guardarComoDatoDemografico = async () => {
  try {
    guardando.value = true;

    // Preparar datos demográficos basados en el impacto proyectado
    const datoData = {
      nombre_zona: `Zona Simulación ${simulacion.value.tipoProyecto}`,
      año: new Date().getFullYear() + 1, // Año siguiente como proyección
      poblacion: resultadoImpacto.value.proyectado.poblacion,
      escuelas: resultadoImpacto.value.proyectado.escuelas,
      hospitales: resultadoImpacto.value.proyectado.hospitales,
      centros_culturales: resultadoImpacto.value.proyectado.centros_culturales,
      descripcion: `Dato demográfico proyectado desde simulación ${simulacion.value.tipoProyecto}. Impacto: ${formatDiferencia(resultadoImpacto.value.diferencias.poblacion)} habitantes`
    };

    // Llamar al servicio de datos demográficos
    const datosService = await import('@/services/datosService');
    await datosService.default.create(datoData);

    // También guardar localmente
    guardarSimulacion();
    
    showGuardarModal.value = false;
    emit('success', '¡Dato demográfico registrado exitosamente!');

  } catch (error) {
    console.error('Error al guardar dato demográfico:', error);
    emit('error', 'Error al registrar dato demográfico: ' + (error.response?.data?.mensaje || error.message));
  } finally {
    guardando.value = false;
  }
};

const eliminarSimulacion = (index) => {
  simulacionesGuardadas.value.splice(index, 1);
  localStorage.setItem('simulaciones', JSON.stringify(simulacionesGuardadas.value));
};

const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  return new Intl.NumberFormat('es-CL').format(num);
};

const formatDiferencia = (diff) => {
  if (diff > 0) return `+${formatNumber(diff)}`;
  if (diff < 0) return formatNumber(diff);
  return '0';
};

const getDiferenciaClass = (diff) => {
  if (diff > 0) return 'positivo';
  if (diff < 0) return 'negativo';
  return '';
};

const calcularDensidad = (poblacion, area) => {
  if (!area || area === 0) return 0;
  return Math.round(poblacion / area);
};

const getIconoTipo = (tipo) => {
  const iconos = {
    'RESIDENCIAL': '🏘️',
    'COMERCIAL': '🏬',
    'INDUSTRIAL': '🏭',
    'EDUCATIVO': '🏫',
    'SALUD': '🏥',
    'RECREATIVO': '🎡',
    'TRANSPORTE': '🚇'
  };
  return iconos[tipo] || '';
};

onMounted(() => {
  initMap();

  // Cargar simulaciones guardadas
  const saved = localStorage.getItem('simulaciones');
  if (saved) {
    try {
      simulacionesGuardadas.value = JSON.parse(saved);
    } catch (e) {
      console.error('Error al cargar simulaciones:', e);
    }
  }
});

onBeforeUnmount(() => {
  if (map) {
    map.remove();
    map = null;
  }
});
</script>

<style scoped>
.mapa-simulacion-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  gap: 1rem;
  min-height: 600px;
}

.mapa-leaflet {
  flex: 1;
  height: 100%;
  min-height: 600px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 1;
}

.panel-simulacion {
  width: 380px;
  background: var(--bg-primary);
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  padding: 1.2rem;
  overflow-y: auto;
  max-height: 100%;
  flex-shrink: 0;
}

.simulacion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.2rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

.simulacion-header h3 {
  margin: 0;
  font-size: 1.15rem;
  color: var(--text-primary);
}

.btn-iniciar {
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
  white-space: nowrap;
}

.btn-iniciar:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.4);
}

.btn-cancelar {
  padding: 0.5rem 1rem;
  background: var(--accent-danger);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s;
  white-space: nowrap;
}

.btn-cancelar:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.formulario-simulacion {
  margin-bottom: 1.2rem;
}

.formulario-simulacion h4 {
  margin: 0 0 0.8rem 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.form-group {
  margin-bottom: 0.8rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.4rem;
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.85rem;
}

.form-group select,
.form-group input {
  width: 100%;
  padding: 0.6rem;
  border: 1px solid var(--border-color);
  border-radius: 6px;
  background: var(--bg-secondary);
  color: var(--text-primary);
  font-size: 0.85rem;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group select:focus,
.form-group input:focus {
  outline: none;
  border-color: var(--accent-primary);
}

.form-group small {
  display: block;
  margin-top: 0.3rem;
  color: var(--text-secondary);
  font-size: 0.75rem;
  line-height: 1.3;
}

.form-actions {
  display: flex;
  gap: 0.5rem;
  margin-top: 1rem;
}

.btn-calcular,
.btn-guardar {
  flex: 1;
  padding: 0.7rem 0.5rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  font-weight: 600;
  transition: all 0.3s;
}

.btn-calcular {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.btn-calcular:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.4);
}

.btn-guardar {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
  box-shadow: 0 2px 8px rgba(139, 92, 246, 0.3);
}

.btn-guardar:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(139, 92, 246, 0.4);
}

.btn-calcular:disabled,
.btn-guardar:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.resultados-impacto {
  margin-bottom: 1.2rem;
}

.resultados-impacto h4 {
  margin: 0 0 0.8rem 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.comparacion-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.8rem;
  margin-bottom: 0.8rem;
}

.comparacion-card {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 0.8rem;
  border: 2px solid var(--border-color);
}

.comparacion-card.proyectado {
  border-color: #fbbf24;
  background: linear-gradient(135deg, rgba(251, 191, 36, 0.1) 0%, rgba(245, 158, 11, 0.1) 100%);
}

.comparacion-card h5 {
  margin: 0 0 0.6rem 0;
  font-size: 0.95rem;
  color: var(--text-primary);
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.4rem 0;
  border-bottom: 1px solid var(--border-color);
}

.stat-item:last-child {
  border-bottom: none;
}

.stat-item .label {
  font-size: 0.85rem;
  color: var(--text-secondary);
}

.stat-item .value {
  font-weight: bold;
  color: var(--text-primary);
  font-size: 0.85rem;
}

.diferencia {
  margin-left: 0.5rem;
  padding: 0.2rem 0.4rem;
  border-radius: 4px;
  font-size: 0.75rem;
  font-weight: bold;
}

.diferencia.positivo {
  background: #d1fae5;
  color: #065f46;
}

.diferencia.negativo {
  background: #fee2e2;
  color: #991b1b;
}

.metricas-adicionales {
  display: grid;
  grid-template-columns: 1fr;
  gap: 0.6rem;
  margin-top: 0.8rem;
}

.metrica-card {
  background: var(--bg-secondary);
  border-radius: 6px;
  padding: 0.6rem;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  border: 1px solid var(--border-color);
}

.metrica-icon {
  font-size: 1.3rem;
  flex-shrink: 0;
}

.metrica-content {
  display: flex;
  flex-direction: column;
}

.metrica-label {
  font-size: 0.75rem;
  color: var(--text-secondary);
}

.metrica-value {
  font-size: 0.9rem;
  font-weight: bold;
  color: var(--text-primary);
}

.simulaciones-guardadas {
  margin-top: 1.2rem;
  padding-top: 1.2rem;
  border-top: 2px solid var(--border-color);
}

.simulaciones-guardadas h4 {
  margin: 0 0 0.8rem 0;
  font-size: 1rem;
  color: var(--text-primary);
}

.simulacion-item {
  background: var(--bg-secondary);
  border-radius: 6px;
  padding: 0.6rem;
  margin-bottom: 0.5rem;
  border: 1px solid var(--border-color);
}

.sim-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.3rem;
}

.sim-tipo {
  font-weight: 600;
  color: var(--text-primary);
  font-size: 0.85rem;
}

.btn-eliminar-sim {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 0.95rem;
  padding: 0.2rem;
  transition: transform 0.2s;
}

.btn-eliminar-sim:hover {
  transform: scale(1.2);
}

.sim-info small {
  color: var(--text-secondary);
  font-size: 0.75rem;
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
@media (max-width: 1200px) {
  .panel-simulacion {
    width: 320px;
  }
}

@media (max-width: 1024px) {
  .mapa-simulacion-wrapper {
    flex-direction: column;
    min-height: auto;
  }

  .panel-simulacion {
    width: 100%;
    max-height: 400px;
  }

  .mapa-leaflet {
    height: 500px;
    min-height: 500px;
  }
}

@media (max-width: 768px) {
  .mapa-leaflet {
    height: 400px;
    min-height: 400px;
  }

  .panel-simulacion {
    max-height: 350px;
  }
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
  backdrop-filter: blur(4px);
}

.modal-content {
  background: var(--bg-primary);
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  animation: modalSlideIn 0.3s ease-out;
}

@keyframes modalSlideIn {
  from {
    opacity: 0;
    transform: translateY(-50px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 2px solid var(--border-color);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-primary);
}

.btn-close {
  background: transparent;
  border: none;
  font-size: 1.8rem;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 0;
  line-height: 1;
  transition: color 0.2s, transform 0.2s;
}

.btn-close:hover {
  color: var(--text-primary);
  transform: scale(1.1);
}

.modal-body {
  padding: 1.5rem;
}

.modal-body > p {
  margin: 0 0 1.5rem 0;
  color: var(--text-secondary);
  font-size: 1rem;
}

.opciones-guardar {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.btn-opcion-guardar {
  background: var(--bg-secondary);
  border: 2px solid var(--border-color);
  border-radius: 8px;
  padding: 1rem;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 1rem;
  transition: all 0.3s ease;
  text-align: left;
}

.btn-opcion-guardar:hover:not(:disabled) {
  border-color: #3b82f6;
  background: rgba(59, 130, 246, 0.1);
  transform: translateX(4px);
}

.btn-opcion-guardar:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.opcion-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.opcion-info {
  flex: 1;
}

.opcion-info h4 {
  margin: 0 0 0.3rem 0;
  font-size: 1.1rem;
  color: var(--text-primary);
}

.opcion-info p {
  margin: 0;
  font-size: 0.85rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

.modal-actions {
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
  display: flex;
  justify-content: center;
}

.btn-guardar-local {
  background: transparent;
  border: 1px solid var(--border-color);
  color: var(--text-secondary);
  padding: 0.6rem 1.2rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.btn-guardar-local:hover:not(:disabled) {
  background: var(--bg-secondary);
  color: var(--text-primary);
}

.btn-guardar-local:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

@media (max-width: 768px) {
  .modal-content {
    width: 95%;
    max-height: 95vh;
  }

  .modal-header h3 {
    font-size: 1.2rem;
  }

  .opcion-icon {
    font-size: 2rem;
  }

  .opcion-info h4 {
    font-size: 1rem;
  }

  .opcion-info p {
    font-size: 0.8rem;
  }
}
</style>

