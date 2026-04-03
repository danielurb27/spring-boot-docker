/**
 * Paquete: shared.api — Componentes de API compartidos entre módulos.
 *
 * ¿Qué va aquí?
 *   - GlobalExceptionHandler: clase con @RestControllerAdvice que centraliza
 *     el manejo de todas las excepciones de la aplicación.
 *   - ErrorResponse: record con el formato estándar de respuesta de error:
 *     { status, error, message, timestamp, path }
 *
 * ¿Por qué un GlobalExceptionHandler centralizado?
 * Sin él, cada controlador tendría que manejar sus propias excepciones con
 * try-catch, generando código duplicado y respuestas inconsistentes.
 * Con @RestControllerAdvice, definimos UNA VEZ cómo manejar cada tipo de
 * excepción y Spring lo aplica automáticamente a todos los controladores.
 *
 * Principio aplicado: DRY (Don't Repeat Yourself).
 * Ver diseño técnico: Error Handling → Estrategia global.
 *
 * Ejemplo de respuesta de error:
 * {
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Oferta con id 42 no encontrada",
 *   "timestamp": "2024-01-15T10:30:00Z",
 *   "path": "/api/offers/42"
 * }
 */
package com.easy.offers.shared.api;
