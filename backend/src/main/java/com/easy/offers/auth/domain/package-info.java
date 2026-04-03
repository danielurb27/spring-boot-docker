/**
 * Paquete: auth.domain — Capa de Dominio del módulo de autenticación.
 *
 * ¿Qué va aquí?
 * Las interfaces de puertos (ports) relacionadas con autenticación:
 *   - TokenProvider: interfaz que define cómo generar y validar tokens JWT
 *   - TokenClaims: record con los datos extraídos de un token válido
 *
 * Responsabilidad de esta capa:
 * Definir las ABSTRACCIONES que el dominio necesita, sin implementarlas.
 * El dominio dice "necesito algo que genere tokens" (TokenProvider),
 * pero no sabe ni le importa si usa JJWT, Auth0 o cualquier otra librería.
 *
 * Patrón aplicado: Ports & Adapters (Arquitectura Hexagonal).
 * Los "puertos" son interfaces en el dominio. Los "adaptadores" son las
 * implementaciones concretas en Infrastructure (JwtProvider implementa TokenProvider).
 * Esto permite cambiar la librería JWT sin tocar el código de negocio.
 *
 * Regla de oro: NUNCA importar Spring, JPA, JJWT ni ninguna librería externa aquí.
 */
package com.easy.offers.auth.domain;
