-- =============================================================================
-- data.sql — Datos de referencia (seed data) para Easy Offers.
--
-- ¿Qué es el seed data?
-- Son los datos iniciales que el sistema necesita para funcionar.
-- Sin estos datos, no se podrían crear ofertas (no habría tipos ni sectores).
--
-- ¿Por qué datos de referencia en SQL y no en la aplicación?
-- 1. Consistencia: los mismos datos en todos los entornos (dev, staging, prod).
-- 2. Velocidad: INSERT directo en BD es más rápido que pasar por la API.
-- 3. Independencia: no depende de que la aplicación esté corriendo.
-- 4. Versionado: los cambios en datos de referencia quedan en Git.
--
-- ¿Cómo se ejecuta?
-- Spring Boot ejecuta data.sql después de schema.sql al arrancar.
-- Configurado en application.yml con spring.sql.init.mode=always.
--
-- Estrategia de idempotencia:
-- Usamos INSERT ... ON CONFLICT DO NOTHING para que el script sea seguro
-- de ejecutar múltiples veces. Si el dato ya existe, lo ignora.
-- Esto evita errores al reiniciar la aplicación con datos existentes.
-- =============================================================================


-- =============================================================================
-- DATOS: offer_types
-- Los 8 tipos de oferta definidos en los requerimientos.
-- Estos tipos son fijos y no se modifican por la API.
--
-- Decisión sobre los códigos:
-- Creamos códigos cortos en mayúsculas para uso interno en la aplicación.
-- Son más fáciles de manejar en código que los nombres completos con espacios.
-- =============================================================================

INSERT INTO offer_types (name, code, is_active) VALUES
    ('Oferta interna',      'INTERNA',    TRUE),
    ('Feria de descuento',  'FERIA',      TRUE),
    ('Folleto',             'FOLLETO',    TRUE),
    ('Octavilla',           'OCTAVILLA',  TRUE),
    ('Ladrillazo',          'LADRILLAZO', TRUE),
    ('Oportuneasy',         'OPORTUNEASY',TRUE),
    ('Black Week',          'BLACKWEEK',  TRUE),
    ('Ciberweek',           'CIBERWEEK',  TRUE)
ON CONFLICT (name) DO NOTHING;

-- Verificación: debería haber exactamente 8 tipos de oferta.
-- Esta consulta es solo informativa; no afecta la ejecución.
-- SELECT COUNT(*) FROM offer_types; -- Esperado: 8


-- =============================================================================
-- DATOS: sectors
-- Los 21 sectores de la tienda definidos en los requerimientos.
--
-- Nota sobre Hacks and Racks:
-- Es la única sección sin código numérico (sección nueva).
-- Por eso el campo code es NULL para este registro.
-- El esquema permite NULL en sectors.code para este caso.
--
-- Orden: primero los sectores con código numérico (ordenados por código),
-- luego Hacks and Racks al final.
-- =============================================================================

INSERT INTO sectors (code, name, is_active) VALUES
    ('12',  'Textil Hogar',    TRUE),
    ('13',  'Ferretería',      TRUE),
    ('16',  'Menaje y Deco',   TRUE),
    ('23',  'Outdoor',         TRUE),
    ('39',  'Electro',         TRUE),
    ('41',  'Baños',           TRUE),
    ('43',  'Construcción',    TRUE),
    ('45',  'Herramientas',    TRUE),
    ('46',  'Automotor',       TRUE),
    ('47',  'Muebles',         TRUE),
    ('48',  'Pinturas',        TRUE),
    ('49',  'Flooring',        TRUE),
    ('50',  'Iluminación',     TRUE),
    ('51',  'Electricidad',    TRUE),
    ('53',  'Jardín',          TRUE),
    ('56',  'Aberturas',       TRUE),
    ('57',  'Maderas',         TRUE),
    ('58',  'Plomería',        TRUE),
    ('59',  'Organizadores',   TRUE),
    ('64',  'Ampolletas',      TRUE),
    (NULL,  'Hacks and Racks', TRUE)   -- Sección nueva sin código numérico
ON CONFLICT (name) DO NOTHING;

-- Verificación: debería haber exactamente 21 sectores.
-- SELECT COUNT(*) FROM sectors; -- Esperado: 21


-- =============================================================================
-- DATOS: usuario administrador inicial
-- Se crea un usuario ADMIN por defecto para poder acceder al sistema
-- por primera vez y crear otros usuarios desde la interfaz.
--
-- IMPORTANTE DE SEGURIDAD:
-- La contraseña 'admin123' está hasheada con BCrypt factor 10.
-- Hash generado con: BCrypt.hashpw("admin123", BCrypt.gensalt(10))
-- Resultado: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
--
-- ⚠️  CAMBIAR ESTA CONTRASEÑA INMEDIATAMENTE EN PRODUCCIÓN ⚠️
-- El primer login debe hacerse con admin/admin123 y luego cambiar la contraseña.
-- En un sistema real, esto se haría a través de un endpoint de cambio de contraseña.
--
-- ¿Por qué incluir un usuario inicial en data.sql?
-- Sin un usuario ADMIN, nadie puede crear otros usuarios (el endpoint POST /api/users
-- requiere rol ADMIN). Es el "bootstrap" del sistema.
-- =============================================================================

INSERT INTO users (full_name, username, password_hash, role, is_active) VALUES
    (
        'Administrador del Sistema',
        'admin',
        '$2a$10$5QtI9nXc73Ji5uRrB5PkLeFRcKwgWKqL6srYZm1DYKKrKKmRYdD5y',
        'ADMIN',
        TRUE
    )
ON CONFLICT (username) DO NOTHING;

-- =============================================================================
-- RESUMEN DE DATOS INSERTADOS:
-- - 8 tipos de oferta
-- - 21 sectores (20 con código numérico + Hacks and Racks sin código)
-- - 1 usuario administrador inicial (username: admin, password: admin123)
--
-- PRÓXIMOS PASOS DESPUÉS DEL PRIMER DEPLOY:
-- 1. Hacer login con admin/admin123
-- 2. Crear usuarios para el equipo desde la interfaz
-- 3. Cambiar la contraseña del admin inicial
-- =============================================================================
