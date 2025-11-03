import api from './api';

const puntosInteresService = {

  async getAll() {
    try {
      console.log('[PuntosInteresService] Obteniendo todos los puntos de interés...');
      const response = await api.get('/puntos-interes');
      console.log('[PuntosInteresService] Puntos obtenidos:', response.data?.length || 0);
      return response.data;
    } catch (error) {
      console.error('[PuntosInteresService] Error:', error);
      if (error.response?.status === 401) {
        throw new Error('No autorizado. Por favor, inicia sesión nuevamente.');
      }
      if (error.response?.status === 403) {
        throw new Error('Acceso denegado. No tienes permisos para ver los puntos de interés.');
      }
      if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
        throw new Error('No se puede conectar al servidor. Verifica que el backend esté corriendo.');
      }
      throw error.response?.data || new Error('Error al obtener puntos de interés');
    }
  },


  async getById(id) {
    try {
      const response = await api.get(`/puntos-interes/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener punto de interés' };
    }
  },


  async create(puntoData) {
    try {
      const response = await api.post('/puntos-interes', puntoData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al crear punto de interés' };
    }
  },


  async update(id, puntoData) {
    try {
      const response = await api.put(`/puntos-interes/${id}`, puntoData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al actualizar punto de interés' };
    }
  },


  async delete(id) {
    try {
      const response = await api.delete(`/puntos-interes/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al eliminar punto de interés' };
    }
  },


  async getByTipo(tipo) {
    try {
      const response = await api.get(`/puntos-interes/tipo/${tipo}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener puntos' };
    }
  },


  async getByZona(zonaId) {
    try {
      const response = await api.get(`/puntos-interes/zona/${zonaId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener puntos' };
    }
  },


  async getEscuelasCercanas() {
    try {
      const response = await api.get('/puntos-interes/analisis/escuelas-cerca-proyectos');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener escuelas cercanas' };
    }
  },


  async getCoberturaInfraestructura() {
    try {
      const response = await api.get('/puntos-interes/analisis/cobertura-infraestructura');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener cobertura' };
    }
  },


  async refrescarCobertura() {
    try {
      const response = await api.post('/puntos-interes/refrescar-cobertura');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al refrescar cobertura' };
    }
  }
};

export default puntosInteresService;
