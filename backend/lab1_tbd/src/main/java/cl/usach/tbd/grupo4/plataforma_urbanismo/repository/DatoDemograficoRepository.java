package cl.usach.tbd.grupo4.plataforma_urbanismo.repository;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.DatoDemografico;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class DatoDemograficoRepository {

    private final JdbcTemplate jdbcTemplate;

    public DatoDemograficoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<DatoDemografico> datoDemograficoRowMapper = (rs, rowNum) -> {
        DatoDemografico dato = new DatoDemografico();
        dato.setDatoDemograficoId(rs.getLong("dato_demografico_id"));
        dato.setZonaUrbanaId(rs.getLong("zona_urbana_id"));
        dato.setAño(rs.getInt("año"));
        dato.setPoblacion(rs.getInt("poblacion"));
        dato.setEdadPromedio(rs.getBigDecimal("edad_promedio"));
        dato.setNumeroViviendas(rs.getInt("numero_viviendas"));
        dato.setFactorPersonasVivienda(rs.getBigDecimal("factor_personas_vivienda"));
        return dato;
    };

    public List<DatoDemografico> findAll() {
        String sql = "SELECT * FROM datos_demograficos ORDER BY zona_urbana_id";
        return jdbcTemplate.query(sql, datoDemograficoRowMapper);
    }

    public Optional<DatoDemografico> findById(Long id) {
        String sql = "SELECT * FROM datos_demograficos WHERE dato_demografico_id = ?";
        try {
            DatoDemografico dato = jdbcTemplate.queryForObject(sql, datoDemograficoRowMapper, id);
            return Optional.ofNullable(dato);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<DatoDemografico> findByZonaUrbanaId(Long zonaUrbanaId) {
        String sql = "SELECT * FROM datos_demograficos WHERE zona_urbana_id = ?";
        return jdbcTemplate.query(sql, datoDemograficoRowMapper, zonaUrbanaId);
    }

    public DatoDemografico save(DatoDemografico dato) {
        String sql = "INSERT INTO datos_demograficos " +
                "(zona_urbana_id, año, poblacion, edad_promedio, numero_viviendas, factor_personas_vivienda) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, dato.getZonaUrbanaId());
            ps.setInt(2, dato.getAño());
            ps.setInt(3, dato.getPoblacion());
            ps.setBigDecimal(4, dato.getEdadPromedio());
            ps.setInt(5, dato.getNumeroViviendas());
            ps.setBigDecimal(6, dato.getFactorPersonasVivienda());
            return ps;
        }, keyHolder);

        dato.setDatoDemograficoId(keyHolder.getKey().longValue());
        return dato;
    }

    public int update(DatoDemografico dato) {
        String sql = "UPDATE datos_demograficos SET " +
                "año = ?, poblacion = ?, edad_promedio = ?, numero_viviendas = ?, factor_personas_vivienda = ? " +
                "WHERE dato_demografico_id = ?";

        return jdbcTemplate.update(sql,
                dato.getAño(),
                dato.getPoblacion(),
                dato.getEdadPromedio(),
                dato.getNumeroViviendas(),
                dato.getFactorPersonasVivienda(),
                dato.getDatoDemograficoId());
    }

    public List<DatoDemografico> findWithZonaInfo() {
        String sql = """
            SELECT 
                dd.*,
                zu.nombre as zona_nombre,
                zu.tipo_zona as zona_tipo,
                zu.area_km2 as zona_area_km2,
                ROUND(dd.poblacion::NUMERIC / NULLIF(zu.area_km2, 0), 2) as densidad_poblacion
            FROM datos_demograficos dd
            INNER JOIN zonas_urbanas zu ON dd.zona_urbana_id = zu.zona_urbana_id
            ORDER BY zu.nombre
        """;
        
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            DatoDemografico dato = new DatoDemografico();
            dato.setDatoDemograficoId(rs.getLong("dato_demografico_id"));
            dato.setZonaUrbanaId(rs.getLong("zona_urbana_id"));
            dato.setAño(rs.getInt("año"));
            dato.setPoblacion(rs.getInt("poblacion"));
            dato.setEdadPromedio(rs.getBigDecimal("edad_promedio"));
            dato.setNumeroViviendas(rs.getInt("numero_viviendas"));
            dato.setFactorPersonasVivienda(rs.getBigDecimal("factor_personas_vivienda"));
            dato.setZonaNombre(rs.getString("zona_nombre"));
            dato.setZonaTipo(rs.getString("zona_tipo"));
            dato.setZonaAreaKm2(rs.getBigDecimal("zona_area_km2"));
            dato.setDensidadPoblacion(rs.getBigDecimal("densidad_poblacion"));
            return dato;
        });
    }

    public int deleteById(Long id) {
        String sql = "DELETE FROM datos_demograficos WHERE dato_demografico_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}