<template>
  <div class="simulacion-proyectos-view">
    <div class="header">
      <h1>Simulación de Proyectos</h1>
      <p class="descripcion">
        Diseñe nuevas zonas de desarrollo o infraestructura en el mapa y visualice su impacto
        potencial en los datos demográficos y servicios del área circundante.
      </p>
    </div>

    <!-- Instrucciones -->
    <div class="instrucciones-card">
      <h3>Cómo usar la simulación:</h3>
      <ol>
        <li>Haga clic en "Iniciar Simulación" para comenzar</li>
        <li>Use las herramientas de dibujo para trazar el área del nuevo proyecto</li>
        <li>Configure el tipo de proyecto y sus parámetros</li>
        <li>Defina el área de influencia y el impacto esperado en la población</li>
        <li>Calcule el impacto para ver las proyecciones demográficas</li>
        <li>Guarde la simulación para referencia futura</li>
      </ol>
    </div>

    <!-- Error/Success Alerts -->
    <ErrorAlert v-if="error" :message="error" @close="error = null" />
    <div v-if="success" class="success-alert">
      <span>{{ success }}</span>
      <button @click="success = null">×</button>
    </div>

    <!-- Mapa de Simulación -->
    <div class="mapa-container">
      <MapaSimulacionProyectos @error="handleError" @success="handleSuccess" />
    </div>

    <!-- Información adicional -->
    <div class="info-footer">
      <div class="info-card">
        <div class="info-content">
          <h4>Tipos de Proyectos</h4>
          <ul>
            <li><strong>Residencial:</strong> Viviendas y condominios</li>
            <li><strong>Comercial:</strong> Centros comerciales y oficinas</li>
            <li><strong>Industrial:</strong> Plantas y parques industriales</li>
            <li><strong>Educativo:</strong> Escuelas y universidades</li>
            <li><strong>Salud:</strong> Hospitales y centros médicos</li>
            <li><strong>Recreativo:</strong> Parques y espacios públicos</li>
            <li><strong>Transporte:</strong> Estaciones de metro o terminales</li>
          </ul>
        </div>
      </div>

      <div class="info-card">
        <div class="info-content">
          <h4>Métricas Calculadas</h4>
          <ul>
            <li>Cambio en población total del área de influencia</li>
            <li>Incremento en infraestructura educativa y de salud</li>
            <li>Densidad poblacional proyectada</li>
            <li>Impacto en servicios existentes</li>
          </ul>
        </div>
      </div>

      <div class="info-card">
        <div class="info-content">
          <h4>Área de Influencia</h4>
          <p>
            Define el radio de impacto del proyecto. Los cambios demográficos se
            aplicarán a las zonas dentro de este radio desde el centro del proyecto.
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import MapaSimulacionProyectos from '@/components/common/MapaSimulacionProyectos.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';

const error = ref(null);
const success = ref(null);

const handleError = (message) => {
  error.value = message;
  setTimeout(() => {
    error.value = null;
  }, 5000);
};

const handleSuccess = (message) => {
  success.value = message;
  setTimeout(() => {
    success.value = null;
  }, 3000);
};
</script>

<style scoped>
.simulacion-proyectos-view {
  padding: 1.5rem;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.header {
  margin-bottom: 0.8rem;
  flex-shrink: 0;
}

.header h1 {
  margin: 0 0 0.3rem 0;
  font-size: 1.75rem;
  color: var(--text-primary);
}

.descripcion {
  margin: 0;
  font-size: 0.95rem;
  color: var(--text-secondary);
  line-height: 1.4;
}

.instrucciones-card {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
  border-radius: 8px;
  padding: 0.8rem 1rem;
  margin-bottom: 0.8rem;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
  flex-shrink: 0;
}

.instrucciones-card h3 {
  margin: 0 0 0.6rem 0;
  font-size: 1.1rem;
}

.instrucciones-card ol {
  margin: 0;
  padding-left: 1.5rem;
}

.instrucciones-card li {
  margin-bottom: 0.3rem;
  line-height: 1.4;
  font-size: 0.9rem;
}

.success-alert {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
  padding: 0.8rem;
  border-radius: 8px;
  margin-bottom: 0.8rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.3);
  animation: slideIn 0.3s ease-out;
  flex-shrink: 0;
}

.success-alert button {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0 0.5rem;
  line-height: 1;
}

.mapa-container {
  flex: 1;
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 0.5rem;
  overflow: hidden;
  min-height: 600px;
  height: 100%;
}

.info-footer {
  display: none;
}

@keyframes slideIn {
  from {
    transform: translateY(-20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@media (max-width: 768px) {
  .simulacion-proyectos-view {
    padding: 1rem;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .mapa-container {
    min-height: 500px;
  }
}
</style>

