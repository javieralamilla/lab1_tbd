package cl.usach.tbd.grupo4.plataforma_urbanismo.service;

import cl.usach.tbd.grupo4.plataforma_urbanismo.dto.EstadisticasDTO;
import cl.usach.tbd.grupo4.plataforma_urbanismo.dto.ReporteDTO;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.ProyectoRepository;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.PuntoInteresRepository;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.ZonaUrbanaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProyectoRepository proyectoRepository;

    @Autowired
    private PuntoInteresRepository puntoInteresRepository;

    @Autowired
    private ZonaUrbanaRepository zonaUrbanaRepository;

    /**
     * Obtiene estadísticas generales del sistema
     */
    public EstadisticasDTO obtenerEstadisticasGenerales() {
        EstadisticasDTO estadisticas = new EstadisticasDTO();

        // Total de proyectos
        String sqlProyectos = "SELECT COUNT(*) FROM proyectos_urbanos";
        Long totalProyectos = jdbcTemplate.queryForObject(sqlProyectos, Long.class);
        estadisticas.setTotalProyectos(totalProyectos != null ? totalProyectos : 0L);

        // Total de puntos de interés
        String sqlPuntos = "SELECT COUNT(*) FROM puntos_interes";
        Long totalPuntos = jdbcTemplate.queryForObject(sqlPuntos, Long.class);
        estadisticas.setTotalPuntos(totalPuntos != null ? totalPuntos : 0L);

        // Total de zonas
        String sqlZonas = "SELECT COUNT(*) FROM zonas_urbanas";
        Long totalZonas = jdbcTemplate.queryForObject(sqlZonas, Long.class);
        estadisticas.setTotalZonas(totalZonas != null ? totalZonas : 0L);

        // Proyectos por estado
        String sqlActivos = "SELECT COUNT(*) FROM proyectos_urbanos WHERE estado = 'Activo'";
        Long proyectosActivos = jdbcTemplate.queryForObject(sqlActivos, Long.class);
        estadisticas.setProyectosActivos(proyectosActivos != null ? proyectosActivos : 0L);

        String sqlEnProceso = "SELECT COUNT(*) FROM proyectos_urbanos WHERE estado = 'En Proceso'";
        Long proyectosEnProceso = jdbcTemplate.queryForObject(sqlEnProceso, Long.class);
        estadisticas.setProyectosEnProceso(proyectosEnProceso != null ? proyectosEnProceso : 0L);

        String sqlCompletados = "SELECT COUNT(*) FROM proyectos_urbanos WHERE estado = 'Completado'";
        Long proyectosCompletados = jdbcTemplate.queryForObject(sqlCompletados, Long.class);
        estadisticas.setProyectosCompletados(proyectosCompletados != null ? proyectosCompletados : 0L);

        String sqlCancelados = "SELECT COUNT(*) FROM proyectos_urbanos WHERE estado = 'Cancelado'";
        Long proyectosCancelados = jdbcTemplate.queryForObject(sqlCancelados, Long.class);
        estadisticas.setProyectosCancelados(proyectosCancelados != null ? proyectosCancelados : 0L);

        return estadisticas;
    }


    public List<ReporteDTO> generarReporteProyectos(LocalDate fechaInicio, LocalDate fechaFin) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT proyecto_urbano_id, nombre, tipo_proyecto as tipo, ")
           .append("fecha_inicio as fecha, estado, descripcion ")
           .append("FROM proyectos_urbanos WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (fechaInicio != null) {
            sql.append("AND fecha_inicio >= ? ");
            params.add(fechaInicio);
        }

        if (fechaFin != null) {
            sql.append("AND fecha_inicio <= ? ");
            params.add(fechaFin);
        }

        sql.append("ORDER BY fecha_inicio DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            ReporteDTO reporte = new ReporteDTO();
            reporte.setId(rs.getLong("proyecto_urbano_id"));
            reporte.setNombre(rs.getString("nombre"));
            reporte.setTipo(rs.getString("tipo"));
            reporte.setFecha(rs.getDate("fecha").toLocalDate());
            reporte.setEstado(rs.getString("estado"));
            reporte.setDescripcion(rs.getString("descripcion"));
            return reporte;
        });
    }

    /**
     * Genera reporte de puntos de interés con filtros
     */
    public List<ReporteDTO> generarReportePuntosInteres(LocalDate fechaInicio, LocalDate fechaFin) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT punto_interes_id, nombre, categoria as tipo, ")
           .append("fecha_creacion as fecha, 'Activo' as estado, descripcion, ")
           .append("ST_Y(ubicacion::geometry) as latitud, ST_X(ubicacion::geometry) as longitud ")
           .append("FROM puntos_interes WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (fechaInicio != null) {
            sql.append("AND fecha_creacion >= ? ");
            params.add(fechaInicio);
        }

        if (fechaFin != null) {
            sql.append("AND fecha_creacion <= ? ");
            params.add(fechaFin);
        }

        sql.append("ORDER BY fecha_creacion DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            ReporteDTO reporte = new ReporteDTO();
            reporte.setId(rs.getLong("punto_interes_id"));
            reporte.setNombre(rs.getString("nombre"));
            reporte.setTipo(rs.getString("tipo"));
            reporte.setFecha(rs.getDate("fecha").toLocalDate());
            reporte.setEstado(rs.getString("estado"));
            reporte.setDescripcion(rs.getString("descripcion"));
            reporte.setLatitud(rs.getDouble("latitud"));
            reporte.setLongitud(rs.getDouble("longitud"));
            return reporte;
        });
    }


    public List<ReporteDTO> generarReporteZonas(LocalDate fechaInicio, LocalDate fechaFin) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT zona_urbana_id, nombre, tipo_zona as tipo, ")
           .append("fecha_creacion as fecha, 'Activo' as estado, descripcion ")
           .append("FROM zonas_urbanas WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        if (fechaInicio != null) {
            sql.append("AND fecha_creacion >= ? ");
            params.add(fechaInicio);
        }

        if (fechaFin != null) {
            sql.append("AND fecha_creacion <= ? ");
            params.add(fechaFin);
        }

        sql.append("ORDER BY fecha_creacion DESC");

        return jdbcTemplate.query(sql.toString(), params.toArray(), (rs, rowNum) -> {
            ReporteDTO reporte = new ReporteDTO();
            reporte.setId(rs.getLong("zona_urbana_id"));
            reporte.setNombre(rs.getString("nombre"));
            reporte.setTipo(rs.getString("tipo"));
            reporte.setFecha(rs.getDate("fecha").toLocalDate());
            reporte.setEstado(rs.getString("estado"));
            reporte.setDescripcion(rs.getString("descripcion"));
            return reporte;
        });
    }


    public Map<String, Object> obtenerEstadisticasCrecimiento() {
        Map<String, Object> estadisticas = new HashMap<>();

        // Proyectos por mes (últimos 12 meses)
        String sqlProyectosPorMes =
            "SELECT TO_CHAR(fecha_inicio, 'YYYY-MM') as mes, COUNT(*) as cantidad " +
            "FROM proyectos_urbanos " +
            "WHERE fecha_inicio >= CURRENT_DATE - INTERVAL '12 months' " +
            "GROUP BY TO_CHAR(fecha_inicio, 'YYYY-MM') " +
            "ORDER BY mes";

        List<Map<String, Object>> proyectosPorMes = jdbcTemplate.queryForList(sqlProyectosPorMes);
        estadisticas.put("proyectosPorMes", proyectosPorMes);

        // Inversión total por tipo de proyecto
        String sqlInversionPorTipo =
            "SELECT tipo_proyecto, SUM(presupuesto) as total_inversion, COUNT(*) as cantidad " +
            "FROM proyectos_urbanos " +
            "GROUP BY tipo_proyecto " +
            "ORDER BY total_inversion DESC";

        List<Map<String, Object>> inversionPorTipo = jdbcTemplate.queryForList(sqlInversionPorTipo);
        estadisticas.put("inversionPorTipo", inversionPorTipo);

        // Distribución de proyectos por estado
        String sqlDistribucionEstado =
            "SELECT estado, COUNT(*) as cantidad " +
            "FROM proyectos_urbanos " +
            "GROUP BY estado " +
            "ORDER BY cantidad DESC";

        List<Map<String, Object>> distribucionEstado = jdbcTemplate.queryForList(sqlDistribucionEstado);
        estadisticas.put("distribucionPorEstado", distribucionEstado);

        // Puntos de interés por categoría
        String sqlPuntosPorCategoria =
            "SELECT categoria, COUNT(*) as cantidad " +
            "FROM puntos_interes " +
            "GROUP BY categoria " +
            "ORDER BY cantidad DESC";

        List<Map<String, Object>> puntosPorCategoria = jdbcTemplate.queryForList(sqlPuntosPorCategoria);
        estadisticas.put("puntosPorCategoria", puntosPorCategoria);

        return estadisticas;
    }

    
    public List<ReporteDTO> generarReporteConsolidado(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin) {
        switch (tipoReporte.toLowerCase()) {
            case "proyectos":
                return generarReporteProyectos(fechaInicio, fechaFin);
            case "puntos":
                return generarReportePuntosInteres(fechaInicio, fechaFin);
            case "zonas":
                return generarReporteZonas(fechaInicio, fechaFin);
            default:
                // Si no se especifica tipo, retornar todos los proyectos
                return generarReporteProyectos(fechaInicio, fechaFin);
        }
    }

    // Generar PDF en el backend usando PDFBox
    public byte[] generarPdfDesdeReporte(List<ReporteDTO> datos, String titulo) {
        try (org.apache.pdfbox.pdmodel.PDDocument doc = new org.apache.pdfbox.pdmodel.PDDocument()) {
            org.apache.pdfbox.pdmodel.PDPage page = new org.apache.pdfbox.pdmodel.PDPage();
            doc.addPage(page);

            final float margin = 50f;
            final float yStart = page.getMediaBox().getHeight() - margin;
            final float tableWidth = page.getMediaBox().getWidth() - 2 * margin;
            final float rowHeight = 18f;
            final float headerHeight = 22f;

            org.apache.pdfbox.pdmodel.font.PDFont fontBold = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
            org.apache.pdfbox.pdmodel.font.PDFont font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;

            // Column widths (sum should be <= tableWidth)
            float colId = 40f;
            float colNombre = 200f;
            float colTipo = 100f;
            float colFecha = 70f;
            float colEstado = tableWidth - (colId + colNombre + colTipo + colFecha);

            float y = yStart;
            int pageNumber = 1;

            org.apache.pdfbox.pdmodel.PDPageContentStream content = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);

            // Title
            content.beginText();
            content.setFont(fontBold, 18);
            content.newLineAtOffset(margin, y);
            content.showText(titulo != null ? titulo : "Reporte");
            content.endText();

            // Generation date
            content.beginText();
            content.setFont(font, 10);
            content.newLineAtOffset(margin, y - 20);
            content.showText("Fecha de generación: " + java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").format(java.time.LocalDateTime.now()));
            content.endText();

            y -= 40f;

            // Draw table header background
            content.setNonStrokingColor(230, 230, 230);
            content.addRect(margin, y - headerHeight + 4, tableWidth, headerHeight);
            content.fill();
            content.setNonStrokingColor(0, 0, 0);

            // Header text
            float textY = y - 14f;
            float x = margin + 4f;
            content.beginText();
            content.setFont(fontBold, 11);
            content.newLineAtOffset(x, textY);
            content.showText("ID");
            content.newLineAtOffset(colId, 0);
            content.showText("Nombre");
            content.newLineAtOffset(colNombre, 0);
            content.showText("Tipo");
            content.newLineAtOffset(colTipo, 0);
            content.showText("Fecha");
            content.newLineAtOffset(colFecha, 0);
            content.showText("Estado");
            content.endText();

            y -= headerHeight;

            boolean alternate = false;

            for (int i = 0; i < datos.size(); i++) {
                ReporteDTO r = datos.get(i);

                // Start new page if needed
                if (y < margin + 60) {
                    // Footer with page number
                    content.beginText();
                    content.setFont(font, 9);
                    content.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 20, margin / 2);
                    content.showText("Página " + pageNumber);
                    content.endText();
                    content.close();

                    page = new org.apache.pdfbox.pdmodel.PDPage();
                    doc.addPage(page);
                    content = new org.apache.pdfbox.pdmodel.PDPageContentStream(doc, page);
                    y = yStart - 20f;
                    pageNumber++;
                }

                // Row background
                if (alternate) {
                    content.setNonStrokingColor(245, 245, 245);
                    content.addRect(margin, y - rowHeight + 4, tableWidth, rowHeight);
                    content.fill();
                    content.setNonStrokingColor(0, 0, 0);
                }

                // Row text
                float rowTextY = y - 14f;
                float cx = margin + 4f;
                content.beginText();
                content.setFont(font, 10);
                content.newLineAtOffset(cx, rowTextY);
                content.showText(r.getId() != null ? String.valueOf(r.getId()) : "");
                content.endText();

                // Nombre (wrap/truncate if needed)
                String nombre = r.getNombre() != null ? r.getNombre() : "";
                String tipo = r.getTipo() != null ? r.getTipo() : "";
                String fecha = r.getFecha() != null ? r.getFecha().toString() : "";
                String estado = r.getEstado() != null ? r.getEstado() : "";

                // Helper to draw text within a column width
                drawTextInColumn(content, font, 10, nombre, margin + colId + 6f, rowTextY, colNombre - 8f);
                drawTextInColumn(content, font, 10, tipo, margin + colId + colNombre + 6f, rowTextY, colTipo - 8f);
                drawTextInColumn(content, font, 10, fecha, margin + colId + colNombre + colTipo + 6f, rowTextY, colFecha - 8f);
                drawTextInColumn(content, font, 10, estado, margin + colId + colNombre + colTipo + colFecha + 6f, rowTextY, colEstado - 8f);

                y -= rowHeight;
                alternate = !alternate;
            }

            // Footer page number
            content.beginText();
            content.setFont(font, 9);
            content.newLineAtOffset(page.getMediaBox().getWidth() / 2 - 20, margin / 2);
            content.showText("Página " + pageNumber);
            content.endText();

            content.close();

            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                doc.save(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Dibuja texto dentro de una columna con ancho máximo, haciendo wrap en espacios.
     */
    private void drawTextInColumn(org.apache.pdfbox.pdmodel.PDPageContentStream content,
                                  org.apache.pdfbox.pdmodel.font.PDFont font,
                                  float fontSize,
                                  String text,
                                  float x,
                                  float y,
                                  float maxWidth) {
        try {
            if (text == null) text = "";
            java.util.List<String> lines = new java.util.ArrayList<>();
            String[] words = text.split(" ");
            StringBuilder current = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                String w = words[i];
                String candidate = current.length() == 0 ? w : current + " " + w;
                float width = font.getStringWidth(candidate) / 1000f * fontSize;
                if (width <= maxWidth) {
                    current.setLength(0);
                    current.append(candidate);
                } else {
                    if (current.length() == 0) {
                        // single long word -> truncate
                        String truncated = truncateToWidth(font, fontSize, w, maxWidth);
                        lines.add(truncated);
                    } else {
                        lines.add(current.toString());
                        current.setLength(0);
                        current.append(w);
                    }
                }
            }
            if (current.length() > 0) lines.add(current.toString());

            float leading = fontSize + 2f;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                content.beginText();
                content.setFont(font, fontSize);
                content.newLineAtOffset(x, y - (i * leading));
                content.showText(line);
                content.endText();
            }
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error escribiendo texto en PDF: " + e.getMessage(), e);
        }
    }

    private String truncateToWidth(org.apache.pdfbox.pdmodel.font.PDFont font, float fontSize, String text, float maxWidth) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        try {
            for (int i = 0; i < text.length(); i++) {
                sb.append(text.charAt(i));
                float w = font.getStringWidth(sb.toString() + "...") / 1000f * fontSize;
                if (w > maxWidth) {
                    if (sb.length() > 3) {
                        return sb.substring(0, Math.max(0, sb.length() - 3)) + "...";
                    } else {
                        return "...";
                    }
                }
            }
            return sb.toString();
        } catch (java.io.IOException e) {
            // Fallback: approximate by character count
            int approxChars = Math.max(1, (int) (maxWidth / (fontSize * 0.6f)));
            if (text.length() > approxChars) {
                if (approxChars > 3) {
                    return text.substring(0, approxChars - 3) + "...";
                } else {
                    return text.substring(0, approxChars);
                }
            }
            return text;
        }
    }

    private String abbreviate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max-3) + "...";
    }

    // Generar Excel básico usando Apache POI (opcional) - por ahora devolver Excel en blanco simple
    public byte[] generarExcelDesdeReporte(List<ReporteDTO> datos) {
        try (org.apache.poi.ss.usermodel.Workbook wb = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("Reporte");
            int rownum = 0;
            org.apache.poi.ss.usermodel.Row header = sheet.createRow(rownum++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Nombre");
            header.createCell(2).setCellValue("Tipo");
            header.createCell(3).setCellValue("Fecha");
            header.createCell(4).setCellValue("Estado");

            for (ReporteDTO r : datos) {
                org.apache.poi.ss.usermodel.Row row = sheet.createRow(rownum++);
                row.createCell(0).setCellValue(r.getId() != null ? r.getId() : 0);
                row.createCell(1).setCellValue(r.getNombre() != null ? r.getNombre() : "");
                row.createCell(2).setCellValue(r.getTipo() != null ? r.getTipo() : "");
                row.createCell(3).setCellValue(r.getFecha() != null ? r.getFecha().toString() : "");
                row.createCell(4).setCellValue(r.getEstado() != null ? r.getEstado() : "");
            }

            try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
                wb.write(baos);
                return baos.toByteArray();
            }
        } catch (Exception e) {
            throw new RuntimeException("Error generando Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Identifica las 5 zonas urbanas con mayor población pero menor cantidad de hospitales
     */
    public List<Map<String, Object>> obtenerZonasEscasezHospitales(Long año) {
        String sql = """
            SELECT
                z.nombre AS zona,
                dd.poblacion,
                COUNT(pi.punto_interes_id) AS cantidad_hospitales
            FROM 
                zonas_urbanas z
                INNER JOIN datos_demograficos dd ON z.zona_urbana_id = dd.zona_urbana_id
                LEFT JOIN puntos_interes pi ON ST_Contains(z.geometria_poligono, pi.coordenadas_punto)
                    AND pi.tipo = 'Hospital' 
                    AND pi.activo = TRUE
            WHERE 
                dd."año" = ?
                AND dd.poblacion > 1000
            GROUP BY 
                z.zona_urbana_id, z.nombre, dd.poblacion
            ORDER BY 
                dd.poblacion DESC, cantidad_hospitales ASC
            LIMIT 5
        """;
        
        return jdbcTemplate.queryForList(sql, año);
    }
}


