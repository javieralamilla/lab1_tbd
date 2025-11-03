package cl.usach.tbd.grupo4.plataforma_urbanismo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    private Long usuarioId;
    private String nombre;
    private String apellido;
    private String email;
    private String contrasena;
    private Rol rol;
    private LocalDateTime fechaRegistro;
    private Boolean activo;


    public enum Rol {
        planificador,
        administrador,
        autoridad_municipal
    }
}