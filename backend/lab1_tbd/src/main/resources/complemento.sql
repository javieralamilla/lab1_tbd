-- ==================================================
-- COMPLEMENTO SQL - CONSULTAS ADICIONALES REQUERIDAS
-- ==================================================

-- 🆕 3. Análisis de Proximidad a Proyectos
-- Encuentra escuelas a menos de 500 metros de proyectos 'En Curso'
CREATE OR REPLACE VIEW escuelas_cerca_proyectos AS
SELECT DISTINCT
    pi.nombre AS nombre_escuela,
    pu.nombre AS nombre_proyecto,
    ROUND(
        ST_Distance(
            pi.coordenadas_punto::geography,
            ST_Centroid(pu.geometria)::geography
        )::NUMERIC, 2
    ) AS distancia_metros
FROM puntos_interes pi
INNER JOIN proyectos_urbanos pu ON ST_DWithin(
    pi.coordenadas_punto::geography,
    ST_Centroid(pu.geometria)::geography,
    500
)
WHERE pi.tipo = 'Escuela'
  AND pu.estado = 'En Curso'
  AND pi.activo = TRUE
ORDER BY distancia_metros;

-- 🆕 9. Análisis de Superposición de Proyectos
-- Identifica proyectos que se superponen geográficamente
CREATE OR REPLACE VIEW proyectos_superpuestos AS
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
FROM proyectos_urbanos p1
INNER JOIN proyectos_urbanos p2 ON p1.proyecto_urbano_id < p2.proyecto_urbano_id
WHERE ST_Intersects(p1.geometria, p2.geometria)
  AND ST_GeometryType(ST_Intersection(p1.geometria, p2.geometria)) IN ('ST_Polygon', 'ST_MultiPolygon')
  AND ST_Area(ST_Transform(ST_Intersection(p1.geometria, p2.geometria), 32719)) > 1
ORDER BY area_superposicion_m2 DESC;

-- ==================================================
-- PROCEDIMIENTOS ADICIONALES PARA REFRESCAR VISTAS
-- ==================================================

-- Procedimiento para refrescar vista de cobertura de infraestructura
CREATE OR REPLACE PROCEDURE refrescar_cobertura_infraestructura()
LANGUAGE plpgsql
AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY cobertura_infraestructura;
    RAISE NOTICE 'Vista materializada cobertura_infraestructura refrescada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        -- Si falla el refresco concurrente, intentar refresco normal
        REFRESH MATERIALIZED VIEW cobertura_infraestructura;
        RAISE NOTICE 'Vista materializada cobertura_infraestructura refrescada (modo normal)';
END;
$$;

-- Procedimiento para refrescar vista de resumen de proyectos
CREATE OR REPLACE PROCEDURE refrescar_resumen_proyectos()
LANGUAGE plpgsql
AS $$
BEGIN
    REFRESH MATERIALIZED VIEW CONCURRENTLY resumen_proyectos_estado_zona;
    RAISE NOTICE 'Vista materializada resumen_proyectos_estado_zona refrescada exitosamente';
EXCEPTION
    WHEN OTHERS THEN
        -- Si falla el refresco concurrente, intentar refresco normal
        REFRESH MATERIALIZED VIEW resumen_proyectos_estado_zona;
        RAISE NOTICE 'Vista materializada resumen_proyectos_estado_zona refrescada (modo normal)';
END;
$$;