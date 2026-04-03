package com.easy.offers.offers.api;

import com.easy.offers.offers.application.CreateOfferCommand;
import com.easy.offers.offers.application.OfferService;
import com.easy.offers.offers.application.UpdateOfferCommand;
import com.easy.offers.offers.domain.Offer;
import com.easy.offers.offers.domain.OfferStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * OfferController — Controlador REST para la gestión de ofertas.
 *
 * Endpoints implementados:
 * - GET    /api/offers              → listar con filtros y paginación
 * - POST   /api/offers              → crear oferta
 * - GET    /api/offers/{id}         → obtener oferta por ID
 * - PUT    /api/offers/{id}         → actualizar oferta
 * - DELETE /api/offers/{id}         → eliminar oferta (solo ADMIN)
 * - GET    /api/dashboard           → datos del dashboard
 *
 * Extracción del userId autenticado:
 * Spring Security carga el SecurityContext con el userId en JwtAuthFilter.
 * Aquí lo extraemos del objeto Authentication para pasarlo al servicio.
 * El userId es el "principal" del Authentication (ver JwtAuthFilter).
 *
 * Authentication authentication: Spring inyecta automáticamente el objeto
 * de autenticación del SecurityContext cuando se declara como parámetro
 * de un método de controlador.
 */
@RestController
@RequestMapping("/api")
public class OfferController {

    private final OfferService offerService;

    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }

    // =========================================================================
    // GET /api/offers — Listar ofertas con filtros opcionales
    // =========================================================================

    /**
     * Lista ofertas con filtros opcionales y paginación.
     *
     * @RequestParam(required = false): todos los filtros son opcionales.
     * Si no se envía un filtro, su valor es null y el repositorio lo ignora.
     *
     * @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME): indica a Spring
     * cómo parsear el parámetro de fecha desde la URL.
     * Formato esperado: 2024-07-01T00:00:00
     *
     * Ejemplo de URL con filtros:
     * GET /api/offers?sectorId=5&status=ACTIVA&page=0&size=20
     *
     * Requerimiento: 6.1, 6.2, 6.5
     */
    @GetMapping("/offers")
    public ResponseEntity<Page<OfferResponse>> getOffers(
            @RequestParam(required = false) Long sectorId,
            @RequestParam(required = false) Long offerTypeId,
            @RequestParam(required = false) OfferStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAfter,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endsBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<Offer> offers = offerService.getOffers(
                sectorId, offerTypeId, status, startsAfter, endsBefore, page, size);

        // Convertir Page<Offer> → Page<OfferResponse>
        // Page.map() aplica la función a cada elemento manteniendo los metadatos de paginación
        return ResponseEntity.ok(offers.map(OfferResponse::from));
    }

    // =========================================================================
    // POST /api/offers — Crear oferta
    // =========================================================================

    /**
     * Crea una nueva oferta.
     *
     * Authentication authentication: Spring inyecta el objeto de autenticación.
     * authentication.getPrincipal() retorna el userId (Long) que cargamos
     * en JwtAuthFilter como el "principal" del UsernamePasswordAuthenticationToken.
     *
     * Requerimiento: 3.1
     */
    @PostMapping("/offers")
    public ResponseEntity<OfferResponse> createOffer(
            @Valid @RequestBody CreateOfferRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        CreateOfferCommand command = new CreateOfferCommand(
                request.title(),
                request.description(),
                request.offerTypeId(),
                request.sectorId(),
                request.startsAt(),
                request.endsAt(),
                userId
        );

        Offer created = offerService.createOffer(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(OfferResponse.from(created));
    }

    // =========================================================================
    // GET /api/offers/{id} — Obtener oferta por ID
    // =========================================================================

    /**
     * Requerimiento: 6.3
     */
    @GetMapping("/offers/{id}")
    public ResponseEntity<OfferResponse> getOfferById(@PathVariable Long id) {
        Offer offer = offerService.getOfferById(id);
        return ResponseEntity.ok(OfferResponse.from(offer));
    }

    // =========================================================================
    // PUT /api/offers/{id} — Actualizar oferta
    // =========================================================================

    /**
     * Requerimiento: 4.1
     */
    @PutMapping("/offers/{id}")
    public ResponseEntity<OfferResponse> updateOffer(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOfferRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);

        UpdateOfferCommand command = new UpdateOfferCommand(
                request.title(),
                request.description(),
                request.offerTypeId(),
                request.sectorId(),
                request.startsAt(),
                request.endsAt(),
                userId
        );

        Offer updated = offerService.updateOffer(id, command);
        return ResponseEntity.ok(OfferResponse.from(updated));
    }

    // =========================================================================
    // DELETE /api/offers/{id} — Eliminar oferta (solo ADMIN)
    // =========================================================================

    /**
     * @PreAuthorize("hasRole('ADMIN')"): solo ADMIN puede eliminar.
     * Requerimiento: 5.1, 5.3
     */
    @DeleteMapping("/offers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOffer(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        offerService.deleteOffer(id, userId);
        // HTTP 204 No Content: operación exitosa sin cuerpo de respuesta
        return ResponseEntity.noContent().build();
    }

    // =========================================================================
    // GET /api/dashboard — Datos del dashboard
    // =========================================================================

    /**
     * Requerimiento: 10.1, 10.2, 10.3
     */
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        OfferService.DashboardData data = offerService.getDashboard();
        return ResponseEntity.ok(DashboardResponse.from(data));
    }

    // =========================================================================
    // MÉTODO AUXILIAR
    // =========================================================================

    /**
     * Extrae el userId del objeto Authentication.
     *
     * En JwtAuthFilter cargamos el SecurityContext con:
     * new UsernamePasswordAuthenticationToken(claims.userId(), null, authorities)
     * El primer parámetro es el "principal" — en nuestro caso, el userId (Long).
     *
     * Aquí lo casteamos de Object a Long para usarlo en los comandos.
     */
    private Long extractUserId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
