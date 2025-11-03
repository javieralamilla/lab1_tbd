<template>
  <div class="login-container">
    <div class="background-image"></div>

    <div class="login-card">
      <form @submit.prevent="handleLogin" class="login-form">
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="credentials.email"
            type="email"
            placeholder="Ingresa tu email"
            required
            :disabled="loading"
          />
        </div>

        <div class="form-group">
          <label for="password">Contraseña</label>
          <input
            id="password"
            v-model="credentials.password"
            type="password"
            placeholder="············"
            required
            :disabled="loading"
          />
        </div>

        <div class="forgot-password">
          <a href="#">¿Olvidaste tu contraseña?</a>
        </div>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <button type="submit" class="btn-login" :disabled="loading">
          <span v-if="!loading">INICIAR SESIÓN</span>
          <span v-else>Iniciando sesión...</span>
        </button>
      </form>

      <div class="test-credentials">
        <small>
          <strong>Credenciales de prueba:</strong><br>
          Email: mj.fernandez@minvu.cl (administrador)<br>
          Email: c.rodriguez@seremi.cl (planificador)<br>
          Email: p.gonzalez@municipalidad.cl (autoridad_municipal)<br>
          Contraseña: password
        </small>
      </div>
    </div>

    <div class="hero-text">
      <h1 class="main-title">
        Plataforma<br>
        de Urbanismo<br>
        y Planificación<br>
        de Ciudades
      </h1>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();

const credentials = ref({
  email: '',
  password: ''
});

const loading = ref(false);
const error = ref(null);

const handleLogin = async () => {
  loading.value = true;
  error.value = null;

  try {
    await authStore.login(credentials.value);

    // Redirigir a la ruta original o al dashboard
    const redirect = route.query.redirect || '/dashboard';
    router.push(redirect);
  } catch (err) {
    error.value = err.message || 'Error al iniciar sesión. Verifica tus credenciales.';
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
/* --- MODIFICADO --- */
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: flex-end; /* Movido a la derecha */
  overflow: hidden;
}

/* Imagen de fondo que ocupa toda la pantalla */
.background-image {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, #1e3a8a 0%, #3b0764 100%);
  background-image:
    linear-gradient(135deg, rgba(30, 58, 138, 0.7) 0%, rgba(59, 7, 100, 0.7) 100%),
    url('@/assets/images/login-background.png'); /* Asegúrate que esta imagen exista */
  background-size: cover;
  background-position: center;
  z-index: 0;
}

/* Efecto de overlay oscuro */
.background-image::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  z-index: 1;
}

/* Texto hero en la izquierda */
.hero-text {
  position: absolute;
  left: 80px;
  top: 50%;
  transform: translateY(-50%);
  z-index: 2;
  color: white;
}

/* --- MODIFICADO --- */
.main-title {
  font-family: 'Bebas Neue', sans-serif; /* <-- CAMBIO DE FUENTE */
  font-size: 80px; /* <-- AJUSTE: Esta fuente es más estrecha, un tamaño mayor se verá bien */
  font-weight: 400; /* <-- AJUSTE: Bebas Neue no tiene peso 900, su peso regular es 400 */
  line-height: 1.1;
  margin: 0;
  text-shadow: 2px 4px 8px rgba(0, 0, 0, 0.4);
  letter-spacing: 1.5px; /* <-- AJUSTE: Añadimos espacio para legibilidad (en vez de -1.5px) */
  text-transform: uppercase; /* <-- NUEVO: Para que siempre esté en mayúsculas como la imagen */
}

/* --- MODIFICADO --- */
/* Card de login flotante */
.login-card {
  position: relative;
  z-index: 3;
  width: 100%;
  max-width: 450px;
  /* Fondo oscuro y transparente */
  background: rgba(28, 25, 52, 0.75);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 40px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  margin-right: 100px;
  /* Borde ligero para el efecto "glass" */
  border: 1px solid rgba(255, 255, 255, 0.15);
}

.login-form {
  width: 100%;
}

.form-group {
  margin-bottom: 24px;
}

/* --- MODIFICADO --- */
.form-group label {
  display: block;
  font-size: 14px;
  font-weight: 600;
  color: #e2e8f0; /* Color de label claro */
  margin-bottom: 8px;
  letter-spacing: 0.3px;
}

/* --- MODIFICADO --- */
.form-group input {
  width: 100%;
  padding: 16px 20px;
  font-size: 15px;
  background-color: rgba(255, 255, 255, 0.1); /* Fondo de input transparente */
  border: 1px solid rgba(255, 255, 255, 0.3); /* Borde de input claro */
  border-radius: 12px;
  transition: all 0.3s;
  box-sizing: border-box;
  color: #ffffff; /* Color de texto al escribir */
}

/* --- MODIFICADO --- */
.form-group input::placeholder {
  color: #cbd5e0; /* Color de placeholder claro */
}

/* --- MODIFICADO --- */
.form-group input:focus {
  outline: none;
  background-color: rgba(255, 255, 255, 0.2); /* Fondo al hacer foco */
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.form-group input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.forgot-password {
  text-align: right;
  margin-bottom: 28px;
  margin-top: -8px;
}

.forgot-password a {
  color: #3b82f6;
  text-decoration: none;
  font-size: 13px;
  font-weight: 500;
  transition: color 0.3s;
}

.forgot-password a:hover {
  color: #60a5fa; /* Un poco más claro para el fondo oscuro */
  text-decoration: underline;
}

.error-message {
  background-color: #fee;
  border: 1px solid #fcc;
  color: #c33;
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 14px;
  margin-bottom: 20px;
}

.btn-login {
  width: 100%;
  padding: 18px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
  color: white;
  background: #3b82f6;
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s;
  margin-bottom: 0;
}

.btn-login:hover:not(:disabled) {
  background: #2563eb;
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
}

.btn-login:active:not(:disabled) {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* --- MODIFICADO --- */
/* Estilo para las credenciales de prueba */
.test-credentials {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
  text-align: center;
}

.test-credentials small {
  color: #e2e8f0; /* Texto claro */
  font-size: 11px;
  line-height: 1.6;
}

.test-credentials strong {
  color: #ffffff; /* Texto 'strong' más claro */
}


/* Responsive */
@media (max-width: 1200px) {
  .hero-text {
    left: 60px;
  }

  .main-title {
    font-size: 60px;
  }

  .login-card {
    margin-right: 60px;
  }
}

@media (max-width: 1024px) {
  .hero-text {
    left: 40px;
  }

  .main-title {
    font-size: 48px;
  }

  .login-card {
    margin-right: 40px;
    max-width: 400px;
  }
}

@media (max-width: 768px) {
  /* --- MODIFICADO --- */
  .login-container {
    padding: 20px;
    justify-content: center; /* Centramos en móvil */
  }

  .hero-text {
    position: static;
    transform: none;
    text-align: center;
    margin-bottom: 40px;
  }

  .main-title {
    font-size: 36px;
  }

  /* --- MODIFICADO --- */
  .login-card {
    margin: 0 auto; /* Centramos en móvil */
    max-width: 100%;
    padding: 36px 28px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 28px;
  }

  .login-card {
    padding: 28px 24px;
    border-radius: 16px;
  }

  .form-group input {
    padding: 14px 16px;
  }

  .btn-login {
    padding: 16px;
  }
}
</style>
