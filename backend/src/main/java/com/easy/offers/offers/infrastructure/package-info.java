/**
 * Paquete: offers.infrastructure — Capa de Infraestructura del módulo de ofertas.
 *
 * ¿Qué va aquí?
 * Persistencia y jobs programados:
 *   - OfferJpaEntity: entidad JPA que mapea a la tabla "offers"
 *   - OfferJpaRepository: repositorio con @Query para filtros dinámicos y paginación
 *   - OfferMapper: convierte entre Offer (dominio) y OfferJpaEntity (JPA)
 *   - StatusUpdateJob: job @Scheduled que recalcula estados cada hora (Req 7.3)
 *   - CleanupJob: job @Scheduled que elimina ofertas vencidas diariamente (Req 8.1)
 *
 * ¿Por qué los jobs están en Infrastructure?
 * Los jobs usan @Scheduled de Spring (una dependencia de framework) y acceden
 * directamente a repositorios JPA. Son "adaptadores" que conectan el scheduler
 * del sistema operativo con la lógica de negocio (OfferService).
 * La lógica de qué eliminar y qué preservar vive en el servicio, no en el job.
 */
package com.easy.offers.offers.infrastructure;
