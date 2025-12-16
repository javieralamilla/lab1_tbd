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

            return zona;
        }
    };

    public List<ZonaUrbana> findAll() {
        String sql = "SELECT zu.zona_urbana_id, zu.nombre, " +
                "ST_AsGeoJSON(zu.geometria_poligono) as geometria_geojson, " +
                "zu.tipo_zona, zu.area_km2, zu.fecha_creacion " +
                "FROM zonas_urbanas zu " +
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
            WHERE zu.area_km2 > 0
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
            ORDER BY dd.poblacion DESC, cantidad_hospitales ASC
            LIMIT 5
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getZonasRapidoCrecimiento() {
        String sql = """
            SELECT 
                z.nombre AS zona,
                d1.poblacion AS poblacion_actual,
                d2.poblacion AS poblacion_anterior,
                ROUND(((d1.poblacion - d2.poblacion)::NUMERIC / d2.poblacion) * 100, 2) AS porcentaje_crecimiento
            FROM zonas_urbanas z
            JOIN datos_demograficos d1 ON z.zona_urbana_id = d1.zona_urbana_id
            JOIN datos_demograficos d2 ON z.zona_urbana_id = d2.zona_urbana_id
            WHERE d1.año = (SELECT MAX(año) FROM datos_demograficos)
              AND d2.año = d1.año - 5
              AND d2.poblacion > 0
              AND ((d1.poblacion - d2.poblacion)::NUMERIC / d2.poblacion) > 0.10
            ORDER BY porcentaje_crecimiento DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getZonasSinPlanificacionReciente() {
        String sql = """
            SELECT
                zu.zona_urbana_id,
                zu.nombre AS zona,
                zu.tipo_zona,
                COALESCE(
                    TO_CHAR(MAX(pu.fecha_inicio), 'DD/MM/YYYY'),
                    'Ninguno'
                ) AS ultimo_proyecto,
                MAX(pu.fecha_inicio) AS fecha_ultimo_proyecto,
                CASE
                    WHEN MAX(pu.fecha_inicio) IS NULL THEN 'Sin proyectos'
                    WHEN MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years' THEN 'Sin planificación reciente'
                    ELSE 'Con planificación reciente'
                END AS estado_planificacion,
                CASE
                    WHEN MAX(pu.fecha_inicio) IS NOT NULL
                    THEN EXTRACT(YEAR FROM AGE(CURRENT_DATE, MAX(pu.fecha_inicio)))::INTEGER
                    ELSE NULL
                END AS anios_sin_proyecto
            FROM zonas_urbanas zu
            LEFT JOIN proyectos_zonas pz ON zu.zona_urbana_id = pz.zona_urbana_id
            LEFT JOIN proyectos_urbanos pu ON pz.proyecto_urbano_id = pu.proyecto_urbano_id
            GROUP BY zu.zona_urbana_id, zu.nombre, zu.tipo_zona
            HAVING MAX(pu.fecha_inicio) IS NULL
               OR MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years'
            ORDER BY
                CASE WHEN MAX(pu.fecha_inicio) IS NULL THEN 1 ELSE 0 END,
                MAX(pu.fecha_inicio) ASC NULLS FIRST
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
        // Actualizar datos de la zona urbana
        String sqlZona = "UPDATE zonas_urbanas SET nombre = ?, tipo_zona = ?, area_km2 = ? " +
                "WHERE zona_urbana_id = ?";

        int zonasAfectadas = jdbcTemplate.update(sqlZona,
                zona.getNombre(),
                zona.getTipoZona(),
                zona.getAreaKm2(),
                zona.getZonaUrbanaId());

        return zonasAfectadas;
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM zonas_urbanas WHERE zona_urbana_id = ?";
        return jdbcTemplate.update(sql, id);
    }

    // Análisis Espacial: Obtener estadísticas dentro de un área delimitada por coordenadas
    public Map<String, Object> getEstadisticasArea(String geojsonArea) {
        // Obtener población total en el área (proporcional al área de intersección)
        String sqlPoblacion = """
            SELECT COALESCE(SUM(
                ROUND(
                    dd.poblacion * 
                    ST_Area(ST_Transform(ST_Intersection(zu.geometria_poligono, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326)), 32719)) / 
                    (ST_Area(ST_Transform(zu.geometria_poligono, 32719)) + 0.000001)
                )
            ), 0) as poblacion_total
            FROM zonas_urbanas zu
            INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
            WHERE ST_Intersects(zu.geometria_poligono, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326))
        """;

        // Obtener cantidad de escuelas en el área
        String sqlEscuelas = """
            SELECT COUNT(*) as total_escuelas
            FROM puntos_interes
            WHERE tipo = 'Escuela'
              AND ST_Within(coordenadas_punto, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326))
        """;

        // Obtener cantidad de hospitales en el área
        String sqlHospitales = """
            SELECT COUNT(*) as total_hospitales
            FROM puntos_interes
            WHERE tipo = 'Hospital'
              AND ST_Within(coordenadas_punto, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326))
        """;

        // Obtener proyectos en curso en el área
        String sqlProyectos = """
            SELECT COUNT(*) as total_proyectos
            FROM proyectos_urbanos
            WHERE estado IN ('En Curso', 'Planeado')
              AND ST_Intersects(geometria, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326))
        """;

        // Obtener zonas del área por tipo
        String sqlZonasPorTipo = """
            SELECT tipo_zona, COUNT(*) as cantidad
            FROM zonas_urbanas
            WHERE ST_Intersects(geometria_poligono, ST_SetSRID(ST_GeomFromGeoJSON(?), 4326))
            GROUP BY tipo_zona
        """;

        // Obtener área total en km2
        String sqlAreaTotal = """
            SELECT ROUND(
                ST_Area(
                    ST_Transform(ST_SetSRID(ST_GeomFromGeoJSON(?), 4326), 32719)
                )::NUMERIC / 1000000, 2
            ) as area_km2
        """;

        try {
            Long poblacion = jdbcTemplate.queryForObject(sqlPoblacion, Long.class, geojsonArea, geojsonArea);
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

            Map<String, Object> estadisticasActuales = getEstadisticasArea(geojsonArea);


            String sqlCentro = """
                SELECT ST_AsText(ST_Centroid(ST_GeomFromGeoJSON(?))) as centro
            """;
            String centroWKT = jdbcTemplate.queryForObject(sqlCentro, String.class, geojsonArea);


            String[] coords = centroWKT.replace("POINT(", "").replace(")", "").split(" ");
            String geojsonBuffer = String.format(
                    "{\"type\":\"Point\",\"coordinates\":[%s,%s]}",
                    coords[0], coords[1]
            );


            String sqlPoblacionInfluencia = """
                SELECT COALESCE(SUM(dd.poblacion), 0) as poblacion
                FROM zonas_urbanas zu
                INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
                WHERE ST_DWithin(
                      zu.geometria_poligono::geography,
                      ST_GeomFromGeoJSON(?)::geography,
                      ?
                  )
            """;
            Long poblacionInfluencia = jdbcTemplate.queryForObject(
                    sqlPoblacionInfluencia, Long.class, geojsonBuffer, areaInfluencia
            );

            String sqlEscuelasInfluencia = """
                SELECT COUNT(*) FROM puntos_interes
                WHERE tipo = 'Escuela'
                  AND ST_DWithin(coordenadas_punto::geography, ST_GeomFromGeoJSON(?)::geography, ?)
            """;
            Integer escuelasInfluencia = jdbcTemplate.queryForObject(
                    sqlEscuelasInfluencia, Integer.class, geojsonBuffer, areaInfluencia
            );


            String sqlHospitalesInfluencia = """
                SELECT COUNT(*) FROM puntos_interes
                WHERE tipo = 'Hospital'
                  AND ST_DWithin(coordenadas_punto::geography, ST_GeomFromGeoJSON(?)::geography, ?)
            """;
            Integer hospitalesInfluencia = jdbcTemplate.queryForObject(
                    sqlHospitalesInfluencia, Integer.class, geojsonBuffer, areaInfluencia
            );


            String sqlProyectosInfluencia = """
                SELECT COUNT(*) FROM proyectos_urbanos
                WHERE estado IN ('En Curso', 'Planeado')
                  AND ST_DWithin(geometria::geography, ST_GeomFromGeoJSON(?)::geography, ?)
            """;
            Integer proyectosInfluencia = jdbcTemplate.queryForObject(
                    sqlProyectosInfluencia, Integer.class, geojsonBuffer, areaInfluencia
            );


            String sqlAreaKm2 = """
                SELECT ROUND(
                    ST_Area(ST_Transform(ST_GeomFromGeoJSON(?), 32719))::NUMERIC / 1000000, 4
                ) as area_km2
            """;
            Double areaKm2 = jdbcTemplate.queryForObject(sqlAreaKm2, Double.class, geojsonArea);

            Long incrementoPoblacionReal = 0L;
            Integer viviendasAgregadas = 0;
            Long zonaAfectadaId = null;
            String zonaAfectadaNombre = null;

            if ("RESIDENCIAL".equalsIgnoreCase(tipoProyecto) && poblacionEstimada != null && poblacionEstimada > 0) {


                String sqlZonaDelCentro = """
                    SELECT zu.zona_urbana_id, zu.nombre
                    FROM zonas_urbanas zu
                    WHERE ST_Contains(zu.geometria_poligono, ST_GeomFromGeoJSON(?))
                    LIMIT 1
                """;

                try {
                    Map<String, Object> zonaEncontrada = jdbcTemplate.queryForMap(sqlZonaDelCentro, geojsonBuffer);
                    zonaAfectadaId = ((Number) zonaEncontrada.get("zona_urbana_id")).longValue();
                    zonaAfectadaNombre = (String) zonaEncontrada.get("nombre");


                    viviendasAgregadas = (int) Math.ceil(poblacionEstimada / 3.0);


                    String sqlPoblacionAntes = """
                        SELECT COALESCE(poblacion, 0) FROM datos_demograficos
                        WHERE zona_urbana_id = ?
                        ORDER BY año DESC LIMIT 1
                    """;
                    Long poblacionAntes = jdbcTemplate.queryForObject(sqlPoblacionAntes, Long.class, zonaAfectadaId);
                    if (poblacionAntes == null) poblacionAntes = 0L;


                    String sqlProcedimiento = "CALL simular_crecimiento_poblacion(?, ?)";
                    jdbcTemplate.update(sqlProcedimiento, zonaAfectadaId.intValue(), viviendasAgregadas);


                    Long poblacionDespues = jdbcTemplate.queryForObject(sqlPoblacionAntes, Long.class, zonaAfectadaId);
                    if (poblacionDespues == null) poblacionDespues = 0L;


                    incrementoPoblacionReal = poblacionDespues - poblacionAntes;

                    System.out.println("=== CONSULTA 6: SIMULACIÓN EJECUTADA ===");
                    System.out.println("Zona afectada: " + zonaAfectadaNombre + " (ID: " + zonaAfectadaId + ")");
                    System.out.println("Viviendas agregadas: " + viviendasAgregadas);
                    System.out.println("Incremento población: " + incrementoPoblacionReal);
                    System.out.println("=========================================");

                } catch (Exception e) {
                    System.err.println("No se encontró zona para el centro del proyecto: " + e.getMessage());
                    // No es error crítico, continuamos sin ejecutar el procedimiento
                }
            }


            Long poblacionActual = poblacionInfluencia != null ? poblacionInfluencia : 0L;
            Integer escuelasActuales = escuelasInfluencia != null ? escuelasInfluencia : 0;
            Integer hospitalesActuales = hospitalesInfluencia != null ? hospitalesInfluencia : 0;
            Integer proyectosActuales = proyectosInfluencia != null ? proyectosInfluencia : 0;

            // Calcular incremento de población
            Long incrementoPoblacion;
            if (incrementoPoblacionReal > 0) {
                // Usar el valor real del procedimiento
                incrementoPoblacion = incrementoPoblacionReal;
            } else if (impactoPoblacion != null && impactoPoblacion != 0) {
                // Usar porcentaje de impacto
                incrementoPoblacion = Math.round(poblacionActual * (impactoPoblacion / 100.0));
            } else if (poblacionEstimada != null && poblacionEstimada > 0) {
                // Usar población estimada directamente
                incrementoPoblacion = poblacionEstimada.longValue();
            } else {
                incrementoPoblacion = 0L;
            }

            Long poblacionProyectada = poblacionActual + incrementoPoblacion;
            Integer escuelasProyectadas = escuelasActuales + (numEscuelas != null ? numEscuelas : 0);
            Integer hospitalesProyectados = hospitalesActuales + (numHospitales != null ? numHospitales : 0);


            Map<String, Object> resultado = new java.util.HashMap<>();

            // Estado actual
            Map<String, Object> actual = new java.util.HashMap<>();
            actual.put("poblacion", poblacionActual);
            actual.put("escuelas", escuelasActuales);
            actual.put("hospitales", hospitalesActuales);
            actual.put("proyectos", proyectosActuales);
            resultado.put("actual", actual);

            // Estado proyectado
            Map<String, Object> proyectado = new java.util.HashMap<>();
            proyectado.put("poblacion", poblacionProyectada);
            proyectado.put("escuelas", escuelasProyectadas);
            proyectado.put("hospitales", hospitalesProyectados);
            proyectado.put("proyectos", proyectosActuales + 1);
            resultado.put("proyectado", proyectado);

            // Diferencias
            Map<String, Object> diferencias = new java.util.HashMap<>();
            diferencias.put("poblacion", incrementoPoblacion);
            diferencias.put("escuelas", numEscuelas != null ? numEscuelas : 0);
            diferencias.put("hospitales", numHospitales != null ? numHospitales : 0);
            resultado.put("diferencias", diferencias);

            // Metadatos
            resultado.put("area_km2", areaKm2 != null ? areaKm2 : 0.0);
            resultado.put("tipo_proyecto", tipoProyecto);
            resultado.put("area_influencia_m", areaInfluencia);

            // Info de la simulación del procedimiento (Consulta 6)
            if (zonaAfectadaId != null) {
                Map<String, Object> simulacionBD = new java.util.HashMap<>();
                simulacionBD.put("ejecutada", true);
                simulacionBD.put("zona_id", zonaAfectadaId);
                simulacionBD.put("zona_nombre", zonaAfectadaNombre);
                simulacionBD.put("viviendas_agregadas", viviendasAgregadas);
                simulacionBD.put("incremento_poblacion_real", incrementoPoblacionReal);
                resultado.put("simulacion_bd", simulacionBD);
            } else {
                Map<String, Object> simulacionBD = new java.util.HashMap<>();
                simulacionBD.put("ejecutada", false);
                simulacionBD.put("razon", "No es proyecto RESIDENCIAL o no se encontró zona");
                resultado.put("simulacion_bd", simulacionBD);
            }

            return resultado;

        } catch (Exception e) {
            throw new RuntimeException("Error al calcular impacto del proyecto: " + e.getMessage(), e);
        }
    }

    // 3. Análisis de Proximidad a Proyectos
    public List<Map<String, Object>> getEscuelasCercaProyectos() {
        String sql = """
            SELECT DISTINCT
                pi.nombre AS nombre_escuela,
                pu.nombre AS nombre_proyecto,
                ROUND(
                    ST_Distance(
                        pi.coordenadas_punto::geography,
                        ST_Centroid(pu.geometria)::geography
                    )::NUMERIC, 2
                ) AS distancia_metros
            FROM puntos_interes pi
            INNER JOIN proyectos_urbanos pu ON ST_DWithin(
                pi.coordenadas_punto::geography,
                ST_Centroid(pu.geometria)::geography,
                500
            )
            WHERE pi.tipo = 'Escuela'
                AND pu.estado = 'En Curso'
                AND pi.activo = TRUE
            ORDER BY distancia_metros
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // 9. Análisis de Superposición de Proyectos
    public List<Map<String, Object>> getProyectosSuperpuestos() {
        String sql = """
            SELECT 
                p1.nombre AS proyecto_1,
                p2.nombre AS proyecto_2,
                ROUND(
                    ST_Area(
                        ST_Transform(
                            ST_Intersection(p1.geometria, p2.geometria), 
                            32719
                        )
                    )::NUMERIC, 2
                ) AS area_superposicion_m2
            FROM proyectos_urbanos p1
            INNER JOIN proyectos_urbanos p2 ON p1.proyecto_urbano_id < p2.proyecto_urbano_id
            WHERE ST_Intersects(p1.geometria, p2.geometria)
              AND ST_GeometryType(ST_Intersection(p1.geometria, p2.geometria)) IN ('ST_Polygon', 'ST_MultiPolygon')
              AND ST_Area(ST_Transform(ST_Intersection(p1.geometria, p2.geometria), 32719)) > 1
            ORDER BY area_superposicion_m2 DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // Refrescar vista materializada de cobertura
    public void refrescarCoberturaInfraestructura() {
        try {
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY cobertura_infraestructura");
        } catch (Exception e) {
            // Si falla el refresco concurrente, intentar refresco normal
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW cobertura_infraestructura");
        }
    }

    // Refrescar vista materializada de resumen de proyectos
    public void refrescarResumenProyectos() {
        try {
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW CONCURRENTLY resumen_proyectos_estado_zona");
        } catch (Exception e) {
            // Si falla el refresco concurrente, intentar refresco normal
            jdbcTemplate.execute("REFRESH MATERIALIZED VIEW resumen_proyectos_estado_zona");
        }
    }

    // Obtener cobertura de infraestructura
    public List<Map<String, Object>> getCoberturaInfraestructura() {
        String sql = """
            SELECT
                zona_urbana_id,
                nombre_zona,
                total_parques,
                total_escuelas,
                total_hospitales,
                total_puntos_interes
            FROM cobertura_infraestructura
            ORDER BY nombre_zona
        """;
        return jdbcTemplate.queryForList(sql);
    }

    // Obtener resumen de proyectos por estado y zona
    public List<Map<String, Object>> getResumenProyectosEstadoZona() {
        String sql = """
            SELECT
                tipo_zona,
                estado_proyecto,
                SUM(cantidad_proyectos) AS cantidad_proyectos,
                SUM(presupuesto_total) AS presupuesto_total
            FROM resumen_proyectos_estado_zona
            WHERE estado_proyecto IS NOT NULL
            GROUP BY tipo_zona, estado_proyecto
            ORDER BY tipo_zona, estado_proyecto
        """;
        return jdbcTemplate.queryForList(sql);
    }
}