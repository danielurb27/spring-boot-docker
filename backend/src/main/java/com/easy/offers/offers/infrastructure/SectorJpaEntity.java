package com.easy.offers.offers.infrastructure;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * SectorJpaEntity — Entidad JPA para la tabla `sectors`.
 *
 * Tabla de catálogo con los 21 sectores de la tienda.
 * El campo `code` es nullable porque "Hacks and Racks" no tiene código numérico.
 *
 * Al igual que OfferTypeJpaEntity, es de solo lectura desde la API.
 */
@Entity
@Table(name = "sectors")
public class SectorJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nullable: "Hacks and Racks" no tiene código numérico.
     * El schema.sql define esta columna como nullable.
     */
    @Column(length = 10)
    private String code;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public SectorJpaEntity() {}

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
}
