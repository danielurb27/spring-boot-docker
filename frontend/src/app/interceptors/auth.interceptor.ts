import { HttpInterceptorFn, HttpRequest, HttpHandlerFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * authInterceptor — Interceptor HTTP funcional de Angular 17.
 *
 * ¿Qué es un interceptor HTTP?
 * Es una función que intercepta TODAS las requests HTTP salientes y las
 * respuestas entrantes. Permite agregar comportamiento común sin repetir
 * código en cada servicio.
 *
 * Este interceptor hace dos cosas:
 * 1. SALIDA: agrega el header "Authorization: Bearer {token}" a cada request
 * 2. ENTRADA: si el backend retorna 401, redirige al login
 *
 * ¿Por qué Angular 17 usa funciones en lugar de clases para interceptores?
 * Angular 17 introdujo los "functional interceptors" como alternativa más
 * simple a los interceptores basados en clases. Son más fáciles de testear
 * y no requieren implementar una interfaz.
 *
 * Flujo de una request con el interceptor:
 * [Componente] → [authInterceptor] → [Backend]
 *                    ↓ agrega header Authorization
 * [Componente] ← [authInterceptor] ← [Backend]
 *                    ↓ si 401, redirige al login
 */
export const authInterceptor: HttpInterceptorFn = (
  req: HttpRequest<unknown>,
  next: HttpHandlerFn
) => {
  // inject() es la forma de inyectar dependencias en funciones de Angular 17
  const authService = inject(AuthService);
  const router = inject(Router);

  const token = authService.getToken();

  // Si hay token, clonar la request y agregar el header Authorization
  // Las requests HTTP son inmutables en Angular — debemos clonarlas para modificarlas
  const authReq = token
    ? req.clone({
        headers: req.headers.set('Authorization', `Bearer ${token}`)
      })
    : req;

  // Pasar la request (con o sin token) al siguiente handler
  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Si el backend retorna 401 (no autenticado o token expirado),
      // limpiar la sesión y redirigir al login
      if (error.status === 401) {
        authService.logout();
        router.navigate(['/login']);
      }
      // Re-lanzar el error para que el componente pueda manejarlo también
      return throwError(() => error);
    })
  );
};
