-- ==================================================
-- DATOS COMPLEMENTARIOS - MÁS PROYECTOS Y PUNTOS
-- ==================================================

-- Más Escuelas y Liceos
INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Liceo 1 Javiera Carrera', 'Escuela', ST_GeomFromText('POINT(-70.6451 -33.4421)', 4326), 'Av. Libertador Bernardo O''Higgins 462', 1),
                                                                                            ('Colegio Santiago College', 'Escuela', ST_GeomFromText('POINT(-70.5625 -33.4183)', 4326), 'Av. Presidente Kennedy 6001', 3),
                                                                                            ('Liceo Carmela Carvajal', 'Escuela', ST_GeomFromText('POINT(-70.6183 -33.4428)', 4326), 'Av. Presidente Eduardo Frei Montalva 3549', 2),
                                                                                            ('Colegio Verbo Divino', 'Escuela', ST_GeomFromText('POINT(-70.5592 -33.4267)', 4326), 'Av. Padre Hurtado 13150', 3),
                                                                                            ('Liceo República de Brasil', 'Escuela', ST_GeomFromText('POINT(-70.6589 -33.4567)', 4326), 'Av. República 526', 1),
                                                                                            ('Colegio San Ignacio El Bosque', 'Escuela', ST_GeomFromText('POINT(-70.6119 -33.4511)', 4326), 'Av. Vicuña Mackenna 3990', 9),
                                                                                            ('Instituto Alonso de Ercilla', 'Escuela', ST_GeomFromText('POINT(-70.7456 -33.5089)', 4326), 'Av. Central 5555', 6),
                                                                                            ('Liceo República Argentina', 'Escuela', ST_GeomFromText('POINT(-70.6778 -33.4556)', 4326), 'Av. Portales 3250', 1);

-- Más Parques y Plazas
INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Plaza de Armas', 'Parque', ST_GeomFromText('POINT(-70.6505 -33.4372)', 4326), 'Plaza de Armas', 1),
                                                                                            ('Quinta Normal', 'Parque', ST_GeomFromText('POINT(-70.6833 -33.4444)', 4326), 'Av. Matucana 520', 1),
                                                                                            ('Parque Los Reyes', 'Parque', ST_GeomFromText('POINT(-70.6717 -33.4556)', 4326), 'Av. Los Reyes', 1),
                                                                                            ('Parque Padre Renato Poblete', 'Parque', ST_GeomFromText('POINT(-70.7222 -33.4378)', 4326), 'Av. Costanera Norte 8000', 11),
                                                                                            ('Parque André Jarlan', 'Parque', ST_GeomFromText('POINT(-70.6556 -33.4967)', 4326), 'Av. Departamental 1255', 10),
                                                                                            ('Plaza Ñuñoa', 'Parque', ST_GeomFromText('POINT(-70.6028 -33.4556)', 4326), 'Av. Irarrázaval 3300', 9),
                                                                                            ('Parque Intercomunal', 'Parque', ST_GeomFromText('POINT(-70.5361 -33.4456)', 4326), 'Av. Las Condes 11001', 8);

-- Centros de Salud Primaria
INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('CESFAM San Joaquín', 'Hospital', ST_GeomFromText('POINT(-70.6339 -33.4944)', 4326), 'Av. Vicuña Mackenna 6630', 10),
                                                                                            ('CESFAM Providencia', 'Hospital', ST_GeomFromText('POINT(-70.6183 -33.4322)', 4326), 'Av. Tobalaba 1623', 2),
                                                                                            ('CESFAM Padre Orellana', 'Hospital', ST_GeomFromText('POINT(-70.7561 -33.5022)', 4326), 'Av. Pajaritos 2551', 6),
                                                                                            ('CESFAM Recreo', 'Hospital', ST_GeomFromText('POINT(-70.6656 -33.4678)', 4326), 'Av. Departamental 879', 10);

-- Más Estaciones de Metro
INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Metro Los Leones', 'Transporte', ST_GeomFromText('POINT(-70.5806 -33.4417)', 4326), 'Av. Presidente Riesco 3201', 2),
                                                                                            ('Metro Manquehue', 'Transporte', ST_GeomFromText('POINT(-70.5639 -33.4147)', 4326), 'Av. Apoquindo 4900', 3),
                                                                                            ('Metro Universidad de Santiago', 'Transporte', ST_GeomFromText('POINT(-70.6806 -33.4489)', 4326), 'Av. Libertador Bernardo O''Higgins 3349', 1),
                                                                                            ('Metro Franklin', 'Transporte', ST_GeomFromText('POINT(-70.6517 -33.4594)', 4326), 'Av. Libertador Bernardo O''Higgins 3350', 1),
                                                                                            ('Metro Vicuña Mackenna', 'Transporte', ST_GeomFromText('POINT(-70.6347 -33.4878)', 4326), 'Av. Vicuña Mackenna 5501', 5),
                                                                                            ('Metro Plaza Egaña', 'Transporte', ST_GeomFromText('POINT(-70.5944 -33.4506)', 4326), 'Av. José Pedro Alessandri 1398', 9);

-- Más Centros Comerciales
INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Mall Apumanque', 'Centro Comercial', ST_GeomFromText('POINT(-70.5778 -33.4278)', 4326), 'Av. Apoquindo 3990', 3),
                                                                                            ('Plaza Los Dominicos', 'Centro Comercial', ST_GeomFromText('POINT(-70.5472 -33.4069)', 4326), 'Av. Padre Hurtado Sur 13150', 3),
                                                                                            ('Mall Plaza Tobalaba', 'Centro Comercial', ST_GeomFromText('POINT(-70.6028 -33.4367)', 4326), 'Av. Providencia 2653', 2),
                                                                                            ('Drugstore Providencia', 'Centro Comercial', ST_GeomFromText('POINT(-70.6150 -33.4289)', 4326), 'Av. Providencia 2124', 2),
                                                                                            ('Portal La Reina', 'Centro Comercial', ST_GeomFromText('POINT(-70.5400 -33.4539)', 4326), 'Av. Inca de Oro 5851', 8),
                                                                                            ('Mall Maipú', 'Centro Comercial', ST_GeomFromText('POINT(-70.7567 -33.5108)', 4326), 'Av. Cinco de Abril 2025', 6);

-- ==================================================
-- MÁS PROYECTOS URBANOS REALES DE SANTIAGO
-- ==================================================

-- Nuevo Hospital de Maipú
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Nuevo Hospital de Maipú',
     'Hospital de alta complejidad con 400 camas',
     'Salud',
     '2024-05-01',
     '2027-12-31',
     'En Curso',
     180000000.00,
     ST_GeomFromText('POINT(-70.7489 -33.5156)', 4326),
     1);

-- Red de Ciclovías Santiago Centro
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Red Ciclovías Centro',
     'Red integrada de ciclovías en el centro de Santiago',
     'Movilidad Sostenible',
     '2024-02-01',
     '2025-06-30',
     'En Curso',
     12000000.00,
     ST_GeomFromText('MULTILINESTRING((-70.6693 -33.4489, -70.6500 -33.4367),(-70.6500 -33.4367, -70.6300 -33.4250))', 4326),
     2);

-- Teleférico Bicentenario
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Teleférico Bicentenario',
     'Teleférico turístico Parque Metropolitano - Providencia',
     'Transporte Turístico',
     '2025-06-01',
     '2026-12-31',
     'Planeado',
     35000000.00,
     ST_GeomFromText('LINESTRING(-70.6372 -33.4226, -70.6150 -33.4280)', 4326),
     2);

-- Plan Maestro Plaza de Armas
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Renovación Plaza de Armas',
     'Mejoramiento integral de Plaza de Armas y entorno',
     'Renovación Urbana',
     '2024-08-01',
     '2025-12-31',
     'En Curso',
     8500000.00,
     ST_GeomFromText('POLYGON((-70.6515 -33.4380, -70.6495 -33.4380, -70.6495 -33.4365, -70.6515 -33.4365, -70.6515 -33.4380))', 4326),
     3);

-- Corredor Bus Eléctrico Vicuña Mackenna
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Corredor Eléctrico Vicuña Mackenna',
     'Pistas exclusivas para buses eléctricos en Vicuña Mackenna',
     'Transporte Público',
     '2023-09-01',
     '2024-12-31',
     'Retrasado',
     45000000.00,
     ST_GeomFromText('LINESTRING(-70.6347 -33.4372, -70.6097 -33.5150)', 4326),
     2);

-- Estadio Municipal de Puente Alto
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Estadio Municipal Puente Alto',
     'Construcción de estadio municipal para 8000 personas',
     'Equipamiento Deportivo',
     '2025-04-01',
     '2026-12-31',
     'Planeado',
     28000000.00,
     ST_GeomFromText('POLYGON((-70.5750 -33.6050, -70.5720 -33.6050, -70.5720 -33.6030, -70.5750 -33.6030, -70.5750 -33.6050))', 4326),
     3);

-- Centro de Innovación Las Condes
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Hub de Innovación Las Condes',
     'Centro de emprendimiento e innovación tecnológica',
     'Equipamiento Tecnológico',
     '2024-11-01',
     '2026-06-30',
     'En Curso',
     22000000.00,
     ST_GeomFromText('POLYGON((-70.5700 -33.4100, -70.5680 -33.4100, -70.5680 -33.4085, -70.5700 -33.4085, -70.5700 -33.4100))', 4326),
     1);

-- Parque Biblioteca Independencia
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Parque Biblioteca de Independencia',
     'Biblioteca pública con parque y espacios culturales',
     'Equipamiento Cultural',
     '2024-06-01',
     '2025-12-31',
     'En Curso',
     9500000.00,
     ST_GeomFromText('POLYGON((-70.6600 -33.4050, -70.6580 -33.4050, -70.6580 -33.4035, -70.6600 -33.4035, -70.6600 -33.4050))', 4326),
     4);

-- Mejoramiento Eje Santa Rosa
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Mejoramiento Santa Rosa',
     'Ampliación y mejoramiento vial de Av. Santa Rosa',
     'Vialidad',
     '2023-03-01',
     '2024-06-30',
     'Retrasado',
     38000000.00,
     ST_GeomFromText('LINESTRING(-70.6339 -33.4944, -70.6339 -33.5500)', 4326),
     2);

-- ==================================================
-- RELACIONAR NUEVOS PROYECTOS CON ZONAS
-- ==================================================
INSERT INTO proyectos_zonas (proyecto_urbano_id, zona_urbana_id) VALUES
                                                                     (9, 6),   -- Hospital Maipú - Maipú
                                                                     (10, 1),  -- Red Ciclovías - Santiago Centro
                                                                     (11, 2),  -- Teleférico - Providencia
                                                                     (12, 1),  -- Plaza de Armas - Santiago Centro
                                                                     (13, 9),  -- Corredor Vicuña Mackenna - Ñuñoa
                                                                     (13, 5),  -- Corredor Vicuña Mackenna - La Florida
                                                                     (14, 7),  -- Estadio - Puente Alto
                                                                     (15, 3),  -- Hub Innovación - Las Condes
                                                                     (16, 12), -- Parque Biblioteca - Independencia
                                                                     (17, 10), -- Mejoramiento Santa Rosa - San Miguel
                                                                     (17, 5);  -- Mejoramiento Santa Rosa - La Florida

-- ==================================================
-- REFRESCAR VISTAS MATERIALIZADAS
-- ==================================================
REFRESH MATERIALIZED VIEW cobertura_infraestructura;
REFRESH MATERIALIZED VIEW resumen_proyectos_estado_zona;

-- ==================================================
-- ANÁLISIS ESTADÍSTICO FINAL
-- ==================================================

SELECT '===== ANÁLISIS COMPLETO DE SANTIAGO =====' as info;

-- Cobertura de infraestructura por comuna
SELECT
    zu.nombre as comuna,
    ci.total_hospitales as hospitales,
    ci.total_escuelas as escuelas,
    ci.total_parques as parques,
    ci.total_puntos_interes as total_puntos,
    dd.poblacion as poblacion_2025
FROM zonas_urbanas zu
         LEFT JOIN cobertura_infraestructura ci ON zu.zona_urbana_id = ci.zona_urbana_id
         LEFT JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id AND dd.anio = 2025
ORDER BY dd.poblacion DESC;

-- Proyectos por estado con presupuesto
SELECT
    estado,
    COUNT(*) as cantidad_proyectos,
    TO_CHAR(SUM(presupuesto), 'FM$999,999,999,999') as presupuesto_total,
    TO_CHAR(AVG(presupuesto), 'FM$999,999,999') as presupuesto_promedio
FROM proyectos_urbanos
GROUP BY estado
ORDER BY SUM(presupuesto) DESC;

-- Densidad poblacional por comuna
SELECT
    zu.nombre as comuna,
    dd.poblacion,
    zu.area_km2,
    ROUND(dd.poblacion::numeric / zu.area_km2, 0) as densidad_hab_km2
FROM zonas_urbanas zu
         JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
WHERE dd.anio = 2025
ORDER BY densidad_hab_km2 DESC;

-- Crecimiento poblacional 2020-2025
SELECT
    zu.nombre as comuna,
    d2020.poblacion as poblacion_2020,
    d2025.poblacion as poblacion_2025,
    (d2025.poblacion - d2020.poblacion) as crecimiento_absoluto,
    ROUND(((d2025.poblacion - d2020.poblacion)::numeric / d2020.poblacion * 100), 2) as crecimiento_porcentual
FROM zonas_urbanas zu
         JOIN datos_demograficos d2020 ON zu.zona_urbana_id = d2020.zona_urbana_id AND d2020.anio = 2020
         JOIN datos_demograficos d2025 ON zu.zona_urbana_id = d2025.zona_urbana_id AND d2025.anio = 2025
ORDER BY crecimiento_porcentual DESC;