package com.easy.offers.audit.api;

import com.easy.offers.audit.application.AuditQueryService;
import com.easy.offers.audit.domain.AuditLog;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuditController — Controlador REST para el historial de auditoría.
 *
 * Endpoint único: GET /api/offers/{id}/audit
 *
 * Nota sobre el mapping:
 * Usamos @RequestMapping("/api") en lugar de "/api/offers" para evitar
 * ambigüedades con OfferController que también usa "/api/offers".
 * Spring MVC puede manejar múltiples controladores con el mismo prefijo,
 * pero es más claro tener un solo controlador por prefijo de recurso.
 *
 * Requerimiento: 9.2, 9.3
 */
@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditQueryService auditQueryService;

    public AuditController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    /**
     * GET /api/offers/{id}/audit — Historial de cambios de una oferta.
     *
     * Retorna todos los eventos de auditoría de la oferta especificada,
     * ordenados del más reciente al más antiguo (Req 9.2).
     *
     * Si la oferta no tiene registros de auditoría, retorna lista vacía (no 404).
     * Si la oferta no existe, retorna lista vacía también — no validamos
     * la existencia de la oferta aquí porque el historial puede existir
     * incluso después de que la oferta fue eliminada (Req 9.4).
     *
     * @PreAuthorize("hasRole('ADMIN')"): solo ADMIN puede ver auditoría (Req 9.3).
     *
     * @param id ID de la oferta
     * @return Lista de registros de auditoría ordenados por created_at DESC
     */
    @GetMapping("/offers/{id}/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getAuditHistory(@PathVariable Long id) {
        List<AuditLog> logs = auditQueryService.getAuditHistory(id);

        // Convertir List<AuditLog> → List<AuditLogResponse>
        List<AuditLogResponse> response = logs.stream()
                .map(AuditLogResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
