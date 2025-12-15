-- ==================================================
-- CONSULTAS SQL - PLATAFORMA URBANISMO
-- ==================================================
-- Este archivo contiene todas las consultas SQL requeridas para el análisis
-- de datos urbanos y demográficos de la plataforma.

-- ==================================================
-- 1) Cálculo de Densidad de Población
-- ==================================================
-- Calcula la densidad de población (habitantes/km²) para cada zona urbana.
-- Devuelve el nombre de la zona y su densidad, ordenado de mayor a menor.
SELECT
    zu.nombre AS zona,
    dd.poblacion,
    zu.area_km2,
    ROUND(dd.poblacion / NULLIF(zu.area_km2, 0), 2) AS densidad_poblacional
FROM 
    zonas_urbanas zu
    INNER JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
WHERE 
    dd.año = (SELECT MAX(año) FROM datos_demograficos WHERE zona_urbana_id = zu.zona_urbana_id)
    AND zu.area_km2 IS NOT NULL
    AND zu.area_km2 > 0
ORDER BY 
    densidad_poblacional DESC;


-- ==================================================
-- 2) Identificación de Zonas con Escasez de Servicios
-- ==================================================
-- Identifica las 5 zonas urbanas con mayor población pero menor cantidad 
-- de hospitales. Devuelve nombre de zona, población y conteo de hospitales.
-- Parámetro: año (por defecto 2024)
SELECT
    z.nombre AS zona,
    dd.poblacion,
    COUNT(pi.punto_interes_id) AS cantidad_hospitales
FROM 
    zonas_urbanas z
    INNER JOIN datos_demograficos dd ON z.zona_urbana_id = dd.zona_urbana_id
    LEFT JOIN puntos_interes pi ON ST_Contains(z.geometria_poligono, pi.coordenadas_punto)
        AND pi.tipo = 'Hospital' 
        AND pi.activo = TRUE
WHERE 
    dd.año = 2024
    AND dd.poblacion > 1000
GROUP BY 
    z.zona_urbana_id, z.nombre, dd.poblacion
ORDER BY 
    dd.poblacion DESC, cantidad_hospitales ASC
LIMIT 5;


-- ==================================================
-- 3) Análisis de Proximidad a Proyectos
-- ==================================================
-- Encuentra todas las escuelas a menos de 500 metros de proyectos 'En Curso'.
-- Devuelve nombre de escuela, nombre de proyecto y distancia en metros.

SELECT
    pi_escuela.nombre AS escuela,
    p.nombre AS proyecto,
    ROUND(
        ST_Distance(
            pi_escuela.coordenadas_punto::geography,
            p.coordenadas::geography
        )::numeric,
        2
    ) AS distancia_metros
FROM 
    puntos_interes pi_escuela
    INNER JOIN proyectos p
        ON ST_DWithin(
            pi_escuela.coordenadas_punto::geography,
            p.coordenadas::geography,
            500
        )
WHERE
    pi_escuela.tipo = 'Escuela'
    AND pi_escuela.activo = TRUE
    AND p.estado = 'En Curso'
ORDER BY
    distancia_metros ASC;
-- ==================================================
-- 4) Detección de Zonas en Rápido Crecimiento
-- ==================================================
-- Identifica las 3 zonas cuya población creció más del 10% en los últimos 5 años.
-- Muestra nombre de zona y porcentaje de crecimiento.

WITH poblacion_anios AS (
    SELECT
        zu.zona_urbana_id,
        zu.nombre,
        dd.anio,
        dd.poblacion,
        MAX(dd.anio) OVER (PARTITION BY zu.zona_urbana_id) AS anio_actual
    FROM
        zonas_urbanas zu
        INNER JOIN datos_demograficos dd
            ON zu.zona_urbana_id = dd.zona_urbana_id
),
crecimiento AS (
    SELECT
        zona_urbana_id,
        nombre,
        MAX(CASE WHEN anio = anio_actual THEN poblacion END) AS poblacion_actual,
        MAX(CASE WHEN anio = anio_actual - 5 THEN poblacion END) AS poblacion_5_anios
    FROM
        poblacion_anios
    GROUP BY
        zona_urbana_id, nombre
)
SELECT
    nombre AS zona,
    ROUND(
        ((poblacion_actual - poblacion_5_anios)::numeric / poblacion_5_anios) * 100,
        2
    ) AS porcentaje_crecimiento
FROM
    crecimiento
WHERE
    poblacion_5_anios IS NOT NULL
    AND poblacion_actual > poblacion_5_anios
    AND ((poblacion_actual - poblacion_5_anios)::numeric / poblacion_5_anios) > 0.10
ORDER BY
    porcentaje_crecimiento DESC
LIMIT 3;

-- ==================================================
-- 5) Análisis de Cobertura de Infraestructura
-- ==================================================
-- Vista materializada que muestra el conteo de parques, escuelas y hospitales
-- por cada zona urbana. Debe actualizarse semanalmente.
-- Nota: Esta vista ya está definida en schema.sql como 'cobertura_infraestructura'
-- Para consultarla de la manera mas básica.
-- 5.1) Ver toda la cobertura de infraestrutura por zona
SELECT
	zona_urbana_id,
	nombre_zona,
	total_parques,
	total_escuelas,
	total_hospitales,
	total_puntos_interes
FROM
	cobertura_infraestructura
ORDER BY
	nombre_zona;

-- 5.2) Zonas con mayor cantidad de infraestructura total
SELECT
	nombre_zona,
	total_parques,
	total_parques,
	total_hospitales,
    total_puntos_interes
FROM 
    cobertura_infraestructura
ORDER BY 
    total_puntos_interes DESC
LIMIT 10;

-- 5.3)  Zonas con deficit de infraestructura (pocas escuelas u hospitales)
SELECT 
    nombre_zona,
    total_escuelas,
    total_hospitales,
    (total_escuelas + total_hospitales) AS servicios_esenciales
FROM 
    cobertura_infraestructura
WHERE 
    total_escuelas < 3 OR total_hospitales < 1
ORDER BY 
    servicios_esenciales ASC;

-- 5.4) Resumen estadistico de cobertura
SELECT 
    COUNT(*) AS total_zonas,
    SUM(total_parques) AS parques_totales,
    SUM(total_escuelas) AS escuelas_totales,
    SUM(total_hospitales) AS hospitales_totales,
    ROUND(AVG(total_puntos_interes), 2) AS promedio_puntos_por_zona
FROM 
    cobertura_infraestructura;

-- ==================================================
-- 6) Simulación de Nuevo Desarrollo Habitacional
-- ==================================================
-- Procedimiento almacenado que simula crecimiento poblacional basado en nuevas viviendas.
-- Recibe: zona_id, número de nuevas viviendas
-- Factor: 3 personas por vivienda
--
-- El procedimiento 'simular_crecimiento_poblacion' ya esta definido en schema.sql.
-- Logica del procedimiento:
--   1. Obtiene el factor de personas por vivienda de la zona (default: 3.0)
--   2. Calcula incremento = nuevas_viviendas * factor
--   3. Actualiza poblacion y numero_viviendas en datos_demograficos
--   4. Si no existe registro para la zona, crea uno nuevo

-- 6.1) Ejecutar simulacion: Agregar 100 viviendas a la zona 1
CALL simular_crecimiento_poblacion(1, 100);

-- 6.2) Ejecutar simulacion: Agregar 50 viviendas a la zona 5
CALL simular_crecimiento_poblacion(5, 50);

-- 6.3) Ejecutar simulacion: Agregar 200 viviendas a la zona 10
CALL simular_crecimiento_poblacion(10, 200);

-- 6.4) Verificar el resultado despues de ejecutar la simulacion
-- (Consulta para ver los datos demograficos actualizados)
SELECT 
    dd.zona_urbana_id,
    zu.nombre AS nombre_zona,
    dd.poblacion,
    dd.numero_viviendas,
    dd.factor_personas_vivienda,
    dd.año
FROM 
    datos_demograficos dd
    INNER JOIN zonas_urbanas zu ON dd.zona_urbana_id = zu.zona_urbana_id
WHERE 
    dd.zona_urbana_id IN (1, 5, 10)
ORDER BY 
    dd.zona_urbana_id, dd.año DESC;

-- 6.5) Ejemplo de uso con zona especifica por nombre
-- Primero obtener el ID de la zona
SELECT zona_urbana_id, nombre FROM zonas_urbanas WHERE nombre ILIKE '%providencia%';
-- Luego ejecutar (reemplazar X por el ID obtenido):
CALL simular_crecimiento_poblacion(35, 150);

-- ==================================================
-- 7) Actualización Masiva de Proyectos
-- ==================================================
-- Procedimiento almacenado que actualiza proyectos retrasados de un usuario.
-- Cambia estado a 'Retrasado' para proyectos que pasaron su fecha límite.
--
-- El procedimiento 'actualizar_proyectos_retrasados' ya esta definido en schema.sql.
-- Logica del procedimiento:
--   1. Busca proyectos del usuario con estado = 'En Curso'
--   2. Filtra los que tienen fecha_termino < fecha actual
--   3. Actualiza su estado a 'Retrasado'
--   4. Muestra cuantos proyectos fueron actualizados

-- 7.1) Ejecutar actualizacion para el usuario con ID 1
CALL actualizar_proyectos_retrasados(1);

-- 7.2) Ejecutar actualizacion para el usuario con ID 2
CALL actualizar_proyectos_retrasados(2);

-- 7.3) Verificar proyectos retrasados despues de la actualizacion
SELECT 
    pu.proyecto_urbano_id,
    pu.nombre AS nombre_proyecto,
    pu.estado,
    pu.fecha_inicio,
    pu.fecha_termino,
    u.nombre AS nombre_usuario,
    u.email
FROM 
    proyectos_urbanos pu
    INNER JOIN usuarios u ON pu.usuario_id = u.usuario_id
WHERE 
    pu.estado = 'Retrasado'
ORDER BY 
    pu.fecha_termino DESC;

-- 7.4) Consulta previa: Ver proyectos que DEBERIAN marcarse como retrasados
-- (Usar antes de ejecutar el procedimiento para ver que se va a actualizar)
SELECT 
    pu.proyecto_urbano_id,
    pu.nombre AS nombre_proyecto,
    pu.estado AS estado_actual,
    pu.fecha_termino,
    CURRENT_DATE AS fecha_actual,
    (CURRENT_DATE - pu.fecha_termino) AS dias_de_retraso,
    u.nombre AS nombre_usuario
FROM 
    proyectos_urbanos pu
    INNER JOIN usuarios u ON pu.usuario_id = u.usuario_id
WHERE 
    pu.estado = 'En Curso'
    AND pu.fecha_termino < CURRENT_DATE
ORDER BY 
    dias_de_retraso DESC;

-- 7.5) Resumen de proyectos por estado y usuario
SELECT 
    u.nombre AS usuario,
    pu.estado,
    COUNT(*) AS cantidad_proyectos
FROM 
    proyectos_urbanos pu
    INNER JOIN usuarios u ON pu.usuario_id = u.usuario_id
GROUP BY 
    u.nombre, pu.estado
ORDER BY 
    u.nombre, pu.estado;
-- ==================================================
-- 8) Listado de Zonas sin Planificación Reciente
-- ==================================================
-- Muestra zonas sin proyectos asociados en los últimos 2 años.
-- Incluye nombre de zona y fecha del último proyecto o 'Ninguno'.

-- Consulta principal: Zonas sin planificación reciente
-- IMPORTANTE: Se usa LEFT JOIN para incluir TODAS las zonas, incluso las que nunca tuvieron proyectos
SELECT
    zu.zona_urbana_id,
    zu.nombre AS zona,
    zu.tipo_zona,
    COALESCE(
        TO_CHAR(MAX(pu.fecha_inicio), 'DD/MM/YYYY'),
        'Ninguno'
    ) AS ultimo_proyecto,
    MAX(pu.fecha_inicio) AS fecha_ultimo_proyecto,
    CASE
        WHEN MAX(pu.fecha_inicio) IS NULL THEN 'Sin proyectos'
        WHEN MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years' THEN 'Sin planificación reciente'
        ELSE 'Con planificación reciente'
    END AS estado_planificacion,
    CASE
        WHEN MAX(pu.fecha_inicio) IS NOT NULL
        THEN EXTRACT(YEAR FROM AGE(CURRENT_DATE, MAX(pu.fecha_inicio)))::INTEGER
        ELSE NULL
    END AS anios_sin_proyecto
FROM zonas_urbanas zu
LEFT JOIN proyectos_zonas pz ON zu.zona_urbana_id = pz.zona_urbana_id
LEFT JOIN proyectos_urbanos pu ON pz.proyecto_urbano_id = pu.proyecto_urbano_id
GROUP BY zu.zona_urbana_id, zu.nombre, zu.tipo_zona
HAVING MAX(pu.fecha_inicio) IS NULL
   OR MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years'
ORDER BY
    CASE WHEN MAX(pu.fecha_inicio) IS NULL THEN 1 ELSE 0 END,
    MAX(pu.fecha_inicio) ASC NULLS FIRST;

-- Variante: Incluir estadísticas adicionales
SELECT
    zu.nombre AS zona,
    zu.tipo_zona,
    COALESCE(TO_CHAR(MAX(pu.fecha_inicio), 'DD/MM/YYYY'), 'Ninguno') AS ultimo_proyecto,
    COUNT(DISTINCT pu.proyecto_urbano_id) AS total_proyectos_historicos,
    COUNT(DISTINCT CASE
        WHEN pu.fecha_inicio >= CURRENT_DATE - INTERVAL '2 years'
        THEN pu.proyecto_urbano_id
    END) AS proyectos_ultimos_2_anios,
    CASE
        WHEN MAX(pu.fecha_inicio) IS NULL THEN 'Nunca tuvo proyectos'
        WHEN MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years' THEN
            CONCAT(
                EXTRACT(YEAR FROM AGE(CURRENT_DATE, MAX(pu.fecha_inicio)))::INTEGER,
                ' años sin planificación'
            )
        ELSE 'Activo'
    END AS estado
FROM zonas_urbanas zu
LEFT JOIN proyectos_zonas pz ON zu.zona_urbana_id = pz.zona_urbana_id
LEFT JOIN proyectos_urbanos pu ON pz.proyecto_urbano_id = pu.proyecto_urbano_id
GROUP BY zu.zona_urbana_id, zu.nombre, zu.tipo_zona
HAVING
    MAX(pu.fecha_inicio) IS NULL
    OR MAX(pu.fecha_inicio) < CURRENT_DATE - INTERVAL '2 years'
ORDER BY
    MAX(pu.fecha_inicio) ASC NULLS FIRST;



-- ==================================================
-- 9) Análisis de Superposición de Proyectos
-- ==================================================
-- Identifica pares de proyectos urbanos que se superponen geográficamente.
-- Devuelve nombres de ambos proyectos y área de superposición en m².
-- Versión robusta: maneja SRID nulo, valida geometrías, proyecta a metros,
-- convierte líneas/puntos a áreas con buffer y suma solo polígonos de la intersección.

WITH params AS (
    SELECT 5.0 AS buffer_m -- buffer en metros para convertir líneas/puntos a área
), preparados AS (
    SELECT
        p.proyecto_urbano_id,
        p.nombre,
        p.estado,
        p.tipo_proyecto,
        -- asegurar SRID válido: si es 0/null asumimos EPSG:4326 (ajustar si su fuente usa otro SRID)
        ST_SetSRID(ST_MakeValid(p.geometria), COALESCE(NULLIF(ST_SRID(p.geometria), 0), 4326)) AS geom_valid_srid
    FROM proyectos_urbanos p
    WHERE p.geometria IS NOT NULL
), proyectados AS (
    SELECT
        pr.*,
        GeometryType(pr.geom_valid_srid) AS geom_type,
        -- proyectar a SRID en metros (ej. 32719) para calcular áreas en m²
        ST_Transform(pr.geom_valid_srid, 32719) AS geom_proj
    FROM preparados pr
), shapes AS (
    SELECT
        proyecto_urbano_id,
        nombre,
        estado,
        tipo_proyecto,
        geom_type,
        -- si es LINESTRING o POINT, aplicar buffer en metros sobre la geometría proyectada
        CASE
            WHEN geom_type ILIKE '%LINESTRING%' THEN ST_Multi(ST_Buffer(geom_proj, (SELECT buffer_m FROM params)))
            WHEN geom_type ILIKE '%POINT%' THEN ST_Multi(ST_Buffer(geom_proj, (SELECT buffer_m FROM params)))
            ELSE ST_Multi(geom_proj)
        END AS geom_area
    FROM proyectados
)
SELECT
    s1.nombre AS proyecto_1,
    s2.nombre AS proyecto_2,
    s1.estado AS estado_proyecto_1,
    s2.estado AS estado_proyecto_2,
    s1.geom_type AS tipo_geometria_1,
    s2.geom_type AS tipo_geometria_2,
    ROUND(COALESCE(intersec.inter_area, 0)::NUMERIC, 2) AS area_superposicion_m2,
    ROUND(ST_Distance(s1.geom_area, s2.geom_area)::NUMERIC, 2) AS distancia_minima_m
FROM shapes s1
JOIN shapes s2 ON s1.proyecto_urbano_id < s2.proyecto_urbano_id
CROSS JOIN LATERAL (
    -- descomponer la intersección y sumar solo las piezas poligonales (POLYGON / MULTIPOLYGON)
    SELECT COALESCE(SUM(CASE WHEN GeometryType((d).geom) IN ('POLYGON','MULTIPOLYGON') THEN ST_Area((d).geom) ELSE 0 END), 0) AS inter_area
    FROM (SELECT ST_Dump(ST_Intersection(s1.geom_area, s2.geom_area)) AS d) AS dump_tbl
) AS intersec
WHERE intersec.inter_area > 0
ORDER BY intersec.inter_area DESC;

-- Variante: porcentaje de superposición respecto al primer proyecto y filtrado por activos
SELECT
    s1.nombre AS proyecto_1,
    s2.nombre AS proyecto_2,
    ROUND(intersec.inter_area::NUMERIC, 2) AS area_superposicion_m2,
    ROUND((intersec.inter_area / NULLIF(ST_Area(s1.geom_area),0) * 100)::NUMERIC, 2) AS porcentaje_superposicion_p1
FROM shapes s1
JOIN shapes s2 ON s1.proyecto_urbano_id < s2.proyecto_urbano_id
CROSS JOIN LATERAL (
    SELECT COALESCE(SUM(CASE WHEN GeometryType((d).geom) IN ('POLYGON','MULTIPOLYGON') THEN ST_Area((d).geom) ELSE 0 END), 0) AS inter_area
    FROM (SELECT ST_Dump(ST_Intersection(s1.geom_area, s2.geom_area)) AS d) AS dump_tbl
) AS intersec
WHERE intersec.inter_area > 0
  AND s1.estado NOT IN ('Cancelado')
  AND s2.estado NOT IN ('Cancelado')
ORDER BY intersec.inter_area DESC;


-- ==================================================
-- 10) Resumen de Proyectos por Estado y Tipo de Zona
-- ==================================================
-- Vista materializada que muestra cantidad de proyectos por estado y tipo de zona.
-- Debe refrescarse concurrentemente para consultas rápidas en informes.
-- Nota: Esta vista ya está definida en schema.sql como 'resumen_proyectos_estado_zona'
-- Para consultarla:

-- Consulta general del resumen de proyectos por estado y tipo de zona
SELECT
    tipo_zona,
    estado_proyecto,
    cantidad_proyectos,
    presupuesto_total
FROM
    resumen_proyectos_estado_zona
WHERE
    estado_proyecto IS NOT NULL
ORDER BY
    tipo_zona, estado_proyecto;

-- Resumen agregado por tipo de zona (total de proyectos por zona)
SELECT
    tipo_zona,
    SUM(cantidad_proyectos) AS total_proyectos,
    SUM(presupuesto_total) AS presupuesto_total_zona
FROM
    resumen_proyectos_estado_zona
WHERE
    estado_proyecto IS NOT NULL
GROUP BY
    tipo_zona
ORDER BY
    total_proyectos DESC;

-- Resumen agregado por estado (total de proyectos por estado)
SELECT
    estado_proyecto,
    SUM(cantidad_proyectos) AS total_proyectos,
    SUM(presupuesto_total) AS presupuesto_total_estado
FROM
    resumen_proyectos_estado_zona
WHERE
    estado_proyecto IS NOT NULL
GROUP BY
    estado_proyecto
ORDER BY
    total_proyectos DESC;

-- Filtrar por tipo de zona específico (ejemplo: Residencial)
SELECT
    tipo_zona,
    estado_proyecto,
    cantidad_proyectos,
    presupuesto_total
FROM
    resumen_proyectos_estado_zona
WHERE
    tipo_zona = 'Residencial'
    AND estado_proyecto IS NOT NULL
ORDER BY
    cantidad_proyectos DESC;

-- Filtrar por estado específico (ejemplo: En Curso)
SELECT
    tipo_zona,
    estado_proyecto,
    cantidad_proyectos,
    presupuesto_total
FROM
    resumen_proyectos_estado_zona
WHERE
    estado_proyecto = 'En Curso'
ORDER BY
    cantidad_proyectos DESC;

-- Para refrescar la vista materializada concurrentemente:
-- REFRESH MATERIALIZED VIEW CONCURRENTLY resumen_proyectos_estado_zona;
