package cl.usach.tbd.grupo4.plataforma_urbanismo.repository;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.PuntoInteres;
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
public class PuntoInteresRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Constante para la consulta base de selección
    private final String SELECT_SQL = "SELECT punto_interes_id, nombre, tipo, " +
            "ST_AsGeoJSON(coordenadas_punto) as coordenadas_geojson, " +
            "direccion, zona_urbana_id, activo, fecha_creacion " +
            "FROM puntos_interes ";

    private final RowMapper<PuntoInteres> puntoInteresRowMapper = new RowMapper<PuntoInteres>() {
        @Override
        public PuntoInteres mapRow(ResultSet rs, int rowNum) throws SQLException {
            PuntoInteres punto = new PuntoInteres();
            punto.setPuntoInteresId(rs.getLong("punto_interes_id"));
            punto.setNombre(rs.getString("nombre"));
            punto.setTipo(PuntoInteres.Tipo.fromString(rs.getString("tipo")));
            punto.setCoordenadasPunto(rs.getString("coordenadas_geojson"));
            punto.setDireccion(rs.getString("direccion"));
            punto.setZonaUrbanaId(rs.getLong("zona_urbana_id"));
            punto.setActivo(rs.getBoolean("activo"));
            punto.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
            return punto;
        }
    };

    public List<PuntoInteres> findAll() {
        String sql = SELECT_SQL + "WHERE activo = TRUE ORDER BY nombre";
        return jdbcTemplate.query(sql, puntoInteresRowMapper);
    }

    public Optional<PuntoInteres> findById(Long id) {
        String sql = SELECT_SQL + "WHERE punto_interes_id = ? AND activo = TRUE";
        try {
            PuntoInteres punto = jdbcTemplate.queryForObject(sql, puntoInteresRowMapper, id);
            return Optional.ofNullable(punto);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<PuntoInteres> findByZonaUrbanaId(Long zonaId) {
        String sql = SELECT_SQL + "WHERE zona_urbana_id = ? AND activo = TRUE ORDER BY nombre";
        return jdbcTemplate.query(sql, puntoInteresRowMapper, zonaId);
    }

    public List<PuntoInteres> findByTipo(PuntoInteres.Tipo tipo) {
        String sql = SELECT_SQL + "WHERE tipo = ? AND activo = TRUE ORDER BY nombre";

        //.getValor() para enviar el String (ej. "Centro Comercial") a la DB
        return jdbcTemplate.query(sql, puntoInteresRowMapper, tipo.getValor());
    }

    public List<Map<String, Object>> getEscuelasCercaProyectos() {
        String sql = """
            SELECT 
                pi.nombre AS nombre_escuela,
                pu.nombre AS nombre_proyecto,
                ROUND(ST_Distance(
                    ST_Transform(pi.coordenadas_punto, 32719),
                    ST_Transform(pu.geometria, 32719)
                )::NUMERIC, 2) AS distancia_metros
            FROM puntos_interes pi
            CROSS JOIN proyectos_urbanos pu
            WHERE pi.tipo = ?
              AND pu.estado = 'En Curso'
              AND ST_DWithin(
                  ST_Transform(pi.coordenadas_punto, 32719),
                  ST_Transform(pu.geometria, 32719),
                  500
              )
            ORDER BY distancia_metros
        """;
        // CAMBIO: Se pasa el valor del enum 'Tipo.ESCUELA'
        return jdbcTemplate.queryForList(sql, PuntoInteres.Tipo.ESCUELA.getValor());
    }

    public List<Map<String, Object>> getCoberturaInfraestructura() {
        String sql = "SELECT * FROM cobertura_infraestructura ORDER BY nombre_zona";
        return jdbcTemplate.queryForList(sql);
    }

    public void refrescarCoberturaInfraestructura() {
        String sql = "REFRESH MATERIALIZED VIEW CONCURRENTLY cobertura_infraestructura";
        jdbcTemplate.execute(sql);
    }

    public PuntoInteres save(PuntoInteres punto) {
        String sql = "INSERT INTO puntos_interes " +
                "(nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) " +
                "VALUES (?, ?, ST_GeomFromGeoJSON(?), ?, ?) " +
                "RETURNING punto_interes_id";

        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                punto.getNombre(),
                punto.getTipo().getValor(),
                punto.getCoordenadasPunto(),
                punto.getDireccion(),
                punto.getZonaUrbanaId());

        punto.setPuntoInteresId(id);
        return punto;
    }

    public int update(PuntoInteres punto) {
        String sql = "UPDATE puntos_interes SET " +
                "nombre = ?, tipo = ?, direccion = ?, zona_urbana_id = ?, activo = ? " +
                "WHERE punto_interes_id = ?";

        return jdbcTemplate.update(sql,
                punto.getNombre(),
                punto.getTipo().getValor(),
                punto.getDireccion(),
                punto.getZonaUrbanaId(),
                punto.getActivo(),
                punto.getPuntoInteresId());
    }

    public int deleteById(Long id) {
        String sql = "UPDATE puntos_interes SET activo = FALSE WHERE punto_interes_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}