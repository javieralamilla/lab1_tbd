package cl.usach.tbd.grupo4.plataforma_urbanismo.dto;

public class EstadisticasDTO {
    private Long totalProyectos;
    private Long totalPuntos;
    private Long totalZonas;
    private Long proyectosActivos;
    private Long proyectosEnProceso;
    private Long proyectosCompletados;
    private Long proyectosCancelados;

    // Constructores
    public EstadisticasDTO() {
    }

    public EstadisticasDTO(Long totalProyectos, Long totalPuntos, Long totalZonas,
                          Long proyectosActivos, Long proyectosEnProceso,
                          Long proyectosCompletados, Long proyectosCancelados) {
        this.totalProyectos = totalProyectos;
        this.totalPuntos = totalPuntos;
        this.totalZonas = totalZonas;
        this.proyectosActivos = proyectosActivos;
        this.proyectosEnProceso = proyectosEnProceso;
        this.proyectosCompletados = proyectosCompletados;
        this.proyectosCancelados = proyectosCancelados;
    }

    // Getters y Setters
    public Long getTotalProyectos() {
        return totalProyectos;
    }

    public void setTotalProyectos(Long totalProyectos) {
        this.totalProyectos = totalProyectos;
    }

    public Long getTotalPuntos() {
        return totalPuntos;
    }

    public void setTotalPuntos(Long totalPuntos) {
        this.totalPuntos = totalPuntos;
    }

    public Long getTotalZonas() {
        return totalZonas;
    }

    public void setTotalZonas(Long totalZonas) {
        this.totalZonas = totalZonas;
    }

    public Long getProyectosActivos() {
        return proyectosActivos;
    }

    public void setProyectosActivos(Long proyectosActivos) {
        this.proyectosActivos = proyectosActivos;
    }

    public Long getProyectosEnProceso() {
        return proyectosEnProceso;
    }

    public void setProyectosEnProceso(Long proyectosEnProceso) {
        this.proyectosEnProceso = proyectosEnProceso;
    }

    public Long getProyectosCompletados() {
        return proyectosCompletados;
    }

    public void setProyectosCompletados(Long proyectosCompletados) {
        this.proyectosCompletados = proyectosCompletados;
    }

    public Long getProyectosCancelados() {
        return proyectosCancelados;
    }

    public void setProyectosCancelados(Long proyectosCancelados) {
        this.proyectosCancelados = proyectosCancelados;
    }
}

