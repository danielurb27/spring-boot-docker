/**
 * Paquete: audit.api — Capa de API del módulo de auditoría.
 *
 * ¿Qué va aquí?
 *   - AuditController: maneja GET /api/offers/{id}/audit
 *     Solo accesible por ADMIN (Requerimiento 9.3).
 *     Retorna los registros de auditoría ordenados por created_at DESC (Req 9.2).
 *
 * ¿Por qué la auditoría tiene su propio módulo?
 * La auditoría es una responsabilidad transversal (cross-cutting concern):
 * afecta a ofertas, usuarios y otros módulos. Al tenerla en su propio módulo,
 * podemos evolucionar la lógica de auditoría sin tocar los módulos de negocio.
 * Principio de diseño: Single Responsibility Principle (SRP).
 */
package com.easy.offers.audit.api;
