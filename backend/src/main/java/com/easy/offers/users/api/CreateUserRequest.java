package com.easy.offers.users.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CreateUserRequest — DTO de entrada para POST /api/users.
 *
 * Contiene los datos necesarios para crear un nuevo usuario del sistema.
 * Solo accesible por usuarios con rol ADMIN (Requerimiento 2.4).
 *
 * Validaciones con Bean Validation:
 * @NotBlank: el campo no puede ser null, vacío ni solo espacios.
 * @Size: limita la longitud del campo.
 *
 * Nota sobre la contraseña:
 * Recibimos la contraseña en texto plano aquí.
 * UserService la hashea con BCrypt antes de persistirla.
 * NUNCA almacenamos ni logueamos contraseñas en texto plano.
 *
 * Nota sobre el rol:
 * Recibimos el rol como String ("ADMIN" o "EMPLOYEE").
 * UserService valida que sea un valor del enum UserRole.
 * Si enviamos @NotBlank aquí, Bean Validation rechaza null/vacío antes
 * de llegar al servicio. La validación del valor específico la hace el servicio.
 */
public record CreateUserRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String fullName,

        @NotBlank(message = "El username es obligatorio")
        @Size(max = 150, message = "El username no puede superar los 150 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        String role
) {}
