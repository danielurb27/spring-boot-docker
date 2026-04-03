package com.easy.offers.offers.application;

import com.easy.offers.audit.application.AuditLogger;
import com.easy.offers.offers.domain.Offer;
import com.easy.offers.offers.domain.OfferStatus;
import com.easy.offers.offers.domain.StatusEngine;
import com.easy.offers.offers.infrastructure.*;
import com.easy.offers.shared.exception.InvalidDateRangeException;
import com.easy.offers.shared.exception.InvalidReferenceException;
import com.easy.offers.shared.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OfferService — Casos de uso para la gestión de ofertas.
 *
 * Responsabilidades:
 * 1. Crear ofertas (con validación de fechas, referencias y cálculo de estado)
 * 2. Editar ofertas (con recálculo de estado y auditoría por campo)
 * 3. Eliminar ofertas (solo Admin, con auditoría)
 * 4. Consultar y filtrar ofertas (con paginación)
 * 5. Dashboard (conteos y listas por estado)
 *
 * Patrón de auditoría en edición:
 * Para cada campo que cambia, comparamos el valor anterior con el nuevo.
 * Si son diferentes, registramos un evento UPDATE con old_value y new_value.
 * Esto da trazabilidad granular: sabemos exactamente qué cambió y cuándo.
 *
 * @Transactional: todas las operaciones de escritura son atómicas.
 * Si falla la auditoría, también se revierte la oferta (y viceversa).
 */
@Service
@Transactional
public class OfferService {

    // Límite máximo de registros por página (Requerimiento 6.1)
    private static final int MAX_PAGE_SIZE = 50;

    // Número de ofertas recientes a mostrar en el dashboard por estado (Req 10.2)
    private static final int DASHBOARD_LIMIT = 10;

    private final OfferJpaRepository offerRepository;
    private final OfferTypeJpaRepository offerTypeRepository;
    private final SectorJpaRepository sectorRepository;
    private final OfferMapper offerMapper;
    private final AuditLogger auditLogger;

    public OfferService(
            OfferJpaRepository offerRepository,
            OfferTypeJpaRepository offerTypeRepository,
            SectorJpaRepository sectorRepository,
            OfferMapper offerMapper,
            AuditLogger auditLogger
    ) {
        this.offerRepository = offerRepository;
        this.offerTypeRepository = offerTypeRepository;
        this.sectorRepository = sectorRepository;
        this.offerMapper = offerMapper;
        this.auditLogger = auditLogger;
    }

    // =========================================================================
    // CREAR OFERTA
    // =========================================================================

    /**
     * Crea una nueva oferta.
     *
     * Flujo:
     * 1. Validar que starts_at < ends_at
     * 2. Validar que offer_type_id y sector_id existen
     * 3. Calcular el estado inicial con StatusEngine
     * 4. Persistir la oferta
     * 5. Registrar evento CREATE en auditoría
     *
     * Requerimiento: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6
     */
    public Offer createOffer(CreateOfferCommand command) {
        // Paso 1: Validar rango de fechas (Req 3.2)
        validateDateRange(command.startsAt(), command.endsAt());

        // Paso 2: Validar referencias (Req 3.6)
        validateOfferTypeExists(command.offerTypeId());
        validateSectorExists(command.sectorId());

        // Paso 3: Calcular estado inicial con StatusEngine (Req 3.3)
        // Pasamos Instant.now() como referencia temporal — esto es testeable
        // porque StatusEngine recibe el instante como parámetro.
        OfferStatus status = StatusEngine.calculate(
                command.startsAt(),
                command.endsAt(),
                Instant.now()
        );

        // Paso 4: Construir y persistir la entidad JPA
        OfferJpaEntity entity = new OfferJpaEntity();
        entity.setTitle(command.title());
        entity.setDescription(command.description());
        entity.setOfferTypeId(command.offerTypeId());
        entity.setSectorId(command.sectorId());
        entity.setStatus(status);
        entity.setStartsAt(command.startsAt());
        entity.setEndsAt(command.endsAt());
        entity.setCreatedBy(command.createdBy());   // Trazabilidad (Req 3.4)
        entity.setCreatedAt(Instant.now());

        OfferJpaEntity saved = offerRepository.save(entity);

        // Paso 5: Registrar auditoría CREATE (Req 3.5)
        auditLogger.logCreate(saved.getId(), command.createdBy());

        return offerMapper.toDomain(saved);
    }

    // =========================================================================
    // EDITAR OFERTA
    // =========================================================================

    /**
     * Actualiza una oferta existente.
     *
     * Flujo:
     * 1. Buscar la oferta (404 si no existe)
     * 2. Validar fechas
     * 3. Validar referencias si cambiaron
     * 4. Recalcular estado
     * 5. Detectar qué campos cambiaron y registrar auditoría por cada uno
     * 6. Persistir cambios
     *
     * Requerimiento: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6
     */
    public Offer updateOffer(Long offerId, UpdateOfferCommand command) {
        // Paso 1: Buscar la oferta (Req 4.5)
        OfferJpaEntity entity = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta", offerId));

        // Paso 2: Validar fechas (Req 4.6)
        validateDateRange(command.startsAt(), command.endsAt());

        // Paso 3: Validar referencias si cambiaron
        if (!command.offerTypeId().equals(entity.getOfferTypeId())) {
            validateOfferTypeExists(command.offerTypeId());
        }
        if (!command.sectorId().equals(entity.getSectorId())) {
            validateSectorExists(command.sectorId());
        }

        // Paso 4: Recalcular estado con las nuevas fechas (Req 4.2)
        OfferStatus newStatus = StatusEngine.calculate(
                command.startsAt(),
                command.endsAt(),
                Instant.now()
        );

        // Paso 5: Detectar cambios y registrar auditoría (Req 4.4)
        // Comparamos cada campo editable. Si cambió, registramos un evento UPDATE.
        // Usamos String.valueOf() para convertir cualquier tipo a String de forma segura.
        detectAndLogChange(offerId, command.updatedBy(), "title",
                entity.getTitle(), command.title());
        detectAndLogChange(offerId, command.updatedBy(), "description",
                entity.getDescription(), command.description());
        detectAndLogChange(offerId, command.updatedBy(), "offer_type_id",
                String.valueOf(entity.getOfferTypeId()), String.valueOf(command.offerTypeId()));
        detectAndLogChange(offerId, command.updatedBy(), "sector_id",
                String.valueOf(entity.getSectorId()), String.valueOf(command.sectorId()));
        detectAndLogChange(offerId, command.updatedBy(), "starts_at",
                entity.getStartsAt() != null ? entity.getStartsAt().toString() : null,
                command.startsAt().toString());
        detectAndLogChange(offerId, command.updatedBy(), "ends_at",
                entity.getEndsAt() != null ? entity.getEndsAt().toString() : null,
                command.endsAt().toString());
        detectAndLogChange(offerId, command.updatedBy(), "status",
                entity.getStatus() != null ? entity.getStatus().name() : null,
                newStatus.name());

        // Paso 6: Actualizar la entidad y persistir (Req 4.3)
        entity.setTitle(command.title());
        entity.setDescription(command.description());
        entity.setOfferTypeId(command.offerTypeId());
        entity.setSectorId(command.sectorId());
        entity.setStatus(newStatus);
        entity.setStartsAt(command.startsAt());
        entity.setEndsAt(command.endsAt());
        entity.setUpdatedBy(command.updatedBy());   // Trazabilidad (Req 4.3)
        entity.setUpdatedAt(Instant.now());

        OfferJpaEntity saved = offerRepository.save(entity);
        return offerMapper.toDomain(saved);
    }

    // =========================================================================
    // ELIMINAR OFERTA
    // =========================================================================

    /**
     * Elimina una oferta manualmente (solo Admin).
     * La restricción de rol se aplica en el controlador con @PreAuthorize.
     *
     * Requerimiento: 5.1, 5.2, 5.4
     */
    public void deleteOffer(Long offerId, Long userId) {
        // Verificar que existe (Req 5.4)
        if (!offerRepository.existsById(offerId)) {
            throw new ResourceNotFoundException("Oferta", offerId);
        }

        // Registrar auditoría ANTES de eliminar.
        // Si registramos después, la FK offer_id ya no existe y quedaría NULL.
        // Al registrar antes, la FK es válida en el momento del INSERT en audit_log.
        // Luego el DELETE pone offer_id = NULL (ON DELETE SET NULL), pero el
        // registro ya quedó guardado con el ID correcto.
        auditLogger.logDelete(offerId, userId);

        offerRepository.deleteById(offerId);
    }

    // =========================================================================
    // CONSULTAR OFERTAS
    // =========================================================================

    /**
     * Obtiene una oferta por ID.
     * Requerimiento: 6.3
     */
    @Transactional(readOnly = true)
    public Offer getOfferById(Long offerId) {
        return offerRepository.findById(offerId)
                .map(offerMapper::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Oferta", offerId));
    }

    /**
     * Lista ofertas con filtros opcionales y paginación.
     *
     * El tamaño de página se limita a MAX_PAGE_SIZE (50) independientemente
     * de lo que pida el cliente. Esto protege contra consultas masivas.
     *
     * Requerimiento: 6.1, 6.2, 6.5
     */
    @Transactional(readOnly = true)
    public Page<Offer> getOffers(
            Long sectorId,
            Long offerTypeId,
            OfferStatus status,
            LocalDateTime startsAfter,
            LocalDateTime endsBefore,
            int page,
            int size
    ) {
        // Limitar el tamaño de página al máximo permitido
        int pageSize = Math.min(size, MAX_PAGE_SIZE);

        // Ordenamiento por defecto: starts_at descendente (Req 6.5)
        Pageable pageable = PageRequest.of(
                page,
                pageSize,
                Sort.by(Sort.Direction.DESC, "startsAt")
        );

        return offerRepository
                .findWithFilters(sectorId, offerTypeId, status, startsAfter, endsBefore, pageable)
                .map(offerMapper::toDomain);
    }

    // =========================================================================
    // DASHBOARD
    // =========================================================================

    /**
     * Obtiene los datos del dashboard: conteos por estado y listas recientes.
     * Requerimiento: 10.1, 10.2
     */
    @Transactional(readOnly = true)
    public DashboardData getDashboard() {
        // Conteos por estado (Req 10.1)
        long activeCount   = offerRepository.countByStatus(OfferStatus.ACTIVA);
        long upcomingCount = offerRepository.countByStatus(OfferStatus.PROXIMA);
        long expiredCount  = offerRepository.countByStatus(OfferStatus.VENCIDA);

        // Las 10 más recientes de cada estado relevante (Req 10.2)
        // Usamos PageRequest para limitar a DASHBOARD_LIMIT registros
        Pageable top10 = PageRequest.of(0, DASHBOARD_LIMIT);

        List<Offer> recentActive = offerRepository
                .findTopByStatus(OfferStatus.ACTIVA, top10)
                .stream()
                .map(offerMapper::toDomain)
                .toList();

        List<Offer> recentUpcoming = offerRepository
                .findTopByStatus(OfferStatus.PROXIMA, top10)
                .stream()
                .map(offerMapper::toDomain)
                .toList();

        return new DashboardData(activeCount, upcomingCount, expiredCount, recentActive, recentUpcoming);
    }

    // =========================================================================
    // MÉTODOS AUXILIARES
    // =========================================================================

    /**
     * Valida que starts_at sea estrictamente anterior a ends_at.
     * Requerimiento: 3.2, 4.6
     */
    private void validateDateRange(LocalDateTime startsAt, LocalDateTime endsAt) {
        if (startsAt == null || endsAt == null || !startsAt.isBefore(endsAt)) {
            throw new InvalidDateRangeException(
                    startsAt != null ? startsAt.toString() : "null",
                    endsAt != null ? endsAt.toString() : "null"
            );
        }
    }

    /**
     * Valida que el offer_type_id exista en la tabla offer_types.
     * Requerimiento: 3.6
     */
    private void validateOfferTypeExists(Long offerTypeId) {
        if (!offerTypeRepository.existsById(offerTypeId)) {
            throw new InvalidReferenceException("offer_type_id", offerTypeId);
        }
    }

    /**
     * Valida que el sector_id exista en la tabla sectors.
     * Requerimiento: 3.6
     */
    private void validateSectorExists(Long sectorId) {
        if (!sectorRepository.existsById(sectorId)) {
            throw new InvalidReferenceException("sector_id", sectorId);
        }
    }

    /**
     * Compara el valor anterior y nuevo de un campo.
     * Si son diferentes, registra un evento UPDATE en auditoría.
     *
     * Usa Objects.equals() para manejar correctamente los casos donde
     * uno o ambos valores son null (ej: description puede ser null).
     */
    private void detectAndLogChange(Long offerId, Long userId,
                                    String fieldName, String oldValue, String newValue) {
        // Normalizar null a string vacío para comparación consistente
        String old = oldValue != null ? oldValue : "";
        String nw  = newValue != null ? newValue : "";

        if (!old.equals(nw)) {
            auditLogger.logUpdate(offerId, userId, fieldName, oldValue, newValue);
        }
    }

    // =========================================================================
    // RECORD INTERNO: DashboardData
    // =========================================================================

    /**
     * DashboardData — Datos agregados para el dashboard.
     * Record interno de OfferService, convertido a DTO en el controlador.
     */
    public record DashboardData(
            long activeCount,
            long upcomingCount,
            long expiredCount,
            List<Offer> recentActive,
            List<Offer> recentUpcoming
    ) {}
}
