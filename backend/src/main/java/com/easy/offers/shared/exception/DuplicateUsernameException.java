package com.easy.offers.shared.exception;

/**
 * DuplicateUsernameException — El username ya está en uso por otro usuario.
 *
 * Se lanza cuando se intenta crear un usuario con un username que ya existe
 * en el sistema (activo o inactivo).
 *
 * ¿Por qué HTTP 409 Conflict y no 400 Bad Request?
 * - 400 Bad Request: el dato enviado es inválido en sí mismo (ej: email sin @)
 * - 409 Conflict: el dato es válido, pero entra en conflicto con el estado
 *   actual del servidor (el username ya existe)
 *
 * La distinción es importante para el cliente:
 * - 400 → "el usuario ingresó datos incorrectos, mostrar error de validación"
 * - 409 → "el username ya está tomado, sugerir otro"
 *
 * Requerimiento: 2.2 — HTTP 409 cuando el username ya existe.
 */
public class DuplicateUsernameException extends DomainException {

    public DuplicateUsernameException(String username) {
        super("El username '" + username + "' ya está en uso. Por favor elija otro.");
    }
}
