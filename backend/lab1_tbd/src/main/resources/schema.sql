-- ==================================================
-- LIMPIEZA DE BASE DE DATOS (IDEMPOTENCIA)
-- ==================================================
-- Borrar triggers (dependen de tablas y funciones)
DROP TRIGGER IF EXISTS trigger_validar_proyecto ON proyectos_urbanos;
DROP TRIGGER IF EXISTS trigger_actualizar_densidad ON datos_demograficos;

-- Borrar funciones
DROP FUNCTION IF EXISTS validar_proyecto_insert();
DROP FUNCTION IF EXISTS actualizar_densidad();

-- Borrar procedimientos
DROP PROCEDURE IF EXISTS simular_crecimiento_poblacion(INTEGER, INTEGER);
DROP PROCEDURE IF EXISTS actualizar_proyectos_retrasados(INTEGER);

-- Borrar vistas materializadas
DROP MATERIALIZED VIEW IF EXISTS cobertura_infraestructura;
DROP MATERIALIZED VIEW IF EXISTS resumen_proyectos_estado_zona;

-- Borrar tablas (en orden inverso de dependencia)
-- 1. Borrar la tabla de unión N:M
DROP TABLE IF EXISTS proyectos_zonas;

-- 2. Borrar tablas que dependen de 'usuarios' y 'zonas_urbanas'
DROP TABLE IF EXISTS proyectos_urbanos;
DROP TABLE IF EXISTS puntos_interes;
DROP TABLE IF EXISTS datos_demograficos;

-- 3. Borrar tablas "padre"
DROP TABLE IF EXISTS usuarios;
DROP TABLE IF EXISTS zonas_urbanas;

-- ==================================================
-- FIN DE LA LIMPIEZA
-- ==================================================


-- ==================================================
-- SCHEMA DE BASE DE DATOS - PLATAFORMA URBANISMO
-- ==================================================

-- Extensión para tipos geométricos
CREATE EXTENSION IF NOT EXISTS postgis;

-- ==================================================
-- TABLA: usuarios
-- ==================================================
CREATE TABLE usuarios (
                          usuario_id SERIAL PRIMARY KEY,
                          nombre VARCHAR(100) NOT NULL,
                          apellido VARCHAR(100) NOT NULL,
                          email VARCHAR(150) UNIQUE NOT NULL,
                          contrasena VARCHAR(255) NOT NULL, -- Hash BCrypt
                          rol VARCHAR(50) NOT NULL CHECK (rol IN ('planificador', 'administrador', 'autoridad_municipal')),
                          fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          activo BOOLEAN DEFAULT TRUE
);

-- ==================================================
-- TABLA: zonas_urbanas
-- ==================================================
CREATE TABLE zonas_urbanas (
                               zona_urbana_id SERIAL PRIMARY KEY,
                               nombre VARCHAR(100) NOT NULL,
                               geometria_poligono GEOMETRY(POLYGON, 4326) NOT NULL,
                               tipo_zona VARCHAR(50) NOT NULL CHECK (tipo_zona IN ('Residencial', 'Comercial', 'Industrial', 'Mixto')),
                               area_km2 DECIMAL(10, 2),
                               fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==================================================
-- TABLA: puntos_interes
-- ==================================================
CREATE TABLE puntos_interes (
                                punto_interes_id SERIAL PRIMARY KEY,
                                nombre VARCHAR(150) NOT NULL,
                                tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('Hospital', 'Escuela', 'Parque', 'Centro Comercial', 'Transporte')),
                                coordenadas_punto GEOMETRY(POINT, 4326) NOT NULL,
                                direccion VARCHAR(255),
                                zona_urbana_id INTEGER NOT NULL,
                                activo BOOLEAN DEFAULT TRUE,
                                fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                FOREIGN KEY (zona_urbana_id) REFERENCES zonas_urbanas(zona_urbana_id) ON DELETE CASCADE
);

-- ==================================================
-- TABLA: datos_demograficos
-- ==================================================
CREATE TABLE datos_demograficos (
                                    dato_demografico_id SERIAL PRIMARY KEY,
                                    zona_urbana_id INTEGER NOT NULL,
                                    poblacion INTEGER NOT NULL CHECK (poblacion >= 0),
                                    densidad_poblacion DECIMAL(10, 2),
                                    edad_promedio DECIMAL(5, 2),
                                    numero_viviendas INTEGER DEFAULT 0,
                                    factor_personas_vivienda DECIMAL(4, 2) DEFAULT 3.0,
                                    anio INTEGER NOT NULL,
                                    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    FOREIGN KEY (zona_urbana_id) REFERENCES zonas_urbanas(zona_urbana_id) ON DELETE CASCADE,
                                    UNIQUE (zona_urbana_id, anio)
);

-- ==================================================
-- TABLA: proyectos_urbanos
-- ==================================================
CREATE TABLE proyectos_urbanos (
                                   proyecto_urbano_id SERIAL PRIMARY KEY,
                                   nombre VARCHAR(150) NOT NULL,
                                   descripcion TEXT,
                                   tipo_proyecto VARCHAR(100),
                                   fecha_inicio DATE NOT NULL,
                                   fecha_termino DATE,
                                   estado VARCHAR(50) NOT NULL CHECK (estado IN ('Planeado', 'En Curso', 'Completado', 'Retrasado', 'Cancelado')),
                                   presupuesto DECIMAL(15, 2),
                                   geometria GEOMETRY(GEOMETRY, 4326),
                                   usuario_id INTEGER NOT NULL,
                                   fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE RESTRICT
);

-- ==================================================
-- TABLA: proyectos_zonas (Relación N:M)
-- ==================================================
CREATE TABLE proyectos_zonas (
                                 proyecto_zona_id SERIAL PRIMARY KEY,
                                 proyecto_urbano_id INTEGER NOT NULL,
                                 zona_urbana_id INTEGER NOT NULL,
                                 area_superposicion DECIMAL(10, 2),
                                 fecha_asignacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (proyecto_urbano_id) REFERENCES proyectos_urbanos(proyecto_urbano_id) ON DELETE CASCADE,
                                 FOREIGN KEY (zona_urbana_id) REFERENCES zonas_urbanas(zona_urbana_id) ON DELETE CASCADE,
                                 UNIQUE (proyecto_urbano_id, zona_urbana_id)
);

-- ==================================================
-- ÍNDICES PARA OPTIMIZACIÓN
-- ==================================================

-- Índices espaciales
CREATE INDEX idx_zonas_geometria ON zonas_urbanas USING GIST(geometria_poligono);
CREATE INDEX idx_puntos_coordenadas ON puntos_interes USING GIST(coordenadas_punto);
CREATE INDEX idx_proyectos_geometria ON proyectos_urbanos USING GIST(geometria);

-- Índices para búsquedas frecuentes
CREATE INDEX idx_usuarios_email ON usuarios(email);
CREATE INDEX idx_puntos_tipo ON puntos_interes(tipo);
CREATE INDEX idx_puntos_zona ON puntos_interes(zona_urbana_id);
CREATE INDEX idx_datos_demo_zona ON datos_demograficos(zona_urbana_id);
CREATE INDEX idx_datos_demo_anio ON datos_demograficos(anio);
CREATE INDEX idx_proyectos_estado ON proyectos_urbanos(estado);
CREATE INDEX idx_proyectos_usuario ON proyectos_urbanos(usuario_id);
CREATE INDEX idx_proyectos_fecha ON proyectos_urbanos(fecha_termino);

-- ==================================================
-- VISTA MATERIALIZADA: cobertura_infraestructura
-- ==================================================
CREATE MATERIALIZED VIEW cobertura_infraestructura AS
SELECT
    zu.zona_urbana_id,
    zu.nombre AS nombre_zona,
    COUNT(CASE WHEN pi.tipo = 'Parque' THEN 1 END) AS total_parques,
    COUNT(CASE WHEN pi.tipo = 'Escuela' THEN 1 END) AS total_escuelas,
    COUNT(CASE WHEN pi.tipo = 'Hospital' THEN 1 END) AS total_hospitales,
    COUNT(pi.punto_interes_id) AS total_puntos_interes
FROM zonas_urbanas zu
         LEFT JOIN puntos_interes pi ON zu.zona_urbana_id = pi.zona_urbana_id AND pi.activo = TRUE
GROUP BY zu.zona_urbana_id, zu.nombre;

CREATE UNIQUE INDEX idx_cobertura_zona ON cobertura_infraestructura(zona_urbana_id);

-- ==================================================
-- VISTA MATERIALIZADA: resumen_proyectos_estado_zona
-- ==================================================
CREATE MATERIALIZED VIEW resumen_proyectos_estado_zona AS
SELECT
    zu.zona_urbana_id,
    zu.tipo_zona,
    pu.estado AS estado_proyecto,
    COUNT(pu.proyecto_urbano_id) AS cantidad_proyectos,
    SUM(pu.presupuesto) AS presupuesto_total
FROM zonas_urbanas zu
         LEFT JOIN proyectos_zonas pz ON zu.zona_urbana_id = pz.zona_urbana_id
         LEFT JOIN proyectos_urbanos pu ON pz.proyecto_urbano_id = pu.proyecto_urbano_id
GROUP BY zu.zona_urbana_id, zu.tipo_zona, pu.estado;

CREATE INDEX idx_resumen_tipo_estado ON resumen_proyectos_estado_zona(tipo_zona, estado_proyecto);

-- ==================================================
-- TRIGGER: Validar inserción de proyectos
-- ==================================================
CREATE OR REPLACE FUNCTION validar_proyecto_insert()
RETURNS TRIGGER AS $$
BEGIN
    -- Comprueba si la fecha de término existe y es anterior a la de inicio
    IF NEW.fecha_termino IS NOT NULL AND NEW.fecha_termino < NEW.fecha_inicio THEN
        RAISE EXCEPTION 'La fecha de término debe ser posterior a la fecha de inicio';
END IF;

    -- Si la validación pasa, permite que la inserción continúe
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_validar_proyecto
    BEFORE INSERT OR UPDATE ON proyectos_urbanos
                         FOR EACH ROW
                         EXECUTE FUNCTION validar_proyecto_insert();

-- ==================================================
-- TRIGGER: Actualizar densidad automáticamente
-- ==================================================
CREATE OR REPLACE FUNCTION actualizar_densidad()
RETURNS TRIGGER AS $$
DECLARE
v_area_km2 DECIMAL(10, 2);
BEGIN
    -- Obtener área de la zona
SELECT area_km2 INTO v_area_km2
FROM zonas_urbanas
WHERE zona_urbana_id = NEW.zona_urbana_id;

-- Calcular densidad
IF v_area_km2 IS NOT NULL AND v_area_km2 > 0 THEN
        NEW.densidad_poblacion := NEW.poblacion / v_area_km2;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_actualizar_densidad
    BEFORE INSERT OR UPDATE ON datos_demograficos
                         FOR EACH ROW
                         EXECUTE FUNCTION actualizar_densidad();

-- ==================================================
-- PROCEDIMIENTO: simular_crecimiento_poblacion
-- ==================================================
CREATE OR REPLACE PROCEDURE simular_crecimiento_poblacion(
    p_zona_id INTEGER,
    p_nuevas_viviendas INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
v_factor DECIMAL(4, 2);
    v_incremento_poblacion INTEGER;
    v_anio_actual INTEGER;
BEGIN
    -- Obtener año actual
    v_anio_actual := EXTRACT(YEAR FROM CURRENT_DATE);

    -- Obtener factor de personas por vivienda (usar el más reciente o 3.0 por defecto)
SELECT COALESCE(factor_personas_vivienda, 3.0) INTO v_factor
FROM datos_demograficos
WHERE zona_urbana_id = p_zona_id
ORDER BY anio DESC
    LIMIT 1;

-- Calcular incremento
v_incremento_poblacion := p_nuevas_viviendas * v_factor;

    -- Actualizar población del año actual
UPDATE datos_demograficos
SET
    poblacion = poblacion + v_incremento_poblacion,
    numero_viviendas = numero_viviendas + p_nuevas_viviendas
WHERE zona_urbana_id = p_zona_id AND anio = v_anio_actual;

-- Si no existe registro para el año actual, crear uno
IF NOT FOUND THEN
        INSERT INTO datos_demograficos (zona_urbana_id, poblacion, numero_viviendas, factor_personas_vivienda, anio)
SELECT p_zona_id,
       COALESCE(d.poblacion, 0) + v_incremento_poblacion,
       COALESCE(d.numero_viviendas, 0) + p_nuevas_viviendas,
       v_factor,
       v_anio_actual
FROM (SELECT poblacion, numero_viviendas
      FROM datos_demograficos
      WHERE zona_urbana_id = p_zona_id
      ORDER BY anio DESC
          LIMIT 1) d;
END IF;

    RAISE NOTICE 'Población actualizada: +% habitantes en zona %', v_incremento_poblacion, p_zona_id;
END;
$$;

-- ==================================================
-- PROCEDIMIENTO: actualizar_proyectos_retrasados
-- ==================================================
CREATE OR REPLACE PROCEDURE actualizar_proyectos_retrasados(
    p_usuario_id INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
v_count INTEGER;
BEGIN
UPDATE proyectos_urbanos
SET estado = 'Retrasado'
WHERE usuario_id = p_usuario_id
  AND estado = 'En Curso'
  AND fecha_termino < CURRENT_DATE;

GET DIAGNOSTICS v_count = ROW_COUNT;

RAISE NOTICE 'Se actualizaron % proyectos a estado Retrasado', v_count;
END;
$$;