package com.easy.offers.shared.exception;

/**
 * ResourceNotFoundException — El recurso solicitado no existe en el sistema.
 *
 * Se lanza cuando:
 * - GET /api/offers/{id} y la oferta no existe → HTTP 404
 * - PUT /api/offers/{id} y la oferta no existe → HTTP 404
 * - DELETE /api/offers/{id} y la oferta no existe → HTTP 404
 * - PATCH /api/users/{id}/deactivate y el usuario no existe → HTTP 404
 *
 * Diseño del mensaje:
 * El constructor recibe el tipo de recurso y el ID para generar mensajes
 * descriptivos y consistentes. Ejemplos:
 * - "Oferta con id '42' no encontrada"
 * - "Usuario con id '15' no encontrado"
 *
 * Requerimiento: 4.5, 5.4, 6.3 — HTTP 404 para recursos inexistentes.
 */
public class ResourceNotFoundException extends DomainException {

    /**
     * @param resourceType Tipo de recurso (ej: "Oferta", "Usuario")
     * @param id           ID del recurso buscado
     */
    public ResourceNotFoundException(String resourceType, Object id) {
        super(resourceType + " con id '" + id + "' no encontrado/a.");
    }

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
