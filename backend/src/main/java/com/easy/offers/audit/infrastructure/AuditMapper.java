package com.easy.offers.audit.infrastructure;

import com.easy.offers.audit.domain.AuditLog;
import org.springframework.stereotype.Component;

/**
 * AuditMapper — Convierte entre AuditLog (dominio) y AuditLogJpaEntity (infraestructura).
 *
 * Los registros de auditoría son de solo escritura (INSERT) y solo lectura (SELECT).
 * Nunca se actualizan (UPDATE) ni se borran (DELETE) desde la aplicación.
 *
 * Por eso este mapper es más simple que los otros:
 * - toDomain: para leer el historial de auditoría (GET /api/offers/{id}/audit)
 * - toJpa: para registrar nuevos eventos de auditoría
 */
@Component
public class AuditMapper {

    /**
     * AuditLogJpaEntity → AuditLog (dominio).
     * Se usa al consultar el historial de auditoría de una oferta.
     */
    public AuditLog toDomain(AuditLogJpaEntity entity) {
        if (entity == null) return null;

        return new AuditLog(
                entity.getId(),
                entity.getOfferId(),
                entity.getChangedBy(),
                entity.getChangeType(),
                entity.getFieldChanged(),
                entity.getOldValue(),
                entity.getNewValue(),
                entity.getObservation(),
                entity.getCreatedAt()
        );
    }

    /**
     * AuditLog (dominio) → AuditLogJpaEntity.
     * Se usa al registrar un nuevo evento de auditoría.
     *
     * Nota: el id siempre es null al crear (la BD lo asigna con BIGSERIAL).
     * Los setters son package-private en AuditLogJpaEntity para reforzar
     * que solo el mapper (en el mismo paquete) puede construir la entidad.
     */
    public AuditLogJpaEntity toJpa(AuditLog auditLog) {
        if (auditLog == null) return null;

        AuditLogJpaEntity entity = new AuditLogJpaEntity();
        entity.setOfferId(auditLog.offerId());
        entity.setChangedBy(auditLog.changedBy());
        entity.setChangeType(auditLog.changeType());
        entity.setFieldChanged(auditLog.fieldChanged());
        entity.setOldValue(auditLog.oldValue());
        entity.setNewValue(auditLog.newValue());
        entity.setObservation(auditLog.observation());
        entity.setCreatedAt(auditLog.createdAt());
        return entity;
    }
}
