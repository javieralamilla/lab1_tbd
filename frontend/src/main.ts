import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router/index.js'
import '@/assets/css/theme.css';
import { vPermission } from '@/composables/usePermissions';

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Registrar directiva global de permisos
app.directive('permission', vPermission)

app.mount('#app')
