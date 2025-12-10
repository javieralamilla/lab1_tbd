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
      // Incluir emoji por estado y preparar datos
      const estadoEmoji = {
        'Planeado': '📋',
        'En Curso': '🚧',
        'Completado': '✅',
        'Retrasado': '⚠️',
        'Cancelado': '❌'
      };

      const estadoColorRGB = {
        'Planeado': [59,130,246], // azul
        'En Curso': [245,158,11], // naranja
        'Completado': [16,185,129], // verde
        'Retrasado': [239,68,68], // rojo
        'Cancelado': [107,114,128] // gris
      };

      const tableColumn = ['ID', 'Nombre', 'Tipo', 'Fecha', 'Estado'];
      const tableRows = datos.map(item => [
        item.id || '',
        item.nombre || '',
        item.tipo || '',
        item.fecha ? new Date(item.fecha).toLocaleDateString('es-ES') : '',
        `${estadoEmoji[item.estado] || ''} ${item.estado || ''}`
      ]);

      // Generar tabla usando jspdf-autotable
      // Primero intentamos usar doc.autoTable (registro automático), si no está disponible usamos la función exportada
      const autoTableModule = await import('jspdf-autotable').catch(() => null);
      const autoTableFunc = autoTableModule && (autoTableModule.default || autoTableModule);

      // Si el plugin no ha agregado doc.autoTable, intentar inicializarlo explícitamente
      if (typeof doc.autoTable !== 'function' && autoTableFunc) {
        try {
          // Algunas variantes exportan una función que espera el objeto jsPDF
          if (typeof autoTableFunc === 'function') {
            // Llamar con doc para intentar registrar el plugin
            try { autoTableFunc(doc); } catch (e) { /* ignore */ }
          }
          // Si sigue sin existir, intentar llamar con el constructor jsPDF
          if (typeof doc.autoTable !== 'function') {
            try {
              const jsPDFModuleAgain = await import('jspdf').catch(() => null);
              const jsPDFCtor = (jsPDFModuleAgain && (jsPDFModuleAgain.jsPDF || jsPDFModuleAgain.default)) || null;
              if (typeof autoTableFunc === 'function' && jsPDFCtor) {
                try { autoTableFunc(jsPDFCtor); } catch (e) { /* ignore */ }
              }
            } catch (e) {
              // ignore
            }
          }
        } catch (e) {
          // ignore initialization errors, se manejará más abajo
        }
      }

      const autoTableOptions = {
        head: [tableColumn],
        body: tableRows,
        startY: 40,
        styles: { fontSize: 9 },
        headStyles: { fillColor: [66, 139, 202] },
        alternateRowStyles: { fillColor: [245, 245, 245] },
        didParseCell: function (data) {
          if (data.section === 'body' && data.column.index === 4) {
            const cellText = data.cell.text && data.cell.text.join(' ');
            if (cellText) {
              const parts = cellText.split(' ');
              const estado = parts.slice(1).join(' ') || parts[0];
              const rgb = estadoColorRGB[estado];
              if (rgb) {
                data.cell.styles.fillColor = rgb;
                data.cell.styles.textColor = [255,255,255];
              }
            }
          }
        }
      };

      if (typeof doc.autoTable === 'function') {
        doc.autoTable(autoTableOptions);
      } else if (typeof autoTableFunc === 'function') {
        // Algunas versiones exportan la función que recibe (doc, options)
        try {
          // Intentar llamar como plugin: autoTable(doc, options)
          autoTableFunc(doc, autoTableOptions);
        } catch (e) {
          // Si falla, intentar la versión que agrega autoTable a jsPDF (algunos módulos requieren inicialización)
          try {
            // Algunos builds exportan una función que espera el constructor jsPDF
            autoTableFunc((await import('jspdf')).jsPDF || (await import('jspdf')).default);
            if (typeof doc.autoTable === 'function') {
              doc.autoTable(autoTableOptions);
            } else {
              throw new Error('doc.autoTable no está disponible luego de inicializar el plugin');
            }
          } catch (e2) {
            throw new Error('No se pudo inicializar jspdf-autotable: ' + (e2.message || String(e2)));
          }
        }
      } else {
        throw new Error('jspdf-autotable no está disponible. Asegúrate de que la dependencia esté instalada.');
      }

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
      throw new Error('No se pudo generar el archivo PDF. Asegúrate de que las bibliotecas estén instaladas. Detalle: ' + (error.message || String(error)));
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
      throw new Error('No se pudo generar el archivo Excel. Asegúrate de que las bibliotecas estén instaladas. Detalle: ' + (error.message || String(error)));
    }
  },

  // Intentar descargar archivo generado por backend (mejor para datasets grandes)
  exportarDesdeBackend: async (filtros = {}, formato = 'pdf') => {
    try {
      const params = new URLSearchParams();
      if (filtros.tipoReporte) params.append('tipo', filtros.tipoReporte);
      if (filtros.fechaInicio) params.append('fechaInicio', filtros.fechaInicio);
      if (filtros.fechaFin) params.append('fechaFin', filtros.fechaFin);
      params.append('format', formato);

      const response = await api.get(`/reportes/export?${params.toString()}`, {
        responseType: 'blob'
      });

      // Forzar descarga del blob recibido
      const blob = new Blob([response.data], { type: response.headers['content-type'] || (formato === 'pdf' ? 'application/pdf' : 'application/octet-stream') });
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      const ext = formato === 'xlsx' ? 'xlsx' : (formato === 'xls' ? 'xls' : 'pdf');
      const filename = `reporte_${Date.now()}.${ext}`;
      a.href = url;
      a.download = filename;
      document.body.appendChild(a);
      a.click();
      a.remove();
      window.URL.revokeObjectURL(url);

      return true;
    } catch (error) {
      console.error('Error al exportar desde backend:', error);
      // Mejorar el mensaje de error para que la UI muestre el código HTTP y la respuesta del servidor
      if (error.response) {
        const status = error.response.status;
        let msg = `Request failed with status code ${status}`;
        try {
          const text = await (new Response(error.response.data).text()).catch(() => null);
          if (text) msg += `: ${text}`;
        } catch (_) {}
        const err = new Error(msg);
        err.status = status;
        throw err;
      }
      throw error;
    }
  },

  obtenerZonasEscasezHospitales: async (año = 2024) => {
    try {
      const response = await api.get(`/reportes/zonas-escasez-hospitales?año=${año}`);
      return response.data;
    } catch (error) {
      console.error('Error al obtener zonas con escasez de hospitales:', error);
      throw error;
    }
  }
};

export default reportesService;


