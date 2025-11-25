<template>
  <div class="mapa-nuevo-wrapper">
    <div ref="mapContainer" class="mapa-leaflet"></div>

    <div class="panel-draw">
      <div class="header">
          <h3>🗺️ Seleccionar ubicación del nuevo proyecto</h3>
          <button class="btn-cancel" @click="cancel">✖</button>
      </div>
      <p class="help">Usa las herramientas de dibujo (rectángulo/polígono) para marcar el área del proyecto.</p>
      <div class="actions">
        <button class="btn-ok" @click="confirmIfExists" :disabled="!areaLayer">Usar área marcada</button>
        <button class="btn-clear" @click="clearDrawing" :disabled="!areaLayer">Limpiar</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';
import 'leaflet-draw';
import 'leaflet-draw/dist/leaflet.draw.css';

const emit = defineEmits(['area-drawn','cancel']);

const mapContainer = ref(null);
let map = null;
let drawnItems = null;
let drawControl = null;
let areaLayer = ref(null);

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

// Función para iniciar el modo de dibujo (igual que en el simulador)
const iniciarDibujo = () => {
  // Remover control existente si lo hay
  if (drawControl) {
    map.removeControl(drawControl);
    drawControl = null;
  }

  drawControl = new L.Control.Draw({
    position: 'topright',
    draw: {
      polygon: {
        allowIntersection: false,
        shapeOptions: { color: '#06b6d4', fillColor: '#67e8f9', fillOpacity: 0.25, weight: 3 }
      },
      rectangle: {
        shapeOptions: { color: '#06b6d4', fillColor: '#67e8f9', fillOpacity: 0.25, weight: 3 }
      },
      circle: false,
      marker: false,
      polyline: false,
      circlemarker: false
    },
    edit: { featureGroup: drawnItems, remove: true }
  });

  map.addControl(drawControl);

  // Eventos de dibujo (igual que simulación)
  map.on(L.Draw.Event.CREATED, (e) => {
    drawnItems.clearLayers();
    areaLayer.value = e.layer;
    drawnItems.addLayer(e.layer);

    // Activar edición inmediatamente para que aparezcan los manejadores
    // igual que en la simulación.
    try {
      // Ejecutar en un timeout corto para que Leaflet haya terminado la creación
      setTimeout(() => {
        if (e.layer && e.layer.editing && typeof e.layer.editing.enable === 'function') {
          e.layer.editing.enable();
          return;
        }

        // Fallback: intentar habilitar handler interno del toolbar
        if (drawControl && drawControl._toolbars && drawControl._toolbars.edit) {
          const editToolbar = drawControl._toolbars.edit;
          const handler = editToolbar._modes && editToolbar._modes.edit && editToolbar._modes.edit.handler;
          if (handler && typeof handler.enable === 'function') {
            handler.enable();
            try {
              if (handler._selectedLayers && typeof handler._selectedLayers.addLayer === 'function') {
                handler._selectedLayers.addLayer(e.layer);
              }
              if (typeof handler._updateMarkers === 'function') handler._updateMarkers();
            } catch (errInner) {
              // ignore
            }
          }
        }
      }, 50);
    } catch (err) {
      // no-op
    }
  });

  map.on(L.Draw.Event.DELETED, () => {
    areaLayer.value = null;
  });
};

const clearDrawing = () => {
  drawnItems.clearLayers();
  areaLayer.value = null;
};

const confirmIfExists = () => {
  if (!areaLayer.value) return;
  const geo = areaLayer.value.toGeoJSON();
  emit('area-drawn', geo.geometry);
};

const cancel = () => {
  emit('cancel');
};

onMounted(() => { initMap(); iniciarDibujo(); });
onBeforeUnmount(() => {
  if (map) { map.remove(); map = null; }
});
</script>

<style scoped>
.mapa-nuevo-wrapper { position: relative; height: 600px; min-height: 400px; display:flex; gap:1rem; }
.mapa-leaflet { flex: 1; height: 100%; border-radius: 8px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
.panel-draw { width: 360px; background: var(--bg-primary); border-radius: 8px; padding: 12px; border:1px solid var(--border-color); }
.panel-draw .header { display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.panel-draw .help { font-size: 13px; color: var(--text-secondary); margin-bottom:12px; }
.actions { display:flex; gap:8px; }
.btn-ok { flex:1; background: linear-gradient(135deg,#10b981 0%, #059669 100%); color:white; border:none; padding:8px; border-radius:6px; cursor:pointer; }
.btn-clear { flex:1; background: transparent; border:1px solid var(--border-color); padding:8px; border-radius:6px; cursor:pointer; }
.btn-cancel { background: transparent; border: none; font-size:18px; cursor:pointer; }
/* .btn-start removed - drawing starts automatically */
</style>
