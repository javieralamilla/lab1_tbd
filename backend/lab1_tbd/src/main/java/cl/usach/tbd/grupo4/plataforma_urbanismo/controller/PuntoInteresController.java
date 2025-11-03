package cl.usach.tbd.grupo4.plataforma_urbanismo.controller;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.PuntoInteres;
import cl.usach.tbd.grupo4.plataforma_urbanismo.service.PuntoInteresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/puntos-interes")
@CrossOrigin(origins = "*")
public class PuntoInteresController {

    @Autowired
    private PuntoInteresService puntoInteresService;

    @GetMapping
    public ResponseEntity<List<PuntoInteres>> obtenerTodos() {
        return ResponseEntity.ok(puntoInteresService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        Optional<PuntoInteres> punto = puntoInteresService.obtenerPorId(id);
        if (punto.isPresent()) {
            return ResponseEntity.ok(punto.get());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Punto de interés no encontrado");
    }

    @GetMapping("/zona/{zonaId}")
    public ResponseEntity<List<PuntoInteres>> obtenerPorZona(@PathVariable Long zonaId) {
        return ResponseEntity.ok(puntoInteresService.obtenerPorZona(zonaId));
    }


    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<?> obtenerPorTipo(@PathVariable String tipo) {
        try {
            // 1. Convierte el String de la URL (ej. "Centro Comercial") a tu enum (ej. Tipo.CENTRO_COMERCIAL)
            PuntoInteres.Tipo tipoEnum = PuntoInteres.Tipo.fromString(tipo);

            // 2. Llama al servicio con el Enum
            List<PuntoInteres> lista = puntoInteresService.obtenerPorTipo(tipoEnum);
            return ResponseEntity.ok(lista);

        } catch (IllegalArgumentException e) {
            // 3. Si el tipo no es válido, devuelve un error 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Tipo de punto de interés no válido: " + tipo);
        }
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody PuntoInteres punto) {
        try {
            PuntoInteres nuevoPunto = puntoInteresService.crear(punto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevoPunto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al crear punto de interés: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id, @RequestBody PuntoInteres punto) {
        try {
            PuntoInteres puntoActualizado = puntoInteresService.actualizar(id, punto);
            return ResponseEntity.ok(puntoActualizado);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al actualizar punto de interés: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        try {
            puntoInteresService.eliminar(id);
            return ResponseEntity.ok("Punto de interés eliminado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al eliminar punto de interés: " + e.getMessage());
        }
    }

    // Endpoints para consultas específicas

    @GetMapping("/analisis/escuelas-cerca-proyectos")
    public ResponseEntity<List<Map<String, Object>>> obtenerEscuelasCercaProyectos() {
        return ResponseEntity.ok(puntoInteresService.obtenerEscuelasCercaProyectos());
    }

    @GetMapping("/analisis/cobertura-infraestructura")
    public ResponseEntity<List<Map<String, Object>>> obtenerCoberturaInfraestructura() {
        return ResponseEntity.ok(puntoInteresService.obtenerCoberturaInfraestructura());
    }

    @PostMapping("/refrescar-cobertura")
    public ResponseEntity<?> refrescarCoberturaInfraestructura() {
        try {
            puntoInteresService.refrescarCoberturaInfraestructura();
            return ResponseEntity.ok("Vista materializada refrescada correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error al refrescar vista: " + e.getMessage());
        }
    }
}