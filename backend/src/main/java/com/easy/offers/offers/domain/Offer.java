package com.easy.offers.offers.domain;

import java.time.Instant;
import java.time.LocalDateTime;

/**
 * Offer — Entidad de dominio principal del sistema.
 *
 * Representa una oferta comercial con toda su información de negocio.
 * Es el objeto más importante del sistema y el que más lógica de negocio
 * concentra (a través de StatusEngine).
 *
 * Decisiones de diseño importantes:
 *
 * 1. LocalDateTime para starts_at y ends_at (no Instant):
 *    Las fechas de inicio y fin de una oferta son fechas de negocio.
 *    "La oferta empieza el 1 de julio" es una fecha local, no un instante UTC.
 *    Usamos LocalDateTime para representar esa intención.
 *    La conversión a UTC ocurre en la capa de infraestructura (JPA/BD).
 *
 *    Diferencia clave:
 *    - LocalDateTime: "2024-07-01T00:00:00" (sin zona horaria)
 *    - Instant: "2024-07-01T03:00:00Z" (UTC, equivalente a medianoche en Argentina)
 *
 * 2. Instant para createdAt y updatedAt:
 *    Los timestamps de auditoría SÍ son instantes absolutos en el tiempo.
 *    "Esta oferta fue creada en este momento exacto" → Instant.
 *
 * 3. offerTypeId y sectorId como Long (no como objetos OfferType/Sector):
 *    Decisión de simplicidad. Podríamos tener Offer con un campo OfferType completo,
 *    pero eso requeriría cargar el tipo en cada consulta de oferta (JOIN).
 *    Para el MVP, guardamos solo el ID y lo resolvemos cuando necesitamos el nombre.
 *    Esta es la diferencia entre un modelo anémico (solo IDs) y un modelo rico
 *    (objetos completos). Para este caso, el modelo con IDs es suficiente.
 *
 * 4. createdBy y updatedBy como Long (nullable):
 *    Nullable porque si el usuario que creó la oferta es eliminado del sistema,
 *    el campo queda en null (ON DELETE SET NULL en la BD).
 *    Requerimiento 3.4 y 4.3.
 *
 * 5. description nullable:
 *    La descripción es opcional al crear una oferta (Requerimiento 3.1
 *    solo requiere title, offer_type_id, sector_id, starts_at, ends_at).
 */
public record Offer(
        Long id,
        String title,

        /**
         * Nullable: la descripción es opcional.
         */
        String description,

        Long offerTypeId,
        Long sectorId,

        /**
         * Estado calculado por StatusEngine.
         * Se persiste en BD para eficiencia de consultas.
         * Se recalcula cada hora por StatusUpdateJob.
         */
        OfferStatus status,

        /**
         * Fecha y hora de inicio de la oferta (sin zona horaria).
         * Se almacena en UTC en la BD (TIMESTAMPTZ).
         */
        LocalDateTime startsAt,

        /**
         * Fecha y hora de fin de la oferta (sin zona horaria).
         * Invariante: endsAt debe ser posterior a startsAt.
         */
        LocalDateTime endsAt,

        /**
         * ID del usuario que creó la oferta. Nullable si el usuario fue eliminado.
         */
        Long createdBy,

        /**
         * ID del último usuario que modificó la oferta. Null si nunca fue editada.
         */
        Long updatedBy,

        Instant createdAt,

        /**
         * Nullable: null si la oferta nunca fue modificada.
         */
        Instant updatedAt
) {
    /**
     * Constructor compacto con validaciones de dominio.
     *
     * La validación de fechas aquí es la "verdad" del dominio:
     * una oferta donde starts_at >= ends_at es inválida por definición.
     * Esta regla se aplica en tres lugares:
     * 1. Aquí (dominio): garantía absoluta, imposible crear un Offer inválido.
     * 2. En OfferService (aplicación): antes de llamar al constructor.
     * 3. En la BD (schema.sql): CHECK constraint como última línea de defensa.
     *
     * Requerimiento 3.2 y 4.6.
     */
    public Offer {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título de la oferta no puede estar vacío");
        }
        if (title.length() > 200) {
            throw new IllegalArgumentException("El título no puede superar los 200 caracteres");
        }
        if (offerTypeId == null) {
            throw new IllegalArgumentException("El tipo de oferta es obligatorio");
        }
        if (sectorId == null) {
            throw new IllegalArgumentException("El sector es obligatorio");
        }
        if (startsAt == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (endsAt == null) {
            throw new IllegalArgumentException("La fecha de fin es obligatoria");
        }
        // Invariante de negocio: la fecha de inicio debe ser anterior a la de fin.
        // Esta es la regla más importante de la entidad Offer.
        if (!startsAt.isBefore(endsAt)) {
            throw new IllegalArgumentException(
                "La fecha de inicio debe ser anterior a la fecha de fin. " +
                "starts_at=" + startsAt + ", ends_at=" + endsAt
            );
        }
        if (status == null) {
            throw new IllegalArgumentException("El estado no puede ser nulo");
        }
    }
}
