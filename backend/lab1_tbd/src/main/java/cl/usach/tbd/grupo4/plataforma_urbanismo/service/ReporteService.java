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
}

