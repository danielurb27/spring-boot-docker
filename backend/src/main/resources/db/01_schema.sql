-- =============================================================================
-- schema.sql — Definición del esquema de base de datos para Easy Offers.
--
-- ¿Qué es este archivo?
-- Contiene las instrucciones DDL (Data Definition Language) que crean la
-- estructura de la base de datos: tablas, columnas, restricciones e índices.
--
-- ¿Por qué SQL manual en lugar de dejar que Hibernate cree las tablas?
-- En application.yml configuramos ddl-auto=validate, lo que significa que
-- Hibernate VERIFICA el esquema pero NO lo crea ni modifica.
-- Esto es una práctica profesional importante:
--   1. Control total: sabemos exactamente qué hay en la BD.
--   2. Seguridad: Hibernate no puede borrar datos accidentalmente.
--   3. Revisión: los cambios de esquema pasan por revisión de código.
--   4. Reproducibilidad: cualquier entorno arranca con el mismo esquema.
--
-- ¿Cómo se ejecuta este archivo?
-- Spring Boot ejecuta automáticamente schema.sql al arrancar si está en
-- src/main/resources/db/ y se configura spring.sql.init.mode=always.
-- En Docker, también podemos montarlo en /docker-entrypoint-initdb.d/
-- para que PostgreSQL lo ejecute al crear el contenedor por primera vez.
--
-- Convención de nombres:
-- - Tablas: snake_case, plural (users, offers, sectors)
-- - Columnas: snake_case (created_at, offer_type_id)
-- - Constraints: tipo_tabla_columna (pk_offers, fk_offers_sector_id, uk_users_username)
-- - Índices: idx_tabla_columna (idx_offers_status)
-- =============================================================================

-- Usar IF NOT EXISTS en todas las creaciones para que el script sea idempotente.
-- Idempotente significa: se puede ejecutar múltiples veces sin errores ni efectos
-- secundarios. Esto es crucial para entornos de desarrollo donde reiniciamos
-- la BD frecuentemente.

-- =============================================================================
-- TABLA: users
-- Almacena los usuarios del sistema (administradores y empleados).
--
-- Decisiones de diseño:
-- - id BIGSERIAL: entero autoincremental. Más eficiente que UUID para PKs
--   en tablas con muchas relaciones (FK más pequeña = índices más pequeños).
-- - username UNIQUE: garantizado a nivel de BD, no solo en la aplicación.
--   La restricción de unicidad en BD es la última línea de defensa.
-- - password_hash VARCHAR(60): BCrypt siempre genera hashes de exactamente
--   60 caracteres. Definir el tamaño exacto evita desperdicio de espacio.
-- - role VARCHAR(20): almacenamos el nombre del enum como string ('ADMIN',
--   'EMPLOYEE'). Más legible en consultas directas que un código numérico.
-- - is_active BOOLEAN: soft delete. En lugar de borrar usuarios (lo que
--   rompería las FKs de auditoría), los desactivamos. Esto preserva la
--   trazabilidad histórica.
-- - created_at/updated_at: timestamps de auditoría automáticos.
--   DEFAULT NOW() los asigna automáticamente al insertar/actualizar.
-- =============================================================================
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL       PRIMARY KEY,
    full_name     VARCHAR(100)    NOT NULL,
    username      VARCHAR(150)    NOT NULL,
    password_hash VARCHAR(60)     NOT NULL,
    role          VARCHAR(20)     NOT NULL,
    is_active     BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,

    -- Restricciones inline:
    -- CHECK garantiza que solo se almacenen roles válidos.
    -- Esto es una segunda capa de validación después de la aplicación.
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'EMPLOYEE')),

    -- UNIQUE garantiza que no haya dos usuarios con el mismo username.
    -- La aplicación también valida esto, pero la BD es la fuente de verdad.
    CONSTRAINT uk_users_username UNIQUE (username)
);

COMMENT ON TABLE users IS 'Usuarios del sistema: administradores y empleados que gestionan ofertas.';
COMMENT ON COLUMN users.password_hash IS 'Hash BCrypt de la contraseña. Nunca almacenar contraseñas en texto plano.';
COMMENT ON COLUMN users.is_active IS 'Soft delete: false = usuario desactivado, no puede autenticarse.';


-- =============================================================================
-- TABLA: offer_types
-- Catálogo de tipos de oferta. Datos de referencia fijos.
--
-- Decisiones de diseño:
-- - Tabla separada en lugar de enum en la BD: más flexible para agregar
--   nuevos tipos sin alterar el esquema. Los tipos se cargan en data.sql.
-- - id BIGSERIAL: aunque los tipos son pocos, usamos BIGSERIAL por consistencia.
-- - name UNIQUE: no puede haber dos tipos con el mismo nombre.
-- - No tiene updated_at: estos datos son de referencia y no se modifican
--   por la API. Solo se actualizan mediante migraciones de esquema.
-- =============================================================================
CREATE TABLE IF NOT EXISTS offer_types (
    id         BIGSERIAL    PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    -- VARCHAR(20): ampliado desde 10 para acomodar códigos como 'OPORTUNEASY' (11 chars)
    code       VARCHAR(20)  NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_offer_types_name UNIQUE (name),
    CONSTRAINT uk_offer_types_code UNIQUE (code)
);

COMMENT ON TABLE offer_types IS 'Catálogo de tipos de oferta. Datos de referencia: Oferta interna, Feria de descuento, etc.';


-- =============================================================================
-- TABLA: sectors
-- Catálogo de sectores de la tienda. Datos de referencia fijos.
--
-- Decisiones de diseño:
-- - code VARCHAR(10): el código numérico del sector (ej: "13", "41").
--   Hacks and Racks no tiene código numérico, por eso es VARCHAR y no INT.
-- - name VARCHAR(100): nombre descriptivo del sector (ej: "Ferretería").
-- - Separado de offers para normalización: si el nombre de un sector cambia,
--   solo se actualiza en un lugar.
-- =============================================================================
CREATE TABLE IF NOT EXISTS sectors (
    id         BIGSERIAL    PRIMARY KEY,
    code       VARCHAR(10),  -- Nullable: Hacks and Racks no tiene código numérico
    name       VARCHAR(100) NOT NULL,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uk_sectors_name UNIQUE (name)
);

COMMENT ON TABLE sectors IS 'Catálogo de sectores de la tienda. Ej: 13 Ferretería, 41 Baños, Hacks and Racks.';
COMMENT ON COLUMN sectors.code IS 'Código numérico del sector. NULL para Hacks and Racks (sección sin código).';


-- =============================================================================
-- TABLA: offers
-- Tabla principal del sistema. Almacena todas las ofertas comerciales.
--
-- Decisiones de diseño:
-- - id BIGSERIAL: PK autoincremental. Más eficiente que UUID para la tabla
--   más consultada del sistema.
-- - title VARCHAR(200): límite razonable para un título de oferta.
-- - description TEXT: sin límite de longitud para descripciones largas.
-- - offer_type_id / sector_id: FKs a las tablas de catálogo.
--   ON DELETE RESTRICT (default): no se puede borrar un tipo/sector si hay
--   ofertas que lo referencian. Protege la integridad referencial.
-- - status VARCHAR(20): estado calculado por StatusEngine.
--   Valores: 'PROXIMA', 'ACTIVA', 'VENCIDA'.
--   Se persiste para eficiencia de consultas (evita recalcular en cada SELECT).
-- - starts_at / ends_at TIMESTAMPTZ: timestamps con zona horaria.
--   TIMESTAMPTZ almacena en UTC internamente y convierte según la zona del cliente.
--   Esto evita bugs de horario de verano/invierno.
-- - created_by / updated_by: FKs a users para trazabilidad.
--   ON DELETE SET NULL: si se borra un usuario, las ofertas que creó no se borran,
--   pero el campo queda en NULL (perdemos la referencia pero no los datos).
--   Alternativa considerada: ON DELETE RESTRICT (no permitir borrar usuarios con
--   ofertas). Elegimos SET NULL para mayor flexibilidad operativa.
-- =============================================================================
CREATE TABLE IF NOT EXISTS offers (
    id            BIGSERIAL       PRIMARY KEY,
    title         VARCHAR(200)    NOT NULL,
    description   TEXT,
    offer_type_id BIGINT          NOT NULL,
    sector_id     BIGINT          NOT NULL,
    status        VARCHAR(20)     NOT NULL DEFAULT 'PROXIMA',
    starts_at     TIMESTAMPTZ     NOT NULL,
    ends_at       TIMESTAMPTZ     NOT NULL,
    created_by    BIGINT,
    updated_by    BIGINT,
    created_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,

    -- Validación de rango de fechas a nivel de BD (Requerimiento 3.2, 4.6).
    -- La aplicación también valida esto, pero la BD es la última línea de defensa.
    CONSTRAINT chk_offers_dates CHECK (starts_at < ends_at),

    -- Validación de estado: solo valores del enum OfferStatus.
    CONSTRAINT chk_offers_status CHECK (status IN ('PROXIMA', 'ACTIVA', 'VENCIDA')),

    -- FK hacia offer_types: no se puede crear una oferta con un tipo inexistente.
    CONSTRAINT fk_offers_offer_type
        FOREIGN KEY (offer_type_id) REFERENCES offer_types(id)
        ON DELETE RESTRICT,

    -- FK hacia sectors: no se puede crear una oferta con un sector inexistente.
    CONSTRAINT fk_offers_sector
        FOREIGN KEY (sector_id) REFERENCES sectors(id)
        ON DELETE RESTRICT,

    -- FK hacia users (creador): SET NULL si el usuario es eliminado.
    CONSTRAINT fk_offers_created_by
        FOREIGN KEY (created_by) REFERENCES users(id)
        ON DELETE SET NULL,

    -- FK hacia users (último editor): SET NULL si el usuario es eliminado.
    CONSTRAINT fk_offers_updated_by
        FOREIGN KEY (updated_by) REFERENCES users(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE offers IS 'Ofertas comerciales. Tabla principal del sistema.';
COMMENT ON COLUMN offers.status IS 'Estado calculado por StatusEngine: PROXIMA, ACTIVA o VENCIDA. Se recalcula cada hora.';
COMMENT ON COLUMN offers.starts_at IS 'Fecha y hora de inicio de la oferta (UTC).';
COMMENT ON COLUMN offers.ends_at IS 'Fecha y hora de fin de la oferta (UTC).';


-- =============================================================================
-- TABLA: offer_audit_log
-- Registro inmutable de todos los cambios realizados sobre las ofertas.
--
-- Decisiones de diseño:
-- - id BIGSERIAL: autoincremental. Esta tabla puede crecer mucho (un registro
--   por cada campo modificado en cada edición).
-- - offer_id NULLABLE con ON DELETE SET NULL: si se borra una oferta, los
--   registros de auditoría se preservan con offer_id = NULL.
--   Esto cumple el Requerimiento 9.4: la auditoría es inmutable.
-- - changed_by NULLABLE: puede ser NULL para eventos AUTO_DELETE (el sistema
--   los genera, no un usuario específico).
-- - change_type VARCHAR(30): tipo de evento. Valores: CREATE, UPDATE, DELETE,
--   AUTO_DELETE. Definido por el enum ChangeType en el dominio.
-- - field_changed VARCHAR(50): nombre del campo modificado (ej: 'title', 'status').
--   NULL para eventos CREATE y DELETE (no hay campo específico).
-- - old_value / new_value TEXT: valores antes y después del cambio.
--   TEXT sin límite porque los valores pueden ser largos (ej: description).
-- - observation TEXT: notas adicionales opcionales sobre el cambio.
-- - NO tiene updated_at: los registros de auditoría son INMUTABLES.
--   Una vez creados, nunca se modifican. Esto garantiza la integridad del log.
-- =============================================================================
CREATE TABLE IF NOT EXISTS offer_audit_log (
    id            BIGSERIAL    PRIMARY KEY,
    offer_id      BIGINT,       -- NULL si la oferta fue eliminada (ON DELETE SET NULL)
    changed_by    BIGINT,       -- NULL para eventos AUTO_DELETE del sistema
    change_type   VARCHAR(30)  NOT NULL,
    field_changed VARCHAR(50),  -- NULL para CREATE y DELETE
    old_value     TEXT,
    new_value     TEXT,
    observation   TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    -- Validación del tipo de cambio.
    CONSTRAINT chk_audit_change_type
        CHECK (change_type IN ('CREATE', 'UPDATE', 'DELETE', 'AUTO_DELETE')),

    -- FK hacia offers: SET NULL cuando la oferta es eliminada.
    -- Esto es lo que permite preservar el historial de auditoría (Req 9.4).
    CONSTRAINT fk_audit_offer
        FOREIGN KEY (offer_id) REFERENCES offers(id)
        ON DELETE SET NULL,

    -- FK hacia users: SET NULL si el usuario es eliminado.
    CONSTRAINT fk_audit_changed_by
        FOREIGN KEY (changed_by) REFERENCES users(id)
        ON DELETE SET NULL
);

COMMENT ON TABLE offer_audit_log IS 'Registro inmutable de cambios en ofertas. Los registros nunca se modifican ni eliminan.';
COMMENT ON COLUMN offer_audit_log.offer_id IS 'NULL si la oferta fue eliminada. El historial se preserva (Req 9.4).';
COMMENT ON COLUMN offer_audit_log.changed_by IS 'NULL para eventos AUTO_DELETE generados por el sistema.';


-- =============================================================================
-- ÍNDICES DE PERFORMANCE — Tabla offers
--
-- ¿Qué es un índice en una base de datos?
-- Un índice es una estructura de datos auxiliar (generalmente un B-Tree) que
-- PostgreSQL mantiene junto a la tabla. Permite encontrar filas rápidamente
-- sin escanear toda la tabla (full table scan).
--
-- Analogía: el índice de un libro. Sin él, para encontrar "Ferretería" tendrías
-- que leer página por página. Con el índice, vas directo a la página correcta.
--
-- Trade-off de los índices:
-- VENTAJA: consultas SELECT mucho más rápidas (O(log n) vs O(n)).
-- DESVENTAJA: cada INSERT/UPDATE/DELETE es un poco más lento porque PostgreSQL
-- debe actualizar también el índice. Para una tabla de ofertas con pocas
-- escrituras y muchas lecturas, el trade-off es claramente favorable.
--
-- ¿Por qué estos 4 índices específicos?
-- Son los campos más usados en los filtros de GET /api/offers (Req 6.2)
-- y en el ordenamiento por defecto (Req 6.5).
-- Sin índices, una consulta sobre 10.000 ofertas podría tardar segundos.
-- Con índices, debería estar por debajo de los 500ms requeridos (Req 6.4).
-- =============================================================================

-- Índice en offer_type_id: optimiza filtros por tipo de oferta.
-- Ejemplo: GET /api/offers?offer_type_id=3 (todas las ofertas de tipo "Folleto")
-- Sin índice: PostgreSQL lee las 10.000 filas y filtra.
-- Con índice: PostgreSQL va directo a las filas con offer_type_id=3.
CREATE INDEX IF NOT EXISTS idx_offers_offer_type_id ON offers(offer_type_id);

-- Índice en sector_id: optimiza filtros por sector.
-- Ejemplo: GET /api/offers?sector_id=5 (todas las ofertas de "Ferretería")
CREATE INDEX IF NOT EXISTS idx_offers_sector_id ON offers(sector_id);

-- Índice en status: optimiza filtros por estado y el dashboard.
-- Ejemplo: GET /api/offers?status=ACTIVA (todas las ofertas activas)
-- También usado por el StatusUpdateJob para encontrar ofertas a recalcular.
CREATE INDEX IF NOT EXISTS idx_offers_status ON offers(status);

-- Índice en starts_at DESC: optimiza el ordenamiento por defecto.
-- El sufijo DESC indica que el índice está ordenado de mayor a menor.
-- Esto hace que ORDER BY starts_at DESC sea O(1) en lugar de O(n log n).
-- También útil para el CleanupJob que busca ofertas con ends_at antiguo.
CREATE INDEX IF NOT EXISTS idx_offers_starts_at ON offers(starts_at DESC);

-- Índice compuesto opcional: status + starts_at para el dashboard.
-- Cuando el dashboard filtra por status='ACTIVA' Y ordena por starts_at,
-- este índice compuesto es más eficiente que dos índices separados.
-- PostgreSQL puede satisfacer tanto el filtro como el orden con un solo recorrido.
CREATE INDEX IF NOT EXISTS idx_offers_status_starts_at ON offers(status, starts_at DESC);
