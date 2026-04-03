package com.easy.offers.users.domain;

import java.time.Instant;

/**
 * User — Entidad de dominio que representa un usuario del sistema.
 *
 * Esta es la representación del usuario en la capa de dominio.
 * Es completamente independiente de cómo se almacena en la BD (UserJpaEntity)
 * o de cómo se expone en la API (UserResponse DTO).
 *
 * Campos importantes:
 *
 * passwordHash: almacenamos el HASH de la contraseña, nunca la contraseña
 * en texto plano. BCrypt genera un hash de 60 caracteres que incluye el
 * salt (valor aleatorio) y el factor de costo. Esto significa que dos
 * usuarios con la misma contraseña tendrán hashes diferentes.
 *
 * Ejemplo de hash BCrypt:
 * Contraseña: "admin123"
 * Hash:       "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
 *              ↑    ↑↑  ↑
 *              │    ││  └── Salt + hash (53 chars)
 *              │    │└───── Factor de costo (10 = 2^10 iteraciones)
 *              │    └────── Versión del algoritmo
 *              └─────────── Identificador BCrypt
 *
 * role: usamos el enum UserRole en lugar de String para seguridad de tipos.
 * En la BD se almacena como 'ADMIN' o 'EMPLOYEE' (EnumType.STRING en JPA).
 *
 * isActive: soft delete. Un usuario desactivado no puede autenticarse
 * pero sus datos históricos (ofertas creadas, auditoría) se preservan.
 * Requerimiento 2.3.
 *
 * updatedAt: nullable porque un usuario recién creado no ha sido modificado.
 */
public record User(
        Long id,
        String fullName,
        String username,

        /**
         * Hash BCrypt de la contraseña. NUNCA exponer este campo en respuestas de API.
         * El mapper que convierte User → UserResponse debe omitir este campo.
         */
        String passwordHash,

        UserRole role,
        boolean isActive,
        Instant createdAt,

        /**
         * Nullable: null si el usuario nunca fue modificado desde su creación.
         */
        Instant updatedAt
) {
    /**
     * Constructor compacto con validaciones de dominio.
     *
     * Estas validaciones garantizan que nunca exista un User inválido
     * en memoria, independientemente de cómo llegó el dato (API, BD, test).
     */
    public User {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("El nombre completo no puede estar vacío");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("El username no puede estar vacío");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("El hash de contraseña no puede estar vacío");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol no puede ser nulo");
        }
    }
}
