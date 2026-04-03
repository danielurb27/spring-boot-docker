/**
 * Paquete: auth.api — Capa de API del módulo de autenticación.
 *
 * ¿Qué va aquí?
 * Los controladores REST relacionados con autenticación:
 *   - AuthController: maneja POST /api/auth/login
 *
 * Responsabilidad de esta capa:
 * Recibir requests HTTP, validar el formato de los datos de entrada (DTOs),
 * delegar la lógica al servicio de aplicación y devolver la respuesta HTTP.
 * Los controladores NO contienen lógica de negocio.
 *
 * Patrón aplicado: Controlador en Clean Architecture.
 * Esta capa solo conoce la capa Application (hacia adentro).
 * Nunca importa clases de Infrastructure ni de Domain directamente.
 */
package com.easy.offers.auth.api;
