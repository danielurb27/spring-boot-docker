package com.easy.offers.offers.infrastructure;

import com.easy.offers.offers.domain.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * OfferJpaRepository — Repositorio principal para la tabla `offers`.
 *
 * Este es el repositorio más complejo del sistema porque necesita soportar
 * filtros opcionales y paginación para el endpoint GET /api/offers.
 *
 * El desafío: filtros opcionales
 * El usuario puede filtrar por sector_id, offer_type_id, status, starts_at y ends_at,
 * pero TODOS son opcionales. Si no se envía ningún filtro, se retornan todas las ofertas.
 *
 * Opciones para implementar filtros opcionales en Spring Data JPA:
 *
 * Opción 1: Múltiples métodos (findBySectorId, findBySectorIdAndStatus, etc.)
 * Problema: con 5 filtros opcionales, necesitaríamos 2^5 = 32 métodos. Inviable.
 *
 * Opción 2: @Query con JPQL y parámetros opcionales usando (:param IS NULL OR ...)
 * Ventaja: una sola query, fácil de entender.
 * Desventaja: puede ser menos eficiente porque el query planner no puede optimizar
 * bien las condiciones IS NULL.
 *
 * Opción 3: Specifications (JPA Criteria API)
 * Ventaja: muy flexible y type-safe.
 * Desventaja: más verboso y complejo para el MVP.
 *
 * Opción 4: QueryDSL
 * Ventaja: muy expresivo y type-safe.
 * Desventaja: requiere dependencia adicional y generación de código.
 *
 * Decisión: Opción 2 (@Query con JPQL) para el MVP.
 * Es el balance correcto entre simplicidad y funcionalidad.
 * Si el sistema crece y necesita filtros más complejos, se puede migrar a Specifications.
 *
 * ¿Qué es JPQL?
 * JPQL (Java Persistence Query Language) es el lenguaje de consultas de JPA.
 * Es similar a SQL pero opera sobre entidades Java en lugar de tablas.
 * Ejemplo: "FROM OfferJpaEntity o WHERE o.status = :status"
 * En lugar de: "SELECT * FROM offers WHERE status = ?"
 *
 * Ventaja de JPQL sobre SQL nativo: es independiente de la BD.
 * Si cambiamos de PostgreSQL a MySQL, las queries JPQL siguen funcionando.
 */
@Repository
public interface OfferJpaRepository extends JpaRepository<OfferJpaEntity, Long> {

    /**
     * Consulta con filtros opcionales y paginación.
     *
     * Técnica: (:param IS NULL OR o.campo = :param)
     * Si el parámetro es null, la condición siempre es true (no filtra).
     * Si el parámetro tiene valor, filtra por ese valor.
     *
     * Ejemplo con sector_id=5 y status=null:
     * WHERE (5 IS NULL OR o.sectorId = 5)     → o.sectorId = 5 (filtra)
     *   AND (null IS NULL OR o.status = null)  → true (no filtra)
     *
     * Page<T>: Spring Data retorna los resultados paginados.
     * Pageable: contiene el número de página, tamaño y ordenamiento.
     * El servicio crea el Pageable con PageRequest.of(page, 50, Sort.by("startsAt").descending())
     *
     * Requerimiento 6.1: máximo 50 registros por página.
     * Requerimiento 6.2: filtros opcionales.
     * Requerimiento 6.5: ordenamiento por starts_at DESC por defecto.
     *
     * @param sectorId      Filtro por sector (null = sin filtro)
     * @param offerTypeId   Filtro por tipo de oferta (null = sin filtro)
     * @param status        Filtro por estado (null = sin filtro)
     * @param startsAfter   Filtro: ofertas que empiezan después de esta fecha (null = sin filtro)
     * @param endsBefore    Filtro: ofertas que terminan antes de esta fecha (null = sin filtro)
     * @param pageable      Configuración de paginación y ordenamiento
     * @return Página de ofertas que cumplen los filtros
     */
    @Query("""
            SELECT o FROM OfferJpaEntity o
            WHERE (:sectorId IS NULL OR o.sectorId = :sectorId)
              AND (:offerTypeId IS NULL OR o.offerTypeId = :offerTypeId)
              AND (CAST(:status AS string) IS NULL OR o.status = :status)
              AND (CAST(:startsAfter AS java.time.LocalDateTime) IS NULL OR o.startsAt >= :startsAfter)
              AND (CAST(:endsBefore AS java.time.LocalDateTime) IS NULL OR o.endsAt <= :endsBefore)
            """)
    Page<OfferJpaEntity> findWithFilters(
            @Param("sectorId") Long sectorId,
            @Param("offerTypeId") Long offerTypeId,
            @Param("status") OfferStatus status,
            @Param("startsAfter") LocalDateTime startsAfter,
            @Param("endsBefore") LocalDateTime endsBefore,
            Pageable pageable
    );

    /**
     * Cuenta ofertas por estado.
     * Usado por el dashboard para mostrar los conteos (Requerimiento 10.1).
     *
     * SQL generado: SELECT COUNT(*) FROM offers WHERE status = ?
     */
    long countByStatus(OfferStatus status);

    /**
     * Obtiene las N ofertas más recientes de un estado dado.
     * Usado por el dashboard para mostrar las ofertas activas y próximas (Req 10.2).
     *
     * Pageable permite limitar el resultado a N registros:
     * PageRequest.of(0, 10, Sort.by("startsAt").descending()) → las 10 más recientes.
     *
     * Retorna List en lugar de Page porque el dashboard no necesita metadatos de paginación.
     */
    @Query("""
            SELECT o FROM OfferJpaEntity o
            WHERE o.status = :status
            ORDER BY o.startsAt DESC
            """)
    List<OfferJpaEntity> findTopByStatus(@Param("status") OfferStatus status, Pageable pageable);

    /**
     * Busca todas las ofertas con un estado dado.
     * Usado por StatusUpdateJob para recalcular estados periódicamente (Req 7.3).
     * También usado por CleanupJob para encontrar ofertas vencidas (Req 8.1).
     */
    List<OfferJpaEntity> findByStatus(OfferStatus status);

    /**
     * Busca ofertas vencidas cuyo ends_at es anterior a una fecha dada.
     * Usado por CleanupJob para eliminar ofertas que superaron el período de retención.
     *
     * Requerimiento 8.1: eliminar ofertas con ends_at < now - 21 días.
     *
     * SQL generado: SELECT * FROM offers WHERE status = 'VENCIDA' AND ends_at < ?
     */
    @Query("""
            SELECT o FROM OfferJpaEntity o
            WHERE o.status = 'VENCIDA'
              AND o.endsAt < :cutoffDate
            """)
    List<OfferJpaEntity> findExpiredBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
