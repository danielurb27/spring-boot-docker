package com.easy.offers.shared.api;

import java.time.Instant;

/**
 * ErrorResponse — DTO estándar para todas las respuestas de error de la API.
 *
 * Todas las respuestas de error del sistema tienen este formato JSON:
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "message": "Credenciales inválidas. Verifique su usuario y contraseña.",
 *   "timestamp": "2024-07-15T10:30:00Z",
 *   "path": "/api/auth/login"
 * }
 *
 * ¿Por qué un formato estándar?
 * El cliente (frontend Angular) puede manejar todos los errores de la misma forma:
 * siempre hay un campo "message" con el mensaje legible y "status" con el código HTTP.
 * Sin esto, cada endpoint podría retornar errores en formatos diferentes,
 * complicando el manejo de errores en el frontend.
 *
 * Campos:
 * - status: código HTTP numérico (400, 401, 403, 404, 409, 500)
 * - error: nombre del código HTTP ("Bad Request", "Unauthorized", etc.)
 * - message: mensaje descriptivo para el usuario/desarrollador
 * - timestamp: cuándo ocurrió el error (útil para correlacionar con logs)
 * - path: qué endpoint generó el error
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp,
        String path
) {}
