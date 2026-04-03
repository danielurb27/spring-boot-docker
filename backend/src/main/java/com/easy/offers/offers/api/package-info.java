/**
 * Paquete: offers.api — Capa de API del módulo de ofertas.
 *
 * ¿Qué va aquí?
 * Los controladores REST para el CRUD de ofertas:
 *   - OfferController: maneja GET/POST/PUT/DELETE /api/offers y GET /api/dashboard
 *   - OfferFilterParams: record con los parámetros de filtro opcionales de la query
 *     (sector_id, offer_type_id, status, starts_at, ends_at)
 *
 * Ver Requerimientos 3, 4, 5, 6 y 10.
 * El endpoint DELETE /api/offers/{id} solo es accesible por ADMIN (Req 5.3).
 * Los demás endpoints son accesibles por ADMIN y EMPLOYEE.
 */
package com.easy.offers.offers.api;
