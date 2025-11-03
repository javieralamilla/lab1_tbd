package cl.usach.tbd.grupo4.plataforma_urbanismo.repository;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Usuario> usuarioRowMapper = new RowMapper<Usuario>() {
        @Override
        public Usuario mapRow(ResultSet rs, int rowNum) throws SQLException {
            Usuario usuario = new Usuario();
            usuario.setUsuarioId(rs.getLong("usuario_id"));
            usuario.setNombre(rs.getString("nombre"));
            usuario.setApellido(rs.getString("apellido"));
            usuario.setEmail(rs.getString("email"));
            usuario.setContrasena(rs.getString("contrasena"));

            // Convertir String de BBDD a Enum
            // El rol en la BD está en minúsculas, así que coincide con el Enum
            usuario.setRol(Usuario.Rol.valueOf(rs.getString("rol")));

            usuario.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
            usuario.setActivo(rs.getBoolean("activo"));
            return usuario;
        }
    };

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT * FROM usuarios WHERE email = ? AND activo = TRUE";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(sql, usuarioRowMapper, email);
            return Optional.ofNullable(usuario);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public Optional<Usuario> findById(Long id) {
        String sql = "SELECT * FROM usuarios WHERE usuario_id = ? AND activo = TRUE";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(sql, usuarioRowMapper, id);
            return Optional.ofNullable(usuario);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Usuario> findAll() {
        String sql = "SELECT * FROM usuarios WHERE activo = TRUE ORDER BY fecha_registro DESC";
        return jdbcTemplate.query(sql, usuarioRowMapper);
    }

    public Usuario save(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre, apellido, email, contrasena, rol) " +
                "VALUES (?, ?, ?, ?, ?) RETURNING usuario_id";

        // Convertir Enum a String para BBDD
        // Como el Enum tiene nombres en minúsculas, .name() retorna "administrador"
        Long id = jdbcTemplate.queryForObject(sql, Long.class,
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getContrasena(),
                usuario.getRol().name());

        usuario.setUsuarioId(id);
        return usuario;
    }

    public int update(Usuario usuario) {
        String sql = "UPDATE usuarios SET nombre = ?, apellido = ?, email = ?, rol = ?, activo = ? " +
                "WHERE usuario_id = ?";

        // Convertir Enum a String para BBDD
        return jdbcTemplate.update(sql,
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getActivo(),
                usuario.getUsuarioId());
    }

    public int deleteById(Long id) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE usuario_id = ?";
        return jdbcTemplate.update(sql, id);
    }
}