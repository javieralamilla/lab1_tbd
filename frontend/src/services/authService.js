import api from './api';

const authService = {
  /**
   * Iniciar sesión
   * @param {Object} credentials - { email, password }
   * @returns {Promise}
   */
  async login(credentials) {
    try {
      console.log('Intentando login con:', credentials.email);
      const response = await api.post('/auth/login', credentials);
      console.log('Respuesta del servidor:', response.data);

      const data = response.data;

      // Aceptar que la respuesta pueda ser un string (mensaje de error) o un objeto
      if (!data) {
        throw new Error('Respuesta vacía del servidor');
      }

      // Si el backend devolviera un mensaje de error como string
      if (typeof data === 'string') {
        throw new Error(data);
      }

      // La respuesta del backend incluye: token, usuarioId, email, nombre, rol
      const token = data.token;

      if (!token) {
        // Si no viene token, propagar error con la info recibida
        const serverMessage = data.message || JSON.stringify(data);
        throw new Error(serverMessage || 'Token no recibido del servidor');
      }

      // Guardar token en localStorage
      const tokenKey = import.meta.env.VITE_TOKEN_KEY || 'auth_token';
      localStorage.setItem(tokenKey, token);
      console.log('Token guardado en localStorage');

      // Parsear el nombre completo (viene como "Nombre Apellido")
      const nombreCompleto = data.nombre || '';
      const partesNombre = nombreCompleto.trim().split(' ');
      const nombre = partesNombre[0] || '';
      const apellido = partesNombre.slice(1).join(' ') || '';

      // El rol ya viene en minúsculas desde el backend
      const usuario = {
        usuario_id: data.usuarioId,
        email: data.email,
        nombre: nombre,
        apellido: apellido,
        rol: data.rol // Ya está en minúsculas
      };

      console.log('Usuario procesado:', usuario);

      // Retornar el usuario en el formato esperado por el store
      return {
        token,
        usuario
      };
    } catch (error) {
      console.error('Error en login:', error);
      console.error('Respuesta del servidor:', error.response?.data);

      // Manejar cuando el backend responde con texto plano
      const respData = error.response?.data;
      let errorMessage = 'Error al iniciar sesión';

      if (typeof respData === 'string') {
        errorMessage = respData;
      } else if (respData?.message) {
        errorMessage = respData.message;
      } else if (error.message) {
        errorMessage = error.message;
      }

      throw new Error(errorMessage);
    }
  },


  logout() {
    const tokenKey = import.meta.env.VITE_TOKEN_KEY || 'auth_token';
    localStorage.removeItem(tokenKey);
    console.log('Sesión cerrada');
  },

  getToken() {
    const tokenKey = import.meta.env.VITE_TOKEN_KEY || 'auth_token';
    return localStorage.getItem(tokenKey);
  },


  isAuthenticated() {
    return !!this.getToken();
  },


  async getProfile() {
    try {
      console.log('Obteniendo perfil del usuario');
      const response = await api.get('/auth/profile');
      console.log('Perfil obtenido:', response.data);

      const data = response.data;

      if (!data) {
        throw new Error('Respuesta vacía al obtener perfil');
      }

      if (typeof data === 'string') {
        throw new Error(data);
      }

      // Parsear el nombre completo
      const nombreCompleto = data.nombre || '';
      const partesNombre = nombreCompleto.trim().split(' ');
      const nombre = partesNombre[0] || '';
      const apellido = partesNombre.slice(1).join(' ') || '';

      return {
        usuario_id: data.usuarioId,
        email: data.email,
        nombre: nombre,
        apellido: apellido,
        rol: data.rol // Ya está en minúsculas
      };
    } catch (error) {
      console.error('Error al obtener perfil:', error);

      const respData = error.response?.data;
      if (typeof respData === 'string') {
        throw new Error(respData);
      }

      throw error.response?.data || { message: 'Error al obtener perfil' };
    }
  },


  async register(userData) {
    try {
      // El backend expone /api/auth/registro
      const response = await api.post('/auth/registro', userData);
      return response.data;
    } catch (error) {
      const respData = error.response?.data;
      if (typeof respData === 'string') {
        throw new Error(respData);
      }
      throw error.response?.data || { message: 'Error al registrar usuario' };
    }
  }
};

export default authService;
