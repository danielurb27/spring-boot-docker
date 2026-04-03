package com.easy.offers.audit.application;

import com.easy.offers.audit.domain.AuditLog;
import com.easy.offers.audit.infrastructure.AuditLogJpaRepository;
import com.easy.offers.audit.infrastructure.AuditMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AuditQueryService — Caso de uso para consultar el historial de auditoría.
 *
 * ¿Por qué separar AuditService de AuditQueryService?
 * Principio de Responsabilidad Única (SRP):
 * - AuditService: escribe registros de auditoría (operaciones de escritura)
 * - AuditQueryService: lee el historial de auditoría (operaciones de lectura)
 *
 * Esta separación también facilita aplicar @Transactional(readOnly = true)
 * solo a las operaciones de lectura, lo que mejora el rendimiento porque
 * Hibernate no rastrea cambios en las entidades leídas.
 *
 * Requerimiento: 9.2 — Retornar registros ordenados por created_at DESC.
 */
@Service
@Transactional(readOnly = true)
public class AuditQueryService {

    private final AuditLogJpaRepository auditRepository;
    private final AuditMapper auditMapper;

    public AuditQueryService(AuditLogJpaRepository auditRepository, AuditMapper auditMapper) {
        this.auditRepository = auditRepository;
        this.auditMapper = auditMapper;
    }

    /**
     * Obtiene el historial de auditoría de una oferta, ordenado del más reciente al más antiguo.
     *
     * Nota: si la oferta fue eliminada, offer_id en la BD es NULL.
     * Este método busca por el ID original de la oferta, que puede no existir
     * en la tabla offers pero sí en offer_audit_log (con offer_id = NULL).
     *
     * Para el caso de uso principal (ver historial de una oferta existente),
     * el offerId siempre corresponde a una oferta activa.
     *
     * Requerimiento: 9.2 — Registros ordenados por created_at DESC.
     *
     * @param offerId ID de la oferta cuyo historial se quiere consultar
     * @return Lista de registros de auditoría ordenados por fecha descendente
     */
    public List<AuditLog> getAuditHistory(Long offerId) {
        return auditRepository
                .findByOfferIdOrderByCreatedAtDesc(offerId)
                .stream()
                .map(auditMapper::toDomain)
                .toList();
    }
}
