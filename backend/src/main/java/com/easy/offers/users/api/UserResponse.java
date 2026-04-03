package com.easy.offers.users.api;

import com.easy.offers.users.domain.User;

import java.time.Instant;

/**
 * UserResponse — DTO de salida para los endpoints de usuarios.
 *
 * Representa la información de un usuario que se retorna en las respuestas de la API.
 *
 * IMPORTANTE: Este DTO NO incluye el campo passwordHash.
 * Nunca exponemos el hash de la contraseña en las respuestas de la API,
 * aunque sea un hash y no la contraseña en texto plano.
 * Principio de mínimo privilegio: solo exponer lo que el cliente necesita.
 *
 * El campo role se retorna como String ("ADMIN" o "EMPLOYEE") para que
 * el frontend pueda usarlo directamente sin necesidad de conocer el enum Java.
 */
public record UserResponse(
        Long id,
        String fullName,
        String username,
        String role,       // "ADMIN" o "EMPLOYEE" como String
        boolean isActive,
        Instant createdAt
) {
    /**
     * Factory method: construye un UserResponse desde una entidad de dominio User.
     * Centraliza la conversión en un solo lugar.
     * El controlador llama a UserResponse.from(user) en lugar de construir el record manualmente.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.fullName(),
                user.username(),
                user.role().name(),   // Enum → String: UserRole.ADMIN → "ADMIN"
                user.isActive(),
                user.createdAt()
                // passwordHash: intencionalmente omitido
        );
    }
}
