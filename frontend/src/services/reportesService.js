import api from './api';


const reportesService = {

  obtenerEstadisticas: async () => {
    try {
      const response = await api.get('/reportes/estadisticas');
      return response.data;
    } catch (error) {
      console.error('Error al obtener estadísticas:', error);
      throw error;
    }
  },


  generarReporte: async (filtros = {}) => {
    try {
      const params = new URLSearchParams();

      if (filtros.tipoReporte) {
        params.append('tipo', filtros.tipoReporte);
      }

      if (filtros.fechaInicio) {
        params.append('fechaInicio', filtros.fechaInicio);
      }

      if (filtros.fechaFin) {
        params.append('fechaFin', filtros.fechaFin);
      }

      const response = await api.get(`/reportes/generar?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error al generar reporte:', error);
      throw error;
    }
  },


  obtenerEstadisticasCrecimiento: async () => {
    try {
      const response = await api.get('/reportes/crecimiento');
      return response.data;
    } catch (error) {
      console.error('Error al obtener estadísticas de crecimiento:', error);
      throw error;
    }
  },


  generarReporteProyectos: async (fechaInicio, fechaFin) => {
    try {
      const params = new URLSearchParams();

      if (fechaInicio) {
        params.append('fechaInicio', fechaInicio);
      }

      if (fechaFin) {
        params.append('fechaFin', fechaFin);
      }

      const response = await api.get(`/reportes/proyectos?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error al generar reporte de proyectos:', error);
      throw error;
    }
  },


  generarReportePuntos: async (fechaInicio, fechaFin) => {
    try {
      const params = new URLSearchParams();

      if (fechaInicio) {
        params.append('fechaInicio', fechaInicio);
      }

      if (fechaFin) {
        params.append('fechaFin', fechaFin);
      }

      const response = await api.get(`/reportes/puntos?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error al generar reporte de puntos:', error);
      throw error;
    }
  },


  generarReporteZonas: async (fechaInicio, fechaFin) => {
    try {
      const params = new URLSearchParams();

      if (fechaInicio) {
        params.append('fechaInicio', fechaInicio);
      }

      if (fechaFin) {
        params.append('fechaFin', fechaFin);
      }

      const response = await api.get(`/reportes/zonas?${params.toString()}`);
      return response.data;
    } catch (error) {
      console.error('Error al generar reporte de zonas:', error);
      throw error;
    }
  },


  exportarPDF: async (datos, titulo = 'Reporte') => {
    try {
      if (!datos || datos.length === 0) {
        throw new Error('No hay datos para exportar');
      }

      // Importar jsPDF dinámicamente
      const jsPDFModule = await import('jspdf');
      const jsPDF = jsPDFModule.default || jsPDFModule.jsPDF;

      // Importar jspdf-autotable
      await import('jspdf-autotable');

      const doc = new jsPDF();

      // Título del reporte
      doc.setFontSize(18);
      doc.text(titulo, 14, 20);

      // Fecha de generación
      doc.setFontSize(11);
      doc.text(`Fecha de generación: ${new Date().toLocaleString('es-ES')}`, 14, 30);

      // Preparar datos para la tabla
      const tableColumn = ['ID', 'Nombre', 'Tipo', 'Fecha', 'Estado'];
      const tableRows = datos.map(item => [
        item.id || '',
        item.nombre || '',
        item.tipo || '',
        item.fecha ? new Date(item.fecha).toLocaleDateString('es-ES') : '',
        item.estado || ''
      ]);

      // Generar tabla
      doc.autoTable({
        head: [tableColumn],
        body: tableRows,
        startY: 40,
        styles: { fontSize: 9 },
        headStyles: { fillColor: [66, 139, 202] },
        alternateRowStyles: { fillColor: [245, 245, 245] }
      });

      // Guardar archivo
      const nombreArchivo = `${titulo.replace(/\s+/g, '_')}_${Date.now()}.pdf`;
      doc.save(nombreArchivo);

      console.log('PDF generado exitosamente:', nombreArchivo);
      return true;
    } catch (error) {
      console.error('Error al exportar PDF:', error);
      if (error.message === 'No hay datos para exportar') {
        throw error;
      }
      throw new Error('No se pudo generar el archivo PDF. Asegúrate de que las bibliotecas estén instaladas.');
    }
  },


  exportarExcel: async (datos, nombreArchivo = 'reporte') => {
    try {
      if (!datos || datos.length === 0) {
        throw new Error('No hay datos para exportar');
      }

      // Importar xlsx dinámicamente
      const XLSX = await import('xlsx');

      // Crear hoja de cálculo
      const worksheet = XLSX.utils.json_to_sheet(datos);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, 'Reporte');

      // Guardar archivo
      const archivo = `${nombreArchivo}_${Date.now()}.xlsx`;
      XLSX.writeFile(workbook, archivo);

      console.log('Excel generado exitosamente:', archivo);
      return true;
    } catch (error) {
      console.error('Error al exportar Excel:', error);
      if (error.message === 'No hay datos para exportar') {
        throw error;
      }
      throw new Error('No se pudo generar el archivo Excel. Asegúrate de que las bibliotecas estén instaladas.');
    }
  }
};

export default reportesService;

