package com.easy.offers.offers.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * OfferTypeJpaRepository — Repositorio para la tabla `offer_types`.
 *
 * Tabla de catálogo de solo lectura.
 * Solo necesitamos findById (heredado de JpaRepository) para validar
 * que el offer_type_id enviado en una oferta existe en el sistema.
 *
 * No hay métodos de escritura porque los tipos de oferta no se crean
 * ni modifican por la API — son datos de referencia fijos.
 */
@Repository
public interface OfferTypeJpaRepository extends JpaRepository<OfferTypeJpaEntity, Long> {
    // Los métodos heredados de JpaRepository son suficientes:
    // - findById(Long id): para validar que el tipo existe
    // - findAll(): para listar todos los tipos (útil para el frontend)
}
