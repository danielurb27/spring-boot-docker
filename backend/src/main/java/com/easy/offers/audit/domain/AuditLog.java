package com.easy.offers.audit.domain;

import java.time.Instant;

/**
 * AuditLog — Entidad de dominio que representa un registro de auditoría.
 *
 * Cada instancia de AuditLog es un evento inmutable que documenta un cambio
 * en el sistema. Una vez creado, un AuditLog nunca se modifica ni se elimina.
 *
 * Inmutabilidad y auditoría:
 * La inmutabilidad es especialmente importante para los registros de auditoría.
 * Si alguien pudiera modificar un AuditLog, la trazabilidad perdería su valor.
 * El uso de record garantiza que una vez creado el objeto, sus datos no cambian.
 *
 * Campos nullable y su significado:
 *
 * offerId: puede ser null si la oferta fue eliminada del sistema.
 * La BD usa ON DELETE SET NULL, así que el registro de auditoría persiste
 * pero pierde la referencia a la oferta. Esto cumple el Requerimiento 9.4.
 *
 * changedBy: puede ser null para eventos AUTO_DELETE (generados por el sistema,
 * no por un usuario específico). También puede quedar null si el usuario
 * que hizo el cambio fue eliminado del sistema.
 *
 * fieldChanged: null para eventos CREATE y DELETE (no hay campo específico).
 * Para UPDATE, contiene el nombre del campo modificado (ej: "title").
 *
 * oldValue / newValue: null para CREATE (no había valor anterior) y DELETE
 * (no hay valor nuevo). Para UPDATE, contienen los valores como String.
 * Usamos String para todos los tipos porque es el formato más universal
 * para almacenar valores históricos (fechas, números, textos, etc.).
 *
 * observation: campo libre para notas adicionales. Usado por AUTO_DELETE
 * para documentar la razón de la eliminación automática.
 *
 * Requerimiento: 9.1 — Campos obligatorios del registro de auditoría.
 */
public record AuditLog(
        Long id,

        /**
         * ID de la oferta afectada. Null si la oferta fue eliminada posteriormente.
         */
        Long offerId,

        /**
         * ID del usuario que realizó el cambio. Null para AUTO_DELETE.
         */
        Long changedBy,

        /**
         * Tipo de evento: CREATE, UPDATE, DELETE, AUTO_DELETE.
         */
        ChangeType changeType,

        /**
         * Nombre del campo modificado. Null para CREATE y DELETE.
         * Ejemplos: "title", "starts_at", "sector_id", "status"
         */
        String fieldChanged,

        /**
         * Valor del campo antes del cambio. Null para CREATE.
         * Todos los valores se convierten a String para uniformidad.
         */
        String oldValue,

        /**
         * Valor del campo después del cambio. Null para DELETE.
         */
        String newValue,

        /**
         * Notas adicionales opcionales sobre el cambio.
         * Ejemplo: "Eliminación automática por período de retención de 21 días"
         */
        String observation,

        /**
         * Timestamp exacto del evento. Nunca null.
         * Usamos Instant (UTC) para timestamps de sistema.
         */
        Instant createdAt
) {
    /**
     * Constructor compacto: valida los campos obligatorios.
     */
    public AuditLog {
        if (changeType == null) {
            throw new IllegalArgumentException("El tipo de cambio no puede ser nulo");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("El timestamp del evento no puede ser nulo");
        }
    }
}
