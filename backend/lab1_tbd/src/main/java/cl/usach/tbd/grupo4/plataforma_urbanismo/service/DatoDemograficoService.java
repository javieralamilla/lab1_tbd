package cl.usach.tbd.grupo4.plataforma_urbanismo.service;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.DatoDemografico;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.DatoDemograficoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DatoDemograficoService {

    private final DatoDemograficoRepository datoDemograficoRepository;

    public DatoDemograficoService(DatoDemograficoRepository datoDemograficoRepository) {
        this.datoDemograficoRepository = datoDemograficoRepository;
    }

    public List<DatoDemografico> obtenerTodos() {
        return datoDemograficoRepository.findWithZonaInfo();
    }

    public Optional<DatoDemografico> obtenerPorId(Long id) {
        return datoDemograficoRepository.findById(id);
    }

    public List<DatoDemografico> obtenerPorZona(Long zonaUrbanaId) {
        return datoDemograficoRepository.findByZonaUrbanaId(zonaUrbanaId);
    }

    @Transactional
    public DatoDemografico crear(DatoDemografico dato) {
        return datoDemograficoRepository.save(dato);
    }

    @Transactional
    public DatoDemografico actualizar(Long id, DatoDemografico dato) {
        Optional<DatoDemografico> existente = datoDemograficoRepository.findById(id);
        
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("No existe dato demográfico con ID: " + id);
        }

        dato.setDatoDemograficoId(id);
        
        // Mantener zona_urbana_id original (no se debe cambiar)
        dato.setZonaUrbanaId(existente.get().getZonaUrbanaId());
        
        int updated = datoDemograficoRepository.update(dato);
        
        if (updated == 0) {
            throw new RuntimeException("No se pudo actualizar el dato demográfico");
        }

        return datoDemograficoRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void eliminar(Long id) {
        Optional<DatoDemografico> existente = datoDemograficoRepository.findById(id);
        
        if (existente.isEmpty()) {
            throw new IllegalArgumentException("No existe dato demográfico con ID: " + id);
        }

        int deleted = datoDemograficoRepository.deleteById(id);
        
        if (deleted == 0) {
            throw new RuntimeException("No se pudo eliminar el dato demográfico");
        }
    }
}
