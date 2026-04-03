import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { authInterceptor } from './interceptors/auth.interceptor';

/**
 * app.config.ts — Configuración global de la aplicación Angular.
 *
 * En Angular 17 con standalone components, la configuración global
 * se hace aquí en lugar de en un NgModule raíz.
 *
 * provideRouter(routes): registra el sistema de rutas con nuestras rutas.
 *
 * provideHttpClient(withInterceptors([authInterceptor])):
 * - provideHttpClient: registra el HttpClient para inyección de dependencias.
 * - withInterceptors: registra nuestro interceptor funcional.
 *   El interceptor se ejecutará en TODAS las requests HTTP de la app.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])
    )
  ]
};
