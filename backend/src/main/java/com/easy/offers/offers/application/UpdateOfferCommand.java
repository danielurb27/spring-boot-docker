package com.easy.offers.offers.application;

import java.time.LocalDateTime;

/**
 * UpdateOfferCommand — Objeto de comando para actualizar una oferta existente.
 *
 * Similar a CreateOfferCommand pero para edición.
 * No incluye createdBy (no cambia al editar) ni id (va en la URL del endpoint).
 *
 * Todos los campos son opcionales en el sentido de negocio:
 * el usuario puede editar solo el título, solo las fechas, o todo a la vez.
 * Sin embargo, para simplificar el MVP, el PUT es una actualización completa
 * (todos los campos deben enviarse). Si quisiéramos actualizaciones parciales,
 * usaríamos PATCH con campos nullable.
 *
 * Requerimiento: 4.1 — Campos modificables de una oferta.
 */
public record UpdateOfferCommand(
        String title,
        String description,
        Long offerTypeId,
        Long sectorId,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Long updatedBy            // ID del usuario autenticado que edita la oferta
) {}
