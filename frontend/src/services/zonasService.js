import api from './api';

const zonasService = {

  async getAll() {
    try {
      console.log('[ZonasService] Obteniendo todas las zonas urbanas...');
      const response = await api.get('/zonas');
      console.log('[ZonasService] Zonas obtenidas:', response.data?.length || 0);
      return response.data;
    } catch (error) {
      console.error('[ZonasService] Error:', error);
      if (error.response?.status === 401) {
        throw new Error('No autorizado. Por favor, inicia sesión nuevamente.');
      }
      if (error.response?.status === 403) {
        throw new Error('Acceso denegado. No tienes permisos para ver las zonas.');
      }
      if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
        throw new Error('No se puede conectar al servidor. Verifica que el backend esté corriendo.');
      }
      throw error.response?.data || new Error('Error al obtener zonas');
    }
  },


  async getById(id) {
    try {
      const response = await api.get(`/zonas/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener zona' };
    }
  },


  async create(zonaData) {
    try {
      const response = await api.post('/zonas', zonaData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al crear zona' };
    }
  },


  async update(id, zonaData) {
    try {
      const response = await api.put(`/zonas/${id}`, zonaData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al actualizar zona' };
    }
  },


  async delete(id) {
    try {
      const response = await api.delete(`/zonas/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al eliminar zona' };
    }
  },


  async getDensidadPoblacion() {
    try {
      console.log('[ZonasService] Obteniendo densidad poblacional...');
      const response = await api.get('/zonas/analisis/densidad-poblacion');
      console.log('[ZonasService] Densidad obtenida:', response.data?.length || 0, 'zonas');
      return response.data;
    } catch (error) {
      console.error('[ZonasService] Error obteniendo densidad:', error);
      if (error.response?.status === 401) {
        throw new Error('No autorizado. Por favor, inicia sesión nuevamente.');
      }
      if (error.response?.status === 403) {
        throw new Error('Acceso denegado. No tienes permisos para ver la densidad.');
      }
      if (error.response?.status === 500) {
        throw new Error('Error en el servidor al calcular densidad. Verifica los datos demográficos.');
      }
      if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
        throw new Error('No se puede conectar al servidor. Verifica que el backend esté corriendo.');
      }
      throw error.response?.data || new Error('Error al obtener densidad');
    }
  },


  async getZonasEscasezHospitales() {
    try {
      const response = await api.get('/zonas/analisis/escasez-hospitales');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener zonas' };
    }
  },


  async getZonasRapidoCrecimiento() {
    try {
      const response = await api.get('/zonas/analisis/rapido-crecimiento');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener zonas' };
    }
  },


  async getZonasSinPlanificacion() {
    try {
      const response = await api.get('/zonas/analisis/sin-planificacion');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener zonas' };
    }
  },


  async getCoberturaInfraestructura() {
    try {
      const response = await api.get('/zonas/analisis/cobertura-infraestructura');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener cobertura de infraestructura' };
    }
  },

  async getEstadisticasArea(geojson) {
    try {
      const response = await api.post('/zonas/analisis/area', { geojson });
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener estadísticas del área' };
    }
  }
};

export default zonasService;
