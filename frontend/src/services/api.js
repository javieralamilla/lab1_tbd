import axios from 'axios';

// Crear instancia de Axios
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// Interceptor para agregar token a las peticiones
api.interceptors.request.use(
  (config) => {
    console.log(`[API] ${config.method.toUpperCase()} ${config.url}`, config.data);
    const token = localStorage.getItem(import.meta.env.VITE_TOKEN_KEY || 'auth_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    console.error('[API] Request error:', error);
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas
api.interceptors.response.use(
  (response) => {
    console.log(`[API] Response ${response.status}:`, response.data);
    return response;
  },
  (error) => {
    console.error('[API] Response error:', error.response?.status, error.response?.data);

    if (error.response?.status === 401) {
      // Token expirado o inválido
      localStorage.removeItem(import.meta.env.VITE_TOKEN_KEY || 'auth_token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
