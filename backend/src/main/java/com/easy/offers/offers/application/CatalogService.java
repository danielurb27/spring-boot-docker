package com.easy.offers.offers.application;

import com.easy.offers.offers.domain.OfferType;
import com.easy.offers.offers.domain.Sector;
import com.easy.offers.offers.infrastructure.OfferMapper;
import com.easy.offers.offers.infrastructure.OfferTypeJpaRepository;
import com.easy.offers.offers.infrastructure.SectorJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CatalogService — Caso de uso para consultar los catálogos de referencia.
 *
 * Expone los tipos de oferta y sectores disponibles.
 * Estos datos son de solo lectura — no se crean ni modifican por la API.
 * Se cargan desde 02_data.sql al arrancar la aplicación.
 *
 * ¿Por qué exponer los catálogos por API?
 * El frontend necesita estos datos para poblar los selectores del formulario
 * de creación/edición de ofertas. Sin este endpoint, el frontend tendría que
 * hardcodear los IDs, lo que sería frágil y difícil de mantener.
 *
 * Requerimiento: 3.1, 3.6 — El frontend necesita conocer los IDs válidos.
 */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final OfferTypeJpaRepository offerTypeRepository;
    private final SectorJpaRepository sectorRepository;
    private final OfferMapper offerMapper;

    public CatalogService(
            OfferTypeJpaRepository offerTypeRepository,
            SectorJpaRepository sectorRepository,
            OfferMapper offerMapper
    ) {
        this.offerTypeRepository = offerTypeRepository;
        this.sectorRepository = sectorRepository;
        this.offerMapper = offerMapper;
    }

    /**
     * Retorna todos los tipos de oferta activos.
     * Usado por el frontend para poblar el selector de tipo de oferta.
     */
    public List<OfferType> getActiveOfferTypes() {
        return offerTypeRepository.findAll()
                .stream()
                .filter(e -> e.isActive())
                .map(offerMapper::toDomain)
                .toList();
    }

    /**
     * Retorna todos los sectores activos.
     * Usado por el frontend para poblar el selector de sector.
     */
    public List<Sector> getActiveSectors() {
        return sectorRepository.findAll()
                .stream()
                .filter(e -> e.isActive())
                .map(offerMapper::toDomain)
                .toList();
    }
}
