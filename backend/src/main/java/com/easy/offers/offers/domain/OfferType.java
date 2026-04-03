package com.easy.offers.offers.domain;

import java.time.Instant;

/**
 * OfferType — Entidad de dominio que representa un tipo de oferta.
 *
 * ¿Qué es un "record" en Java?
 * Introducido en Java 16 (estable), un record es una clase especial diseñada
 * para ser un contenedor inmutable de datos. El compilador genera automáticamente:
 * - Constructor con todos los campos
 * - Métodos getter (accedidos como offerType.name(), no offerType.getName())
 * - equals() y hashCode() basados en todos los campos
 * - toString() legible
 *
 * ¿Por qué usar record para entidades de dominio?
 * Las entidades de dominio representan el estado del negocio. Ese estado
 * no debería cambiar arbitrariamente — si necesitamos "modificar" una entidad,
 * creamos una nueva instancia con los valores actualizados.
 * Esto elimina toda una clase de bugs relacionados con estado mutable compartido.
 *
 * Comparación con una clase normal:
 * // Con clase normal (mutable, propenso a bugs):
 * OfferType type = new OfferType();
 * type.setName("Folleto");
 * type.setName(""); // ¡Nadie lo detecta hasta que falla en producción!
 *
 * // Con record (inmutable, seguro):
 * OfferType type = new OfferType(1L, "Folleto", "FOLLETO", true, Instant.now());
 * // No hay setters. El estado es fijo desde la creación.
 *
 * Regla de Clean Architecture:
 * Esta clase NO tiene anotaciones de Spring (@Component, @Service) ni de JPA
 * (@Entity, @Table). Es Java puro. La capa Infrastructure tiene su propia
 * clase OfferTypeJpaEntity con esas anotaciones.
 *
 * Tipos válidos (definidos en data.sql):
 * Oferta interna, Feria de descuento, Folleto, Octavilla, Ladrillazo,
 * Oportuneasy, Black Week, Ciberweek.
 */
public record OfferType(
        Long id,
        String name,
        String code,
        boolean isActive,
        Instant createdAt
) {
    /**
     * Constructor compacto con validaciones de dominio.
     *
     * ¿Qué es un constructor compacto en records?
     * Es una forma especial de validar los parámetros antes de que el record
     * los asigne. Se ejecuta antes de la asignación automática de campos.
     * No necesitamos escribir "this.name = name" — el record lo hace solo.
     *
     * Estas validaciones son reglas de negocio del dominio:
     * un OfferType sin nombre o con nombre vacío no tiene sentido.
     */
    public OfferType {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del tipo de oferta no puede estar vacío");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("El código del tipo de oferta no puede estar vacío");
        }
    }
}
