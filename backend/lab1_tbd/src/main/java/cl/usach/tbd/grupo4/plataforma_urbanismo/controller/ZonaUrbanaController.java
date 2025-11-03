package cl.usach.tbd.grupo4.plataforma_urbanismo.controller;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ZonaUrbana;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.ZonaUrbanaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/zonas")
@CrossOrigin(origins = "*")
public class ZonaUrbanaController {

    @Autowired
    private ZonaUrbanaService zonaUrbanaService;

    @GetMapping
    public ResponseEntity<List<ZonaUrbana>> obtenerTodas() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<ZonaUrbana> zona = zonaUrbanaService.obtenerPorId(id);
        if (zona.isPresent()) {
            return ResponseEntity.ok(zona.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Zona urbana no encontrada");
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ZonaUrbana zona) {
        try {
            ZonaUrbana nuevaZona = zonaUrbanaService.crear(zona);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaZona);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear zona: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ZonaUrbana zona) {
        try {
            ZonaUrbana zonaActualizada = zonaUrbanaService.actualizar(id, zona);
            return ResponseEntity.ok(zonaActualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al actualizar zona: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            zonaUrbanaService.eliminar(id);
            return ResponseEntity.ok("Zona eliminada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al eliminar zona: " + e.getMessage());
        }
    }

    // Endpoints para consultas específicas

    @GetMapping("/analisis/densidad-poblacion")
    public ResponseEntity<List<Map<String, Object>>> obtenerDensidadPoblacion() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerDensidadPoblacion());
    }

    @GetMapping("/analisis/escasez-hospitales")
    public ResponseEntity<List<Map<String, Object>>> obtenerZonasEscasezHospitales() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerZonasEscasezHospitales());
    }

    @GetMapping("/analisis/rapido-crecimiento")
    public ResponseEntity<List<Map<String, Object>>> obtenerZonasRapidoCrecimiento() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerZonasRapidoCrecimiento());
    }

    @GetMapping("/analisis/sin-planificacion")
    public ResponseEntity<List<Map<String, Object>>> obtenerZonasSinPlanificacion() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerZonasSinPlanificacionReciente());
    }

    // Análisis Espacial: Obtener estadísticas de un área seleccionada
    @PostMapping("/analisis/area")
    public ResponseEntity<?> obtenerEstadisticasArea(@RequestBody Map<String, Object> request) {
        try {
            String geojsonArea = (String) request.get("geojson");
            if (geojsonArea == null || geojsonArea.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Se requiere el campo 'geojson' con la geometría del área");
            }

            Map<String, Object> estadisticas = zonaUrbanaService.obtenerEstadisticasArea(geojsonArea);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al calcular estadísticas: " + e.getMessage());
        }
    }

    // Simulación de Proyectos: Calcular impacto potencial de un nuevo proyecto
    @PostMapping("/simulacion/impacto")
    public ResponseEntity<?> calcularImpactoProyecto(@RequestBody Map<String, Object> request) {
        try {
            String geojsonArea = (String) request.get("geojson");
            if (geojsonArea == null || geojsonArea.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Se requiere el campo 'geojson' con la geometría del proyecto");
            }

            String tipoProyecto = (String) request.get("tipoProyecto");
            Integer poblacionEstimada = request.get("poblacionEstimada") != null
                ? ((Number) request.get("poblacionEstimada")).intValue() : 0;
            Integer numEscuelas = request.get("numEscuelas") != null
                ? ((Number) request.get("numEscuelas")).intValue() : 0;
            Integer numHospitales = request.get("numHospitales") != null
                ? ((Number) request.get("numHospitales")).intValue() : 0;
            Double impactoPoblacion = request.get("impactoPoblacion") != null
                ? ((Number) request.get("impactoPoblacion")).doubleValue() : 0.0;
            Integer areaInfluencia = request.get("areaInfluencia") != null
                ? ((Number) request.get("areaInfluencia")).intValue() : 1000;

            Map<String, Object> impacto = zonaUrbanaService.calcularImpactoProyecto(
                geojsonArea, tipoProyecto, poblacionEstimada, numEscuelas,
                numHospitales, impactoPoblacion, areaInfluencia
            );

            return ResponseEntity.ok(impacto);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al calcular impacto del proyecto: " + e.getMessage());
        }
    }
}