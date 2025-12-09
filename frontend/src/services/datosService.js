import api from './api';

const datosService = {
  // Obtener todos los datos demográficos (con filtro opcional de zona)
  async getAll(zonaId = null) {
    try {
      let url = '/datos-demograficos';
      
      if (zonaId) {
        url += `?zonaId=${zonaId}`;
      }
      
      const response = await api.get(url);
      return response.data;
    } catch (error) {
      console.error('Error al obtener datos demográficos:', error);
      throw error.response?.data || { message: 'Error al obtener datos demográficos' };
    }
  },

  // Obtener un dato demográfico por ID
  async getById(id) {
    try {
      const response = await api.get(`/datos-demograficos/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error al obtener dato demográfico:', error);
      throw error.response?.data || { message: 'Error al obtener dato demográfico' };
    }
  },

  // Crear nuevo dato demográfico
  async create(datoData) {
    try {
      const response = await api.post('/datos-demograficos', datoData);
      return response.data;
    } catch (error) {
      console.error('Error al crear dato demográfico:', error);
      throw error.response?.data || { message: 'Error al crear dato demográfico' };
    }
  },

  // Actualizar dato demográfico
  async update(id, datoData) {
    try {
      const response = await api.put(`/datos-demograficos/${id}`, datoData);
      return response.data;
    } catch (error) {
      console.error('Error al actualizar dato demográfico:', error);
      throw error.response?.data || { message: 'Error al actualizar dato demográfico' };
    }
  },

  // Eliminar dato demográfico
  async delete(id) {
    try {
      const response = await api.delete(`/datos-demograficos/${id}`);
      return response.data;
    } catch (error) {
      console.error('Error al eliminar dato demográfico:', error);
      throw error.response?.data || { message: 'Error al eliminar dato demográfico' };
    }
  }
};

export default datosService;
