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
public class DatoDemografico {
    @JsonProperty("dato_demografico_id")
    private Long datoDemograficoId;
    
    @JsonProperty("zona_urbana_id")
    private Long zonaUrbanaId;
    
    @JsonProperty("poblacion")
    private Integer poblacion;
    
    @JsonProperty("densidad_poblacion")
    private BigDecimal densidadPoblacion;
    
    @JsonProperty("edad_promedio")
    private BigDecimal edadPromedio;
    
    @JsonProperty("numero_viviendas")
    private Integer numeroViviendas;
    
    @JsonProperty("factor_personas_vivienda")
    private BigDecimal factorPersonasVivienda;
    
    // Campos adicionales para JOIN con zona
    @JsonProperty("zona_nombre")
    private String zonaNombre;
    
    @JsonProperty("zona_tipo")
    private String zonaTipo;
    
    @JsonProperty("zona_area_km2")
    private BigDecimal zonaAreaKm2;
}