package cl.usach.tbd.grupo4.plataforma_urbanismo.controller;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.DatoDemografico;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.DatoDemograficoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/datos-demograficos")
@CrossOrigin(origins = "*")
public class DatoDemograficoController {

    private final DatoDemograficoService datoDemograficoService;

    public DatoDemograficoController(DatoDemograficoService datoDemograficoService) {
        this.datoDemograficoService = datoDemograficoService;
    }

    @GetMapping
    public ResponseEntity<List<DatoDemografico>> obtenerTodos(
            @RequestParam(required = false) Long zonaId
    ) {
        try {
            List<DatoDemografico> datos;

            if (zonaId != null) {
                // Buscar por zona
                datos = datoDemograficoService.obtenerPorZona(zonaId);
            } else {
                // Obtener todos
                datos = datoDemograficoService.obtenerTodos();
            }

            return ResponseEntity.ok(datos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return datoDemograficoService.obtenerPorId(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener dato demográfico: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody DatoDemografico dato) {
        try {
            DatoDemografico nuevoDato = datoDemograficoService.crear(dato);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoDato);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error de validación: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear dato demográfico: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody DatoDemografico dato) {
        try {
            DatoDemografico datoActualizado = datoDemograficoService.actualizar(id, dato);
            return ResponseEntity.ok(datoActualizado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar dato demográfico: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            datoDemograficoService.eliminar(id);
            return ResponseEntity.ok().body("Dato demográfico eliminado correctamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar dato demográfico: " + e.getMessage());
        }
    }
}
