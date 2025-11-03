package cl.usach.tbd.grupo4.plataforma_urbanismo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DatoDemografico {
    private Long datoDemograficoId;
    private Long zonaUrbanaId;
    private Integer poblacion;
    private BigDecimal densidadPoblacion;
    private BigDecimal edadPromedio;
    private Integer numeroViviendas;
    private BigDecimal factorPersonasVivienda;
    private Integer anio;
    private LocalDateTime fechaRegistro;
}