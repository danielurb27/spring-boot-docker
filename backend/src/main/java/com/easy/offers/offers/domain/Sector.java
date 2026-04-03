package com.easy.offers.offers.domain;

import java.time.Instant;

/**
 * Sector — Entidad de dominio que representa un sector de la tienda.
 *
 * Un sector es el área comercial a la que pertenece una oferta.
 * Ejemplos: "13 Ferretería", "41 Baños", "Hacks and Racks".
 *
 * Nota sobre el campo code:
 * La mayoría de sectores tienen un código numérico (13, 41, 45, etc.).
 * Sin embargo, "Hacks and Racks" es una sección nueva sin código asignado.
 * Por eso code es String (no int) y puede ser null.
 *
 * Esto es un ejemplo de modelar la realidad del negocio fielmente:
 * en lugar de forzar un código artificial para Hacks and Racks,
 * aceptamos que el código es opcional (nullable).
 *
 * Los 21 sectores válidos están definidos en data.sql.
 * No son editables por la API — son datos de referencia del negocio.
 */
public record Sector(
        Long id,

        /**
         * Código numérico del sector como String.
         * Nullable: "Hacks and Racks" no tiene código.
         * Ejemplos: "13", "41", "45", null (para Hacks and Racks)
         */
        String code,

        String name,
        boolean isActive,
        Instant createdAt
) {
    /**
     * Constructor compacto: valida que el nombre no sea vacío.
     * El código puede ser null (caso Hacks and Racks).
     */
    public Sector {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre del sector no puede estar vacío");
        }
    }
}
