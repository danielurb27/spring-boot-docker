/**
 * Paquete: shared.exception — Excepciones de dominio compartidas.
 *
 * ¿Qué va aquí?
 * Las excepciones de dominio puras que pueden ser lanzadas por cualquier módulo:
 *   - InvalidCredentialsException → HTTP 401
 *   - TokenExpiredException → HTTP 401
 *   - InsufficientRoleException → HTTP 403
 *   - ResourceNotFoundException → HTTP 404
 *   - DuplicateUsernameException → HTTP 409
 *   - InvalidDateRangeException → HTTP 400
 *   - InvalidReferenceException → HTTP 400
 *   - InvalidRoleException → HTTP 400
 *
 * ¿Por qué están en "shared" y no en cada módulo?
 * Algunas excepciones son usadas por múltiples módulos. Por ejemplo,
 * ResourceNotFoundException la usan offers, users y audit.
 * Al centralizarlas en shared, evitamos duplicación.
 *
 * Decisión de diseño: excepciones de dominio puras (no extienden Spring).
 * Estas excepciones NO extienden ResponseStatusException ni ninguna clase de Spring.
 * El GlobalExceptionHandler (en shared.api) las mapea a respuestas HTTP.
 * Esto mantiene el dominio independiente del framework web.
 *
 * Ver diseño técnico: Error Handling → Mapa de excepciones a respuestas HTTP.
 */
package com.easy.offers.shared.exception;
