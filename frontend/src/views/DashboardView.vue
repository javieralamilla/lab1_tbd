<template>
  <div class="dashboard-container">
    <div class="dashboard-header">
      <h1>Panel de Control</h1>
      <p>Resumen general de la plataforma de urbanismo</p>
    </div>

    <LoadingSpinner v-if="loading" message="Cargando datos..." />

    <ErrorAlert
      v-if="error"
      :message="error"
      type="error"
      @close="error = null"
    />

    <div v-if="!loading && !error" class="dashboard-content">
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon zonas">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalZonas }}</div>
            <div class="stat-label">Zonas Urbanas</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon proyectos">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
              <polyline points="14 2 14 8 20 8"></polyline>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalProyectos }}</div>
            <div class="stat-label">Proyectos Urbanos</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon puntos">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalPuntos }}</div>
            <div class="stat-label">Puntos de Interés</div>
          </div>
        </div>

        <div class="stat-card">
          <div class="stat-icon poblacion">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ formatNumber(stats.poblacionTotal) }}</div>
            <div class="stat-label">Población Total</div>
          </div>
        </div>
      </div>

      <div class="section">
        <h2>Proyectos por Estado</h2>
        <div class="projects-status">
          <div
            v-for="(count, estado) in stats.proyectosPorEstado"
            :key="estado"
            class="status-item"
            :class="estado.toLowerCase().replace(' ', '-')"
          >
            <div class="status-icon-wrapper">
              <component :is="getIconForStatus(estado)" />
            </div>
            <div class="status-count">{{ count }}</div>
            <div class="status-label">{{ estado }}</div>
          </div>
        </div>
      </div>

      <div class="section">
        <h2>Puntos de Interés por Tipo</h2>
        <div class="points-grid">
          <div
            v-for="(count, tipo) in stats.puntosPorTipo"
            :key="tipo"
            class="point-card"
          >
            <div class="point-icon" :class="tipo.toLowerCase()">
              <component :is="getIconForType(tipo)" />
            </div>
            <div class="point-count">{{ count }}</div>
            <div class="point-label">{{ tipo }}</div>
          </div>
        </div>
      </div>


    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, h } from 'vue';
import LoadingSpinner from '@/components/common/LoadingSpinner.vue';
import ErrorAlert from '@/components/common/ErrorAlert.vue';
import zonasService from '@/services/zonasService';
import proyectosService from '@/services/proyectosService';
import puntosInteresService from '@/services/puntosInteresService';

const loading = ref(true);
const error = ref(null);
const stats = ref({
  totalZonas: 0,
  totalProyectos: 0,
  totalPuntos: 0,
  poblacionTotal: 0,
  proyectosPorEstado: {},
  puntosPorTipo: {}
});

const formatNumber = (num) => {
  return new Intl.NumberFormat('es-CL').format(num);
};

const getIconForStatus = (estado) => {
  const icons = {
    'En Curso': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '32',
      height: '32',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('circle', { cx: '12', cy: '12', r: '10' }),
      h('polyline', { points: '12 6 12 12 16 14' })
    ]),
    'Completado': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '32',
      height: '32',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M22 11.08V12a10 10 0 1 1-5.93-9.14' }),
      h('polyline', { points: '22 4 12 14.01 9 11.01' })
    ]),
    'Planeado': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '32',
      height: '32',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('rect', { width: '18', height: '18', x: '3', y: '4', rx: '2', ry: '2' }),
      h('line', { x1: '16', x2: '16', y1: '2', y2: '6' }),
      h('line', { x1: '8', x2: '8', y1: '2', y2: '6' }),
      h('line', { x1: '3', x2: '21', y1: '10', y2: '10' })
    ]),
    'Retrasado': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '32',
      height: '32',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('circle', { cx: '12', cy: '12', r: '10' }),
      h('line', { x1: '12', y1: '8', x2: '12', y2: '12' }),
      h('line', { x1: '12', y1: '16', x2: '12.01', y2: '16' })
    ])
  };
  return icons[estado] || icons['Planeado'];
};

const getIconForType = (tipo) => {
  const icons = {
    'HOSPITAL': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '24',
      height: '24',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M11 2a2 2 0 0 0-2 2v5H4a2 2 0 0 0-2 2v2c0 1.1.9 2 2 2h5v5c0 1.1.9 2 2 2h2a2 2 0 0 0 2-2v-5h5a2 2 0 0 0 2-2v-2a2 2 0 0 0-2-2h-5V4a2 2 0 0 0-2-2h-2z' })
    ]),
    'ESCUELA': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '24',
      height: '24',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M22 10v6M2 10l10-5 10 5-10 5z' }),
      h('path', { d: 'M6 12v5c3 3 9 3 12 0v-5' })
    ]),
    'PARQUE': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '24',
      height: '24',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('path', { d: 'M12 3v3m0 12v3M5.5 5.5l2.1 2.1m8.8 8.8l2.1 2.1m-13 0l2.1-2.1m8.8-8.8l2.1-2.1' }),
      h('circle', { cx: '12', cy: '12', r: '4' })
    ]),
    'CENTRO_COMERCIAL': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '24',
      height: '24',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('circle', { cx: '8', cy: '21', r: '1' }),
      h('circle', { cx: '19', cy: '21', r: '1' }),
      h('path', { d: 'M2.05 2.05h2l2.66 12.42a2 2 0 0 0 2 1.58h9.78a2 2 0 0 0 1.95-1.57l1.65-7.43H5.12' })
    ]),
    'TRANSPORTE': () => h('svg', {
      xmlns: 'http://www.w3.org/2000/svg',
      width: '24',
      height: '24',
      viewBox: '0 0 24 24',
      fill: 'none',
      stroke: 'currentColor',
      'stroke-width': '2',
      'stroke-linecap': 'round',
      'stroke-linejoin': 'round'
    }, [
      h('rect', { width: '16', height: '16', x: '4', y: '3', rx: '2' }),
      h('path', { d: 'M4 11h16M12 3v8m-4 8h.01M16 19h.01' })
    ])
  };
  return icons[tipo] || icons['PARQUE'];
};

const loadDashboardData = async () => {
  loading.value = true;
  error.value = null;

  try {
    const [zonas, proyectos, puntos] = await Promise.all([
      zonasService.getAll(),
      proyectosService.getAll(),
      puntosInteresService.getAll()
    ]);

    stats.value.totalZonas = zonas.length;
    stats.value.totalProyectos = proyectos.length;
    stats.value.totalPuntos = puntos.length;

    // Calcular proyectos por estado
    const estadosCount = {};
    proyectos.forEach(p => {
      estadosCount[p.estado] = (estadosCount[p.estado] || 0) + 1;
    });
    stats.value.proyectosPorEstado = estadosCount;

    // Calcular puntos por tipo
    const tiposCount = {};
    puntos.forEach(p => {
      tiposCount[p.tipo] = (tiposCount[p.tipo] || 0) + 1;
    });
    stats.value.puntosPorTipo = tiposCount;

    // Simulación de población total (ajustar según tu API)
    stats.value.poblacionTotal = 5500000;

  } catch (err) {
    error.value = err.message || 'Error al cargar datos del dashboard';
  } finally {
    loading.value = false;
  }
};

onMounted(() => {
  loadDashboardData();
});
</script>

<style scoped>
/* La clase .dashboard ya no es necesaria, .dashboard-container es la raíz */
.dashboard-container {
  /* max-width: 1400px; */
  /* margin: 0 auto; */
  width: 100%;
  margin: 0;
  padding: 0; /* El padding y margin lo maneja AppLayout.vue */
}

.dashboard-header {
  margin-bottom: 32px;
}

.dashboard-header h1 {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary); /* CAMBIO */
  margin: 0 0 8px 0;
}

.dashboard-header p {
  font-size: 16px;
  color: var(--text-secondary); /* CAMBIO */
  margin: 0;
}

.dashboard-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.stat-card {
  background: var(--bg-secondary); /* CAMBIO */
  border: 1px solid var(--border-color); /* CAMBIO */
  border-radius: 12px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: none; /* CAMBIO */
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

/* Usamos las variables para los gradientes */
.stat-icon.zonas { background: var(--icon-zonas-bg); }
.stat-icon.proyectos { background: var(--icon-proyectos-bg); }
.stat-icon.puntos { background: var(--icon-puntos-bg); }
.stat-icon.poblacion { background: var(--icon-poblacion-bg); }

.stat-info { flex: 1; }

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary); /* CAMBIO */
  line-height: 1;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary); /* CAMBIO */
}

.section {
  background: var(--bg-secondary); /* CAMBIO */
  border: 1px solid var(--border-color); /* CAMBIO */
  border-radius: 12px;
  padding: 24px;
  box-shadow: none; /* CAMBIO */
}

.section h2 {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary); /* CAMBIO */
  margin: 0 0 20px 0;
}

.projects-status {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.status-item {
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  border: 2px solid;
  color: var(--text-primary);
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  position: relative;
  overflow: hidden;
}

.status-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.status-icon-wrapper {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  margin-bottom: 8px;
}

/* Usamos las variables de estado */
.status-item.en-curso {
  background: linear-gradient(135deg, var(--status-en-curso-bg) 0%, var(--status-en-curso-border) 100%);
  border-color: var(--status-en-curso-border);
  color: white;
}

.status-item.en-curso .status-icon-wrapper {
  color: var(--status-en-curso-border);
}

.status-item.completado {
  background: linear-gradient(135deg, var(--status-completado-bg) 0%, var(--status-completado-border) 100%);
  border-color: var(--status-completado-border);
  color: white;
}

.status-item.completado .status-icon-wrapper {
  color: var(--status-completado-border);
}

.status-item.planeado {
  background: linear-gradient(135deg, var(--status-planeado-bg) 0%, var(--status-planeado-border) 100%);
  border-color: var(--status-planeado-border);
  color: white;
}

.status-item.planeado .status-icon-wrapper {
  color: var(--status-planeado-border);
}

.status-item.retrasado {
  background: linear-gradient(135deg, var(--status-retrasado-bg) 0%, var(--status-retrasado-border) 100%);
  border-color: var(--status-retrasado-border);
  color: white;
}

.status-item.retrasado .status-icon-wrapper {
  color: var(--status-retrasado-border);
}

.status-count {
  font-size: 36px;
  font-weight: 700;
  margin: 0;
  line-height: 1;
}

.status-label {
  font-size: 15px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.points-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
}

.point-card {
  padding: 24px;
  border-radius: 12px;
  text-align: center;
  background-color: var(--bg-primary);
  border: 2px solid var(--border-color);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.point-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.12);
  border-color: var(--accent-primary);
}

.point-icon {
  width: 64px;
  height: 64px;
  margin: 0;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  background: linear-gradient(135deg, var(--icon-puntos-bg) 0%, var(--accent-primary) 100%);
  transition: transform 0.3s ease;
}

.point-card:hover .point-icon {
  transform: scale(1.1);
}

.point-icon.hospital {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
}
.point-icon.escuela {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}
.point-icon.parque {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}
.point-icon.centro_comercial {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}
.point-icon.transporte {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

.point-count {
  font-size: 32px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0;
  line-height: 1;
}

.point-label {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
  text-transform: capitalize;
}

@media (max-width: 768px) {
  .stats-grid,
  .projects-status,
  .points-grid {
    grid-template-columns: 1fr;
  }
}
</style>
