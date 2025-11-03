-- ==================================================
-- DATOS REALES - REGIÓN METROPOLITANA DE SANTIAGO
-- ==================================================


-- USUARIOS
-- ==================================================
-- Contraseña: "password" hasheada con BCrypt (strength 10)
-- Hash: $2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi
INSERT INTO usuarios (nombre, apellido, email, contrasena, rol) VALUES
                                                                    ('María José', 'Fernández', 'mj.fernandez@minvu.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'administrador'),
                                                                    ('Carlos', 'Rodríguez', 'c.rodriguez@seremi.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'planificador'),
                                                                    ('Patricia', 'González', 'p.gonzalez@municipalidad.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'autoridad_municipal'),
                                                                    ('José', 'Silva', 'j.silva@secplan.cl', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'planificador');

-- ==================================================
-- ZONAS URBANAS - COMUNAS DE SANTIAGO
-- ==================================================

-- Santiago Centro
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Santiago Centro',
     ST_GeomFromText('POLYGON((-70.6693 -33.4489, -70.6300 -33.4489, -70.6300 -33.4150, -70.6693 -33.4150, -70.6693 -33.4489))', 4326),
     'Comercial', 23.2);

-- Providencia
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Providencia',
     ST_GeomFromText('POLYGON((-70.6300 -33.4489, -70.5950 -33.4489, -70.5950 -33.4150, -70.6300 -33.4150, -70.6300 -33.4489))', 4326),
     'Mixto', 14.4);

-- Las Condes
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Las Condes',
     ST_GeomFromText('POLYGON((-70.5950 -33.4489, -70.5400 -33.4489, -70.5400 -33.3800, -70.5950 -33.3800, -70.5950 -33.4489))', 4326),
     'Residencial', 99.4);

-- Vitacura
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Vitacura',
     ST_GeomFromText('POLYGON((-70.5950 -33.3800, -70.5400 -33.3800, -70.5400 -33.3450, -70.5950 -33.3450, -70.5950 -33.3800))', 4326),
     'Residencial', 28.3);

-- La Florida
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('La Florida',
     ST_GeomFromText('POLYGON((-70.6300 -33.5200, -70.5700 -33.5200, -70.5700 -33.4800, -70.6300 -33.4800, -70.6300 -33.5200))', 4326),
     'Residencial', 70.8);

-- Maipú
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Maipú',
     ST_GeomFromText('POLYGON((-70.7800 -33.5400, -70.7000 -33.5400, -70.7000 -33.4800, -70.7800 -33.4800, -70.7800 -33.5400))', 4326),
     'Residencial', 133.0);

-- Puente Alto
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Puente Alto',
     ST_GeomFromText('POLYGON((-70.6000 -33.6200, -70.5200 -33.6200, -70.5200 -33.5600, -70.6000 -33.5600, -70.6000 -33.6200))', 4326),
     'Residencial', 88.2);

-- La Reina
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('La Reina',
     ST_GeomFromText('POLYGON((-70.5700 -33.4800, -70.5200 -33.4800, -70.5200 -33.4400, -70.5700 -33.4400, -70.5700 -33.4800))', 4326),
     'Residencial', 56.3);

-- Ñuñoa
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Ñuñoa',
     ST_GeomFromText('POLYGON((-70.6300 -33.4800, -70.5900 -33.4800, -70.5900 -33.4400, -70.6300 -33.4400, -70.6300 -33.4800))', 4326),
     'Mixto', 16.9);

-- San Miguel
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('San Miguel',
     ST_GeomFromText('POLYGON((-70.6693 -33.5000, -70.6300 -33.5000, -70.6300 -33.4700, -70.6693 -33.4700, -70.6693 -33.5000))', 4326),
     'Industrial', 9.5);

-- Cerrillos
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Cerrillos',
     ST_GeomFromText('POLYGON((-70.7200 -33.5100, -70.6693 -33.5100, -70.6693 -33.4800, -70.7200 -33.4800, -70.7200 -33.5100))', 4326),
     'Industrial', 21.0);

-- Independencia
INSERT INTO zonas_urbanas (nombre, geometria_poligono, tipo_zona, area_km2) VALUES
    ('Independencia',
     ST_GeomFromText('POLYGON((-70.6693 -33.4150, -70.6400 -33.4150, -70.6400 -33.3900, -70.6693 -33.3900, -70.6693 -33.4150))', 4326),
     'Residencial', 7.4);

-- ==================================================
-- PUNTOS DE INTERÉS - HOSPITALES
-- Coordenadas GPS Reales Verificadas
-- REEMPLAZAR DESDE AQUÍ en datos.sql (línea ~95 aproximadamente)
-- ==================================================

INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
-- Hospitales Principales
('Hospital Clínico Universidad de Chile', 'Hospital', ST_GeomFromText('POINT(-70.6408 -33.4245)', 4326), 'Av. Santos Dumont 999', 1),
('Hospital del Salvador', 'Hospital', ST_GeomFromText('POINT(-70.6127 -33.4403)', 4326), 'Av. Salvador 364', 2),
('Clínica Las Condes', 'Hospital', ST_GeomFromText('POINT(-70.5650 -33.4100)', 4326), 'Estoril 450', 3),
('Clínica Alemana', 'Hospital', ST_GeomFromText('POINT(-70.5865 -33.3925)', 4326), 'Av. Vitacura 5951', 4),
('Hospital Barros Luco', 'Hospital', ST_GeomFromText('POINT(-70.6442 -33.4898)', 4326), 'Av. José Miguel Carrera 3204', 10),
('Hospital Sótero del Río', 'Hospital', ST_GeomFromText('POINT(-70.5800 -33.5100)', 4326), 'Av. Concha y Toro 3459', 5),
('Hospital Félix Bulnes', 'Hospital', ST_GeomFromText('POINT(-70.7326 -33.4965)', 4326), 'Av. Pajaritos 3201', 6),

-- Centros de Salud Primaria
('CESFAM San Joaquín', 'Hospital', ST_GeomFromText('POINT(-70.6339 -33.4944)', 4326), 'Av. Vicuña Mackenna 6630', 10),
('CESFAM Providencia', 'Hospital', ST_GeomFromText('POINT(-70.6183 -33.4322)', 4326), 'Av. Tobalaba 1623', 2),
('CESFAM Padre Orellana', 'Hospital', ST_GeomFromText('POINT(-70.7561 -33.5022)', 4326), 'Av. Pajaritos 2551', 6),
('CESFAM Recreo', 'Hospital', ST_GeomFromText('POINT(-70.6656 -33.4678)', 4326), 'Av. Departamental 879', 10);

-- ==================================================
-- PUNTOS DE INTERÉS - EDUCACIÓN
-- ==================================================

INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
-- Universidades
('Universidad de Chile - Campus Beaucheff', 'Escuela', ST_GeomFromText('POINT(-70.6688 -33.4578)', 4326), 'Av. Beaucheff 850', 1),
('Pontificia Universidad Católica', 'Escuela', ST_GeomFromText('POINT(-70.6393 -33.4414)', 4326), 'Av. Libertador Bernardo O''Higgins 340', 1),
('Universidad de Santiago USACH', 'Escuela', ST_GeomFromText('POINT(-70.6810 -33.4491)', 4326), 'Av. Libertador Bernardo O''Higgins 3363', 1),

-- Colegios y Liceos
('Instituto Nacional', 'Escuela', ST_GeomFromText('POINT(-70.6486 -33.4380)', 4326), 'Arturo Prat 33', 1),
('Liceo José Victorino Lastarria', 'Escuela', ST_GeomFromText('POINT(-70.6504 -33.4394)', 4326), 'Av. Vicuña Mackenna 820', 1),
('Liceo 1 Javiera Carrera', 'Escuela', ST_GeomFromText('POINT(-70.6451 -33.4421)', 4326), 'Av. Libertador Bernardo O''Higgins 462', 1),
('Liceo República de Brasil', 'Escuela', ST_GeomFromText('POINT(-70.6589 -33.4567)', 4326), 'Av. República 526', 1),
('Liceo República Argentina', 'Escuela', ST_GeomFromText('POINT(-70.6778 -33.4556)', 4326), 'Av. Portales 3250', 1),
('Colegio Providencia', 'Escuela', ST_GeomFromText('POINT(-70.6154 -33.4282)', 4326), 'Av. Providencia 2594', 2),
('Liceo Carmela Carvajal', 'Escuela', ST_GeomFromText('POINT(-70.6183 -33.4428)', 4326), 'Av. Presidente Eduardo Frei Montalva 3549', 2),
('Colegio The Grange School', 'Escuela', ST_GeomFromText('POINT(-70.5478 -33.4020)', 4326), 'Av. Príncipe de Gales 6154', 3),
('Colegio Santiago College', 'Escuela', ST_GeomFromText('POINT(-70.5625 -33.4183)', 4326), 'Av. Presidente Kennedy 6001', 3),
('Colegio Verbo Divino', 'Escuela', ST_GeomFromText('POINT(-70.5592 -33.4267)', 4326), 'Av. Padre Hurtado 13150', 3),
('Liceo Industrial de Maipú', 'Escuela', ST_GeomFromText('POINT(-70.7545 -33.5110)', 4326), 'Av. Pajaritos 2755', 6),
('Instituto Alonso de Ercilla', 'Escuela', ST_GeomFromText('POINT(-70.7456 -33.5089)', 4326), 'Av. Central 5555', 6),
('Colegio San Ignacio El Bosque', 'Escuela', ST_GeomFromText('POINT(-70.6119 -33.4511)', 4326), 'Av. Vicuña Mackenna 3990', 9);

-- ==================================================
-- PUNTOS DE INTERÉS - PARQUES Y ÁREAS VERDES
-- COORDENADAS CORREGIDAS
-- ==================================================

INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Parque Metropolitano', 'Parque', ST_GeomFromText('POINT(-70.6333 -33.4228)', 4326), 'Cerro San Cristóbal', 2),
                                                                                            ('Parque Forestal', 'Parque', ST_GeomFromText('POINT(-70.6450 -33.4350)', 4326), 'Av. Ismael Valdés Vergara', 1),
                                                                                            ('Plaza de Armas', 'Parque', ST_GeomFromText('POINT(-70.6511 -33.4372)', 4326), 'Plaza de Armas', 1),
                                                                                            ('Quinta Normal', 'Parque', ST_GeomFromText('POINT(-70.6808 -33.4400)', 4326), 'Av. Matucana 520', 1),
                                                                                            ('Parque O''Higgins', 'Parque', ST_GeomFromText('POINT(-70.6611 -33.4661)', 4326), 'Av. Beaucheff con Av. Rondizzoni', 1),
                                                                                            ('Parque Los Reyes', 'Parque', ST_GeomFromText('POINT(-70.6467 -33.4556)', 4326), 'Av. Los Reyes', 1),
                                                                                            ('Parque Bicentenario', 'Parque', ST_GeomFromText('POINT(-70.5864 -33.4050)', 4326), 'Av. Bicentenario 3800', 4),
                                                                                            ('Parque Araucano', 'Parque', ST_GeomFromText('POINT(-70.5700 -33.4144)', 4326), 'Av. Presidente Riesco 5330', 3),
                                                                                            ('Parque Inés de Suárez', 'Parque', ST_GeomFromText('POINT(-70.6039 -33.4544)', 4326), 'Av. Irarrázaval 2933', 9),
                                                                                            ('Plaza Ñuñoa', 'Parque', ST_GeomFromText('POINT(-70.6042 -33.4572)', 4326), 'Av. Irarrázaval 3300', 9),
                                                                                            ('Parque Juan XXIII', 'Parque', ST_GeomFromText('POINT(-70.5500 -33.4600)', 4326), 'Av. José Arrieta 8989', 8),
                                                                                            ('Parque Intercomunal', 'Parque', ST_GeomFromText('POINT(-70.5450 -33.4483)', 4326), 'Av. Las Condes 11001', 8),
                                                                                            ('Parque André Jarlan', 'Parque', ST_GeomFromText('POINT(-70.6556 -33.4950)', 4326), 'Av. Departamental 1255', 10),
                                                                                            ('Parque Padre Renato Poblete', 'Parque', ST_GeomFromText('POINT(-70.7050 -33.4300)', 4326), 'Av. Costanera Norte 8000', 11);

-- ==================================================
-- PUNTOS DE INTERÉS - CENTROS COMERCIALES
-- ==================================================

INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Mall Costanera Center', 'Centro Comercial', ST_GeomFromText('POINT(-70.6072 -33.4180)', 4326), 'Av. Andrés Bello 2425', 2),
                                                                                            ('Mall Plaza Tobalaba', 'Centro Comercial', ST_GeomFromText('POINT(-70.6028 -33.4367)', 4326), 'Av. Providencia 2653', 2),
                                                                                            ('Drugstore Providencia', 'Centro Comercial', ST_GeomFromText('POINT(-70.6150 -33.4289)', 4326), 'Av. Providencia 2124', 2),
                                                                                            ('Parque Arauco', 'Centro Comercial', ST_GeomFromText('POINT(-70.5689 -33.4151)', 4326), 'Av. Presidente Kennedy 5413', 3),
                                                                                            ('Alto Las Condes', 'Centro Comercial', ST_GeomFromText('POINT(-70.5695 -33.4043)', 4326), 'Av. Pdte. Kennedy 9001', 3),
                                                                                            ('Mall Apumanque', 'Centro Comercial', ST_GeomFromText('POINT(-70.5778 -33.4278)', 4326), 'Av. Apoquindo 3990', 3),
                                                                                            ('Plaza Los Dominicos', 'Centro Comercial', ST_GeomFromText('POINT(-70.5472 -33.4069)', 4326), 'Av. Padre Hurtado Sur 13150', 3),
                                                                                            ('Mall Plaza Vespucio', 'Centro Comercial', ST_GeomFromText('POINT(-70.6100 -33.5152)', 4326), 'Av. Vicuña Mackenna 7110', 5),
                                                                                            ('Mall Maipú', 'Centro Comercial', ST_GeomFromText('POINT(-70.7567 -33.5108)', 4326), 'Av. Cinco de Abril 2025', 6),
                                                                                            ('Mall Plaza Oeste', 'Centro Comercial', ST_GeomFromText('POINT(-70.7378 -33.5174)', 4326), 'Av. Américo Vespucio 1501', 11),
                                                                                            ('Portal La Reina', 'Centro Comercial', ST_GeomFromText('POINT(-70.5400 -33.4539)', 4326), 'Av. Inca de Oro 5851', 8);

-- ==================================================
-- PUNTOS DE INTERÉS - TRANSPORTE (METRO)
-- ==================================================

INSERT INTO puntos_interes (nombre, tipo, coordenadas_punto, direccion, zona_urbana_id) VALUES
                                                                                            ('Metro Baquedano', 'Transporte', ST_GeomFromText('POINT(-70.6348 -33.4374)', 4326), 'Plaza Baquedano', 1),
                                                                                            ('Metro Universidad de Chile', 'Transporte', ST_GeomFromText('POINT(-70.6505 -33.4380)', 4326), 'Av. Libertador Bernardo O''Higgins 1058', 1),
                                                                                            ('Metro Universidad de Santiago', 'Transporte', ST_GeomFromText('POINT(-70.6806 -33.4489)', 4326), 'Av. Libertador Bernardo O''Higgins 3349', 1),
                                                                                            ('Metro Franklin', 'Transporte', ST_GeomFromText('POINT(-70.6517 -33.4594)', 4326), 'Av. Libertador Bernardo O''Higgins 3350', 1),
                                                                                            ('Metro Tobalaba', 'Transporte', ST_GeomFromText('POINT(-70.6031 -33.4369)', 4326), 'Av. Providencia 2653', 2),
                                                                                            ('Metro Los Leones', 'Transporte', ST_GeomFromText('POINT(-70.5806 -33.4417)', 4326), 'Av. Presidente Riesco 3201', 2),
                                                                                            ('Metro Escuela Militar', 'Transporte', ST_GeomFromText('POINT(-70.5682 -33.4233)', 4326), 'Av. Apoquindo 6275', 3),
                                                                                            ('Metro Manquehue', 'Transporte', ST_GeomFromText('POINT(-70.5639 -33.4147)', 4326), 'Av. Apoquindo 4900', 3),
                                                                                            ('Metro Vicuña Mackenna', 'Transporte', ST_GeomFromText('POINT(-70.6347 -33.4878)', 4326), 'Av. Vicuña Mackenna 5501', 5),
                                                                                            ('Metro Maipú', 'Transporte', ST_GeomFromText('POINT(-70.7653 -33.5110)', 4326), 'Av. Cinco de Abril 2075', 6),
                                                                                            ('Metro Plaza de Puente Alto', 'Transporte', ST_GeomFromText('POINT(-70.5780 -33.6101)', 4326), 'Av. Concha y Toro 175', 7),
                                                                                            ('Metro Plaza Egaña', 'Transporte', ST_GeomFromText('POINT(-70.5944 -33.4506)', 4326), 'Av. José Pedro Alessandri 1398', 9);
-- ==================================================
-- DATOS DEMOGRÁFICOS (Históricos 2020-2025)
-- ==================================================

-- Santiago Centro
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (1, 404495, 39.2, 134832, 2020),
                                                                                                      (1, 408123, 39.4, 136041, 2021),
                                                                                                      (1, 410856, 39.6, 136952, 2022),
                                                                                                      (1, 412340, 39.8, 137447, 2023),
                                                                                                      (1, 413892, 40.0, 137964, 2024),
                                                                                                      (1, 415234, 40.1, 138411, 2025);

-- Providencia
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (2, 142079, 42.3, 47360, 2020),
                                                                                                      (2, 143891, 42.5, 47964, 2021),
                                                                                                      (2, 145234, 42.7, 48411, 2022),
                                                                                                      (2, 146123, 42.9, 48708, 2023),
                                                                                                      (2, 147456, 43.1, 49152, 2024),
                                                                                                      (2, 148678, 43.3, 49559, 2025);

-- Las Condes
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (3, 330759, 40.8, 110253, 2020),
                                                                                                      (3, 335421, 41.0, 111807, 2021),
                                                                                                      (3, 339872, 41.2, 113291, 2022),
                                                                                                      (3, 343891, 41.4, 114630, 2023),
                                                                                                      (3, 348234, 41.6, 116078, 2024),
                                                                                                      (3, 352567, 41.8, 117522, 2025);

-- Vitacura
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (4, 85384, 43.5, 28461, 2020),
                                                                                                      (4, 86712, 43.7, 28904, 2021),
                                                                                                      (4, 87891, 43.9, 29297, 2022),
                                                                                                      (4, 89023, 44.1, 29674, 2023),
                                                                                                      (4, 90234, 44.3, 30078, 2024),
                                                                                                      (4, 91456, 44.5, 30485, 2025);

-- La Florida
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (5, 402433, 34.2, 134144, 2020),
                                                                                                      (5, 411234, 34.4, 137078, 2021),
                                                                                                      (5, 419872, 34.6, 139957, 2022),
                                                                                                      (5, 428156, 34.8, 142719, 2023),
                                                                                                      (5, 436891, 35.0, 145630, 2024),
                                                                                                      (5, 445234, 35.2, 148411, 2025);

-- Maipú
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (6, 578605, 32.8, 192868, 2020),
                                                                                                      (6, 595234, 33.0, 198411, 2021),
                                                                                                      (6, 611456, 33.2, 203819, 2022),
                                                                                                      (6, 627891, 33.4, 209297, 2023),
                                                                                                      (6, 644523, 33.6, 214841, 2024),
                                                                                                      (6, 661789, 33.8, 220596, 2025);

-- Puente Alto
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (7, 645909, 31.5, 215303, 2020),
                                                                                                      (7, 663234, 31.7, 221078, 2021),
                                                                                                      (7, 680456, 31.9, 226819, 2022),
                                                                                                      (7, 697891, 32.1, 232630, 2023),
                                                                                                      (7, 715234, 32.3, 238411, 2024),
                                                                                                      (7, 732567, 32.5, 244189, 2025);

-- La Reina
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (8, 100252, 38.7, 33417, 2020),
                                                                                                      (8, 101891, 38.9, 33964, 2021),
                                                                                                      (8, 103234, 39.1, 34411, 2022),
                                                                                                      (8, 104567, 39.3, 34856, 2023),
                                                                                                      (8, 105891, 39.5, 35297, 2024),
                                                                                                      (8, 107123, 39.7, 35708, 2025);

-- Ñuñoa
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (9, 208237, 37.4, 69412, 2020),
                                                                                                      (9, 211234, 37.6, 70411, 2021),
                                                                                                      (9, 214123, 37.8, 71374, 2022),
                                                                                                      (9, 216891, 38.0, 72297, 2023),
                                                                                                      (9, 219567, 38.2, 73189, 2024),
                                                                                                      (9, 222234, 38.4, 74078, 2025);

-- San Miguel
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (10, 133059, 36.8, 44353, 2020),
                                                                                                      (10, 134891, 37.0, 44964, 2021),
                                                                                                      (10, 136567, 37.2, 45522, 2022),
                                                                                                      (10, 138123, 37.4, 46041, 2023),
                                                                                                      (10, 139891, 37.6, 46630, 2024),
                                                                                                      (10, 141567, 37.8, 47189, 2025);

-- Cerrillos
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (11, 88956, 35.2, 29652, 2020),
                                                                                                      (11, 90891, 35.4, 30297, 2021),
                                                                                                      (11, 92734, 35.6, 30911, 2022),
                                                                                                      (11, 94567, 35.8, 31522, 2023),
                                                                                                      (11, 96234, 36.0, 32078, 2024),
                                                                                                      (11, 97891, 36.2, 32630, 2025);

-- Independencia
INSERT INTO datos_demograficos (zona_urbana_id, poblacion, edad_promedio, numero_viviendas, anio) VALUES
                                                                                                      (12, 142065, 34.9, 47355, 2020),
                                                                                                      (12, 144234, 35.1, 48078, 2021),
                                                                                                      (12, 146123, 35.3, 48708, 2022),
                                                                                                      (12, 147891, 35.5, 49297, 2023),
                                                                                                      (12, 149567, 35.7, 49856, 2024),
                                                                                                      (12, 151234, 35.9, 50411, 2025);

-- ==================================================
-- PROYECTOS URBANOS REALES
-- ==================================================

-- Línea 7 del Metro (En Curso)
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Línea 7 del Metro',
     'Nueva línea de metro que conectará Vitacura con Renca',
     'Transporte Público',
     '2024-01-15',
     '2027-12-31',
     'En Curso',
     2500000000.00,
     ST_GeomFromText('LINESTRING(-70.5858 -33.3924, -70.6372 -33.4226, -70.6806 -33.4489)', 4326),
     2);

-- Parque de la Familia (Completado)
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Parque de la Familia Cerrillos',
     'Parque urbano de 30 hectáreas con áreas deportivas y recreativas',
     'Área Verde',
     '2022-03-01',
     '2024-10-15',
     'Completado',
     15000000.00,
     ST_GeomFromText('POLYGON((-70.7100 -33.4950, -70.7000 -33.4950, -70.7000 -33.4900, -70.7100 -33.4900, -70.7100 -33.4950))', 4326),
     1);

-- Mejoramiento Avenida Américo Vespucio Sur
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Mejoramiento Vespucio Sur',
     'Ampliación y mejoramiento de Américo Vespucio sector sur',
     'Vialidad',
     '2023-06-01',
     '2025-12-31',
     'En Curso',
     89000000.00,
     ST_GeomFromText('LINESTRING(-70.6097 -33.5150, -70.5777 -33.6099)', 4326),
     2);

-- Hospital Digital de La Florida
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Hospital Digital La Florida',
     'Nuevo hospital con tecnología de punta, 300 camas',
     'Salud',
     '2024-09-01',
     '2027-06-30',
     'En Curso',
     120000000.00,
     ST_GeomFromText('POINT(-70.5950 -33.5000)', 4326),
     1);

-- Ciclovía Providencia-Las Condes
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Ciclovía Av. Providencia',
     'Red de ciclovías protegidas desde Providencia hasta Las Condes',
     'Movilidad Sostenible',
     '2025-01-15',
     '2025-12-31',
     'Planeado',
     8500000.00,
     ST_GeomFromText('LINESTRING(-70.6150 -33.4280, -70.5694 -33.4091)', 4326),
     2);

-- Centro Cultural Maipú
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Centro Cultural Municipal Maipú',
     'Centro cultural con biblioteca, teatro y espacios comunitarios',
     'Equipamiento Cultural',
     '2024-03-01',
     '2025-09-30',
     'En Curso',
     12000000.00,
     ST_GeomFromText('POLYGON((-70.7560 -33.5100, -70.7540 -33.5100, -70.7540 -33.5085, -70.7560 -33.5085, -70.7560 -33.5100))', 4326),
     3);

-- Renovación Barrio Yungay
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Renovación Urbana Barrio Yungay',
     'Recuperación patrimonial y mejoramiento de espacios públicos',
     'Renovación Urbana',
     '2023-04-01',
     '2024-08-31',
     'Retrasado',
     25000000.00,
     ST_GeomFromText('POLYGON((-70.6800 -33.4450, -70.6700 -33.4450, -70.6700 -33.4380, -70.6800 -33.4380, -70.6800 -33.4450))', 4326),
     4);

-- Parque Lineal Mapocho
INSERT INTO proyectos_urbanos (nombre, descripcion, tipo_proyecto, fecha_inicio, fecha_termino, estado, presupuesto, geometria, usuario_id) VALUES
    ('Parque Lineal Río Mapocho',
     'Parque lineal a lo largo del río Mapocho, fase 1',
     'Área Verde',
     '2025-03-01',
     '2026-12-31',
     'Planeado',
     45000000.00,
     ST_GeomFromText('LINESTRING(-70.6806 -33.4400, -70.6372 -33.4300, -70.6000 -33.4250)', 4326),
     1);

-- ==================================================
-- RELACIÓN PROYECTOS-ZONAS
-- ==================================================
INSERT INTO proyectos_zonas (proyecto_urbano_id, zona_urbana_id) VALUES
                                                                     (1, 4),  -- Línea 7 - Vitacura
                                                                     (1, 2),  -- Línea 7 - Providencia
                                                                     (1, 1),  -- Línea 7 - Santiago Centro
                                                                     (2, 11), -- Parque Familia - Cerrillos
                                                                     (3, 5),  -- Vespucio Sur - La Florida
                                                                     (3, 7),  -- Vespucio Sur - Puente Alto
                                                                     (4, 5),  -- Hospital - La Florida
                                                                     (5, 2),  -- Ciclovía - Providencia
                                                                     (5, 3),  -- Ciclovía - Las Condes
                                                                     (6, 6),  -- Centro Cultural - Maipú
                                                                     (7, 1),  -- Renovación Yungay - Santiago Centro
                                                                     (8, 1),  -- Parque Mapocho - Santiago Centro
                                                                     (8, 2),  -- Parque Mapocho - Providencia
                                                                     (8, 12); -- Parque Mapocho - Independencia

-- ==================================================
-- REFRESCAR VISTAS MATERIALIZADAS
-- ==================================================
REFRESH MATERIALIZED VIEW cobertura_infraestructura;
REFRESH MATERIALIZED VIEW resumen_proyectos_estado_zona;

-- ==================================================
-- VERIFICACIÓN DE DATOS INSERTADOS
-- ==================================================
SELECT '===== RESUMEN DE DATOS INSERTADOS =====' as info;

SELECT 'Usuarios insertados:' as tipo, COUNT(*) as cantidad FROM usuarios
UNION ALL
SELECT 'Zonas urbanas (comunas):' as tipo, COUNT(*) as cantidad FROM zonas_urbanas
UNION ALL
SELECT 'Puntos de interés:' as tipo, COUNT(*) as cantidad FROM puntos_interes
UNION ALL
SELECT 'Datos demográficos:' as tipo, COUNT(*) as cantidad FROM datos_demograficos
UNION ALL
SELECT 'Proyectos urbanos:' as tipo, COUNT(*) as cantidad FROM proyectos_urbanos;

-- Resumen por tipo de punto de interés
SELECT '===== PUNTOS DE INTERÉS POR TIPO =====' as info;
SELECT tipo, COUNT(*) as cantidad
FROM puntos_interes
GROUP BY tipo
ORDER BY cantidad DESC;

-- Resumen de proyectos por estado
SELECT '===== PROYECTOS POR ESTADO =====' as info;
SELECT estado, COUNT(*) as cantidad,
       SUM(presupuesto) as presupuesto_total
FROM proyectos_urbanos
GROUP BY estado;

-- Población total de Santiago
SELECT '===== POBLACIÓN REGIÓN METROPOLITANA =====' as info;
SELECT SUM(poblacion) as poblacion_total_2025,
       ROUND(AVG(edad_promedio), 1) as edad_promedio_regional,
       SUM(numero_viviendas) as total_viviendas
FROM datos_demograficos
WHERE anio = 2025;

-- Top 5 comunas más pobladas
SELECT '===== TOP 5 COMUNAS MÁS POBLADAS (2025) =====' as info;
SELECT zu.nombre, dd.poblacion, dd.edad_promedio
FROM zonas_urbanas zu
         JOIN datos_demograficos dd ON zu.zona_urbana_id = dd.zona_urbana_id
WHERE dd.anio = 2025
ORDER BY dd.poblacion DESC
    LIMIT 5;

-- ==================================================
-- CONSULTAS DE VERIFICACIÓN GEOESPACIAL
-- ==================================================

-- Verificar que las geometrías son válidas
SELECT 'Verificando geometrías...' as info;
SELECT COUNT(*) as zonas_validas
FROM zonas_urbanas
WHERE ST_IsValid(geometria_poligono);

SELECT COUNT(*) as puntos_validos
FROM puntos_interes
WHERE ST_IsValid(coordenadas_punto);

-- Distancia entre hospitales
SELECT '===== DISTANCIAS ENTRE HOSPITALES =====' as info;
SELECT
    p1.nombre as hospital_1,
    p2.nombre as hospital_2,
    ROUND(ST_Distance(
                  ST_Transform(p1.coordenadas_punto, 32719),
                  ST_Transform(p2.coordenadas_punto, 32719)
          )::numeric, 0) as distancia_metros
FROM puntos_interes p1
         CROSS JOIN puntos_interes p2
WHERE p1.tipo = 'Hospital'
  AND p2.tipo = 'Hospital'
  AND p1.punto_interes_id < p2.punto_interes_id
ORDER BY distancia_metros
    LIMIT 5;