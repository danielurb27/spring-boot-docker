/**
 * Paquete: audit.domain — Capa de Dominio del módulo de auditoría.
 *
 * ¿Qué va aquí?
 *   - AuditLog: record inmutable que representa un registro de auditoría
 *     Campos: offer_id, changed_by, change_type, field_changed,
 *             old_value, new_value, observation, created_at
 *   - ChangeType: enum con los tipos de cambio (CREATE, UPDATE, DELETE, AUTO_DELETE)
 *
 * Decisión de diseño: registros de auditoría son INMUTABLES.
 * Una vez creado un registro de auditoría, nunca se modifica ni elimina.
 * Esto garantiza la integridad del historial de cambios.
 * En la base de datos, la tabla offer_audit_log no tiene operaciones UPDATE ni DELETE
 * (excepto el ON DELETE SET NULL en offer_id cuando se elimina la oferta padre).
 *
 * Ver Requerimiento 9.4: los registros persisten incluso después de eliminar la oferta.
 */
package com.easy.offers.audit.domain;
