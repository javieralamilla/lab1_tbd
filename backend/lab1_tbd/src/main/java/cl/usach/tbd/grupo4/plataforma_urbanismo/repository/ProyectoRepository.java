package cl.usach.tbd.grupo4.plataforma_urbanismo.repository;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ProyectoUrbano;
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
            SELECT 
                p1.nombre AS proyecto_1,
                p2.nombre AS proyecto_2,
                ROUND(
                    ST_Area(
                        ST_Intersection(
                            ST_Transform(p1.geometria, 32719),
                            ST_Transform(p2.geometria, 32719)
                        )
                    )::NUMERIC, 2
                ) AS area_superposicion_m2
            FROM proyectos_urbanos p1
            INNER JOIN proyectos_urbanos p2 ON p1.proyecto_urbano_id < p2.proyecto_urbano_id
            WHERE ST_Intersects(p1.geometria, p2.geometria)
              AND p1.geometria IS NOT NULL
              AND p2.geometria IS NOT NULL
            ORDER BY area_superposicion_m2 DESC
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> getResumenProyectosPorEstadoYZona() {
        String sql = """
            SELECT 
                tipo_zona,
                estado_proyecto,
                cantidad_proyectos,
                COALESCE(presupuesto_total, 0) AS presupuesto_total
            FROM resumen_proyectos_estado_zona
            WHERE cantidad_proyectos > 0
            ORDER BY tipo_zona, estado_proyecto
        """;
        return jdbcTemplate.queryForList(sql);
    }

    public void actualizarProyectosRetrasados(Long usuarioId) {
        String sql = "CALL actualizar_proyectos_retrasados(?)";
        jdbcTemplate.update(sql, usuarioId);
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