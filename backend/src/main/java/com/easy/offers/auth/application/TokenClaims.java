package com.easy.offers.auth.application;

import java.time.Instant;

/**
 * TokenClaims — Datos extraídos de un token JWT validado.
 *
 * Un JWT tiene tres partes: header.payload.signature
 * El payload contiene los "claims" (afirmaciones sobre el usuario).
 *
 * Ejemplo de payload decodificado:
 * {
 *   "sub": "42",           ← userId
 *   "role": "ADMIN",       ← role
 *   "iat": 1720000000,     ← issued at (timestamp de emisión)
 *   "exp": 1720028800      ← expiration (iat + 8 horas)
 * }
 *
 * Este record encapsula los claims que nos interesan del token.
 * JwtProvider lo construye después de validar y parsear el token.
 * JwtAuthFilter lo usa para cargar el SecurityContext de Spring Security.
 *
 * ¿Por qué userId es Long y no String?
 * El claim "sub" en el JWT es un String (los JWTs solo tienen strings en claims).
 * JwtProvider convierte el String a Long al extraerlo.
 * Así el resto del código trabaja con Long directamente.
 */
public record TokenClaims(
        /**
         * ID del usuario autenticado.
         * Extraído del claim "sub" (subject) del JWT.
         */
        Long userId,

        /**
         * Rol del usuario: "ADMIN" o "EMPLOYEE".
         * Extraído del claim "role" del JWT.
         */
        String role,

        /**
         * Timestamp de expiración del token.
         * Útil para informar al cliente cuándo debe renovar el token.
         */
        Instant expiresAt
) {}
