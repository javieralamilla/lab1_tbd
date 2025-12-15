<template>
  <div class="app-layout">
    <Sidebar />
    <main class="main-content">
      <div class="content-wrapper">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import Sidebar from '@/components/common/Sidebar.vue';
</script>

<style scoped>
.app-layout {
  display: flex;
  min-height: 100vh;
  background-color: var(--bg-primary);
}

.main-content {
  flex: 1;
  overflow-y: auto; /* Permite scroll solo en el contenido */
  padding: 32px 24px 48px 24px; /* Padding similar a Simulación de Proyectos */
  box-sizing: border-box;
  /* Ocultar scrollbar pero mantener funcionalidad */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE y Edge */
}

/* Ocultar scrollbar en Chrome, Safari y Opera */
.main-content::-webkit-scrollbar {
  display: none;
}

/* Si el sidebar es fijo, desplazamos el contenido principal para evitar solapamiento */
@media (min-width: 769px) {
  .main-content {
    margin-left: 260px; /* Igual que el width del sidebar */
  }
}


.content-wrapper {
  width: 100%;
  max-width: none;
  margin: 0;        /* Elimina centrado automático */
}

/* Usamos selectors profundos para afectar los elementos renderizados por <router-view />
   Scoped styles no los afectan directamente, por eso ::v-deep es necesario. */
.main-content ::v-deep .dashboard-container,
.main-content ::v-deep .container,
.main-content ::v-deep .page-container,
.main-content ::v-deep .content,
.main-content ::v-deep .wrapper {
  max-width: none !important;
  width: 100% !important;
  margin: 0 !important;
  padding: 0 !important; /* Deja que cada vista controle su propio padding si lo necesita */
  box-sizing: border-box;
}

.main-content ::v-deep .dashboard-content {
  width: 100% !important;
  max-width: none !important;
}

/* Para pantallas pequeñas, podemos mantener diseño en columna */
@media (max-width: 768px) {
  .app-layout {
    flex-direction: column;
  }
  .main-content {
    padding: 16px;
    margin-left: 0; /* Revertir desplazamiento en móviles */
  }

}
</style>
