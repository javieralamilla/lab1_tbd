package cl.usach.tbd.grupo4.plataforma_urbanismo.controller;

import cl.usach.tbd.grupo4.plataforma_urbanismo.dto.AuthResponse;
import cl.usach.tbd.grupo4.plataforma_urbanismo.dto.LoginRequest;
import cl.usach.tbd.grupo4.plataforma_urbanismo.model.Usuario;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.AuthService;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.JwtService;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Error en autenticación: " + e.getMessage());
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = authService.registrar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error en registro: " + e.getMessage());
        }
    }

    // NUEVO: Endpoint para obtener el perfil del usuario autenticado
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("No autenticado");
            }

            String email = authentication.getName();
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Usuario no encontrado");
            }

            Usuario usuario = usuarioOpt.get();

            // Crear respuesta sin el token (ya lo tiene el cliente)
            AuthResponse response = new AuthResponse(
                    null, // No enviamos el token de nuevo
                    usuario.getUsuarioId(),
                    usuario.getEmail(),
                    usuario.getNombre() + " " + usuario.getApellido(),
                    usuario.getRol().name() // Ya está en minúsculas
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener perfil: " + e.getMessage());
        }
    }
}