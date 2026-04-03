/**
 * Paquete: offers.application — Capa de Aplicación del módulo de ofertas.
 *
 * ¿Qué va aquí?
 * Los servicios y comandos del módulo de ofertas:
 *   - OfferService: orquesta creación, edición, eliminación y consulta de ofertas
 *   - CreateOfferCommand: record inmutable con los datos necesarios para crear una oferta
 *   - UpdateOfferCommand: record inmutable con los datos para actualizar una oferta
 *
 * ¿Qué es un "Command"?
 * Es un patrón de diseño (Command Pattern) donde encapsulamos los datos de una
 * operación en un objeto inmutable. En lugar de pasar 5 parámetros sueltos al
 * servicio, pasamos un solo objeto que los agrupa.
 * Ventajas: más legible, más fácil de validar, más fácil de extender.
 *
 * El OfferService usa StatusEngine (dominio) para calcular el estado de la oferta
 * y AuditLogger (puerto) para registrar los cambios.
 */
package com.easy.offers.offers.application;
