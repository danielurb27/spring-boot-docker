package com.easy.offers.auth.api;

import java.time.Instant;

/**
 * LoginResponse — DTO de salida para el endpoint POST /api/auth/login.
 *
 * Respuesta JSON que el cliente recibe al autenticarse exitosamente:
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "expiresAt": "2024-07-15T18:30:00Z",
 *   "tokenType": "Bearer"
 * }
 *
 * El cliente debe guardar el token y enviarlo en cada request posterior:
 * Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 *
 * tokenType: siempre "Bearer" por convención del estándar OAuth2/JWT.
 * Informar el tipo ayuda al cliente a construir el header correctamente.
 */
public record LoginResponse(
        String token,
        Instant expiresAt,
        String tokenType
) {
    /**
     * Factory method: crea una LoginResponse con tokenType="Bearer" por defecto.
     * Evita que el controlador tenga que recordar pasar "Bearer" cada vez.
     */
    public static LoginResponse of(String token, Instant expiresAt) {
        return new LoginResponse(token, expiresAt, "Bearer");
    }
}
