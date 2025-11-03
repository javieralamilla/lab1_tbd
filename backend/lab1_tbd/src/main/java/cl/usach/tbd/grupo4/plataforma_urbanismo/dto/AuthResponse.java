package cl.usach.tbd.grupo4.plataforma_urbanismo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String tipo;
    private Long usuarioId;
    private String email;
    private String nombre;
    private String rol;

    public AuthResponse(String token, Long usuarioId, String email, String nombre, String rol) {
        this.token = token;
        this.tipo = "Bearer";
        this.usuarioId = usuarioId;
        this.email = email;
        this.nombre = nombre;
        this.rol = rol;
    }
}