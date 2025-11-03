import api from './api';

const proyectosService = {

  async getAll() {
    try {
      console.log('[ProyectosService] Obteniendo todos los proyectos...');
      const response = await api.get('/proyectos');
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


  async getResumenEstadoZona() {
    try {
      const response = await api.get('/proyectos/analisis/resumen-estado-zona');
      return response.data;
    } catch (error) {
      throw error.response?.data || { message: 'Error al obtener resumen' };
    }
  }
};

export default proyectosService;
