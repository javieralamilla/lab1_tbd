-- ==================================================
--                     CONSULTAS 
-- ==================================================

-- 1) Cálculo de Densidad de Población
-- Usar la vista densidad_poblacional_actual desde densidad_poblacional.sql
-- Ejemplo:
-- SELECT * FROM densidad_poblacional_actual ORDER BY densidad_poblacional DESC;


-- 2) Identificación de Zonas con Escasez de Servicios
-- Top 5 zonas: mayor población y menor conteo de hospitales
SELECT
  z.zona_urbana_id,
  z.nombre AS zona,
  d.poblacion,
  COUNT(pi.punto_interes_id) FILTER (WHERE pi.tipo = 'Hospital') AS hospitales
FROM zonas_urbanas z
JOIN LATERAL (
  SELECT poblacion
  FROM datos_demograficos dd
  WHERE dd.zona_urbana_id = z.zona_urbana_id
  ORDER BY anio DESC
  LIMIT 1
) d ON TRUE
LEFT JOIN puntos_interes pi
  ON pi.zona_urbana_id = z.zona_urbana_id
  AND pi.activo = TRUE  -- Solo contar puntos activos
WHERE d.poblacion > 1000  -- Filtrar zonas muy pequeñas
GROUP BY z.zona_urbana_id, z.nombre, d.poblacion
ORDER BY d.poblacion DESC, hospitales ASC
LIMIT 5;



-- 3) Análisis de Proximidad a Proyectos
-- Usar la vista escuelas_cerca_proyectos desde complemento.sql
-- Ejemplo:
-- SELECT * FROM escuelas_cerca_proyectos ORDER BY distancia_metros;


-- 4) Detección de Zonas en Rápido Crecimiento (>10% en últimos 5 años)
WITH datos AS (
  SELECT
    z.zona_urbana_id,
    z.nombre,
    (SELECT poblacion 
     FROM datos_demograficos dd 
     WHERE dd.zona_urbana_id = z.zona_urbana_id 
     ORDER BY anio DESC 
     LIMIT 1) AS poblacion_actual,
    (SELECT poblacion 
     FROM datos_demograficos dd 
     WHERE dd.zona_urbana_id = z.zona_urbana_id 
     AND dd.anio <= EXTRACT(YEAR FROM CURRENT_DATE)::integer - 5
     ORDER BY anio DESC 
     LIMIT 1) AS poblacion_hace_5,
    (SELECT anio 
     FROM datos_demograficos dd 
     WHERE dd.zona_urbana_id = z.zona_urbana_id 
     AND dd.anio <= EXTRACT(YEAR FROM CURRENT_DATE)::integer - 5
     ORDER BY anio DESC 
     LIMIT 1) AS anio_referencia
  FROM zonas_urbanas z
)
SELECT
  zona_urbana_id,
  nombre,
  anio_referencia,
  poblacion_hace_5,
  poblacion_actual,
  ROUND(
    ((poblacion_actual::numeric - poblacion_hace_5::numeric) / 
     NULLIF(poblacion_hace_5::numeric, 0)) * 100.0, 
    2
  ) AS porcentaje_crecimiento
FROM datos
WHERE poblacion_hace_5 IS NOT NULL
  AND poblacion_hace_5 > 0
  AND poblacion_actual > poblacion_hace_5
  AND ((poblacion_actual::numeric - poblacion_hace_5::numeric) / 
       poblacion_hace_5::numeric) * 100.0 > 10
ORDER BY porcentaje_crecimiento DESC
LIMIT 3;


-- 5) Cobertura de Infraestructura
-- Usar la vista materializada cobertura_infraestructura desde schema.sql
-- Ejemplo:
-- SELECT * FROM cobertura_infraestructura ORDER BY total_puntos_interes DESC;


-- 6) Procedimientos Almacenados
-- Ver procedimientos en complemento.sql:
-- - simular_crecimiento_poblacion(zona_id, nuevas_viviendas)
-- - actualizar_proyectos_retrasados(usuario_id)
-- - refrescar_cobertura_infraestructura()
-- - refrescar_resumen_proyectos()


-- 7) Listado de Zonas sin Planificación Reciente (sin proyectos en últimos 2 años)
SELECT
  z.zona_urbana_id,
  z.nombre,
  CASE
    WHEN MAX(pu.fecha_inicio) IS NULL THEN 'Ninguno'
    ELSE TO_CHAR(MAX(pu.fecha_inicio), 'YYYY-MM-DD')
  END AS fecha_ultimo_proyecto
FROM zonas_urbanas z
LEFT JOIN proyectos_zonas pz ON pz.zona_urbana_id = z.zona_urbana_id
LEFT JOIN proyectos_urbanos pu ON pu.proyecto_urbano_id = pz.proyecto_urbano_id
GROUP BY z.zona_urbana_id, z.nombre
HAVING MAX(pu.fecha_inicio) IS NULL OR MAX(pu.fecha_inicio) < (CURRENT_DATE - INTERVAL '2 years')
ORDER BY fecha_ultimo_proyecto NULLS FIRST, z.nombre;


-- 9) Análisis de Superposición de Proyectos
-- Usar la vista proyectos_superpuestos desde complemento.sql
-- Ejemplo:
-- SELECT * FROM proyectos_superpuestos ORDER BY area_superposicion_m2 DESC;


-- 8) Vista materializada: Resumen de Proyectos por Estado y Tipo de Zona
CREATE MATERIALIZED VIEW IF NOT EXISTS resumen_proyectos_estado_tipozona AS
SELECT
  zu.tipo_zona,
  pu.estado,
  COUNT(pu.proyecto_urbano_id) AS cantidad_proyectos
FROM zonas_urbanas zu
LEFT JOIN proyectos_zonas pz ON pz.zona_urbana_id = zu.zona_urbana_id
LEFT JOIN proyectos_urbanos pu ON pu.proyecto_urbano_id = pz.proyecto_urbano_id
GROUP BY zu.tipo_zona, pu.estado;

CREATE INDEX IF NOT EXISTS idx_resumen_tipo_estado_tipozona ON resumen_proyectos_estado_tipozona(tipo_zona, estado);

