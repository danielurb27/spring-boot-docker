package com.easy.offers.offers.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * StatusEngineTest — Tests unitarios para StatusEngine.
 *
 * ¿Qué son los tests unitarios?
 * Prueban una unidad de código (clase o método) de forma aislada,
 * sin dependencias externas (BD, red, Spring context).
 * Son los tests más rápidos y los primeros en ejecutarse.
 *
 * ¿Por qué testear StatusEngine específicamente?
 * Es la lógica de negocio más crítica del sistema. Un error aquí
 * haría que las ofertas muestren estados incorrectos a los usuarios.
 * Los tests documentan exactamente qué comportamiento se espera.
 *
 * Estructura de cada test (patrón AAA — Arrange, Act, Assert):
 * - Arrange: preparar los datos de entrada
 * - Act: ejecutar el método bajo prueba
 * - Assert: verificar que el resultado es el esperado
 *
 * Herramientas usadas:
 * - JUnit 5 (@Test, @DisplayName): framework de testing estándar en Java
 * - AssertJ (assertThat): librería de aserciones fluidas, más legible que JUnit assertions
 *
 * Nota: estos tests NO usan Spring (@SpringBootTest).
 * StatusEngine es código Java puro, no necesita el contexto de Spring.
 * Esto los hace extremadamente rápidos (milisegundos, no segundos).
 */
@DisplayName("StatusEngine — Cálculo de estado de ofertas")
class StatusEngineTest {

    // =========================================================================
    // Fechas de referencia para los tests
    // Usamos fechas fijas para que los tests sean deterministas.
    // =========================================================================

    // Fecha de inicio: 1 de julio de 2024 a las 00:00
    private static final LocalDateTime STARTS_AT = LocalDateTime.of(2024, 7, 1, 0, 0, 0);

    // Fecha de fin: 31 de julio de 2024 a las 23:59:59
    private static final LocalDateTime ENDS_AT = LocalDateTime.of(2024, 7, 31, 23, 59, 59);

    /**
     * Método auxiliar: convierte un LocalDateTime UTC a Instant.
     * Útil para crear el parámetro 'now' en los tests.
     */
    private static Instant toInstant(LocalDateTime dateTime) {
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    // =========================================================================
    // Tests para estado PROXIMA
    // =========================================================================

    @Test
    @DisplayName("Debe retornar PROXIMA cuando now es anterior a starts_at")
    void shouldReturnProximaWhenNowIsBeforeStartsAt() {
        // Arrange: now es el 15 de junio (antes del 1 de julio)
        Instant now = toInstant(LocalDateTime.of(2024, 6, 15, 12, 0, 0));

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert
        assertThat(result).isEqualTo(OfferStatus.PROXIMA);
    }

    @Test
    @DisplayName("Debe retornar PROXIMA cuando now es un segundo antes de starts_at")
    void shouldReturnProximaWhenNowIsOneSecondBeforeStartsAt() {
        // Arrange: now es exactamente 1 segundo antes del inicio
        // Este es un caso borde importante: el último instante antes de activarse
        Instant now = toInstant(STARTS_AT.minusSeconds(1));

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert
        assertThat(result).isEqualTo(OfferStatus.PROXIMA);
    }

    // =========================================================================
    // Tests para estado ACTIVA
    // =========================================================================

    @Test
    @DisplayName("Debe retornar ACTIVA cuando now es exactamente igual a starts_at (límite inferior)")
    void shouldReturnActivaWhenNowEqualsStartsAt() {
        // Arrange: now es exactamente el momento de inicio
        // CASO BORDE CRÍTICO: el primer instante de vigencia debe ser ACTIVA, no PROXIMA
        Instant now = toInstant(STARTS_AT);

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert: starts_at es INCLUSIVO → debe ser ACTIVA
        assertThat(result).isEqualTo(OfferStatus.ACTIVA);
    }

    @Test
    @DisplayName("Debe retornar ACTIVA cuando now está en el medio del período")
    void shouldReturnActivaWhenNowIsInTheMiddle() {
        // Arrange: now es el 15 de julio (en el medio del período)
        Instant now = toInstant(LocalDateTime.of(2024, 7, 15, 12, 0, 0));

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert
        assertThat(result).isEqualTo(OfferStatus.ACTIVA);
    }

    @Test
    @DisplayName("Debe retornar ACTIVA cuando now es exactamente igual a ends_at (límite superior)")
    void shouldReturnActivaWhenNowEqualsEndsAt() {
        // Arrange: now es exactamente el momento de fin
        // CASO BORDE CRÍTICO: el último instante de vigencia debe ser ACTIVA, no VENCIDA
        Instant now = toInstant(ENDS_AT);

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert: ends_at es INCLUSIVO → debe ser ACTIVA (no VENCIDA)
        assertThat(result).isEqualTo(OfferStatus.ACTIVA);
    }

    @Test
    @DisplayName("Debe retornar ACTIVA cuando now es un segundo después de starts_at")
    void shouldReturnActivaWhenNowIsOneSecondAfterStartsAt() {
        // Arrange: now es el primer segundo de vigencia
        Instant now = toInstant(STARTS_AT.plusSeconds(1));

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert
        assertThat(result).isEqualTo(OfferStatus.ACTIVA);
    }

    // =========================================================================
    // Tests para estado VENCIDA
    // =========================================================================

    @Test
    @DisplayName("Debe retornar VENCIDA cuando now es un segundo después de ends_at")
    void shouldReturnVencidaWhenNowIsOneSecondAfterEndsAt() {
        // Arrange: now es exactamente 1 segundo después del fin
        // CASO BORDE CRÍTICO: el primer instante después de ends_at debe ser VENCIDA
        Instant now = toInstant(ENDS_AT.plusSeconds(1));

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert
        assertThat(result).isEqualTo(OfferStatus.VENCIDA);
    }

    @Test
    @DisplayName("Debe retornar VENCIDA cuando now es posterior a ends_at")
    void shouldReturnVencidaWhenNowIsAfterEndsAt() {
        // Arrange: now es el 15 de agosto (después del 31 de julio)
        Instant now = toInstant(LocalDateTime.of(2024, 8, 15, 12, 0, 0));

        // Act
        OfferStatus result = StatusEngine.calculate(STARTS_AT, ENDS_AT, now);

        // Assert
        assertThat(result).isEqualTo(OfferStatus.VENCIDA);
    }

    // =========================================================================
    // Tests de validación de parámetros
    // =========================================================================

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando startsAt es null")
    void shouldThrowWhenStartsAtIsNull() {
        Instant now = Instant.now();

        // assertThatThrownBy: verifica que el código lanza la excepción esperada
        assertThatThrownBy(() -> StatusEngine.calculate(null, ENDS_AT, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("startsAt");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando endsAt es null")
    void shouldThrowWhenEndsAtIsNull() {
        Instant now = Instant.now();

        assertThatThrownBy(() -> StatusEngine.calculate(STARTS_AT, null, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("endsAt");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException cuando now es null")
    void shouldThrowWhenNowIsNull() {
        assertThatThrownBy(() -> StatusEngine.calculate(STARTS_AT, ENDS_AT, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("now");
    }

    // =========================================================================
    // Tests de consistencia
    // =========================================================================

    @Test
    @DisplayName("calculateNow debe retornar el mismo resultado que calculate con Instant.now()")
    void calculateNowShouldBeConsistentWithCalculate() {
        // Arrange: oferta que ya está activa (starts_at en el pasado, ends_at en el futuro)
        LocalDateTime pastStart = LocalDateTime.now(ZoneOffset.UTC).minusDays(1);
        LocalDateTime futureEnd = LocalDateTime.now(ZoneOffset.UTC).plusDays(1);

        // Act: ambos métodos deben retornar ACTIVA
        OfferStatus withNow = StatusEngine.calculate(pastStart, futureEnd, Instant.now());
        OfferStatus withCalculateNow = StatusEngine.calculateNow(pastStart, futureEnd);

        // Assert: ambos deben coincidir (pueden diferir por nanosegundos, pero el estado es el mismo)
        assertThat(withNow).isEqualTo(withCalculateNow);
    }
}
