<template>
  <aside class="sidebar">
    <div class="sidebar-container">
      <div class="sidebar-top">
        <div class="sidebar-brand">
          <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
          </svg>
          <span>Plataforma de Urbanismo</span>
        </div>

        <nav class="sidebar-menu">
          <router-link to="/dashboard" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect>
            </svg>
            <span>Dashboard</span>
          </router-link>

          <router-link to="/zonas" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z"></path>
            </svg>
            <span>Zonas</span>
          </router-link>

          <router-link to="/proyectos" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline>
            </svg>
            <span>Proyectos</span>
          </router-link>

          <router-link to="/puntos-interes" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path><circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span>Puntos de Interés</span>
          </router-link>

          <router-link to="/datos-demograficos" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
              <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
            </svg>
            <span>Datos Demográficos</span>
          </router-link>

          <router-link to="/mapa-integrado" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="m12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83Z"/>
              <path d="m22 17.65-9.17 4.16a2 2 0 0 1-1.66 0L2 17.65"/>
              <path d="m22 12.65-9.17 4.16a2 2 0 0 1-1.66 0L2 12.65"/>
            </svg>
            <span>Mapa Integrado</span>
          </router-link>

          <router-link to="/analisis-espacial" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
              <circle cx="12" cy="10" r="3"></circle>
            </svg>
            <span>Análisis Espacial</span>
          </router-link>

          <router-link to="/simulacion-proyectos" class="nav-item">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"></path>
              <polyline points="9 22 9 12 15 12 15 22"></polyline>
            </svg>
            <span>Simulación de Proyectos</span>
          </router-link>

          <router-link
            v-if="authStore.isAdmin || authStore.isPlanificador"
            to="/reportes"
            class="nav-item"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <line x1="18" y1="20" x2="18" y2="10"></line><line x1="12" y1="20" x2="12" y2="4"></line><line x1="6" y1="20" x2="6" y2="14"></line>
            </svg>
            <span>Reportes</span>
          </router-link>
        </nav>
      </div>

      <div class="sidebar-user">
        <div class="user-info">
          <div class="user-avatar">
            {{ initials }}
          </div>
          <div class="user-details">
            <div class="user-name">{{ authStore.userName }}</div>
            <div class="user-role">{{ roleName }}</div>
          </div>
        </div>
        <button @click="handleLogout" class="btn-logout" title="Cerrar Sesión">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line>
          </svg>
        </button>
      </div>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const initials = computed(() => {
  if (!authStore.user) return '';
  const names = authStore.userName.split(' ');
  return names.map(n => n[0]).join('').toUpperCase().slice(0, 2);
});

const roleName = computed(() => {
  const roles = {
    'administrador': 'Administrador',
    'planificador': 'Planificador',
    'autoridad_municipal': 'Autoridad Municipal'
  };
  return roles[authStore.userRole] || '';
});

const handleLogout = () => {
  authStore.logout();
  router.push('/login');
};
</script>

<style scoped>
.sidebar {
  width: 260px;
  background: var(--bg-secondary);
  border-right: 1px solid var(--border-color);
  height: 100vh;
  position: sticky; /* Se mantiene fijo mientras se hace scroll en el contenido */
  top: 0;
  display: flex;
  flex-direction: column;
}

.sidebar-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100%;
  /* Reducimos un poco el padding lateral para que el contenido quede más a la izquierda */
  padding: 24px 12px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 32px;
  padding: 0 8px; /* Alineación con items */
}

.sidebar-brand svg {
  color: var(--accent-primary);
  flex-shrink: 0;
}

.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
  flex: 1; /* Ocupa el espacio disponible */
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  color: var(--text-secondary);
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.2s;
  font-weight: 500;
  font-size: 14px;
}

.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
  color: var(--text-primary);
}

.nav-item.router-link-active {
  background-color: var(--accent-primary);
  color: white;
  font-weight: 600;
}

.sidebar-user {
  border-top: 1px solid var(--border-color);
  padding-top: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1; /* Permite que el nombre se corte si es largo */
  overflow: hidden;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--icon-zonas-bg); /* Reusamos un gradiente bonito */
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.user-details {
  text-align: left;
  overflow: hidden; /* Evita que el texto se desborde */
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-role {
  font-size: 12px;
  color: var(--text-secondary);
}

.btn-logout {
  padding: 10px;
  border: none;
  background-color: var(--accent-danger-bg);
  color: var(--accent-danger);
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: all 0.2s;
  flex-shrink: 0;
}

.btn-logout:hover {
  background-color: var(--accent-danger-bg-hover);
  color: white;
}

/* Ocultamos el texto en pantallas muy pequeñas, si el sidebar se mantiene */
@media (max-width: 768px) {
  /* Esta media query debería ser manejada por AppLayout.vue,
     pero si el sidebar se hiciera colapsable, aquí irían los estilos */
  .sidebar {
    /* En pantallas pequeñas dejamos que el layout sea natural (no fixed) */
    position: relative;
    width: 100%;
    height: auto;
    border-right: none;
  }
  .sidebar-brand span, .nav-item span, .user-details {
    /* display: none; */
  }
}

/*fijar sidebar al borde izquierdo en pantallas grandes */
@media (min-width: 769px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    /* Asegurar que el sidebar no tenga margen que lo aleje del borde */
    margin: 0;
  }
}
</style>
