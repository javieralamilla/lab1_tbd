package cl.usach.tbd.grupo4.plataforma_urbanismo.repository;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ProyectoUrbano;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProyectoRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<ProyectoUrbano> proyectoRowMapper = new RowMapper<ProyectoUrbano>() {
        @Override
        public ProyectoUrbano mapRow(ResultSet rs, int rowNum) throws SQLException {
            ProyectoUrbano proyecto = new ProyectoUrbano();
            proyecto.setProyectoUrbanoId(rs.getLong("proyecto_urbano_id"));
            proyecto.setNombre(rs.getString("nombre"));
            proyecto.setDescripcion(rs.getString("descripcion"));
            proyecto.setTipoProyecto(rs.getString("tipo_proyecto"));
            proyecto.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());

            if (rs.getDate("fecha_termino") != null) {
                proyecto.setFechaTermino(rs.getDate("fecha_termino").toLocalDate());
            }

            proyecto.setEstado(rs.getString("estado"));
            proyecto.setPresupuesto(rs.getBigDecimal("presupuesto"));
            proyecto.setGeometria(rs.getString("geometria_geojson"));
            proyecto.setUsuarioId(rs.getLong("usuario_id"));
            proyecto.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
            return proyecto;
        }
    };

    public List<ProyectoUrbano> findAll() {
        String sql = "SELECT proyecto_urbano_id, nombre, descripcion, tipo_proyecto, " +
                "fecha_inicio, fecha_termino, estado, presupuesto, " +
                "ST_AsGeoJSON(geometria) as geometria_geojson, " +
                "usuario_id, fecha_creacion " +
                "FROM proyectos_urbanos ORDER BY fecha_creacion DESC";
        return jdbcTemplate.query(sql, proyectoRowMapper);
    }

    public List<ProyectoUrbano> findByTipoZona(String tipoZona) {
        String sql = "SELECT DISTINCT p.proyecto_urbano_id, p.nombre, p.descripcion, p.tipo_proyecto, " +
                "p.fecha_inicio, p.fecha_termino, p.estado, p.presupuesto, " +
                "ST_AsGeoJSON(p.geometria) as geometria_geojson, " +
                "p.usuario_id, p.fecha_creacion " +
                "FROM proyectos_urbanos p " +
                "INNER JOIN proyectos_zonas pz ON p.proyecto_urbano_id = pz.proyecto_urbano_id " +
                "INNER JOIN zonas_urbanas zu ON pz.zona_urbana_id = zu.zona_urbana_id " +
                "WHERE zu.tipo_zona = ? " +
                "ORDER BY p.fecha_creacion DESC";
        return jdbcTemplate.query(sql, proyectoRowMapper, tipoZona);
    }

    public Optional<ProyectoUrbano> findById(Long id) {
        String sql = "SELECT proyecto_urbano_id, nombre, descripcion, tipo_proyecto, " +
                "fecha_inicio, fecha_termino, estado, presupuesto, " +
                "ST_AsGeoJSON(geometria) as geometria_geojson, " +
                "usuario_id, fecha_creacion " +
                "FROM proyectos_urbanos WHERE proyecto_urbano_id = ?";
        try {
            ProyectoUrbano proyecto = jdbcTemplate.queryForObject(sql, proyectoRowMapper, id);
            return Optional.ofNullable(proyecto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<ProyectoUrbano> findByUsuarioId(Long usuarioId) {
        String sql = "SELECT proyecto_urbano_id, nombre, descripcion, tipo_proyecto, " +
                "fecha_inicio, fecha_termino, estado, presupuesto, " +
                "ST_AsGeoJSON(geometria) as geometria_geojson, " +
                "usuario_id, fecha_creacion " +
                "FROM proyectos_urbanos WHERE usuario_id = ? ORDER BY fecha_creacion DESC";
        return jdbcTemplate.query(sql, proyectoRowMapper, usuarioId);
    }

    public List<Map<String, Object>> getSuperposicionProyectos() {
        String sql = """
            WITH params AS (
                SELECT 10.0 AS buffer_m -- buffer en metros
            ), preparados AS (
                SELECT
                    p.proyecto_urbano_id,
                    p.nombre,
                    p.estado,
                    ST_SRID(p.geometria) AS srid_orig,
                    ST_IsValid(p.geometria) AS is_valid_orig,
                    ST_SetSRID(ST_MakeValid(p.geometria), COALESCE(NULLIF(ST_SRID(p.geometria), 0), 4326)) AS geom_valid
                FROM proyectos_urbanos p
                WHERE p.geometria IS NOT NULL
            ), proyectados AS (
                SELECT
                    pr.*, GeometryType(pr.geom_valid) AS geom_type,
                    ST_Transform(pr.geom_valid, 32719) AS geom_proj
                FROM preparados pr
            )
            SELECT
                s1.nombre AS proyecto_1,
                s2.nombre AS proyecto_2,
                s1.estado AS estado_proyecto_1,
                s2.estado AS estado_proyecto_2,
                s1.geom_type AS tipo_geom_1,
                s2.geom_type AS tipo_geom_2,
                s1.srid_orig AS srid_origen_1,
                s2.srid_orig AS srid_origen_2,
                s1.is_valid_orig AS is_valid_orig_1,
                s2.is_valid_orig AS is_valid_orig_2,
                -- Intersección usando buffer en geography (buffer métrico en lat/lon)
                ST_AsGeoJSON(
                    ST_Intersection(
                        ST_Transform(ST_Buffer(s1.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719),
                        ST_Transform(ST_Buffer(s2.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719)
                    )
                ) AS inter_geojson_geog_buffer,
                -- Intersección usando buffer en geometría proyectada (transform -> buffer)
                ST_AsGeoJSON(
                    ST_Intersection(
                        CASE WHEN s1.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s1.geom_proj, (SELECT buffer_m FROM params)) ELSE s1.geom_proj END,
                        CASE WHEN s2.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s2.geom_proj, (SELECT buffer_m FROM params)) ELSE s2.geom_proj END
                    )
                ) AS inter_geojson_proj_buffer,
                -- Área extraída de las partes poligonales para cada método
                ROUND(COALESCE(ST_Area(ST_CollectionExtract(ST_Intersection(
                    ST_Transform(ST_Buffer(s1.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719),
                    ST_Transform(ST_Buffer(s2.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719)
                ), 3)), 0)::NUMERIC, 2) AS area_geog_buffer_m2,

                ROUND(COALESCE(ST_Area(ST_CollectionExtract(ST_Intersection(
                    CASE WHEN s1.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s1.geom_proj, (SELECT buffer_m FROM params)) ELSE s1.geom_proj END,
                    CASE WHEN s2.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s2.geom_proj, (SELECT buffer_m FROM params)) ELSE s2.geom_proj END
                ), 3)), 0)::NUMERIC, 2) AS area_proj_buffer_m2,

                -- Área final: max(geog, proj) y alias correcto esperado por frontend
                ROUND(
                GREATEST(
                    COALESCE(ST_Area(ST_CollectionExtract(ST_Intersection(
                        ST_Transform(ST_Buffer(s1.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719),
                        ST_Transform(ST_Buffer(s2.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719)
                    ), 3)), 0),
                    COALESCE(ST_Area(ST_CollectionExtract(ST_Intersection(
                        CASE WHEN s1.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s1.geom_proj, (SELECT buffer_m FROM params)) ELSE s1.geom_proj END,
                        CASE WHEN s2.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s2.geom_proj, (SELECT buffer_m FROM params)) ELSE s2.geom_proj END
                    ), 3)), 0)
                )::NUMERIC, 2) AS area_superposicion_m2
            FROM proyectados s1
            JOIN proyectados s2 ON s1.proyecto_urbano_id < s2.proyecto_urbano_id
            WHERE (
                -- filtrar por proximidad rápida: si las cajas proyectadas se solapan o están muy cerca
                ST_Intersects(ST_Envelope(s1.geom_proj), ST_Envelope(s2.geom_proj))
                OR ST_DWithin(s1.geom_proj, s2.geom_proj, (SELECT buffer_m FROM params))
            )
            -- IMPORTANTE: excluir pares con área de superposición = 0 (no son superposiciones reales)
            AND (
                GREATEST(
                    COALESCE(ST_Area(ST_CollectionExtract(ST_Intersection(
                        ST_Transform(ST_Buffer(s1.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719),
                        ST_Transform(ST_Buffer(s2.geom_valid::geography, (SELECT buffer_m FROM params))::geometry, 32719)
                    ), 3)), 0),
                    COALESCE(ST_Area(ST_CollectionExtract(ST_Intersection(
                        CASE WHEN s1.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s1.geom_proj, (SELECT buffer_m FROM params)) ELSE s1.geom_proj END,
                        CASE WHEN s2.geom_type ILIKE '%LINESTRING%' THEN ST_Buffer(s2.geom_proj, (SELECT buffer_m FROM params)) ELSE s2.geom_proj END
                    ), 3)), 0)
                ) > 0
            )
            ORDER BY area_superposicion_m2 DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getResumenProyectosPorEstadoYZona(String tipoZona, String estado) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                tipo_zona,
                estado_proyecto,
                cantidad_proyectos,
                COALESCE(presupuesto_total, 0) AS presupuesto_total
            FROM resumen_proyectos_estado_zona
            WHERE cantidad_proyectos > 0
        """);

        boolean hasFilters = false;

        if (tipoZona != null && !tipoZona.isEmpty() && !tipoZona.equalsIgnoreCase("todos")) {
            sql.append(" AND tipo_zona = ?");
            hasFilters = true;
        }

        if (estado != null && !estado.isEmpty() && !estado.equalsIgnoreCase("todos")) {
            sql.append(" AND estado_proyecto = ?");
            hasFilters = true;
        }

        sql.append(" ORDER BY tipo_zona, estado_proyecto");

        if (!hasFilters) {
            return jdbcTemplate.queryForList(sql.toString());
        } else if (tipoZona != null && !tipoZona.isEmpty() && !tipoZona.equalsIgnoreCase("todos")
                   && estado != null && !estado.isEmpty() && !estado.equalsIgnoreCase("todos")) {
            return jdbcTemplate.queryForList(sql.toString(), tipoZona, estado);
        } else if (tipoZona != null && !tipoZona.isEmpty() && !tipoZona.equalsIgnoreCase("todos")) {
            return jdbcTemplate.queryForList(sql.toString(), tipoZona);
        } else {
            return jdbcTemplate.queryForList(sql.toString(), estado);
        }
    }

    public int actualizarProyectosRetrasados(Long usuarioId) {
        String sql = """    
            UPDATE proyectos_urbanos
            SET estado = 'Retrasado',
                usuario_id = ?
            WHERE estado = 'En Curso'
            AND fecha_termino < CURRENT_DATE
        """;
        return jdbcTemplate.update(sql, usuarioId);
    }

    public ProyectoUrbano save(ProyectoUrbano proyecto) {
        String sql = "INSERT INTO proyectos_urbanos " +
                "(nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, " +
                "estado, presupuesto, geometria, usuario_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ST_GeomFromGeoJSON(?), ?) " +
                "RETURNING proyecto_urbano_id";

        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getTipoProyecto(),
                proyecto.getFechaInicio(),
                proyecto.getFechaTermino(),
                proyecto.getEstado(),
                proyecto.getPresupuesto(),
                proyecto.getGeometria(),
                proyecto.getUsuarioId());

        proyecto.setProyectoUrbanoId(id);
        return proyecto;
    }

    public int update(ProyectoUrbano proyecto) {
        String sql = "UPDATE proyectos_urbanos SET " +
                "nombre = ?, descripcion = ?, tipo_proyecto = ?, " +
                "fecha_inicio = ?, fecha_termino = ?, estado = ?, presupuesto = ? " +
                "WHERE proyecto_urbano_id = ?";

        return jdbcTemplate.update(sql,
                proyecto.getNombre(),
                proyecto.getDescripcion(),
                proyecto.getTipoProyecto(),
                proyecto.getFechaInicio(),
                proyecto.getFechaTermino(),
                proyecto.getEstado(),
                proyecto.getPresupuesto(),
                proyecto.getProyectoUrbanoId());
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM proyectos_urbanos WHERE proyecto_urbano_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}
