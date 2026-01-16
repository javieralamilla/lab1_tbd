<template>
  <div class="analisis-espacial-view">
    <div class="header">
      <h1>Análisis Espacial</h1>
      <p class="descripcion">
        Seleccione un área en el mapa para obtener estadísticas resumidas de población,
        infraestructura y proyectos en curso.
      </p>
    </div>

    <!-- Instrucciones -->
    <div class="instrucciones-card">
      <h3>Instrucciones:</h3>
      <ol>
        <li>Use las herramientas de dibujo en la esquina superior derecha del mapa</li>
        <li>Dibuje un polígono, rectángulo o círculo sobre el área que desea analizar</li>
        <li>Las estadísticas se calcularán automáticamente</li>
        <li>Puede editar o eliminar el área dibujada usando las herramientas de edición</li>
      </ol>
    </div>

    <!-- Error Alert -->
    <ErrorAlert v-if="error" :message="error" @close="error = null" />

    <!-- Mapa de Análisis -->
    <div class="mapa-container">
      <MapaAnalisisEspacial @error="handleError" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import MapaAnalisisEspacial from '@/components/common/MapaAnalisisEspacial.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';

const error = ref(null);

const handleError = (message) => {
  error.value = message;
  setTimeout(() => {
    error.value = null;
  }, 5000);
};
</script>

<style scoped>
.analisis-espacial-view {
  padding: 1.5rem;
  max-width: 1800px;
  margin: 0 auto;
  height: calc(100vh - 60px);
  display: flex;
  flex-direction: column;
}

.header {
  margin-bottom: 1rem;
}

.header h1 {
  margin: 0 0 0.5rem 0;
  font-size: 2rem;
  color: var(--text-primary);
}

.descripcion {
  margin: 0;
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.5;
}

.instrucciones-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 8px;
  padding: 1rem;
  margin-bottom: 1rem;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.instrucciones-card h3 {
  margin: 0 0 1rem 0;
  font-size: 1.25rem;
}

.instrucciones-card ol {
  margin: 0;
  padding-left: 1.5rem;
}

.instrucciones-card li {
  margin-bottom: 0.5rem;
  line-height: 1.6;
}

.instrucciones-card li:last-child {
  margin-bottom: 0;
}

.mapa-container {
  flex: 1;
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 1rem;
  min-height: 500px;
}

.info-footer {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-top: auto;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--bg-secondary);
  border-radius: 6px;
  border: 1px solid var(--border-color);
}

.info-item .icon {
  font-size: 1.5rem;
  flex-shrink: 0;
}

.info-item span:last-child {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

/* Responsive */
@media (max-width: 768px) {
  .analisis-espacial-view {
    padding: 1rem;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .instrucciones-card {
    padding: 1rem;
  }

  .instrucciones-card h3 {
    font-size: 1.1rem;
  }

  .info-footer {
    grid-template-columns: 1fr;
  }
}
</style>
