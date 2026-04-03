package com.easy.offers.offers.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * CreateOfferRequest — DTO de entrada para POST /api/offers.
 *
 * Contiene las validaciones de Bean Validation (@NotBlank, @NotNull).
 * Si alguna validación falla, Spring retorna HTTP 400 automáticamente
 * antes de llegar al controlador.
 *
 * La validación de negocio (starts_at < ends_at, offer_type_id existe)
 * se hace en OfferService, no aquí.
 */
public record CreateOfferRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

        // description es opcional — sin @NotBlank
        String description,

        @NotNull(message = "El tipo de oferta es obligatorio")
        Long offerTypeId,

        @NotNull(message = "El sector es obligatorio")
        Long sectorId,

        @NotNull(message = "La fecha de inicio es obligatoria")
        LocalDateTime startsAt,

        @NotNull(message = "La fecha de fin es obligatoria")
        LocalDateTime endsAt
) {}
