package com.easy.offers.audit.api;

import com.easy.offers.audit.domain.AuditLog;

import java.time.Instant;

/**
 * AuditLogResponse — DTO de salida para el historial de auditoría.
 *
 * Representa un registro de auditoría en las respuestas JSON.
 * El changeType se expone como String para que el frontend lo use directamente.
 *
 * Campos nullable y su significado en la respuesta:
 * - offerId: null si la oferta fue eliminada (ON DELETE SET NULL)
 * - changedBy: null para eventos AUTO_DELETE (generados por el sistema)
 * - fieldChanged: null para eventos CREATE y DELETE
 * - oldValue/newValue: null según el tipo de evento
 */
public record AuditLogResponse(
        Long id,
        Long offerId,
        Long changedBy,
        String changeType,      // "CREATE", "UPDATE", "DELETE", "AUTO_DELETE"
        String fieldChanged,
        String oldValue,
        String newValue,
        String observation,
        Instant createdAt
) {
    /**
     * Factory method: construye AuditLogResponse desde la entidad de dominio AuditLog.
     */
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(
                log.id(),
                log.offerId(),
                log.changedBy(),
                log.changeType().name(),    // Enum → String
                log.fieldChanged(),
                log.oldValue(),
                log.newValue(),
                log.observation(),
                log.createdAt()
        );
    }
}
