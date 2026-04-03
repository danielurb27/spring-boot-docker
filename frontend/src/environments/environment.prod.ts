/**
 * environment.prod.ts — Configuración para producción (Docker).
 *
 * En producción, el frontend se sirve desde Nginx en el mismo contenedor.
 * Nginx hace de proxy: las requests a /api se redirigen al backend.
 * Por eso la URL es relativa (/api) en lugar de absoluta (http://...).
 *
 * Esto permite que el frontend funcione sin conocer la IP del backend.
 */
export const environment = {
  production: true,
  apiUrl: '/api'
};
