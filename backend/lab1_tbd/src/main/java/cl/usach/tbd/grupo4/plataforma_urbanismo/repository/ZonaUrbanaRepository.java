package cl.usach.tbd.grupo4.plataforma_urbanismo.repository;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ZonaUrbana;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ZonaUrbanaRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ZonaUrbana> zonaUrbanaRowMapper = new RowMapper<ZonaUrbana>() {
        @Override
        public ZonaUrbana mapRow(ResultSet rs, int rowNum) throws SQLException {
            ZonaUrbana zona = new ZonaUrbana();
            zona.setZonaUrbanaId(rs.getLong("zona_urbana_id"));
            zona.setNombre(rs.getString("nombre"));
            // Convertir geometría a GeoJSON
            zona.setGeometriaPoligono(rs.getString("geometria_geojson"));
            zona.setTipoZona(rs.getString("tipo_zona"));
            zona.setAreaKm2(rs.getBigDecimal("area_km2"));
            zona.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());

            // Agregar datos demográficos si existen
            try {
                long poblacion = rs.getLong("poblacion");
                if (!rs.wasNull()) {
                    zona.setPoblacion(poblacion);

                    // Calcular densidad
                    if (zona.getAreaKm2() != null && zona.getAreaKm2().doubleValue() > 0) {
                        double densidad = poblacion / zona.getAreaKm2().doubleValue();
                        zona.setDensidadPoblacion(densidad);
                    }
                }
            } catch (SQLException e) {
                // Si no hay datos demográficos, continuar sin ellos
            }

            return zona;
        }
    };

    public List<ZonaUrbana> findAll() {
        String sql = "SELECT zu.zona_urbana_id, zu.nombre, " +
                "ST_AsGeoJSON(zu.geometria_poligono) as geometria_geojson, " +
                "zu.tipo_zona, zu.area_km2, zu.fecha_creacion, " +
                "dd.poblacion " +
                "FROM zonas_urbanas zu " +
                "LEFT JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id AND dd.anio = 2025 " +
                "ORDER BY zu.nombre";
        return jdbcTemplate.query(sql, zonaUrbanaRowMapper);
    }

    public Optional<ZonaUrbana> findById(Long id) {
        String sql = "SELECT zona_urbana_id, nombre, " +
                "ST_AsGeoJSON(geometria_poligono) as geometria_geojson, " +
                "tipo_zona, area_km2, fecha_creacion " +
                "FROM zonas_urbanas WHERE zona_urbana_id = ?";
        try {
            ZonaUrbana zona = jdbcTemplate.queryForObject(sql, zonaUrbanaRowMapper, id);
            return Optional.ofNullable(zona);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Map<String, Object>> getDensidadPoblacion() {
        String sql = """
            SELECT 
                zu.nombre AS zona,
                dd.poblacion,
                zu.area_km2,
                ROUND(dd.poblacion::NUMERIC / NULLIF(zu.area_km2, 0), 2) AS densidad_poblacion_km2
            FROM zonas_urbanas zu
            INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
            WHERE dd.anio = EXTRACT(YEAR FROM CURRENT_DATE)
              AND zu.area_km2 > 0
            ORDER BY densidad_poblacion_km2 DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getZonasEscasezHospitales() {
        String sql = """
            SELECT 
                zu.nombre AS zona,
                dd.poblacion,
                COALESCE(ci.total_hospitales, 0) AS cantidad_hospitales
            FROM zonas_urbanas zu
            INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
            LEFT JOIN cobertura_infraestructura ci ON zu.zona_urbana_id = ci.zona_urbana_id
            WHERE dd.anio = EXTRACT(YEAR FROM CURRENT_DATE)
            ORDER BY dd.poblacion DESC, cantidad_hospitales ASC
            LIMIT 5
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getZonasRapidoCrecimiento() {
        String sql = """
            WITH poblacion_comparativa AS (
                SELECT 
                    zona_urbana_id,
                    MAX(CASE WHEN anio = EXTRACT(YEAR FROM CURRENT_DATE) THEN poblacion END) AS poblacion_actual,
                    MAX(CASE WHEN anio = EXTRACT(YEAR FROM CURRENT_DATE) - 5 THEN poblacion END) AS poblacion_hace_5_anios
                FROM datos_demograficos
                WHERE anio IN (EXTRACT(YEAR FROM CURRENT_DATE), EXTRACT(YEAR FROM CURRENT_DATE) - 5)
                GROUP BY zona_urbana_id
            )
            SELECT 
                zu.nombre AS zona,
                pc.poblacion_hace_5_anios,
                pc.poblacion_actual,
                ROUND(
                    ((pc.poblacion_actual - pc.poblacion_hace_5_anios)::NUMERIC / 
                    NULLIF(pc.poblacion_hace_5_anios, 0) * 100), 2
                ) AS porcentaje_crecimiento
            FROM poblacion_comparativa pc
            INNER JOIN zonas_urbanas zu ON pc.zona_urbana_id = zu.zona_urbana_id
            WHERE pc.poblacion_hace_5_anios IS NOT NULL 
              AND pc.poblacion_actual IS NOT NULL
              AND pc.poblacion_hace_5_anios > 0
              AND ((pc.poblacion_actual - pc.poblacion_hace_5_anios)::NUMERIC / pc.poblacion_hace_5_anios) > 0.10
            ORDER BY porcentaje_crecimiento DESC
            LIMIT 3
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getZonasSinPlanificacionReciente() {
        String sql = """
            SELECT 
                zu.nombre AS zona,
                COALESCE(TO_CHAR(MAX(pu.fecha_inicio), 'DD-MM-YYYY'), 'Ninguno') AS fecha_ultimo_proyecto
            FROM zonas_urbanas zu
            LEFT JOIN proyectos_zonas pz ON zu.zona_urbana_id = pz.zona_urbana_id
            LEFT JOIN proyectos_urbanos pu ON pz.proyecto_urbano_id = pu.proyecto_urbano_id
                AND pu.fecha_inicio >= CURRENT_DATE - INTERVAL '2 years'
            GROUP BY zu.zona_urbana_id, zu.nombre
            HAVING MAX(pu.fecha_inicio) IS NULL
               OR MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years'
            ORDER BY zu.nombre
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public ZonaUrbana save(ZonaUrbana zona) {
        String sql = "INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) " +
                "VALUES (?, ST_GeomFromGeoJSON(?), ?, ?) RETURNING zona_urbana_id";

        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                zona.getNombre(),
                zona.getGeometriaPoligono(),
                zona.getTipoZona(),
                zona.getAreaKm2());

        zona.setZonaUrbanaId(id);
        return zona;
    }

    public int update(ZonaUrbana zona) {
        String sql = "UPDATE zonas_urbanas SET nombre = ?, tipo_zona = ?, area_km2 = ? " +
                "WHERE zona_urbana_id = ?";

        return jdbcTemplate.update(sql,
                zona.getNombre(),
                zona.getTipoZona(),
                zona.getAreaKm2(),
                zona.getZonaUrbanaId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM zonas_urbanas WHERE zona_urbana_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Análisis Espacial: Obtener estadísticas dentro de un área delimitada por coordenadas
    public Map<String, Object> getEstadisticasArea(String geojsonArea) {
        // Obtener población total en el área
        String sqlPoblacion = """
            SELECT COALESCE(SUM(dd.poblacion), 0) as poblacion_total
            FROM zonas_urbanas zu
            INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
            WHERE dd.anio = EXTRACT(YEAR FROM CURRENT_DATE)
              AND ST_Intersects(zu.geometria_poligono, ST_GeomFromGeoJSON(?))
        """;

        // Obtener cantidad de escuelas en el área
        String sqlEscuelas = """
            SELECT COUNT(*) as total_escuelas
            FROM puntos_interes
            WHERE tipo = 'Escuela'
              AND ST_Within(coordenadas_punto, ST_GeomFromGeoJSON(?))
        """;

        // Obtener cantidad de hospitales en el área
        String sqlHospitales = """
            SELECT COUNT(*) as total_hospitales
            FROM puntos_interes
            WHERE tipo = 'Hospital'
              AND ST_Within(coordenadas_punto, ST_GeomFromGeoJSON(?))
        """;

        // Obtener proyectos en curso en el área
        String sqlProyectos = """
            SELECT COUNT(*) as total_proyectos
            FROM proyectos_urbanos
            WHERE estado IN ('En Curso', 'Planeado')
              AND ST_Intersects(geometria, ST_GeomFromGeoJSON(?))
        """;

        // Obtener zonas del área por tipo
        String sqlZonasPorTipo = """
            SELECT tipo_zona, COUNT(*) as cantidad
            FROM zonas_urbanas
            WHERE ST_Intersects(geometria_poligono, ST_GeomFromGeoJSON(?))
            GROUP BY tipo_zona
        """;

        // Obtener área total en km2
        String sqlAreaTotal = """
            SELECT ROUND(
                ST_Area(
                    ST_Transform(ST_GeomFromGeoJSON(?), 32719)
                )::NUMERIC / 1000000, 2
            ) as area_km2
        """;

        try {
            Long poblacion = jdbcTemplate.queryForObject(sqlPoblacion, Long.class, geojsonArea);
            Integer escuelas = jdbcTemplate.queryForObject(sqlEscuelas, Integer.class, geojsonArea);
            Integer hospitales = jdbcTemplate.queryForObject(sqlHospitales, Integer.class, geojsonArea);
            Integer proyectos = jdbcTemplate.queryForObject(sqlProyectos, Integer.class, geojsonArea);
            List<Map<String, Object>> zonasPorTipo = jdbcTemplate.queryForList(sqlZonasPorTipo, geojsonArea);
            Double areaKm2 = jdbcTemplate.queryForObject(sqlAreaTotal, Double.class, geojsonArea);

            // Construir respuesta
            return Map.of(
                "poblacion_total", poblacion != null ? poblacion : 0,
                "total_escuelas", escuelas != null ? escuelas : 0,
                "total_hospitales", hospitales != null ? hospitales : 0,
                "proyectos_en_curso", proyectos != null ? proyectos : 0,
                "zonas_por_tipo", zonasPorTipo,
                "area_km2", areaKm2 != null ? areaKm2 : 0.0
            );
        } catch (Exception e) {
            throw new RuntimeException("Error al calcular estadísticas del área: " + e.getMessage());
        }
    }

    // Simulación de Proyectos: Calcular impacto potencial de un nuevo proyecto
    public Map<String, Object> calcularImpactoProyecto(
            String geojsonArea,
            String tipoProyecto,
            Integer poblacionEstimada,
            Integer numEscuelas,
            Integer numHospitales,
            Double impactoPoblacion,
            Integer areaInfluencia) {

        try {
            // 1. Obtener estadísticas actuales del área del proyecto
            Map<String, Object> estadisticasActuales = getEstadisticasArea(geojsonArea);

            // 2. Calcular el centro del proyecto
            String sqlCentro = """
                SELECT ST_AsText(ST_Centroid(ST_GeomFromGeoJSON(?))) as centro
            """;
            String centroWKT = jdbcTemplate.queryForObject(sqlCentro, String.class, geojsonArea);

            // 3. Calcular estadísticas del área de influencia (buffer alrededor del proyecto)
            String geojsonBuffer = String.format(
                "{\"type\":\"Point\",\"coordinates\":[%s]}",
                centroWKT.replace("POINT(", "").replace(")", "").replace(" ", ",")
            );

            // Población en el área de influencia
            String sqlPoblacionInfluencia = """
                SELECT COALESCE(SUM(dd.poblacion), 0) as poblacion
                FROM zonas_urbanas zu
                INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
                WHERE dd.anio = EXTRACT(YEAR FROM CURRENT_DATE)
                  AND ST_DWithin(
                      zu.geometria_poligono::geography,
                      ST_GeomFromGeoJSON(?)::geography,
                      ?
                  )
            """;

            Long poblacionInfluencia = jdbcTemplate.queryForObject(
                sqlPoblacionInfluencia,
                Long.class,
                geojsonBuffer,
                areaInfluencia
            );

            // Escuelas en el área de influencia
            String sqlEscuelasInfluencia = """
                SELECT COUNT(*) as total
                FROM puntos_interes
                WHERE tipo = 'Escuela'
                  AND ST_DWithin(
                      coordenadas_punto::geography,
                      ST_GeomFromGeoJSON(?)::geography,
                      ?
                  )
            """;

            Integer escuelasInfluencia = jdbcTemplate.queryForObject(
                sqlEscuelasInfluencia,
                Integer.class,
                geojsonBuffer,
                areaInfluencia
            );

            // Hospitales en el área de influencia
            String sqlHospitalesInfluencia = """
                SELECT COUNT(*) as total
                FROM puntos_interes
                WHERE tipo = 'Hospital'
                  AND ST_DWithin(
                      coordenadas_punto::geography,
                      ST_GeomFromGeoJSON(?)::geography,
                      ?
                  )
            """;

            Integer hospitalesInfluencia = jdbcTemplate.queryForObject(
                sqlHospitalesInfluencia,
                Integer.class,
                geojsonBuffer,
                areaInfluencia
            );

            // Proyectos en el área de influencia
            String sqlProyectosInfluencia = """
                SELECT COUNT(*) as total
                FROM proyectos_urbanos
                WHERE estado IN ('En Curso', 'Planeado')
                  AND ST_DWithin(
                      geometria::geography,
                      ST_GeomFromGeoJSON(?)::geography,
                      ?
                  )
            """;

            Integer proyectosInfluencia = jdbcTemplate.queryForObject(
                sqlProyectosInfluencia,
                Integer.class,
                geojsonBuffer,
                areaInfluencia
            );

            // 4. Calcular valores proyectados con el nuevo proyecto
            long poblacionActual = (Long) estadisticasActuales.get("poblacion_total");
            int escuelasActuales = (Integer) estadisticasActuales.get("total_escuelas");
            int hospitalesActuales = (Integer) estadisticasActuales.get("total_hospitales");
            int proyectosActuales = (Integer) estadisticasActuales.get("proyectos_en_curso");

            // Calcular impacto en población
            long incrementoPoblacion = poblacionEstimada;
            if (impactoPoblacion != 0 && poblacionInfluencia != null) {
                incrementoPoblacion += (long) (poblacionInfluencia * (impactoPoblacion / 100.0));
            }

            long poblacionProyectada = poblacionActual + incrementoPoblacion;
            int escuelasProyectadas = escuelasActuales + numEscuelas;
            int hospitalesProyectados = hospitalesActuales + numHospitales;
            int proyectosProyectados = proyectosActuales + 1; // El nuevo proyecto

            // 5. Construir respuesta con comparación
            return Map.of(
                "actual", Map.of(
                    "poblacion", poblacionActual,
                    "escuelas", escuelasActuales,
                    "hospitales", hospitalesActuales,
                    "proyectos", proyectosActuales
                ),
                "proyectado", Map.of(
                    "poblacion", poblacionProyectada,
                    "escuelas", escuelasProyectadas,
                    "hospitales", hospitalesProyectados,
                    "proyectos", proyectosProyectados
                ),
                "diferencias", Map.of(
                    "poblacion", incrementoPoblacion,
                    "escuelas", numEscuelas,
                    "hospitales", numHospitales,
                    "proyectos", 1
                ),
                "area_km2", estadisticasActuales.get("area_km2"),
                "area_influencia_m", areaInfluencia,
                "tipo_proyecto", tipoProyecto,
                "poblacion_area_influencia", poblacionInfluencia != null ? poblacionInfluencia : 0
            );

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error al calcular impacto del proyecto: " + e.getMessage());
        }
    }
}