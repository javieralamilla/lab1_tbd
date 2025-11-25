-- ==================================================
--                     CONSULTAS 
-- ==================================================

-- 1) Cálculo de Densidad de Población (por zona, último dato disponible)
-- Devuelve nombre de la zona y densidad (personas / km2), ordenado desc
SELECT
  z.zona_urbana_id,
  z.nombre AS zona,
  d.poblacion,
  COALESCE(z.area_km2,
           ST_Area(ST_Transform(z.geometria_poligono, 32719)) / 1000000.0) AS area_km2,
  ROUND(
    (d.poblacion::numeric /
     NULLIF(COALESCE(z.area_km2, ST_Area(ST_Transform(z.geometria_poligono,32719))/1000000.0), 0)
    )::numeric, 2
  ) AS densidad_personas_por_km2
FROM zonas_urbanas z
JOIN LATERAL (
  SELECT poblacion, anio
  FROM datos_demograficos dd
  WHERE dd.zona_urbana_id = z.zona_urbana_id
  ORDER BY anio DESC
  LIMIT 1
) d ON TRUE
WHERE COALESCE(z.area_km2, ST_Area(ST_Transform(z.geometria_poligono,32719))/1000000.0) > 0
ORDER BY densidad_personas_por_km2 DESC;


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
-- Escuelas a menos de 500 metros de proyectos 'En Curso'
SELECT
  pi.punto_interes_id,
  pi.nombre AS escuela,
  pu.proyecto_urbano_id,
  pu.nombre AS proyecto,
  ROUND(
    ST_Distance(
      pi.coordenadas_punto::geography,
      ST_Centroid(pu.geometria)::geography
    )::numeric, 2
  ) AS distancia_metros
FROM puntos_interes pi
JOIN proyectos_urbanos pu
  ON ST_DWithin(
       pi.coordenadas_punto::geography,
       ST_Centroid(pu.geometria)::geography,
       500
     )
WHERE pi.tipo = 'Escuela'
  AND pu.estado = 'En Curso'
ORDER BY distancia_metros;


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



-- 5) Vista materializada: Cobertura de Infraestructura (parques/escuelas/hospitales)
CREATE MATERIALIZED VIEW IF NOT EXISTS cobertura_infraestructura_adicional AS
SELECT
  z.zona_urbana_id,
  z.nombre AS nombre_zona,
  COUNT(pi.punto_interes_id) FILTER (WHERE pi.tipo = 'Parque') AS total_parques,
  COUNT(pi.punto_interes_id) FILTER (WHERE pi.tipo = 'Escuela') AS total_escuelas,
  COUNT(pi.punto_interes_id) FILTER (WHERE pi.tipo = 'Hospital') AS total_hospitales,
  COUNT(pi.punto_interes_id) AS total_puntos_interes
FROM zonas_urbanas z
LEFT JOIN puntos_interes pi ON pi.zona_urbana_id = z.zona_urbana_id AND pi.activo = TRUE
GROUP BY z.zona_urbana_id, z.nombre;

CREATE INDEX IF NOT EXISTS idx_cobertura_adicional_zona ON cobertura_infraestructura_adicional(zona_urbana_id);


-- 6) Procedimiento: Simulación de Nuevo Desarrollo Habitacional
-- simular_crecimiento_poblacion(zona_id, nuevas_viviendas) usa factor promedio 3 personas/vivienda
CREATE OR REPLACE PROCEDURE simular_crecimiento_poblacion(
  p_zona_id INTEGER,
  p_nuevas_viviendas INTEGER
)
LANGUAGE plpgsql
AS $$
DECLARE
  v_factor NUMERIC := 3.0;
  v_incremento_poblacion INTEGER;
  v_anio_actual INTEGER;
  v_col_exists BOOLEAN;
  v_last_poblacion INTEGER := 0;
  v_last_num_viviendas INTEGER := 0;
BEGIN
  v_anio_actual := EXTRACT(YEAR FROM CURRENT_DATE)::integer;

  -- Comprobar si la columna factor_personas_vivienda existe en la tabla
  SELECT EXISTS(
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public' AND table_name = 'datos_demograficos' AND column_name = 'factor_personas_vivienda'
  ) INTO v_col_exists;

  IF v_col_exists THEN
    -- Si existe la columna, intentar obtener el factor histórico (si hay uno)
    SELECT COALESCE(factor_personas_vivienda, 3.0)::numeric INTO v_factor
    FROM datos_demograficos
    WHERE zona_urbana_id = p_zona_id
    ORDER BY anio DESC
    LIMIT 1;
  ELSE
    -- Si no existe la columna en el esquema, usar el factor por defecto
    v_factor := 3.0;
  END IF;

  -- Calcular incremento y redondear
  v_incremento_poblacion := ROUND(p_nuevas_viviendas * v_factor)::integer;

  -- Intentar actualizar registro del año actual
  UPDATE datos_demograficos
  SET
    poblacion = poblacion + v_incremento_poblacion,
    numero_viviendas = numero_viviendas + p_nuevas_viviendas
  WHERE zona_urbana_id = p_zona_id AND anio = v_anio_actual;

  -- Si no existía registro del año actual, preparar valores basales y crear nuevo registro
  IF NOT FOUND THEN
    SELECT poblacion, numero_viviendas INTO v_last_poblacion, v_last_num_viviendas
    FROM datos_demograficos
    WHERE zona_urbana_id = p_zona_id
    ORDER BY anio DESC
    LIMIT 1;

    IF v_last_poblacion IS NULL THEN
      v_last_poblacion := 0;
    END IF;
    IF v_last_num_viviendas IS NULL THEN
      v_last_num_viviendas := 0;
    END IF;

    IF v_col_exists THEN
      INSERT INTO datos_demograficos (zona_urbana_id, poblacion, numero_viviendas, factor_personas_vivienda, anio)
      VALUES (p_zona_id, v_last_poblacion + v_incremento_poblacion, v_last_num_viviendas + p_nuevas_viviendas, v_factor, v_anio_actual);
    ELSE
      INSERT INTO datos_demograficos (zona_urbana_id, poblacion, numero_viviendas, anio)
      VALUES (p_zona_id, v_last_poblacion + v_incremento_poblacion, v_last_num_viviendas + p_nuevas_viviendas, v_anio_actual);
    END IF;
  END IF;

  RAISE NOTICE 'Simulación completa: +% habitantes en zona % (nuevas viviendas %)', v_incremento_poblacion, p_zona_id, p_nuevas_viviendas;
END;
$$;


-- 7) Procedimiento: Actualización Masiva de Proyectos a 'Retrasado' por Usuario
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
    AND fecha_termino IS NOT NULL
    AND fecha_termino < CURRENT_DATE;

  GET DIAGNOSTICS v_count = ROW_COUNT;
  RAISE NOTICE 'Se actualizaron % proyectos a Retrasado para el usuario %', v_count, p_usuario_id;
END;
$$;


-- 8) Listado de Zonas sin Planificación Reciente (sin proyectos en últimos 2 años)
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


-- 9) Análisis de Superposición de Proyectos (pares que se superponen y área en m²)
SELECT
  p1.proyecto_urbano_id AS proj1_id,
  p1.nombre AS proyecto_1,
  p2.proyecto_urbano_id AS proj2_id,
  p2.nombre AS proyecto_2,
  ROUND(
    ST_Area(
      ST_Transform(
        ST_Intersection(p1.geometria, p2.geometria),
        32719
      )
    )::numeric, 2
  ) AS area_interseccion_m2
FROM proyectos_urbanos p1
JOIN proyectos_urbanos p2
  ON p1.proyecto_urbano_id < p2.proyecto_urbano_id
WHERE ST_Intersects(p1.geometria, p2.geometria)
  AND ST_Area(ST_Transform(ST_Intersection(p1.geometria, p2.geometria), 32719)) > 0
ORDER BY area_interseccion_m2 DESC;


-- 10) Vista materializada: Resumen de Proyectos por Estado y Tipo de Zona
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

