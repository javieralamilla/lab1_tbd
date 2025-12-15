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
-- Para consultarla:



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



-- ==================================================
-- 9) Análisis de Superposición de Proyectos
-- ==================================================
-- Identifica pares de proyectos urbanos que se superponen geográficamente.
-- Devuelve nombres de ambos proyectos y área de superposición en m².



-- ==================================================
-- 10) Resumen de Proyectos por Estado y Tipo de Zona
-- ==================================================
-- Vista materializada que muestra cantidad de proyectos por estado y tipo de zona.
-- Debe refrescarse concurrentemente para consultas rápidas en informes.
-- Nota: Esta vista ya está definida en schema.sql como 'resumen_proyectos_estado_zona'
-- Para consultarla:

