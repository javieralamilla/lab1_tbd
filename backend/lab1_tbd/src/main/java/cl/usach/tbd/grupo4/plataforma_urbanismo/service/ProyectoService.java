package cl.usach.tbd.grupo4.plataforma_urbanismo.service;

import cl.usach.tbd.grupo4.plataforma_urbanismo.model.ProyectoUrbano;
import cl.usach.tbd.grupo4.plataforma_urbanismo.repository.ProyectoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProyectoService {

    @Autowired
    private ProyectoRepository proyectoRepository;

    public List<ProyectoUrbano> obtenerTodos() {
        return proyectoRepository.findAll();
    }

    public Optional<ProyectoUrbano> obtenerPorId(Long id) {
        return proyectoRepository.findById(id);
    }

    public List<ProyectoUrbano> obtenerPorUsuario(Long usuarioId) {
        return proyectoRepository.findByUsuarioId(usuarioId);
    }

    public ProyectoUrbano crear(ProyectoUrbano proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public ProyectoUrbano actualizar(Long id, ProyectoUrbano proyecto) {
        Optional<ProyectoUrbano> existente = proyectoRepository.findById(id);
        if (existente.isEmpty()) {
            throw new RuntimeException("Proyecto no encontrado");
        }
        proyecto.setProyectoUrbanoId(id);
        proyectoRepository.update(proyecto);
        return proyecto;
    }

    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }

    // Consultas específicas
    public List<Map<String, Object>> obtenerSuperposicionProyectos() {
        return proyectoRepository.getSuperposicionProyectos();
    }

    public List<Map<String, Object>> obtenerResumenPorEstadoYZona() {
        return proyectoRepository.getResumenProyectosPorEstadoYZona();
    }

    public void actualizarProyectosRetrasados(Long usuarioId) {
        proyectoRepository.actualizarProyectosRetrasados(usuarioId);
    }
}