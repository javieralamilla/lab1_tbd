// src/router/index.js (Actualizado)

import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import AppLayout from '@/layouts/AppLayout.vue'; // <-- 1. Importa tu nuevo layout

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { requiresAuth: false, title: 'Iniciar Sesión' }
  },
  {
    // 2. Esta es la nueva ruta "padre" que usa el layout
    path: '/',
    component: AppLayout,
    meta: { requiresAuth: true }, // 3. Protegemos el layout y todos sus hijos
    children: [
      {
        path: '', // Redirige de '/' a '/dashboard'
        redirect: '/dashboard'
      },
      {
        path: 'dashboard', // El path ahora es relativo al padre '/'
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: 'Panel de Control' } // 'requiresAuth' es heredado
      },
      {
        path: 'zonas',
        name: 'Zonas',
        component: () => import('@/views/ZonasView.vue'),
        meta: {
          title: 'Zonas Urbanas',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'proyectos',
        name: 'Proyectos',
        component: () => import('@/views/ProyectosView.vue'),
        meta: {
          title: 'Proyectos Urbanos',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'puntos-interes',
        name: 'PuntosInteres',
        component: () => import('@/views/PuntosInteresView.vue'),
        meta: {
          title: 'Puntos de Interés',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'datos-demograficos',
        name: 'DatosDemograficos',
        component: () => import('@/views/DatosView.vue'),
        meta: {
          title: 'Datos Demográficos',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'mapa-integrado',
        name: 'MapaIntegrado',
        component: () => import('@/views/MapaIntegradoView.vue'),
        meta: { title: 'Mapa Integrado' }
      },
      {
        path: 'analisis-espacial',
        name: 'AnalisisEspacial',
        component: () => import('@/views/AnalisisEspacialView.vue'),
        meta: {
          title: 'Análisis Espacial',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'proximidad-escuelas',
        name: 'ProximidadEscuelas',
        component: () => import('@/views/ProximidadEscuelasView.vue'),
        meta: {
          title: 'Proximidad de Escuelas',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'simulacion-proyectos',
        name: 'SimulacionProyectos',
        component: () => import('@/views/SimulacionProyectosView.vue'),
        meta: {
          title: 'Simulación de Proyectos',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'cobertura-infraestructura',
        name: 'CoberturaInfraestructura',
        component: () => import('@/views/CoberturaView.vue'),
        meta: {
          title: 'Cobertura de Infraestructura',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      },
      {
        path: 'reportes',
        name: 'Reportes',
        component: () => import('@/views/ReportesView.vue'),
        meta: {
          title: 'Reportes',
          roles: ['administrador', 'planificador', 'autoridad_municipal']
        }
      }
    ]
  },
  {
    // 4. La ruta 404 se mantiene fuera del layout
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '404 - No Encontrado' }
  }
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
});

router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  // Actualizar título de la página
  document.title = to.meta.title
    ? `${to.meta.title} - ${import.meta.env.VITE_APP_NAME}`
    : import.meta.env.VITE_APP_NAME;

  // Verificar autenticación
  // Esta lógica ahora protegerá la ruta '/' (AppLayout) y todos sus hijos.
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return next({ name: 'Login', query: { redirect: to.fullPath } });
  }

  // Si está autenticado y va a login, redirigir a dashboard
  if (to.name === 'Login' && authStore.isAuthenticated) {
    return next({ name: 'Dashboard' });
  }

  // Cargar perfil si está autenticado pero no tiene datos de usuario
  if (authStore.isAuthenticated && !authStore.user) {
    try {
      await authStore.loadProfile();
    } catch (error) {
      authStore.logout();
      return next({ name: 'Login' });
    }
  }

  // Verificar roles requeridos
  // Esto seguirá funcionando para la ruta de 'Reportes'
  if (to.meta.roles && authStore.user) {
    if (!to.meta.roles.includes(authStore.userRole)) {
      return next({ name: 'Dashboard' });
    }
  }

  next();
});

export default router;
