package cl.usach.tbd.grupo4.plataforma_urbanismo.service;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ZonaUrbana;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.ZonaUrbanaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ZonaUrbanaService {

    @Autowired
    private ZonaUrbanaRepository zonaUrbanaRepository;

    public List<ZonaUrbana> obtenerTodas() {
        return zonaUrbanaRepository.findAll();
    }

    public Optional<ZonaUrbana> obtenerPorId(Long id) {
        return zonaUrbanaRepository.findById(id);
    }

    public ZonaUrbana crear(ZonaUrbana zona) {
        return zonaUrbanaRepository.save(zona);
    }

    public ZonaUrbana actualizar(Long id, ZonaUrbana zona) {
        Optional<ZonaUrbana> existente = zonaUrbanaRepository.findById(id);
        if (existente.isEmpty()) {
            throw new RuntimeException("Zona urbana no encontrada");
        }
        zona.setZonaUrbanaId(id);
        zonaUrbanaRepository.update(zona);
        return zona;
    }

    public void eliminar(Long id) {
        zonaUrbanaRepository.deleteById(id);
    }

    // Consultas específicas
    public List<Map<String, Object>> obtenerDensidadPoblacion() {
        return zonaUrbanaRepository.getDensidadPoblacion();
    }

    public List<Map<String, Object>> obtenerZonasEscasezHospitales() {
        return zonaUrbanaRepository.getZonasEscasezHospitales();
    }

    public List<Map<String, Object>> obtenerZonasRapidoCrecimiento() {
        return zonaUrbanaRepository.getZonasRapidoCrecimiento();
    }

    public List<Map<String, Object>> obtenerZonasSinPlanificacionReciente() {
        return zonaUrbanaRepository.getZonasSinPlanificacionReciente();
    }

    // Análisis Espacial
    public Map<String, Object> obtenerEstadisticasArea(String geojsonArea) {
        return zonaUrbanaRepository.getEstadisticasArea(geojsonArea);
    }

    // Simulación de Proyectos
    public Map<String, Object> calcularImpactoProyecto(
            String geojsonArea,
            String tipoProyecto,
            Integer poblacionEstimada,
            Integer numEscuelas,
            Integer numHospitales,
            Double impactoPoblacion,
            Integer areaInfluencia) {
        return zonaUrbanaRepository.calcularImpactoProyecto(
            geojsonArea, tipoProyecto, poblacionEstimada, numEscuelas,
            numHospitales, impactoPoblacion, areaInfluencia
        );
    }
}