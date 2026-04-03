/**
 * environment.ts — Configuración para desarrollo local.
 *
 * ¿Qué son los environments en Angular?
 * Son archivos de configuración que varían según el entorno.
 * Angular reemplaza este archivo con environment.prod.ts al hacer
 * `ng build --configuration production`.
 *
 * apiUrl: la URL base del backend.
 * En desarrollo: el backend corre en localhost:8080.
 * En producción (Docker): el frontend y backend están en la misma red,
 * pero el frontend accede al backend a través del proxy de Nginx.
 */
export const environment = {
  production: true,
  apiUrl: 'https://spring-boot-docker-1-ooxb.onrender.com/'
};
