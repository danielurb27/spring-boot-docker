package com.easy.offers.audit.domain;

/**
 * ChangeType — Enum que representa el tipo de evento registrado en la auditoría.
 *
 * Cada vez que algo cambia en una oferta, el AuditLogger registra un evento
 * con uno de estos tipos. Esto permite saber de un vistazo qué pasó con
 * una oferta sin necesidad de comparar valores.
 *
 * Requerimiento: 9.1 — El Audit_Logger debe registrar el change_type en cada evento.
 *
 * Uso en la tabla offer_audit_log:
 * - CREATE: una fila por oferta creada (sin field_changed, old_value ni new_value)
 * - UPDATE: una fila por CADA campo modificado (con field_changed, old_value, new_value)
 * - DELETE: una fila por oferta eliminada manualmente por un Admin
 * - AUTO_DELETE: una fila por oferta eliminada automáticamente por el CleanupJob
 *
 * Ejemplo de registros para una edición que cambia título y fechas:
 *   offer_id | change_type | field_changed | old_value        | new_value
 *   ---------|-------------|---------------|------------------|------------------
 *   42       | UPDATE      | title         | "Oferta verano"  | "Oferta invierno"
 *   42       | UPDATE      | starts_at     | "2024-06-01"     | "2024-07-01"
 *   42       | UPDATE      | ends_at       | "2024-06-30"     | "2024-07-31"
 *
 * ¿Por qué un registro por campo y no uno por operación?
 * Granularidad: permite ver exactamente qué cambió. Si solo guardáramos
 * "se editó la oferta 42", no sabríamos qué campos cambiaron ni sus valores anteriores.
 */
public enum ChangeType {

    /**
     * CREATE: Se creó una nueva oferta.
     * field_changed = null (no hay campo específico, es la oferta completa)
     * old_value = null (no había valor anterior)
     * new_value = null (los datos iniciales están en la oferta misma)
     */
    CREATE,

    /**
     * UPDATE: Se modificó un campo de una oferta existente.
     * field_changed = nombre del campo (ej: "title", "starts_at", "sector_id")
     * old_value = valor antes del cambio (como String)
     * new_value = valor después del cambio (como String)
     * Se genera un registro por cada campo modificado.
     */
    UPDATE,

    /**
     * DELETE: Un Admin eliminó manualmente la oferta.
     * changed_by = ID del Admin que ejecutó la eliminación
     * field_changed = null
     */
    DELETE,

    /**
     * AUTO_DELETE: El CleanupJob eliminó la oferta automáticamente
     * por haber superado el período de retención de 21 días (Req 8.1).
     * changed_by = null (no hay usuario, es el sistema)
     * observation = "Eliminación automática por período de retención de 21 días"
     */
    AUTO_DELETE
}
