package com.easy.offers.offers.infrastructure;

import com.easy.offers.offers.domain.OfferStatus;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDateTime;

/**
 * OfferJpaEntity — Entidad JPA para la tabla `offers`.
 *
 * Esta es la entidad JPA más importante del sistema.
 * Mapea todos los campos de la tabla `offers` incluyendo las FKs.
 *
 * Decisión sobre las FKs (offer_type_id, sector_id, created_by, updated_by):
 * Almacenamos solo los IDs (Long) en lugar de usar @ManyToOne con objetos completos.
 *
 * ¿Por qué IDs en lugar de @ManyToOne?
 * Con @ManyToOne, Hibernate cargaría automáticamente el OfferType y Sector
 * completos en cada consulta de oferta (JOIN implícito).
 * Para el dashboard y el listado, no necesitamos los objetos completos,
 * solo los IDs para mostrar el nombre en el frontend.
 * Esto evita el problema N+1: si listamos 50 ofertas, no queremos 50 JOINs adicionales.
 *
 * Trade-off: perdemos la navegación directa (offer.getOfferType().getName()),
 * pero ganamos control total sobre cuándo se cargan los datos relacionados.
 * Para el MVP, esta es la decisión correcta.
 *
 * Nota sobre LocalDateTime vs Instant:
 * - starts_at y ends_at: LocalDateTime (fechas de negocio, sin zona horaria explícita)
 * - created_at y updated_at: Instant (timestamps de sistema, siempre UTC)
 * JPA/Hibernate maneja ambos tipos correctamente con PostgreSQL TIMESTAMPTZ.
 */
@Entity
@Table(name = "offers")
public class OfferJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    /**
     * columnDefinition = "TEXT": mapea al tipo TEXT de PostgreSQL (sin límite de longitud).
     * Sin esto, Hibernate usaría VARCHAR(255) por defecto.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * FK a offer_types: almacenamos solo el ID.
     * La validación de que el ID existe se hace en OfferService antes de persistir.
     */
    @Column(name = "offer_type_id", nullable = false)
    private Long offerTypeId;

    /**
     * FK a sectors: almacenamos solo el ID.
     */
    @Column(name = "sector_id", nullable = false)
    private Long sectorId;

    /**
     * Estado calculado por StatusEngine.
     * EnumType.STRING: almacena 'PROXIMA', 'ACTIVA' o 'VENCIDA' en la BD.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OfferStatus status;

    /**
     * LocalDateTime: fecha de negocio sin zona horaria.
     * Hibernate la almacena en la columna TIMESTAMPTZ de PostgreSQL.
     * La zona horaria se configura en application.yml (hibernate.jdbc.time_zone=UTC).
     */
    @Column(name = "starts_at", nullable = false)
    private LocalDateTime startsAt;

    @Column(name = "ends_at", nullable = false)
    private LocalDateTime endsAt;

    /**
     * FK a users (creador). Nullable: si el usuario es eliminado, queda en NULL.
     * ON DELETE SET NULL está definido en el schema.sql.
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * FK a users (último editor). Nullable: null si nunca fue editada.
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Constructor público requerido por JPA y por servicios de otros paquetes.
    public OfferJpaEntity() {}

    // Getters y Setters completos (necesarios para JPA y para el mapper)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Long getOfferTypeId() { return offerTypeId; }
    public void setOfferTypeId(Long offerTypeId) { this.offerTypeId = offerTypeId; }

    public Long getSectorId() { return sectorId; }
    public void setSectorId(Long sectorId) { this.sectorId = sectorId; }

    public OfferStatus getStatus() { return status; }
    public void setStatus(OfferStatus status) { this.status = status; }

    public LocalDateTime getStartsAt() { return startsAt; }
    public void setStartsAt(LocalDateTime startsAt) { this.startsAt = startsAt; }

    public LocalDateTime getEndsAt() { return endsAt; }
    public void setEndsAt(LocalDateTime endsAt) { this.endsAt = endsAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
