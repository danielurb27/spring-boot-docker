import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * authGuard — Guard funcional que protege rutas que requieren autenticación.
 *
 * ¿Qué es un Guard en Angular?
 * Es una función que Angular ejecuta ANTES de activar una ruta.
 * Si retorna true, la navegación continúa.
 * Si retorna false (o un UrlTree), la navegación se cancela y puede redirigir.
 *
 * Angular 17 usa guards funcionales (CanActivateFn) en lugar de clases.
 * Son más simples y no requieren implementar una interfaz.
 *
 * Flujo:
 * Usuario navega a /dashboard
 *   → authGuard verifica si está autenticado
 *   → Si sí: muestra el dashboard
 *   → Si no: redirige a /login
 *
 * Requerimiento: 1.3 — Rechazar acceso sin token válido.
 */
export const authGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Redirigir al login si no está autenticado
  // router.createUrlTree() crea un UrlTree que Angular usa para redirigir
  return router.createUrlTree(['/login']);
};

/**
 * guestGuard — Guard para rutas públicas como /login.
 *
 * Si el usuario ya está autenticado y navega a /login,
 * lo redirige al dashboard en lugar de mostrar el formulario de login.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return router.createUrlTree(['/dashboard']);
  }

  return true;
};
 *
 * Verifica dos condiciones:
 * 1. El usuario está autenticado (tiene token válido)
 * 2. El usuario tiene rol ADMIN
 *
 * Si el usuario está autenticado pero no es ADMIN, redirige al dashboard
 * en lugar del login (ya está autenticado, solo no tiene permisos).
 *
 * Requerimiento: 1.4 — HTTP 403 para rol insuficiente.
 */
export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login']);
  }

  if (authService.isAdmin()) {
    return true;
  }

  // Autenticado pero no es ADMIN → redirigir al dashboard
  return router.createUrlTree(['/dashboard']);
};
