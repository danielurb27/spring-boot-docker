package com.easy.offers.users.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * UpdateUserRequest — DTO de entrada para PUT /api/users/{id}.
 *
 * Permite modificar nombre completo, contraseña y rol de un usuario.
 * El username no se puede cambiar (es el identificador de login).
 * La contraseña es opcional: si viene vacía/null, no se actualiza.
 */
public record UpdateUserRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String fullName,

        // Contraseña opcional: si es null o vacía, no se cambia
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        String role
) {}
