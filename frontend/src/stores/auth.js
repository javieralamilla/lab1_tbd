import { defineStore } from 'pinia';
import authService from '@/services/authService';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: authService.getToken(),
    loading: false,
    error: null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token,
    userRole: (state) => state.user?.rol || null,
    userName: (state) => state.user ? `${state.user.nombre} ${state.user.apellido}` : '',
    isAdmin: (state) => state.user?.rol === 'administrador',
    isPlanificador: (state) => state.user?.rol === 'planificador',
    isAutoridadMunicipal: (state) => state.user?.rol === 'autoridad_municipal'
  },

  actions: {
    async login(credentials) {
      this.loading = true;
      this.error = null;

      try {
        const { token, usuario } = await authService.login(credentials);
        this.token = token;
        this.user = usuario;
        return true;
      } catch (error) {
        this.error = error.message || 'Error al iniciar sesión';
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async loadProfile() {
      if (!this.token) return;

      this.loading = true;
      try {
        const user = await authService.getProfile();
        this.user = user;
      } catch (error) {
        this.error = error.message;
        this.logout();
      } finally {
        this.loading = false;
      }
    },

    logout() {
      authService.logout();
      this.user = null;
      this.token = null;
      this.error = null;
    },

    clearError() {
      this.error = null;
    }
  }
});
