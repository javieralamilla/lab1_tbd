import api from './api';

const proyectosService = {

  async getAll(tipoZona = null) {
    try {
      console.log('[ProyectosService] Obteniendo todos los proyectos...');

      let url = '/proyectos';
      if (tipoZona && tipoZona !== '') {
        url += `?tipoZona=${encodeURIComponent(tipoZona)}`;
      }

      const response = await api.get(url);
      console.log('[ProyectosService] Proyectos obtenidos:', response.data?.length || 0);
      return response.data;
    } catch (error) {
      console.error('[ProyectosService] Error:', error);
      if (error.response?.status === 401) {
        throw new Error('No autorizado. Por favor, inicia sesión nuevamente.');
      }
      if (error.response?.status === 403) {
        throw new Error('Acceso denegado. No tienes permisos para ver los proyectos.');
      }
      if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
        throw new Error('No se puede conectar al servidor. Verifica que el backend esté corriendo.');
      }
      throw error.response?.data || new Error('Error al obtener proyectos');
    }
  },


  async getById(id) {
    try {
      const response = await api.get(`/proyectos/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener proyecto' };
    }
  },


  async create(proyectoData) {
    try {
      const response = await api.post('/proyectos', proyectoData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al crear proyecto' };
    }
  },


  async update(id, proyectoData) {
    try {
      const response = await api.put(`/proyectos/${id}`, proyectoData);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al actualizar proyecto' };
    }
  },


  async delete(id) {
    try {
      const response = await api.delete(`/proyectos/${id}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al eliminar proyecto' };
    }
  },


  async getByEstado(estado) {
    try {
      const response = await api.get(`/proyectos?estado=${estado}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener proyectos' };
    }
  },


  async getByUsuario(usuarioId) {
    try {
      const response = await api.get(`/proyectos/usuario/${usuarioId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener proyectos' };
    }
  },


  async actualizarProyectosRetrasados(usuarioId) {
    try {
      const response = await api.post(`/proyectos/actualizar-retrasados/${usuarioId}`);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al actualizar proyectos' };
    }
  },


  async getSuperposicion() {
    try {
      const response = await api.get('/proyectos/analisis/superposicion');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener superposición' };
    }
  },


  async getResumenEstadoZona(tipoZona = null, estado = null) {
    try {
      const params = new URLSearchParams();
      if (tipoZona && tipoZona !== '' && tipoZona !== 'todos') {
        params.append('tipoZona', tipoZona);
      }
      if (estado && estado !== '' && estado !== 'todos') {
        params.append('estado', estado);
      }

      const queryString = params.toString();
      const url = queryString
        ? `/proyectos/analisis/resumen-estado-zona?${queryString}`
        : '/proyectos/analisis/resumen-estado-zona';

      const response = await api.get(url);
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener resumen' };
    }
  },

  async getZonasSinPlanificacion() {
    try {
      const response = await api.get('/proyectos/analisis/zonas-sin-planificacion');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener zonas sin planificación' };
    }
  }
};

export default proyectosService;
