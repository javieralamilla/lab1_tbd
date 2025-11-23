package cl.usach.tbd.grupo4.plataforma_urbanismo.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("geometria_geojson")
    private String geometriaPoligono; // GeoJSON o WKT string
    
    @JsonProperty("tipo_zona")
    private String tipoZona; // 'Residencial', 'Comercial', 'Industrial', 'Mixto'
    
    @JsonProperty("area_km2")
    private BigDecimal areaKm2;
    
    @JsonProperty("fecha_creacion")
    private LocalDateTime fechaCreacion;

    // Datos demográficos del año actual (opcional - viene del JOIN)
    @JsonProperty("poblacion_estimada_2025")
    private Long poblacion;
    
    @JsonProperty("densidad_poblacion")
    private Double densidadPoblacion; // Calculado: poblacion / areaKm2
}