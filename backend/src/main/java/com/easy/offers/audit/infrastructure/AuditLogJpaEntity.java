package com.easy.offers.audit.infrastructure;

import com.easy.offers.audit.domain.ChangeType;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * AuditLogJpaEntity — Entidad JPA para la tabla `offer_audit_log`.
 *
 * Características especiales de esta entidad:
 *
 * 1. INMUTABLE en la práctica: una vez creado un registro de auditoría,
 *    nunca se modifica. No hay métodos de actualización en AuditService.
 *    La tabla no tiene columna updated_at por diseño.
 *
 * 2. offer_id NULLABLE: cuando una oferta es eliminada, PostgreSQL pone
 *    offer_id = NULL (ON DELETE SET NULL). El registro de auditoría persiste
 *    pero pierde la referencia. Esto cumple el Requerimiento 9.4.
 *
 * 3. changed_by NULLABLE: para eventos AUTO_DELETE, no hay usuario responsable.
 *    También puede quedar NULL si el usuario que hizo el cambio fue eliminado.
 *
 * 4. field_changed NULLABLE: solo tiene valor para eventos UPDATE.
 *    Para CREATE y DELETE, no hay un campo específico que cambió.
 *
 * Decisión de no usar @ManyToOne aquí tampoco:
 * Almacenamos offer_id y changed_by como Long nullable.
 * Si usáramos @ManyToOne, Hibernate intentaría cargar la oferta y el usuario
 * en cada consulta de auditoría, lo que sería ineficiente y problemático
 * cuando offer_id es NULL (oferta eliminada).
 */
@Entity
@Table(name = "offer_audit_log")
public class AuditLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Nullable: NULL cuando la oferta fue eliminada (ON DELETE SET NULL en BD).
     * Requerimiento 9.4: los registros de auditoría persisten aunque se borre la oferta.
     */
    @Column(name = "offer_id")
    private Long offerId;

    /**
     * Nullable: NULL para eventos AUTO_DELETE (generados por el sistema).
     */
    @Column(name = "changed_by")
    private Long changedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false, length = 30)
    private ChangeType changeType;

    /**
     * Nullable: NULL para eventos CREATE y DELETE.
     * Para UPDATE: nombre del campo modificado (ej: "title", "starts_at").
     */
    @Column(name = "field_changed", length = 50)
    private String fieldChanged;

    /**
     * Nullable: NULL para eventos CREATE (no había valor anterior).
     * TEXT sin límite de longitud para valores potencialmente largos.
     */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /**
     * Nullable: NULL para eventos DELETE (no hay valor nuevo).
     */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /**
     * Nullable: notas adicionales opcionales.
     * Usado por AUTO_DELETE para documentar la razón.
     */
    @Column(columnDefinition = "TEXT")
    private String observation;

    /**
     * updatable = false: los registros de auditoría son inmutables.
     * Una vez creado el timestamp, nunca cambia.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AuditLogJpaEntity() {}

    // Solo getters: esta entidad es de solo escritura (INSERT) y solo lectura (SELECT).
    // No hay setters públicos para reforzar la inmutabilidad en el código de aplicación.
    // El mapper usa los setters package-private para construir la entidad.

    public Long getId() { return id; }
    public Long getOfferId() { return offerId; }
    public Long getChangedBy() { return changedBy; }
    public ChangeType getChangeType() { return changeType; }
    public String getFieldChanged() { return fieldChanged; }
    public String getOldValue() { return oldValue; }
    public String getNewValue() { return newValue; }
    public String getObservation() { return observation; }
    public Instant getCreatedAt() { return createdAt; }

    // Setters necesarios para el mapper (package-private para limitar el acceso)
    void setOfferId(Long offerId) { this.offerId = offerId; }
    void setChangedBy(Long changedBy) { this.changedBy = changedBy; }
    void setChangeType(ChangeType changeType) { this.changeType = changeType; }
    void setFieldChanged(String fieldChanged) { this.fieldChanged = fieldChanged; }
    void setOldValue(String oldValue) { this.oldValue = oldValue; }
    void setNewValue(String newValue) { this.newValue = newValue; }
    void setObservation(String observation) { this.observation = observation; }
    void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
