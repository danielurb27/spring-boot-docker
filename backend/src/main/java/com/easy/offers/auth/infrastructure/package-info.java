/**
 * Paquete: auth.infrastructure — Capa de Infraestructura del módulo de autenticación.
 *
 * ¿Qué va aquí?
 * Las implementaciones concretas que dependen de frameworks y librerías externas:
 *   - JwtProvider: implementa TokenProvider usando la librería JJWT
 *   - JwtAuthFilter: filtro de Spring Security que valida el JWT en cada request
 *   - SecurityConfig: configuración de Spring Security (rutas públicas/protegidas,
 *     modo stateless, etc.)
 *
 * Responsabilidad de esta capa:
 * Adaptar las tecnologías externas (Spring Security, JJWT) a las interfaces
 * definidas en el dominio. Esta capa SÍ puede importar Spring, JJWT, etc.
 *
 * Patrón aplicado: Adapter en Ports & Adapters.
 * JwtProvider es el "adaptador" que conecta la interfaz TokenProvider (puerto)
 * con la librería JJWT (tecnología externa).
 *
 * Regla: esta capa conoce el dominio (implementa sus interfaces) pero el dominio
 * NO conoce esta capa. La dependencia es unidireccional: infra → dominio.
 */
package com.easy.offers.auth.infrastructure;
