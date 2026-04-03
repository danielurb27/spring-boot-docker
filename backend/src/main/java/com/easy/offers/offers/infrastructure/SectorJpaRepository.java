package com.easy.offers.offers.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * SectorJpaRepository — Repositorio para la tabla `sectors`.
 *
 * Tabla de catálogo de solo lectura.
 * Igual que OfferTypeJpaRepository: solo necesitamos findById para validar
 * que el sector_id enviado en una oferta existe en el sistema.
 */
@Repository
public interface SectorJpaRepository extends JpaRepository<SectorJpaEntity, Long> {
    // findById(Long id) heredado es suficiente para validar existencia.
    // findAll() heredado es útil para poblar los selectores del frontend.
}
