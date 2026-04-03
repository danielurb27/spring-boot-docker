package com.easy.offers.offers.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * OfferTypeJpaEntity — Entidad JPA para la tabla `offer_types`.
 *
 * Tabla de catálogo: datos de referencia que no cambian por la API.
 * Los 8 tipos de oferta se cargan desde 02_data.sql al arrancar.
 *
 * Esta entidad es de solo lectura desde la perspectiva de la aplicación:
 * no hay endpoints para crear/editar tipos de oferta.
 * Solo se usa para validar que el offer_type_id enviado en una oferta existe.
 */
@Entity
@Table(name = "offer_types")
public class OfferTypeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    // length = 20: ampliado para acomodar códigos como 'OPORTUNEASY' (11 chars)
    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public OfferTypeJpaEntity() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
}
