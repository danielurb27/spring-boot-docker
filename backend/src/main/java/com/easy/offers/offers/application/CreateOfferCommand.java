package com.easy.offers.offers.application;

import java.time.LocalDateTime;

/**
 * CreateOfferCommand — Objeto de comando para crear una nueva oferta.
 *
 * ¿Qué es un Command en Clean Architecture?
 * Un Command es un objeto inmutable que encapsula todos los datos necesarios
 * para ejecutar un caso de uso. Es el "mensaje" que la capa API envía a la
 * capa Application.
 *
 * Ventajas de usar Commands en lugar de pasar parámetros sueltos:
 * 1. Claridad: el método del servicio recibe un objeto con nombre descriptivo
 *    en lugar de 6 parámetros sueltos.
 * 2. Extensibilidad: agregar un campo nuevo no cambia la firma del método.
 * 3. Testabilidad: fácil de construir en tests con datos específicos.
 * 4. Separación: el Command no tiene anotaciones de validación (@NotBlank, etc.)
 *    — esas van en el DTO de la API. El Command asume que los datos ya fueron
 *    validados por la capa API.
 *
 * Diferencia entre Command y DTO:
 * - DTO (CreateOfferRequest): vive en la capa API, tiene anotaciones de validación,
 *   representa el contrato HTTP de entrada.
 * - Command (CreateOfferCommand): vive en la capa Application, es Java puro,
 *   representa la intención de negocio.
 *
 * El controlador convierte el DTO → Command antes de llamar al servicio.
 * Esto desacopla la API del caso de uso.
 *
 * Requerimiento: 3.1 — Campos requeridos para crear una oferta.
 */
public record CreateOfferCommand(
        String title,
        String description,       // Nullable: la descripción es opcional
        Long offerTypeId,
        Long sectorId,
        LocalDateTime startsAt,
        LocalDateTime endsAt,
        Long createdBy            // ID del usuario autenticado que crea la oferta
) {}
