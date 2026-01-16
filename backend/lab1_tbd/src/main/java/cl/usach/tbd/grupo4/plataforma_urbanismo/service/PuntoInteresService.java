package cl.usach.tbd.grupo4.plataforma_urbanismo.service;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.PuntoInteres;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.PuntoInteresRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class PuntoInteresService {

    @Autowired
    private PuntoInteresRepository puntoInteresRepository;

    public List<PuntoInteres> obtenerTodos() {
        return puntoInteresRepository.findAll();
    }

    public Page<PuntoInteres> obtenerTodosPaginado(Pageable pageable) {
        List<PuntoInteres> todosLosPuntos = puntoInteresRepository.findAll();
        
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todosLosPuntos.size());
        
        List<PuntoInteres> pageContent = todosLosPuntos.subList(start, end);
        
        return new PageImpl<>(pageContent, pageable, todosLosPuntos.size());
    }

    public Optional<PuntoInteres> obtenerPorId(Long id) {
        return puntoInteresRepository.findById(id);
    }

    public List<PuntoInteres> obtenerPorZona(Long zonaId) {
        return puntoInteresRepository.findByZonaUrbanaId(zonaId);
    }

    public List<PuntoInteres> obtenerPorTipo(PuntoInteres.Tipo tipo) {
        return puntoInteresRepository.findByTipo(tipo);
    }

    public PuntoInteres crear(PuntoInteres punto) {
        return puntoInteresRepository.save(punto);
    }

    public PuntoInteres actualizar(Long id, PuntoInteres punto) {
        Optional<PuntoInteres> existente = puntoInteresRepository.findById(id);
        if (existente.isEmpty()) {
            throw new RuntimeException("Punto de interés no encontrado");
        }
        punto.setPuntoInteresId(id);
        puntoInteresRepository.update(punto);
        return punto;
    }

    public void eliminar(Long id) {
        puntoInteresRepository.deleteById(id);
    }

    // Consultas específicas
    public List<Map<String, Object>> obtenerEscuelasCercaProyectos() {
        return puntoInteresRepository.getEscuelasCercaProyectos();
    }

    public List<Map<String, Object>> obtenerCoberturaInfraestructura() {
        return puntoInteresRepository.getCoberturaInfraestructura();
    }

    public void refrescarCoberturaInfraestructura() {
        puntoInteresRepository.refrescarCoberturaInfraestructura();
    }
}