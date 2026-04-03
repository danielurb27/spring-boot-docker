package com.easy.offers.audit.application;

/**
 * AuditLogger — Puerto (interfaz) para registrar eventos de auditoría.
 *
 * OfferService usa esta interfaz para registrar cambios sin conocer
 * los detalles de cómo se persisten los registros.
 *
 * Métodos separados por tipo de evento para mayor claridad:
 * - logCreate: cuando se crea una oferta
 * - logUpdate: cuando se modifica un campo específico
 * - logDelete: cuando un Admin elimina manualmente
 * - logAutoDelete: cuando el CleanupJob elimina automáticamente
 *
 * Requerimiento: 9.1 — Registrar eventos con todos los campos requeridos.
 */
public interface AuditLogger {

    /**
     * Registra la creación de una oferta.
     * @param offerId   ID de la oferta creada
     * @param userId    ID del usuario que la creó
     */
    void logCreate(Long offerId, Long userId);

    /**
     * Registra la modificación de un campo específico de una oferta.
     * Se llama una vez por cada campo que cambió.
     *
     * @param offerId       ID de la oferta modificada
     * @param userId        ID del usuario que la modificó
     * @param fieldChanged  Nombre del campo (ej: "title", "starts_at")
     * @param oldValue      Valor anterior como String
     * @param newValue      Valor nuevo como String
     */
    void logUpdate(Long offerId, Long userId, String fieldChanged, String oldValue, String newValue);

    /**
     * Registra la eliminación manual de una oferta por un Admin.
     * @param offerId   ID de la oferta eliminada
     * @param userId    ID del Admin que la eliminó
     */
    void logDelete(Long offerId, Long userId);

    /**
     * Registra la eliminación automática por el CleanupJob.
     * @param offerId   ID de la oferta eliminada automáticamente
     */
    void logAutoDelete(Long offerId);
}
