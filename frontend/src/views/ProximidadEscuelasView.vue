<template>
  <div class="proximidad-escuelas-view">
    <div class="header">
      <h1> Análisis de Proximidad: Escuelas</h1>
      <p class="descripcion">
        Visualización de escuelas que se encuentran a menos de 500 metros de proyectos en curso.
      </p>
    </div>

    <!-- Error Alert -->
    <ErrorAlert v-if="error" :message="error" @close="error = null" />

    <!-- Loading Spinner -->
    <LoadingSpinner v-if="loading" message="Cargando análisis de proximidad..." />

    <!-- Estadísticas generales -->
    <div v-if="!loading && resultados.length > 0" class="estadisticas-container">
      <div class="stat-card">
        <div class="stat-icon">🏫</div>
        <div class="stat-content">
          <div class="stat-label">Total Escuelas Cercanas</div>
          <div class="stat-value">{{ totalEscuelas }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">🚧</div>
        <div class="stat-content">
          <div class="stat-label">Proyectos Involucrados</div>
          <div class="stat-value">{{ totalProyectos }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📏</div>
        <div class="stat-content">
          <div class="stat-label">Distancia Promedio</div>
          <div class="stat-value">{{ distanciaPromedio }} m</div>
        </div>
      </div>
    </div>

    <!-- Contenido principal -->
    <div v-if="!loading" class="content-wrapper">
      <!-- Mapa -->
      <div class="mapa-container">
        <MapaProximidadEscuelas
          :resultados="resultados"
          @error="handleError"
        />
      </div>

      <!-- Tabla de resultados -->
      <div class="tabla-container">
        <div class="tabla-header">
          <h2>📋 Resultados del Análisis</h2>
          <button
            v-if="resultados.length > 0"
            @click="exportarCSV"
            class="btn-exportar"
          >
            📥 Exportar CSV
          </button>
        </div>

        <div v-if="resultados.length === 0" class="mensaje-vacio">
          <p>ℹ️ No se encontraron escuelas cerca de proyectos en curso.</p>
          <p class="submensaje">Verifique que existan proyectos con estado "En Curso" y escuelas registradas en el sistema.</p>
        </div>

        <div v-else class="tabla-scroll">
          <table class="tabla-resultados">
            <thead>
              <tr>
                <th @click="ordenarPor('nombre_escuela')" class="sortable">
                  🏫 Escuela
                  <span class="sort-icon">{{ getSortIcon('nombre_escuela') }}</span>
                </th>
                <th @click="ordenarPor('nombre_proyecto')" class="sortable">
                  🚧 Proyecto
                  <span class="sort-icon">{{ getSortIcon('nombre_proyecto') }}</span>
                </th>
                <th @click="ordenarPor('distancia_metros')" class="sortable">
                  📏 Distancia (m)
                  <span class="sort-icon">{{ getSortIcon('distancia_metros') }}</span>
                </th>
                <th>🎯 Proximidad</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(resultado, index) in resultadosOrdenados"
                :key="index"
                :class="{ 'row-highlight': resultado.distancia_metros < 250 }"
              >
                <td>
                  <div class="cell-content">
                    <span class="nombre-principal">{{ resultado.nombre_escuela }}</span>
                  </div>
                </td>
                <td>
                  <div class="cell-content">
                    <span class="nombre-principal">{{ resultado.nombre_proyecto }}</span>
                  </div>
                </td>
                <td>
                  <div class="cell-content">
                    <span class="distancia-valor">{{ formatNumber(resultado.distancia_metros) }}</span>
                  </div>
                </td>
                <td>
                  <span
                    class="badge-proximidad"
                    :class="getProximidadClass(resultado.distancia_metros)"
                  >
                    {{ getProximidadLabel(resultado.distancia_metros) }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import api from '@/services/api';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import MapaProximidadEscuelas from '@/components/common/MapaProximidadEscuelas.vue';

const loading = ref(false);
const error = ref(null);
const resultados = ref([]);
const ordenActual = ref({ campo: 'distancia_metros', direccion: 'asc' });

const cargarDatos = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await api.get('/puntos-interes/analisis/escuelas-cerca-proyectos');
    resultados.value = response.data;
  } catch (err) {
    console.error('Error al cargar análisis de proximidad:', err);
    error.value = 'Error al cargar los datos de proximidad. Por favor, intente nuevamente.';
  } finally {
    loading.value = false;
  }
};

const totalEscuelas = computed(() => {
  const escuelasUnicas = new Set(resultados.value.map(r => r.nombre_escuela));
  return escuelasUnicas.size;
});

const totalProyectos = computed(() => {
  const proyectosUnicos = new Set(resultados.value.map(r => r.nombre_proyecto));
  return proyectosUnicos.size;
});

const distanciaPromedio = computed(() => {
  if (resultados.value.length === 0) return 0;
  const suma = resultados.value.reduce((acc, r) => acc + parseFloat(r.distancia_metros), 0);
  return Math.round(suma / resultados.value.length);
});

const resultadosOrdenados = computed(() => {
  const copia = [...resultados.value];
  return copia.sort((a, b) => {
    const valorA = a[ordenActual.value.campo];
    const valorB = b[ordenActual.value.campo];

    let comparacion = 0;
    if (typeof valorA === 'string') {
      comparacion = valorA.localeCompare(valorB);
    } else {
      comparacion = valorA - valorB;
    }

    return ordenActual.value.direccion === 'asc' ? comparacion : -comparacion;
  });
});

const ordenarPor = (campo) => {
  if (ordenActual.value.campo === campo) {
    ordenActual.value.direccion = ordenActual.value.direccion === 'asc' ? 'desc' : 'asc';
  } else {
    ordenActual.value.campo = campo;
    ordenActual.value.direccion = 'asc';
  }
};

const getSortIcon = (campo) => {
  if (ordenActual.value.campo !== campo) return '↕️';
  return ordenActual.value.direccion === 'asc' ? '↑' : '↓';
};

const getProximidadClass = (distancia) => {
  if (distancia < 250) return 'muy-cerca';
  if (distancia < 400) return 'cerca';
  return 'moderada';
};

const getProximidadLabel = (distancia) => {
  if (distancia < 250) return 'Muy Cerca';
  if (distancia < 400) return 'Cerca';
  return 'Moderada';
};

const formatNumber = (num) => {
  return new Intl.NumberFormat('es-CL', { maximumFractionDigits: 2 }).format(num);
};

const exportarCSV = () => {
  const headers = ['Escuela', 'Proyecto', 'Distancia (metros)', 'Proximidad'];
  const filas = resultadosOrdenados.value.map(r => [
    r.nombre_escuela,
    r.nombre_proyecto,
    r.distancia_metros,
    getProximidadLabel(r.distancia_metros)
  ]);

  let csv = headers.join(',') + '\n';
  filas.forEach(fila => {
    csv += fila.map(campo => `"${campo}"`).join(',') + '\n';
  });

  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
  const link = document.createElement('a');
  link.href = URL.createObjectURL(blob);
  link.download = `proximidad_escuelas_${new Date().toISOString().split('T')[0]}.csv`;
  link.click();
};

const handleError = (message) => {
  error.value = message;
  setTimeout(() => {
    error.value = null;
  }, 5000);
};

onMounted(() => {
  cargarDatos();
});
</script>

<style scoped>
.proximidad-escuelas-view {
  padding: 1.5rem;
  max-width: 1800px;
  margin: 0 auto;
}

.header {
  margin-bottom: 1.5rem;
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

.estadisticas-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1rem;
  margin-bottom: 1.5rem;
}

.stat-card {
  background: linear-gradient(135deg, var(--accent-primary) 0%, #667eea 100%);
  color: white;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-4px);
}

.stat-icon {
  font-size: 2.5rem;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 0.9rem;
  opacity: 0.9;
  margin-bottom: 0.25rem;
}

.stat-value {
  font-size: 2rem;
  font-weight: bold;
}

.content-wrapper {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
  margin-top: 1.5rem;
  min-height: 700px;
}

.mapa-container {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 1rem;
  height: 650px;
  width: 100%;
  max-width: 100%;
}

.tabla-container {
  background: var(--bg-secondary);
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
}

.tabla-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
}

.tabla-header h2 {
  margin: 0;
  font-size: 1.5rem;
  color: var(--text-primary);
}

.btn-exportar {
  padding: 0.6rem 1.2rem;
  background: var(--accent-success);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  font-weight: 500;
  transition: all 0.3s;
}

.btn-exportar:hover {
  background: #059669;
  transform: translateY(-2px);
}

.mensaje-vacio {
  text-align: center;
  padding: 3rem 1rem;
  color: var(--text-secondary);
}

.mensaje-vacio p {
  margin: 0.5rem 0;
  font-size: 1.1rem;
}

.submensaje {
  font-size: 0.95rem;
  opacity: 0.8;
}

.tabla-scroll {
  overflow-x: auto;
  flex: 1;
}

.tabla-resultados {
  width: 100%;
  border-collapse: collapse;
}

.tabla-resultados thead {
  background: var(--bg-primary);
  position: sticky;
  top: 0;
  z-index: 10;
}

.tabla-resultados th {
  padding: 1rem;
  text-align: left;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 2px solid var(--border-color);
  white-space: nowrap;
}

.tabla-resultados th.sortable {
  cursor: pointer;
  user-select: none;
  transition: background 0.3s;
}

.tabla-resultados th.sortable:hover {
  background: var(--bg-secondary);
}

.sort-icon {
  margin-left: 0.5rem;
  font-size: 0.8rem;
  opacity: 0.6;
}

.tabla-resultados td {
  padding: 1rem;
  border-bottom: 1px solid var(--border-color);
}

.tabla-resultados tbody tr {
  transition: background 0.3s;
}

.tabla-resultados tbody tr:hover {
  background: var(--bg-primary);
}

.tabla-resultados tbody tr.row-highlight {
  background: rgba(239, 68, 68, 0.05);
}

.cell-content {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.nombre-principal {
  font-weight: 500;
  color: var(--text-primary);
}

.distancia-valor {
  font-weight: 600;
  color: var(--accent-primary);
  font-size: 1.1rem;
}

.badge-proximidad {
  display: inline-block;
  padding: 0.4rem 0.8rem;
  border-radius: 12px;
  font-size: 0.85rem;
  font-weight: 600;
  text-align: center;
}

.badge-proximidad.muy-cerca {
  background: rgba(239, 68, 68, 0.1);
  color: #dc2626;
}

.badge-proximidad.cerca {
  background: rgba(245, 158, 11, 0.1);
  color: #d97706;
}

.badge-proximidad.moderada {
  background: rgba(34, 197, 94, 0.1);
  color: #16a34a;
}


@media (max-width: 768px) {
  .proximidad-escuelas-view {
    padding: 1rem;
  }
  .mapa-container {
    height: 400px;
  }


  .mapa-container {
    height: 450px;
  }

  .header h1 {
    font-size: 1.5rem;
  }

  .estadisticas-container {
    grid-template-columns: 1fr;
  }

  .stat-card {
    padding: 1rem;
  }

  .stat-icon {
    font-size: 2rem;
  }

  .stat-value {
    font-size: 1.5rem;
  }

  .tabla-header {
    flex-direction: column;
    gap: 1rem;
    align-items: flex-start;
  }

  .btn-exportar {
    width: 100%;
  }
}
</style>

