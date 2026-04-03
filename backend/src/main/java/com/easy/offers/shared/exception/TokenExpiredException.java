package com.easy.offers.shared.exception;

/**
 * TokenExpiredException — El token JWT está expirado o es inválido.
 *
 * Se lanza cuando:
 * - El header Authorization está ausente en la request.
 * - El token JWT está malformado (no tiene el formato correcto).
 * - El token JWT está expirado (la fecha exp es anterior a now).
 * - La firma del token no es válida (fue alterado o firmado con otra clave).
 *
 * Esta excepción es lanzada por JwtAuthFilter en la capa Infrastructure
 * y capturada por el GlobalExceptionHandler para retornar HTTP 401.
 *
 * Requerimiento: 1.3 — Rechazar requests sin JWT o con JWT expirado con HTTP 401.
 */
public class TokenExpiredException extends DomainException {

    public TokenExpiredException(String message) {
        super(message);
    }

    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
