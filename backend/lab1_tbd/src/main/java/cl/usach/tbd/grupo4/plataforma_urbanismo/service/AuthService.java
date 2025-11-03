package cl.usach.tbd.grupo4.plataforma_urbanismo.service;

import cl.usach.tbd.grupo4.plataforma_urbanismo.dto.AuthResponse;
import cl.usach.tbd.grupo4.plataforma_urbanismo.dto.LoginRequest;
import cl.usach.tbd.grupo4.plataforma_urbanismo.model.Usuario;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthResponse login(LoginRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(request.getEmail());

        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), usuario.getContrasena())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // El rol del Enum ya está en minúsculas (ej: "administrador")
        String token = jwtService.generateToken(
                usuario.getEmail(),
                usuario.getUsuarioId(),
                usuario.getRol().name()
        );

        return new AuthResponse(
                token,
                usuario.getUsuarioId(),
                usuario.getEmail(),
                usuario.getNombre() + " " + usuario.getApellido(),
                usuario.getRol().name()
        );
    }

    public Usuario registrar(Usuario usuario) {
        // Verificar si el email ya existe
        Optional<Usuario> existente = usuarioRepository.findByEmail(usuario.getEmail());
        if (existente.isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Hashear la contraseña
        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));

        // Al registrar, Spring/Jackson mapeará automáticamente el
        // String del JSON (ej. "ADMINISTRADOR") al Enum Usuario.Rol.
        return usuarioRepository.save(usuario);
    }
}