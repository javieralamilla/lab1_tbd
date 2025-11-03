package cl.usach.tbd.grupo4.plataforma_urbanismo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ZonaUrbana {
    private Long zonaUrbanaId;
    private String nombre;
    private String geometriaPoligono; // GeoJSON o WKT string
    private String tipoZona; // 'Residencial', 'Comercial', 'Industrial', 'Mixto'
    private BigDecimal areaKm2;
    private LocalDateTime fechaCreacion;

    // Datos demográficos del año actual (opcional - viene del JOIN)
    private Long poblacion;
    private Double densidadPoblacion; // Calculado: poblacion / areaKm2
}