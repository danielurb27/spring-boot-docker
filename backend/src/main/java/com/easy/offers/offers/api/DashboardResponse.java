package com.easy.offers.offers.api;

import com.easy.offers.offers.application.OfferService;

import java.util.List;

/**
 * DashboardResponse — DTO de salida para GET /api/dashboard.
 *
 * Contiene:
 * - Conteos por estado (para las tarjetas del dashboard)
 * - Listas de las ofertas más recientes activas y próximas
 *
 * Requerimiento: 10.1, 10.2
 */
public record DashboardResponse(
        long activeCount,
        long upcomingCount,
        long expiredCount,
        List<OfferResponse> recentActive,
        List<OfferResponse> recentUpcoming
) {
    /**
     * Factory method: construye DashboardResponse desde DashboardData del servicio.
     */
    public static DashboardResponse from(OfferService.DashboardData data) {
        return new DashboardResponse(
                data.activeCount(),
                data.upcomingCount(),
                data.expiredCount(),
                data.recentActive().stream().map(OfferResponse::from).toList(),
                data.recentUpcoming().stream().map(OfferResponse::from).toList()
        );
    }
}
