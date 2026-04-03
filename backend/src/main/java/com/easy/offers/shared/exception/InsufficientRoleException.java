package com.easy.offers.shared.exception;

/**
 * InsufficientRoleException — El usuario autenticado no tiene el rol requerido.
 *
 * Se lanza cuando un usuario con rol EMPLOYEE intenta acceder a un recurso
 * que requiere rol ADMIN. Ejemplos:
 * - DELETE /api/offers/{id} (solo ADMIN puede eliminar)
 * - POST /api/users (solo ADMIN puede crear usuarios)
 * - GET /api/offers/{id}/audit (solo ADMIN puede ver auditoría)
 *
 * Diferencia entre 401 y 403:
 * - HTTP 401 Unauthorized: "No sé quién eres" (no autenticado o token inválido)
 * - HTTP 403 Forbidden: "Sé quién eres, pero no tienes permiso" (autenticado pero sin rol)
 *
 * Esta distinción es importante para el cliente:
 * - 401 → el cliente debe redirigir al login
 * - 403 → el cliente debe mostrar "No tienes permisos" (sin redirigir al login)
 *
 * Requerimiento: 1.4 — HTTP 403 cuando el rol es insuficiente.
 */
public class InsufficientRoleException extends DomainException {

    public InsufficientRoleException(String requiredRole) {
        super("Acceso denegado. Se requiere el rol: " + requiredRole);
    }
}
