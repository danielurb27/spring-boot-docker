package com.easy.offers.offers.api;

import com.easy.offers.offers.domain.Offer;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * OfferResponse — DTO de salida para los endpoints de ofertas.
 *
 * Representa una oferta en las respuestas JSON de la API.
 * Expone el status como String para que el frontend lo use directamente.
 *
 * Nota: offerTypeId y sectorId se exponen como IDs.
 * En una versión más completa, podríamos incluir los nombres resueltos
 * (ej: "offerTypeName": "Folleto") haciendo un JOIN en el repositorio.
 * Para el MVP, los IDs son suficientes — el frontend puede cachear los catálogos.
 */
public record OfferResponse(
        Long id,
        String title,
        String description,
        Long offerTypeId,
        Long sectorId,
        String status,          // "PROXIMA", "ACTIVA" o "VENCIDA"
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Long createdBy,
        Long updatedBy,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Factory method: construye OfferResponse desde la entidad de dominio Offer.
     */
    public static OfferResponse from(Offer offer) {
        return new OfferResponse(
                offer.id(),
                offer.title(),
                offer.description(),
                offer.offerTypeId(),
                offer.sectorId(),
                offer.status().name(),   // Enum → String
                offer.startsAt(),
                offer.endsAt(),
                offer.createdBy(),
                offer.updatedBy(),
                offer.createdAt(),
                offer.updatedAt()
        );
    }
}
