package com.easy.offers.offers.api;

import com.easy.offers.offers.application.CatalogService;
import com.easy.offers.offers.domain.OfferType;
import com.easy.offers.offers.domain.Sector;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CatalogController — Endpoints para los catálogos de referencia.
 *
 * Expone los tipos de oferta y sectores disponibles para que el frontend
 * pueda poblar los selectores del formulario de ofertas.
 *
 * Estos endpoints requieren autenticación (cualquier rol) pero no requieren
 * un rol específico — tanto ADMIN como EMPLOYEE necesitan los catálogos.
 *
 * Endpoints:
 * - GET /api/catalog/offer-types → lista de tipos de oferta
 * - GET /api/catalog/sectors     → lista de sectores
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * GET /api/catalog/offer-types — Lista todos los tipos de oferta activos.
     *
     * Respuesta ejemplo:
     * [
     *   {"id": 1, "name": "Oferta interna", "code": "INTERNA"},
     *   {"id": 2, "name": "Feria de descuento", "code": "FERIA"},
     *   ...
     * ]
     */
    @GetMapping("/offer-types")
    public ResponseEntity<List<OfferTypeResponse>> getOfferTypes() {
        List<OfferType> types = catalogService.getActiveOfferTypes();
        List<OfferTypeResponse> response = types.stream()
                .map(t -> new OfferTypeResponse(t.id(), t.name(), t.code()))
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/catalog/sectors — Lista todos los sectores activos.
     *
     * Respuesta ejemplo:
     * [
     *   {"id": 1, "code": "13", "name": "Ferretería"},
     *   {"id": 21, "code": null, "name": "Hacks and Racks"},
     *   ...
     * ]
     */
    @GetMapping("/sectors")
    public ResponseEntity<List<SectorResponse>> getSectors() {
        List<Sector> sectors = catalogService.getActiveSectors();
        List<SectorResponse> response = sectors.stream()
                .map(s -> new SectorResponse(s.id(), s.code(), s.name()))
                .toList();
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // DTOs internos del controlador
    // Son simples y solo se usan aquí, por eso los definimos como records internos
    // en lugar de crear archivos separados.
    // =========================================================================

    /**
     * OfferTypeResponse — DTO de salida para tipos de oferta.
     * code: null si el tipo no tiene código (no aplica en este caso, todos tienen).
     */
    public record OfferTypeResponse(Long id, String name, String code) {}

    /**
     * SectorResponse — DTO de salida para sectores.
     * code: null para "Hacks and Racks" (sección sin código numérico).
     */
    public record SectorResponse(Long id, String code, String name) {}
}
