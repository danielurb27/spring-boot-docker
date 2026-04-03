package com.easy.offers.offers.domain;

/**
 * OfferStatus — Enum que representa el estado de vigencia de una oferta.
 *
 * Los tres estados posibles son mutuamente excluyentes y cubren todo el
 * ciclo de vida de una oferta. El estado se calcula automáticamente por
 * StatusEngine en base a las fechas starts_at y ends_at.
 *
 * Ciclo de vida de una oferta:
 *
 *   [Creación]
 *       │
 *       ▼
 *   PROXIMA ──────────────────────────────────────────────────────────────┐
 *   (starts_at > now)                                                     │
 *       │                                                                 │
 *       │ (cuando now >= starts_at)                                       │
 *       ▼                                                                 │
 *   ACTIVA ────────────────────────────────────────────────────────────── │
 *   (starts_at <= now <= ends_at)                                         │
 *       │                                                                 │
 *       │ (cuando now > ends_at)                                          │
 *       ▼                                                                 │
 *   VENCIDA ──────────────────────────────────────────────────────────────┘
 *   (ends_at < now)
 *       │
 *       │ (después de 21 días: CleanupJob la elimina)
 *       ▼
 *   [Eliminada de la BD]
 *
 * Decisión de diseño: el estado se persiste en la BD aunque sea calculable.
 * Esto permite hacer consultas eficientes como WHERE status = 'ACTIVA'
 * sin necesidad de comparar fechas en cada consulta.
 * El StatusUpdateJob recalcula y actualiza el estado cada hora para mantener
 * la consistencia entre el estado persistido y las fechas reales.
 *
 * Requerimiento: 7.1 — Reglas de cálculo de estado.
 */
public enum OfferStatus {

    /**
     * PROXIMA: La oferta aún no ha comenzado.
     * Condición: now < starts_at
     * Visible en el dashboard como "próximas ofertas".
     */
    PROXIMA,

    /**
     * ACTIVA: La oferta está vigente en este momento.
     * Condición: starts_at <= now <= ends_at (inclusive en ambos extremos)
     * Es el estado principal que los empleados y clientes ven.
     */
    ACTIVA,

    /**
     * VENCIDA: La oferta ya terminó.
     * Condición: now > ends_at
     * Se mantiene en el sistema por 21 días para referencia histórica,
     * luego el CleanupJob la elimina automáticamente (Requerimiento 8.1).
     */
    VENCIDA
}
