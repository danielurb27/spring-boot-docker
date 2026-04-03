package com.easy.offers.shared.exception;

/**
 * InvalidRoleException — El rol enviado no es un valor válido del sistema.
 *
 * Se lanza cuando se intenta crear un usuario con un rol que no existe
 * en el enum UserRole. Los únicos valores válidos son "ADMIN" y "EMPLOYEE".
 *
 * Ejemplo: POST /api/users con body { "role": "SUPERADMIN" } → HTTP 400
 *
 * Nota: esta validación también ocurre a nivel de Bean Validation en el DTO
 * de request, pero la excepción de dominio es la fuente de verdad.
 *
 * Requerimiento: 2.5 — Solo se permiten roles ADMIN y EMPLOYEE.
 */
public class InvalidRoleException extends DomainException {

    public InvalidRoleException(String role) {
        super("El rol '" + role + "' no es válido. Los roles permitidos son: ADMIN, EMPLOYEE.");
    }
}
