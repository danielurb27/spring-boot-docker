package com.easy.offers.offers.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * UpdateOfferRequest — DTO de entrada para PUT /api/offers/{id}.
 * Mismos campos que CreateOfferRequest (PUT = reemplazo completo del recurso).
 */
public record UpdateOfferRequest(

        @NotBlank(message = "El título es obligatorio")
        @Size(max = 200, message = "El título no puede superar los 200 caracteres")
        String title,

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
