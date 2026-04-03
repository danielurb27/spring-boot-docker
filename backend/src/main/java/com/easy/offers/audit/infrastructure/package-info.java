/**
 * Paquete: audit.infrastructure — Capa de Infraestructura del módulo de auditoría.
 *
 * ¿Qué va aquí?
 *   - AuditLogJpaEntity: entidad JPA que mapea a la tabla "offer_audit_log"
 *     Nota: offer_id es nullable (ON DELETE SET NULL) para preservar registros
 *     cuando la oferta asociada es eliminada (Requerimiento 9.4).
 *   - AuditLogJpaRepository: repositorio con método para buscar por offer_id
 *     ordenado por created_at DESC
 *   - AuditMapper: convierte entre AuditLog (dominio) y AuditLogJpaEntity (JPA)
 *
 * Decisión de diseño: offer_id como Long nullable (no como FK obligatoria).
 * En la BD usamos ON DELETE SET NULL: si se elimina una oferta, el campo offer_id
 * en sus registros de auditoría se pone en NULL en lugar de eliminar los registros.
 * Esto cumple el Requerimiento 9.4 de preservar el historial de auditoría.
 */
package com.easy.offers.audit.infrastructure;
