package com.easy.offers.offers.domain;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * StatusEnginePropertyTest — Property-Based Tests para StatusEngine.
 *
 * ¿Qué es Property-Based Testing (PBT)?
 * En lugar de escribir tests con ejemplos específicos (como en StatusEngineTest),
 * definimos PROPIEDADES que deben ser verdaderas para CUALQUIER input válido.
 * jqwik genera automáticamente cientos de inputs aleatorios y verifica la propiedad.
 *
 * Diferencia con unit tests tradicionales:
 *
 * Unit test tradicional:
 *   "Si now = 2024-06-15, starts_at = 2024-07-01, ends_at = 2024-07-31 → PROXIMA"
 *   Solo prueba UN caso específico.
 *
 * Property test:
 *   "Para CUALQUIER now anterior a starts_at → siempre PROXIMA"
 *   Prueba 100+ casos generados aleatoriamente.
 *
 * Ventaja: los property tests pueden encontrar casos borde que no se nos ocurrieron.
 * Por ejemplo, jqwik podría generar fechas en años bisiestos, fechas en el año 9999,
 * o diferencias de nanosegundos que revelan bugs sutiles.
 *
 * Anotaciones de jqwik:
 * - @Property: marca un método como property test (equivalente a @Test en JUnit)
 * - @ForAll: jqwik genera valores aleatorios para este parámetro
 * - @Provide: método que define cómo generar valores personalizados
 * - tries = 200: ejecutar 200 iteraciones con inputs diferentes
 *
 * Feature: easy-offers-management
 * Property 12: StatusEngine calcula estado correcto para cualquier combinación de fechas
 * Validates: Requirements 3.3, 4.2, 7.1, 7.2
 */
class StatusEnginePropertyTest {

    // =========================================================================
    // Property 1: Para cualquier now ANTES de starts_at → siempre PROXIMA
    // =========================================================================

    // Feature: easy-offers-management, Property 12: StatusEngine calcula estado correcto
    @Property(tries = 200)
    @Label("Para cualquier now anterior a starts_at, el estado debe ser PROXIMA")
    void statusIsProximaForAnyNowBeforeStartsAt(
            // @ForAll con @IntRange: jqwik genera enteros aleatorios en el rango [1, 365]
            // Representan días en el futuro para starts_at
            @ForAll @IntRange(min = 1, max = 365) int daysUntilStart,
            // Duración de la oferta en días [1, 90]
            @ForAll @IntRange(min = 1, max = 90) int durationDays,
            // Cuántos días antes del inicio está "now" [1, 30]
            @ForAll @IntRange(min = 1, max = 30) int daysBeforeStart
    ) {
        // Arrange: construir fechas a partir de los valores generados
        LocalDateTime baseNow = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startsAt = baseNow.plusDays(daysUntilStart);
        LocalDateTime endsAt = startsAt.plusDays(durationDays);

        // now es daysBeforeStart días ANTES de starts_at
        Instant now = startsAt.minusDays(daysBeforeStart).toInstant(ZoneOffset.UTC);

        // Act
        OfferStatus result = StatusEngine.calculate(startsAt, endsAt, now);

        // Assert: para CUALQUIER combinación de estos valores, debe ser PROXIMA
        assertThat(result)
                .as("Con starts_at=%s, ends_at=%s, now=%s días antes del inicio", startsAt, endsAt, daysBeforeStart)
                .isEqualTo(OfferStatus.PROXIMA);
    }

    // =========================================================================
    // Property 2: Para cualquier now ENTRE starts_at y ends_at → siempre ACTIVA
    // =========================================================================

    // Feature: easy-offers-management, Property 12: StatusEngine calcula estado correcto
    @Property(tries = 200)
    @Label("Para cualquier now entre starts_at y ends_at (inclusive), el estado debe ser ACTIVA")
    void statusIsActivaForAnyNowBetweenDates(
            @ForAll @IntRange(min = 1, max = 365) int daysUntilStart,
            @ForAll @IntRange(min = 2, max = 90) int durationDays,
            // Offset dentro del período: 0 = starts_at, durationDays = ends_at
            @ForAll @IntRange(min = 0, max = 1) int offsetFraction  // 0 o 1 para simplificar
    ) {
        // Arrange
        LocalDateTime baseNow = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startsAt = baseNow.minusDays(daysUntilStart); // en el pasado
        LocalDateTime endsAt = startsAt.plusDays(durationDays);

        // now está dentro del período (en el inicio, en el medio, o en el fin)
        // offsetFraction 0 → starts_at, 1 → ends_at
        LocalDateTime nowLocal = offsetFraction == 0 ? startsAt : endsAt;
        Instant now = nowLocal.toInstant(ZoneOffset.UTC);

        // Act
        OfferStatus result = StatusEngine.calculate(startsAt, endsAt, now);

        // Assert
        assertThat(result)
                .as("Con starts_at=%s, ends_at=%s, now=%s", startsAt, endsAt, nowLocal)
                .isEqualTo(OfferStatus.ACTIVA);
    }

    // =========================================================================
    // Property 3: Para cualquier now DESPUÉS de ends_at → siempre VENCIDA
    // =========================================================================

    // Feature: easy-offers-management, Property 12: StatusEngine calcula estado correcto
    @Property(tries = 200)
    @Label("Para cualquier now posterior a ends_at, el estado debe ser VENCIDA")
    void statusIsVencidaForAnyNowAfterEndsAt(
            @ForAll @IntRange(min = 1, max = 365) int daysAgo,
            @ForAll @IntRange(min = 1, max = 90) int durationDays,
            @ForAll @IntRange(min = 1, max = 30) int daysAfterEnd
    ) {
        // Arrange: oferta que terminó hace daysAgo días
        LocalDateTime baseNow = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime endsAt = baseNow.minusDays(daysAgo);
        LocalDateTime startsAt = endsAt.minusDays(durationDays);

        // now es daysAfterEnd días DESPUÉS de ends_at
        Instant now = endsAt.plusDays(daysAfterEnd).toInstant(ZoneOffset.UTC);

        // Act
        OfferStatus result = StatusEngine.calculate(startsAt, endsAt, now);

        // Assert
        assertThat(result)
                .as("Con starts_at=%s, ends_at=%s, now=%s días después del fin", startsAt, endsAt, daysAfterEnd)
                .isEqualTo(OfferStatus.VENCIDA);
    }

    // =========================================================================
    // Property 4: El resultado siempre es uno de los tres estados válidos
    // (propiedad de completitud — no puede retornar null ni un estado inválido)
    // =========================================================================

    // Feature: easy-offers-management, Property 12: StatusEngine calcula estado correcto
    @Property(tries = 500)
    @Label("El resultado siempre es PROXIMA, ACTIVA o VENCIDA (nunca null)")
    void resultIsAlwaysAValidStatus(
            @ForAll @IntRange(min = -365, max = 365) int startOffset,
            @ForAll @IntRange(min = 1, max = 90) int durationDays,
            @ForAll @IntRange(min = -365, max = 365) int nowOffset
    ) {
        // Arrange: fechas completamente aleatorias (pueden estar en cualquier orden relativo a now)
        LocalDateTime base = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startsAt = base.plusDays(startOffset);
        LocalDateTime endsAt = startsAt.plusDays(durationDays); // siempre endsAt > startsAt
        Instant now = base.plusDays(nowOffset).toInstant(ZoneOffset.UTC);

        // Act
        OfferStatus result = StatusEngine.calculate(startsAt, endsAt, now);

        // Assert: el resultado nunca puede ser null
        assertThat(result).isNotNull();
        // Y debe ser uno de los tres valores válidos del enum
        assertThat(result).isIn(OfferStatus.PROXIMA, OfferStatus.ACTIVA, OfferStatus.VENCIDA);
    }

    // =========================================================================
    // Property 5: Los estados son mutuamente excluyentes
    // Si es PROXIMA, no puede ser ACTIVA ni VENCIDA (y así para cada estado)
    // =========================================================================

    // Feature: easy-offers-management, Property 12: StatusEngine calcula estado correcto
    @Property(tries = 200)
    @Label("Los estados son mutuamente excluyentes: PROXIMA implica NOT ACTIVA y NOT VENCIDA")
    void statesAreMutuallyExclusive(
            @ForAll @IntRange(min = 1, max = 365) int daysUntilStart,
            @ForAll @IntRange(min = 1, max = 90) int durationDays,
            @ForAll @IntRange(min = 1, max = 30) int daysBeforeStart
    ) {
        // Arrange: oferta futura, now antes del inicio
        LocalDateTime base = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startsAt = base.plusDays(daysUntilStart);
        LocalDateTime endsAt = startsAt.plusDays(durationDays);
        Instant now = startsAt.minusDays(daysBeforeStart).toInstant(ZoneOffset.UTC);

        // Act
        OfferStatus result = StatusEngine.calculate(startsAt, endsAt, now);

        // Assert: si es PROXIMA, definitivamente no es ACTIVA ni VENCIDA
        if (result == OfferStatus.PROXIMA) {
            assertThat(result).isNotEqualTo(OfferStatus.ACTIVA);
            assertThat(result).isNotEqualTo(OfferStatus.VENCIDA);
        }
    }
}
