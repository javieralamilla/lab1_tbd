package cl.usach.tbd.grupo4.plataforma_urbanismo.controller;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ProyectoUrbano;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.ProyectoService;
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
@RequestMapping("/api/proyectos")
@CrossOrigin(origins = "*")
public class ProyectoController {

    @Autowired
    private ProyectoService proyectoService;

    @GetMapping
    public ResponseEntity<?> obtenerTodos(
            @RequestParam(required = false) String tipoZona,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "proyecto_urbano_id") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        // Filtro por tipoZona sin paginación
        if (tipoZona != null && !tipoZona.isEmpty()) {
            return ResponseEntity.ok(proyectoService.obtenerPorTipoZona(tipoZona));
        }
        
        // Si size es -1, retornar todos sin paginación
        if (size == -1) {
            return ResponseEntity.ok(proyectoService.obtenerTodos());
        }
        
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));
        Page<ProyectoUrbano> proyectosPage = proyectoService.obtenerTodosPaginado(pageable);
        
        Map<String, Object> response = Map.of(
            "content", proyectosPage.getContent(),
            "currentPage", proyectosPage.getNumber(),
            "totalItems", proyectosPage.getTotalElements(),
            "totalPages", proyectosPage.getTotalPages()
        );
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<ProyectoUrbano> proyecto = proyectoService.obtenerPorId(id);
        if (proyecto.isPresent()) {
            return ResponseEntity.ok(proyecto.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Proyecto no encontrado");
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<ProyectoUrbano>> obtenerPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(proyectoService.obtenerPorUsuario(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody ProyectoUrbano proyecto) {
        try {
            ProyectoUrbano nuevoProyecto = proyectoService.crear(proyecto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoProyecto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear proyecto: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody ProyectoUrbano proyecto) {
        try {
            ProyectoUrbano proyectoActualizado = proyectoService.actualizar(id, proyecto);
            return ResponseEntity.ok(proyectoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al actualizar proyecto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            proyectoService.eliminar(id);
            return ResponseEntity.ok("Proyecto eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al eliminar proyecto: " + e.getMessage());
        }
    }

    // Endpoints para consultas específicas

    @GetMapping("/analisis/superposicion")
    public ResponseEntity<List<Map<String, Object>>> obtenerSuperposicion() {
        return ResponseEntity.ok(proyectoService.obtenerSuperposicionProyectos());
    }

    @GetMapping("/analisis/resumen-estado-zona")
    public ResponseEntity<List<Map<String, Object>>> obtenerResumenPorEstadoYZona(
            @RequestParam(required = false) String tipoZona,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(proyectoService.obtenerResumenPorEstadoYZona(tipoZona, estado));
    }

    @PostMapping("/actualizar-retrasados/{usuarioId}")
    public ResponseEntity<?> actualizarProyectosRetrasados(@PathVariable Long usuarioId) {
        try {
            int actualizados = proyectoService.actualizarProyectosRetrasados(usuarioId);
            return ResponseEntity.ok(Map.of(
                "mensaje", "Proyectos actualizados correctamente",
                "cantidad", actualizados
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al actualizar proyectos: " + e.getMessage());
        }
    }

    @GetMapping("/analisis/zonas-sin-planificacion")
    public ResponseEntity<List<Map<String, Object>>> obtenerZonasSinPlanificacion() {
        return ResponseEntity.ok(proyectoService.obtenerZonasSinPlanificacion());
    }
}