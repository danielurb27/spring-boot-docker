package com.easy.offers.audit.application;

import com.easy.offers.audit.domain.AuditLog;
import com.easy.offers.audit.domain.ChangeType;
import com.easy.offers.audit.infrastructure.AuditLogJpaEntity;
import com.easy.offers.audit.infrastructure.AuditLogJpaRepository;
import com.easy.offers.audit.infrastructure.AuditMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * AuditService — Implementación de AuditLogger.
 *
 * Responsabilidad: construir registros de auditoría y persistirlos.
 *
 * Decisión de diseño: AuditService es llamado DENTRO de la transacción
 * de OfferService. Esto garantiza que si la operación principal falla,
 * el registro de auditoría también se revierte (atomicidad).
 *
 * Si quisiéramos que la auditoría persista incluso cuando la operación
 * principal falla, usaríamos @Transactional(propagation = REQUIRES_NEW).
 * Para el MVP, la auditoría dentro de la misma transacción es suficiente.
 *
 * @Service: Spring gestiona esta clase como bean singleton.
 */
@Service
public class AuditService implements AuditLogger {

    private final AuditLogJpaRepository auditRepository;
    private final AuditMapper auditMapper;

    public AuditService(AuditLogJpaRepository auditRepository, AuditMapper auditMapper) {
        this.auditRepository = auditRepository;
        this.auditMapper = auditMapper;
    }

    /**
     * Registra la creación de una oferta.
     * field_changed, old_value y new_value son null para CREATE
     * (no hay campo específico ni valor anterior).
     */
    @Override
    public void logCreate(Long offerId, Long userId) {
        save(new AuditLog(
                null,           // id: la BD lo asigna
                offerId,
                userId,
                ChangeType.CREATE,
                null,           // fieldChanged: no aplica para CREATE
                null,           // oldValue: no había valor anterior
                null,           // newValue: los datos están en la oferta misma
                "Oferta creada",
                Instant.now()
        ));
    }

    /**
     * Registra la modificación de un campo.
     * Se llama una vez por cada campo que cambió en la edición.
     *
     * Ejemplo: si se cambia título y fechas, se generan 3 registros:
     * - UPDATE, "title", "Oferta verano", "Oferta invierno"
     * - UPDATE, "starts_at", "2024-06-01T00:00", "2024-07-01T00:00"
     * - UPDATE, "ends_at", "2024-06-30T23:59", "2024-07-31T23:59"
     */
    @Override
    public void logUpdate(Long offerId, Long userId, String fieldChanged, String oldValue, String newValue) {
        save(new AuditLog(
                null,
                offerId,
                userId,
                ChangeType.UPDATE,
                fieldChanged,
                oldValue,
                newValue,
                null,           // observation: opcional para UPDATE
                Instant.now()
        ));
    }

    /**
     * Registra la eliminación manual por un Admin.
     */
    @Override
    public void logDelete(Long offerId, Long userId) {
        save(new AuditLog(
                null,
                offerId,
                userId,
                ChangeType.DELETE,
                null,
                null,
                null,
                "Oferta eliminada manualmente por administrador",
                Instant.now()
        ));
    }

    /**
     * Registra la eliminación automática por el CleanupJob.
     * changed_by es null porque no hay usuario responsable — es el sistema.
     */
    @Override
    public void logAutoDelete(Long offerId) {
        save(new AuditLog(
                null,
                offerId,
                null,           // changedBy: null para AUTO_DELETE (es el sistema)
                ChangeType.AUTO_DELETE,
                null,
                null,
                null,
                "Eliminación automática por período de retención de 21 días",
                Instant.now()
        ));
    }

    /**
     * Método auxiliar: convierte AuditLog de dominio a JPA y persiste.
     */
    private void save(AuditLog auditLog) {
        AuditLogJpaEntity entity = auditMapper.toJpa(auditLog);
        auditRepository.save(entity);
    }
}
