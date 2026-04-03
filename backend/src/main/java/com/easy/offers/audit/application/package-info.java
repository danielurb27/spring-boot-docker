/**
 * Paquete: audit.application — Capa de Aplicación del módulo de auditoría.
 *
 * ¿Qué va aquí?
 *   - AuditLogger: interfaz (puerto) que define los métodos de registro de eventos
 *     logCreate(), logUpdate(), logDelete(), logAutoDelete()
 *   - AuditService: implementación de AuditLogger que persiste los registros
 *
 * ¿Por qué AuditLogger es una interfaz?
 * Otros módulos (OfferService) necesitan registrar auditoría, pero no deben
 * depender de la implementación concreta (que usa JPA).
 * Al depender de la interfaz AuditLogger, el OfferService puede ser testeado
 * con un mock de AuditLogger sin necesitar una base de datos real.
 * Patrón: Dependency Inversion Principle (DIP) — depender de abstracciones.
 *
 * Ver Requerimiento 9: Auditoría de cambios.
 * Ver Properties 14, 22, 23 en el diseño técnico.
 */
package com.easy.offers.audit.application;
