package com.easy.offers.shared.exception;

/**
 * InvalidCredentialsException — Credenciales de autenticación incorrectas.
 *
 * Se lanza cuando:
 * - El username no existe en el sistema.
 * - La contraseña no coincide con el hash almacenado.
 * - El usuario existe pero está desactivado (is_active = false).
 *
 * Decisión de seguridad importante:
 * El mensaje de error es GENÉRICO intencionalmente: "Credenciales inválidas".
 * NO decimos "usuario no encontrado" ni "contraseña incorrecta".
 *
 * ¿Por qué? Porque un mensaje específico ayuda a un atacante:
 * - "Usuario no encontrado" → el atacante sabe que ese username no existe
 *   y puede probar otros (enumeración de usuarios).
 * - "Contraseña incorrecta" → el atacante sabe que el username SÍ existe
 *   y puede hacer fuerza bruta solo en la contraseña.
 *
 * Con un mensaje genérico, el atacante no obtiene información útil.
 * Este principio se llama "seguridad por oscuridad" y es una práctica estándar
 * en endpoints de autenticación.
 *
 * Requerimiento: 1.2 — HTTP 401 con mensaje descriptivo (pero no específico).
 */
public class InvalidCredentialsException extends DomainException {

    public InvalidCredentialsException() {
        super("Credenciales inválidas. Verifique su usuario y contraseña.");
    }
}
