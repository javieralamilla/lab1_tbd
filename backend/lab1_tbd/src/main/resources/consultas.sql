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
    dd.anio = (SELECT MAX(anio) FROM datos_demograficos WHERE zona_urbana_id = zu.zona_urbana_id)
    AND zu.area_km2 IS NOT NULL
    AND zu.area_km2 > 0
ORDER BY 
    densidad_poblacional DESC;


-- ==================================================
-- 2) Identificación de Zonas con Escasez de Servicios
-- ==================================================
-- Identifica las 5 zonas urbanas con mayor población pero menor cantidad 
-- de hospitales. Devuelve nombre de zona, población y conteo de hospitales.
SELECT
    z.nombre AS zona,
    d.poblacion,
    COUNT(pi.punto_interes_id) AS cantidad_hospitales
FROM 
    zonas_urbanas z
    INNER JOIN LATERAL (
        SELECT poblacion
        FROM datos_demograficos dd
        WHERE dd.zona_urbana_id = z.zona_urbana_id
        ORDER BY anio DESC
        LIMIT 1
    ) d ON TRUE
    LEFT JOIN puntos_interes pi ON pi.zona_urbana_id = z.zona_urbana_id 
        AND pi.tipo = 'Hospital' 
        AND pi.activo = TRUE
WHERE 
    d.poblacion > 1000
GROUP BY 
    z.zona_urbana_id, z.nombre, d.poblacion
ORDER BY 
    d.poblacion DESC, cantidad_hospitales ASC
LIMIT 5;


-- ==================================================
-- 3) Análisis de Proximidad a Proyectos
-- ==================================================
-- Encuentra todas las escuelas a menos de 500 metros de proyectos 'En Curso'.
-- Devuelve nombre de escuela, nombre de proyecto y distancia en metros.
SELECT
    pi.nombre AS escuela,
    pu.nombre AS proyecto,
    ROUND(
        ST_Distance(
            pi.coordenadas_punto::geography,
            ST_Centroid(pu.geometria)::geography
        )::NUMERIC, 2
    ) AS distancia_metros
FROM 
    puntos_interes pi
    INNER JOIN proyectos_urbanos pu ON ST_DWithin(
        pi.coordenadas_punto::geography,
        ST_Centroid(pu.geometria)::geography,
        500
    )
WHERE 
    pi.tipo = 'Escuela'
    AND pu.estado = 'En Curso'
    AND pi.activo = TRUE
ORDER BY 
    distancia_metros;


-- ==================================================
-- 4) Detección de Zonas en Rápido Crecimiento
-- ==================================================
-- Identifica las 3 zonas cuya población creció más del 10% en los últimos 5 años.
-- Muestra nombre de zona y porcentaje de crecimiento.
WITH datos_crecimiento AS (
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
         AND dd.anio <= EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - 5
         ORDER BY anio DESC 
         LIMIT 1) AS poblacion_hace_5,
        (SELECT anio 
         FROM datos_demograficos dd 
         WHERE dd.zona_urbana_id = z.zona_urbana_id 
         AND dd.anio <= EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER - 5
         ORDER BY anio DESC 
         LIMIT 1) AS anio_hace_5
    FROM zonas_urbanas z
)
SELECT
    nombre AS zona,
    poblacion_hace_5,
    poblacion_actual,
    ROUND(
        ((poblacion_actual::NUMERIC - poblacion_hace_5::NUMERIC) / poblacion_hace_5::NUMERIC) * 100.0, 
        2
    ) AS porcentaje_crecimiento
FROM datos_crecimiento
WHERE 
    poblacion_hace_5 IS NOT NULL
    AND poblacion_actual IS NOT NULL
    AND poblacion_actual > poblacion_hace_5
    AND ((poblacion_actual::NUMERIC - poblacion_hace_5::NUMERIC) / poblacion_hace_5::NUMERIC) * 100.0 > 10
ORDER BY 
    porcentaje_crecimiento DESC
LIMIT 3;


-- ==================================================
-- 5) Análisis de Cobertura de Infraestructura
-- ==================================================
-- Vista materializada que muestra el conteo de parques, escuelas y hospitales
-- por cada zona urbana. Debe actualizarse semanalmente.
-- Nota: Esta vista ya está definida en schema.sql como 'cobertura_infraestructura'
-- Para consultarla:
SELECT * FROM cobertura_infraestructura ORDER BY total_puntos_interes DESC;


-- ==================================================
-- 6) Simulación de Nuevo Desarrollo Habitacional
-- ==================================================
-- Procedimiento almacenado que simula crecimiento poblacional basado en nuevas viviendas.
-- Recibe: zona_id, número de nuevas viviendas
-- Factor: 3 personas por vivienda
-- Nota: Este procedimiento ya está definido en schema.sql
-- Para ejecutarlo:
-- CALL simular_crecimiento_poblacion(1, 100);


-- ==================================================
-- 7) Actualización Masiva de Proyectos
-- ==================================================
-- Procedimiento almacenado que actualiza proyectos retrasados de un usuario.
-- Cambia estado a 'Retrasado' para proyectos que pasaron su fecha límite.
-- Nota: Este procedimiento ya está definido en schema.sql
-- Para ejecutarlo:
-- CALL actualizar_proyectos_retrasados(1);


-- ==================================================
-- 8) Listado de Zonas sin Planificación Reciente
-- ==================================================
-- Muestra zonas sin proyectos asociados en los últimos 2 años.
-- Incluye nombre de zona y fecha del último proyecto o 'Ninguno'.
SELECT
    z.zona_urbana_id,
    z.nombre AS zona,
    CASE
        WHEN MAX(pu.fecha_inicio) IS NULL THEN 'Ninguno'
        ELSE TO_CHAR(MAX(pu.fecha_inicio), 'YYYY-MM-DD')
    END AS fecha_ultimo_proyecto
FROM 
    zonas_urbanas z
    LEFT JOIN proyectos_zonas pz ON pz.zona_urbana_id = z.zona_urbana_id
    LEFT JOIN proyectos_urbanos pu ON pu.proyecto_urbano_id = pz.proyecto_urbano_id
GROUP BY 
    z.zona_urbana_id, z.nombre
HAVING 
    MAX(pu.fecha_inicio) IS NULL 
    OR MAX(pu.fecha_inicio) < (CURRENT_DATE - INTERVAL '2 years')
ORDER BY 
    fecha_ultimo_proyecto NULLS FIRST, z.nombre;


-- ==================================================
-- 9) Análisis de Superposición de Proyectos
-- ==================================================
-- Identifica pares de proyectos urbanos que se superponen geográficamente.
-- Devuelve nombres de ambos proyectos y área de superposición en m².
SELECT
    p1.nombre AS proyecto_1,
    p2.nombre AS proyecto_2,
    ROUND(
        ST_Area(
            ST_Transform(
                ST_Intersection(p1.geometria, p2.geometria),
                32719
            )
        )::NUMERIC, 2
    ) AS area_superposicion_m2
FROM 
    proyectos_urbanos p1
    INNER JOIN proyectos_urbanos p2 ON p1.proyecto_urbano_id < p2.proyecto_urbano_id
WHERE 
    ST_Intersects(p1.geometria, p2.geometria)
    AND ST_GeometryType(ST_Intersection(p1.geometria, p2.geometria)) IN ('ST_Polygon', 'ST_MultiPolygon')
    AND ST_Area(ST_Transform(ST_Intersection(p1.geometria, p2.geometria), 32719)) > 1
ORDER BY 
    area_superposicion_m2 DESC;


-- ==================================================
-- 10) Resumen de Proyectos por Estado y Tipo de Zona
-- ==================================================
-- Vista materializada que muestra cantidad de proyectos por estado y tipo de zona.
-- Debe refrescarse concurrentemente para consultas rápidas en informes.
-- Nota: Esta vista ya está definida en schema.sql como 'resumen_proyectos_estado_zona'
-- Para consultarla:
SELECT * FROM resumen_proyectos_estado_zona ORDER BY tipo_zona, estado;
