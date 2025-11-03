package cl.usach.tbd.grupo4.plataforma_urbanismo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProyectoUrbano {
    private Long proyectoUrbanoId;
    private String nombre;
    private String descripcion;
    private String tipoProyecto;
    private LocalDate fechaInicio;
    private LocalDate fechaTermino;
    private String estado; // 'Planeado', 'En Curso', 'Completado', 'Retrasado', 'Cancelado'
    private BigDecimal presupuesto;
    private String geometria; // GeoJSON o WKT string
    private Long usuarioId;
    private LocalDateTime fechaCreacion;
}