package cl.usach.tbd.grupo4.plataforma_urbanismo.controller;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ZonaUrbana;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.ZonaUrbanaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    public ResponseEntity<?> obtenerTodas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "zona_urbana_id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        // Si size es -1, retornar todos sin paginación (para compatibilidad con frontend actual)
        if (size == -1) {
            return ResponseEntity.ok(zonaUrbanaService.obtenerTodas());
        }
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ZonaUrbana> zonasPage = zonaUrbanaService.obtenerTodasPaginado(pageable);
        
        Map<String, Object> response = Map.of(
            "content", zonasPage.getContent(),
            "currentPage", zonasPage.getNumber(),
            "totalItems", zonasPage.getTotalElements(),
            "totalPages", zonasPage.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
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
    public ResponseEntity<?> obtenerDensidadPoblacion() {
        try {
            List<Map<String, Object>> resultado = zonaUrbanaService.obtenerDensidadPoblacion();
            System.out.println("[ZonaUrbanaController] Densidad - Resultado obtenido: " + resultado.size() + " registros");
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            System.err.println("[ZonaUrbanaController] Error al obtener densidad poblacional: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al calcular densidad: " + e.getMessage());
        }
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

    // 3. Análisis de Proximidad a Proyectos
    @GetMapping("/analisis/escuelas-cerca-proyectos")
    public ResponseEntity<List<Map<String, Object>>> obtenerEscuelasCercaProyectos() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerEscuelasCercaProyectos());
    }

    // 5. Análisis de Cobertura de Infraestructura
    @GetMapping("/analisis/cobertura-infraestructura")
    public ResponseEntity<List<Map<String, Object>>> obtenerCoberturaInfraestructura() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerCoberturaInfraestructura());
    }

    // 9. Análisis de Superposición de Proyectos
    @GetMapping("/analisis/proyectos-superpuestos")
    public ResponseEntity<List<Map<String, Object>>> obtenerProyectosSuperpuestos() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerProyectosSuperpuestos());
    }

    // 10. Resumen de Proyectos por Estado y Tipo de Zona
    @GetMapping("/analisis/resumen-proyectos-estado-zona")
    public ResponseEntity<List<Map<String, Object>>> obtenerResumenProyectosEstadoZona() {
        return ResponseEntity.ok(zonaUrbanaService.obtenerResumenProyectosEstadoZona());
    }

    // Endpoint para refrescar vistas materializadas
    @PostMapping("/admin/refrescar-vistas")
    public ResponseEntity<String> refrescarVistasMaterializadas() {
        try {
            zonaUrbanaService.refrescarVistasMaterializadas();
            return ResponseEntity.ok("Vistas materializadas refrescadas exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al refrescar vistas: " + e.getMessage());
        }
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