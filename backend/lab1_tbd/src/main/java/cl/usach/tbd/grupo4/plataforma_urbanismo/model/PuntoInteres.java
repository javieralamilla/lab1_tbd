package cl.usach.tbd.grupo4.plataforma_urbanismo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PuntoInteres {
    private Long puntoInteresId;
    private String nombre;
    private Tipo tipo;
    private String coordenadasPunto; // GeoJSON o WKT string - POINT
    private String direccion;
    private Long zonaUrbanaId;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

     //Tu enum 'Tipo' con los nombres de constantes en mayúsculas.
     //Se agrega el mapeo al String que se guarda en la base de datos
     //(ej. "Centro Comercial" con espacio).

    public enum Tipo {
        HOSPITAL("Hospital"),
        ESCUELA("Escuela"),
        PARQUE("Parque"),
        CENTRO_COMERCIAL("Centro Comercial"),
        TRANSPORTE("Transporte");

        private final String valor;

        Tipo(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }


         //Metodo helper para convertir el String de la base de datos

        public static Tipo fromString(String text) {
            if (text != null) {
                for (Tipo t : Tipo.values()) {
                    if (text.equalsIgnoreCase(t.valor)) {
                        return t;
                    }
                }
            }
            throw new IllegalArgumentException("No se pudo encontrar un Tipo con el texto: " + text);
        }
    }
}