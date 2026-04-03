package com.easy.offers.offers.infrastructure;

import com.easy.offers.offers.domain.Offer;
import com.easy.offers.offers.domain.OfferType;
import com.easy.offers.offers.domain.Sector;
import org.springframework.stereotype.Component;

/**
 * OfferMapper — Convierte entre entidades de dominio y entidades JPA del módulo offers.
 *
 * Maneja tres conversiones:
 * 1. Offer (dominio) ↔ OfferJpaEntity (infraestructura)
 * 2. OfferTypeJpaEntity → OfferType (dominio) [solo lectura, no hay escritura de tipos]
 * 3. SectorJpaEntity → Sector (dominio) [solo lectura, no hay escritura de sectores]
 */
@Component
public class OfferMapper {

    // =========================================================================
    // Conversiones de Offer
    // =========================================================================

    /**
     * OfferJpaEntity → Offer (dominio).
     * Se usa al leer ofertas de la BD.
     */
    public Offer toDomain(OfferJpaEntity entity) {
        if (entity == null) return null;

        return new Offer(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getOfferTypeId(),
                entity.getSectorId(),
                entity.getStatus(),
                entity.getStartsAt(),
                entity.getEndsAt(),
                entity.getCreatedBy(),
                entity.getUpdatedBy(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Offer (dominio) → OfferJpaEntity.
     * Se usa al crear o actualizar una oferta en la BD.
     *
     * Nota sobre createdAt:
     * Si offer.id() es null (nueva oferta), createdAt se asigna aquí.
     * Si offer.id() tiene valor (actualización), createdAt ya existe en la BD
     * y updatable=false en la anotación @Column lo protege de ser sobreescrito.
     */
    public OfferJpaEntity toJpa(Offer offer) {
        if (offer == null) return null;

        OfferJpaEntity entity = new OfferJpaEntity();
        entity.setId(offer.id());
        entity.setTitle(offer.title());
        entity.setDescription(offer.description());
        entity.setOfferTypeId(offer.offerTypeId());
        entity.setSectorId(offer.sectorId());
        entity.setStatus(offer.status());
        entity.setStartsAt(offer.startsAt());
        entity.setEndsAt(offer.endsAt());
        entity.setCreatedBy(offer.createdBy());
        entity.setUpdatedBy(offer.updatedBy());
        entity.setCreatedAt(offer.createdAt());
        entity.setUpdatedAt(offer.updatedAt());
        return entity;
    }

    // =========================================================================
    // Conversiones de OfferType (solo dominio → lectura)
    // =========================================================================

    /**
     * OfferTypeJpaEntity → OfferType (dominio).
     * Los tipos de oferta son de solo lectura; no hay conversión inversa.
     */
    public OfferType toDomain(OfferTypeJpaEntity entity) {
        if (entity == null) return null;

        return new OfferType(
                entity.getId(),
                entity.getName(),
                entity.getCode(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    // =========================================================================
    // Conversiones de Sector (solo dominio → lectura)
    // =========================================================================

    /**
     * SectorJpaEntity → Sector (dominio).
     * Los sectores son de solo lectura; no hay conversión inversa.
     */
    public Sector toDomain(SectorJpaEntity entity) {
        if (entity == null) return null;

        return new Sector(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }
}
