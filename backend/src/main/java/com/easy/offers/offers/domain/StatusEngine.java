package com.easy.offers.offers.domain;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * StatusEngine — Motor de cálculo del estado de vigencia de una oferta.
 *
 * Esta es la clase de lógica de negocio más importante del sistema.
 * Determina si una oferta es PROXIMA, ACTIVA o VENCIDA en función de
 * sus fechas y el momento actual.
 *
 * ¿Por qué es una clase de dominio puro?
 * Esta lógica es el corazón del negocio. Debe ser:
 * 1. Testeable sin Spring, sin BD, sin Docker: solo Java puro.
 * 2. Determinista: dado el mismo input, siempre produce el mismo output.
 * 3. Sin efectos secundarios: no modifica estado, no llama a servicios externos.
 *
 * Decisión de diseño: método estático vs instancia
 * Usamos un método estático porque StatusEngine no tiene estado propio.
 * No necesita ser instanciado ni inyectado como bean de Spring.
 * Es una función pura: input → output, sin estado interno.
 *
 * Decisión de diseño: recibir Instant now como parámetro
 * En lugar de llamar a Instant.now() dentro del método, recibimos el
 * instante actual como parámetro. Esto es fundamental para los tests:
 *
 * // Sin parámetro (difícil de testear):
 * public static OfferStatus calculate(LocalDateTime startsAt, LocalDateTime endsAt) {
 *     Instant now = Instant.now(); // ← no podemos controlar esto en tests
 *     ...
 * }
 *
 * // Con parámetro (fácil de testear):
 * public static OfferStatus calculate(LocalDateTime startsAt, LocalDateTime endsAt, Instant now) {
 *     // En tests, pasamos cualquier Instant que queramos
 *     // En producción, pasamos Instant.now()
 * }
 *
 * Este patrón se llama "Dependency Injection" a nivel de función (o "clock injection").
 * Es una forma de hacer el código determinista y testeable.
 *
 * Decisión de diseño: LocalDateTime para fechas de oferta, Instant para now
 * - startsAt/endsAt son LocalDateTime porque representan fechas de negocio
 *   ("la oferta empieza el 1 de julio a las 00:00") sin zona horaria explícita.
 * - now es Instant porque representa un punto absoluto en el tiempo (UTC).
 * - La conversión se hace dentro del método: convertimos now a LocalDateTime UTC
 *   para comparar con las fechas de la oferta (que también están en UTC en la BD).
 *
 * Requerimiento: 7.1, 7.2 — Reglas de cálculo y uso de UTC.
 * Property: 12 — StatusEngine calcula estado correcto para cualquier combinación de fechas.
 */
public final class StatusEngine {

    /**
     * Constructor privado: esta clase no debe ser instanciada.
     * Es una clase de utilidad con solo métodos estáticos.
     * El modificador 'final' evita que sea extendida.
     */
    private StatusEngine() {
        throw new UnsupportedOperationException("StatusEngine es una clase de utilidad, no instanciar.");
    }

    /**
     * Calcula el estado de vigencia de una oferta.
     *
     * Reglas de negocio (Requerimiento 7.1):
     * - PROXIMA:  now < startsAt
     * - ACTIVA:   startsAt <= now <= endsAt  (inclusive en ambos extremos)
     * - VENCIDA:  now > endsAt
     *
     * @param startsAt Fecha y hora de inicio de la oferta (en UTC, sin zona)
     * @param endsAt   Fecha y hora de fin de la oferta (en UTC, sin zona)
     * @param now      Instante actual en UTC (usar Instant.now() en producción)
     * @return El estado calculado: PROXIMA, ACTIVA o VENCIDA
     *
     * @throws IllegalArgumentException si startsAt, endsAt o now son null
     */
    public static OfferStatus calculate(LocalDateTime startsAt, LocalDateTime endsAt, Instant now) {
        // Validación defensiva: nunca deberíamos recibir nulls aquí,
        // pero es buena práctica validar en métodos públicos.
        if (startsAt == null) throw new IllegalArgumentException("startsAt no puede ser null");
        if (endsAt == null)   throw new IllegalArgumentException("endsAt no puede ser null");
        if (now == null)      throw new IllegalArgumentException("now no puede ser null");

        // Convertimos el Instant (UTC) a LocalDateTime UTC para poder comparar
        // con las fechas de la oferta (que también están en UTC).
        //
        // ZoneOffset.UTC es equivalente a ZoneId.of("UTC") pero más eficiente
        // porque no requiere lookup en la base de datos de zonas horarias.
        //
        // Ejemplo:
        // now = 2024-07-15T10:30:00Z (Instant)
        // nowUtc = 2024-07-15T10:30:00 (LocalDateTime, misma hora pero sin "Z")
        LocalDateTime nowUtc = LocalDateTime.ofInstant(now, ZoneOffset.UTC);

        // Regla 1: PROXIMA — la oferta aún no ha comenzado
        // nowUtc es estrictamente anterior a startsAt
        if (nowUtc.isBefore(startsAt)) {
            return OfferStatus.PROXIMA;
        }

        // Regla 2: ACTIVA — la oferta está vigente
        // nowUtc está entre startsAt y endsAt (inclusive en endsAt)
        // !nowUtc.isAfter(endsAt) es equivalente a nowUtc <= endsAt
        // Usamos esta forma para incluir el caso nowUtc == endsAt (último segundo de vigencia)
        if (!nowUtc.isAfter(endsAt)) {
            return OfferStatus.ACTIVA;
        }

        // Regla 3: VENCIDA — la oferta ya terminó
        // Si no es PROXIMA ni ACTIVA, necesariamente es VENCIDA
        // (nowUtc > endsAt)
        return OfferStatus.VENCIDA;
    }

    /**
     * Versión conveniente que usa el instante actual del sistema.
     * Usar en producción cuando no se necesita inyectar el tiempo.
     *
     * NOTA: No usar en tests — usar la versión con parámetro 'now' para
     * poder controlar el tiempo en los tests.
     *
     * @param startsAt Fecha y hora de inicio de la oferta
     * @param endsAt   Fecha y hora de fin de la oferta
     * @return El estado calculado usando Instant.now() como referencia
     */
    public static OfferStatus calculateNow(LocalDateTime startsAt, LocalDateTime endsAt) {
        return calculate(startsAt, endsAt, Instant.now());
    }
}
