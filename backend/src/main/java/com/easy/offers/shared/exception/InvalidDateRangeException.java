package com.easy.offers.shared.exception;

/**
 * InvalidDateRangeException — El rango de fechas de la oferta es inválido.
 *
 * Se lanza cuando starts_at >= ends_at al crear o editar una oferta.
 * Una oferta donde la fecha de inicio es igual o posterior a la de fin
 * no tiene sentido de negocio.
 *
 * Casos que la disparan:
 * - starts_at == ends_at: la oferta duraría 0 segundos
 * - starts_at > ends_at: la oferta "terminaría antes de empezar"
 *
 * Requerimiento: 3.2 y 4.6 — HTTP 400 cuando starts_at >= ends_at.
 */
public class InvalidDateRangeException extends DomainException {

    public InvalidDateRangeException() {
        super("La fecha de inicio debe ser anterior a la fecha de fin.");
    }

    public InvalidDateRangeException(String startsAt, String endsAt) {
        super("Rango de fechas inválido: starts_at (" + startsAt +
              ") debe ser anterior a ends_at (" + endsAt + ").");
    }
}
