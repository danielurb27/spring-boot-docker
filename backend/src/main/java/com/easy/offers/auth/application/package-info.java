/**
 * Paquete: auth.application — Capa de Aplicación del módulo de autenticación.
 *
 * ¿Qué va aquí?
 * Los servicios y casos de uso de autenticación:
 *   - AuthService: orquesta el proceso de login (buscar usuario, verificar
 *     contraseña con BCrypt, generar JWT)
 *
 * Responsabilidad de esta capa:
 * Coordinar el flujo de trabajo entre el dominio y la infraestructura.
 * Los servicios de aplicación implementan los "casos de uso" del sistema.
 * No contienen lógica de negocio pura (eso va en Domain), pero sí
 * orquestan las llamadas a repositorios, servicios de dominio y puertos.
 *
 * Patrón aplicado: Application Service / Use Case en Clean Architecture.
 * Conoce Domain (hacia adentro) y usa puertos (interfaces) para hablar
 * con Infrastructure (hacia afuera), nunca importa clases concretas de infra.
 */
package com.easy.offers.auth.application;
