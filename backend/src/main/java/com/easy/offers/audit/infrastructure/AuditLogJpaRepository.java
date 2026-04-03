package com.easy.offers.audit.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * AuditLogJpaRepository — Repositorio para la tabla `offer_audit_log`.
 *
 * Características especiales de este repositorio:
 *
 * 1. Solo INSERT y SELECT: nunca UPDATE ni DELETE desde la aplicación.
 *    Los registros de auditoría son inmutables por diseño.
 *
 * 2. Ordenamiento por created_at DESC: el historial se muestra del más
 *    reciente al más antiguo (Requerimiento 9.2).
 *
 * 3. Busca por offer_id: para obtener el historial de una oferta específica.
 *    Cuando la oferta es eliminada, offer_id queda en NULL en la BD,
 *    pero los registros siguen existiendo (Requerimiento 9.4).
 *
 * Método derivado con ordenamiento:
 * findByOfferIdOrderByCreatedAtDesc(Long offerId)
 * Spring Data genera: SELECT * FROM offer_audit_log WHERE offer_id = ? ORDER BY created_at DESC
 */
@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogJpaEntity, Long> {

    /**
     * Obtiene todos los registros de auditoría de una oferta, ordenados del más reciente al más antiguo.
     *
     * Requerimiento 9.2: retornar registros ordenados por created_at DESC.
     *
     * Nombre del método descompuesto:
     * findBy + OfferId + OrderBy + CreatedAt + Desc
     * → WHERE offer_id = ? ORDER BY created_at DESC
     *
     * @param offerId ID de la oferta (puede ser null para buscar registros huérfanos)
     * @return Lista de registros de auditoría ordenados por fecha descendente
     */
    List<AuditLogJpaEntity> findByOfferIdOrderByCreatedAtDesc(Long offerId);
}
