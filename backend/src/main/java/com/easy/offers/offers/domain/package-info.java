/**
 * Paquete: offers.domain — Capa de Dominio del módulo de ofertas.
 *
 * ¿Qué va aquí?
 * El corazón del negocio — entidades, enums y lógica pura:
 *   - Offer: record inmutable que representa una oferta comercial
 *   - OfferStatus: enum con los estados posibles (PROXIMA, ACTIVA, VENCIDA)
 *   - OfferType: record que representa un tipo de oferta (Folleto, Ladrillazo, etc.)
 *   - Sector: record que representa un sector comercial (Ferretería, Baños, etc.)
 *   - StatusEngine: clase con lógica pura para calcular el estado de una oferta
 *
 * StatusEngine es la pieza más importante de este paquete.
 * Es una clase de dominio puro: no depende de Spring, JPA, ni de la hora del sistema.
 * Recibe los parámetros que necesita (startsAt, endsAt, now) y devuelve el estado.
 * Esto la hace trivialmente testeable con cualquier fecha arbitraria.
 *
 * Ver Requerimiento 7: Cálculo automático de estados.
 * Ver Property 12 en el diseño técnico.
 */
package com.easy.offers.offers.domain;
