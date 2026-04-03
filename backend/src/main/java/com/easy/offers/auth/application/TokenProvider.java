package com.easy.offers.auth.application;

import java.time.Duration;

/**
 * TokenProvider — Puerto (interfaz) para la generación y validación de tokens JWT.
 *
 * ¿Qué es un "puerto" en Clean Architecture?
 * Un puerto es una interfaz que define un contrato entre la capa de aplicación
 * y el mundo exterior (infraestructura). La capa de aplicación (AuthService)
 * solo conoce esta interfaz, no la implementación concreta.
 *
 * Flujo de dependencias:
 *
 *   AuthService (Application)
 *       │ usa
 *       ▼
 *   TokenProvider (interfaz — Application)
 *       ▲ implementa
 *       │
 *   JwtProvider (Infrastructure)
 *
 * ¿Por qué esta separación?
 * AuthService no importa ninguna clase de la librería jjwt.
 * Si mañana cambiamos de jjwt a nimbus-jose-jwt (otra librería JWT),
 * solo cambia JwtProvider. AuthService no se toca.
 * Esto es el Principio de Inversión de Dependencias (la D de SOLID).
 *
 * Requerimiento: 11.2 — Arquitectura en capas sin dependencias inversas.
 * Requerimiento: 11.3 — JWT firmado con HS256 y clave configurable.
 */
public interface TokenProvider {

    /**
     * Genera un token JWT firmado con HS256.
     *
     * El token incluye en sus claims:
     * - sub (subject): ID del usuario como String
     * - role: rol del usuario ('ADMIN' o 'EMPLOYEE')
     * - iat (issued at): timestamp de emisión
     * - exp (expiration): timestamp de expiración (iat + validity)
     *
     * @param userId   ID del usuario autenticado
     * @param role     Rol del usuario (ej: "ADMIN", "EMPLOYEE")
     * @param validity Duración de validez del token (ej: Duration.ofHours(8))
     * @return String con el token JWT firmado (formato: header.payload.signature)
     */
    String generateToken(Long userId, String role, Duration validity);

    /**
     * Valida un token JWT y extrae sus claims.
     *
     * Verifica:
     * 1. Que el token tenga el formato correcto (3 partes separadas por puntos)
     * 2. Que la firma sea válida (firmado con nuestra clave secreta)
     * 3. Que el token no haya expirado (exp > now)
     *
     * @param token El token JWT a validar (sin el prefijo "Bearer ")
     * @return TokenClaims con los datos extraídos del token
     * @throws com.easy.offers.shared.exception.TokenExpiredException si el token es inválido o expirado
     */
    TokenClaims validateToken(String token);
}
