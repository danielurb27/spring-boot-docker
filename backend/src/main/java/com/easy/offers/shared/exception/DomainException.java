package com.easy.offers.shared.exception;

/**
 * DomainException — Clase base para todas las excepciones de dominio del sistema.
 *
 * ¿Por qué una clase base para excepciones?
 * Permite al GlobalExceptionHandler capturar todas las excepciones de dominio
 * con un solo handler genérico, además de los handlers específicos por tipo.
 *
 * ¿Por qué extender RuntimeException y no Exception?
 * - Exception (checked): el compilador obliga a declarar o capturar la excepción.
 *   Esto contamina las firmas de los métodos con "throws Exception" en todas partes.
 * - RuntimeException (unchecked): no requiere declaración explícita.
 *   Es el estándar moderno en Spring y la mayoría de frameworks Java.
 *
 * Decisión de Clean Architecture:
 * Esta clase NO extiende ninguna clase de Spring (como ResponseStatusException).
 * El dominio no sabe nada de HTTP. La traducción de excepción → código HTTP
 * ocurre en el GlobalExceptionHandler de la capa API.
 *
 * Jerarquía de excepciones del sistema:
 *
 * DomainException (base)
 * ├── InvalidCredentialsException    → HTTP 401
 * ├── TokenExpiredException          → HTTP 401
 * ├── InsufficientRoleException      → HTTP 403
 * ├── ResourceNotFoundException      → HTTP 404
 * ├── DuplicateUsernameException     → HTTP 409
 * ├── InvalidDateRangeException      → HTTP 400
 * ├── InvalidReferenceException      → HTTP 400
 * └── InvalidRoleException           → HTTP 400
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
