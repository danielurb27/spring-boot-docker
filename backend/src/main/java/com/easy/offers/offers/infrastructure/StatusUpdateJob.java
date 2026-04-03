package com.easy.offers.offers.infrastructure;

import com.easy.offers.offers.domain.OfferStatus;
import com.easy.offers.offers.domain.StatusEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * StatusUpdateJob — Job programado que recalcula el estado de todas las ofertas.
 *
 * ¿Por qué necesitamos este job?
 * El estado de una oferta (PROXIMA, ACTIVA, VENCIDA) depende de las fechas
 * y del momento actual. Si una oferta tiene starts_at = hoy a las 00:00,
 * a las 00:01 debería pasar de PROXIMA a ACTIVA automáticamente.
 *
 * Sin este job, el estado solo se actualizaría cuando alguien edite la oferta.
 * Con el job, el estado se mantiene sincronizado con la realidad cada hora.
 *
 * ¿Por qué cada hora y no en tiempo real?
 * Para el negocio de ofertas comerciales, una precisión de ±1 hora es suficiente.
 * Actualizar en tiempo real requeriría un mecanismo más complejo (eventos, timers
 * por oferta) que no justifica la complejidad para este caso de uso.
 *
 * @Component: Spring gestiona esta clase como un bean.
 * @Scheduled requiere @EnableScheduling en EasyOffersApplication (ya configurado).
 *
 * Requerimiento: 7.3 — Recalcular estados con frecuencia máxima de 1 hora.
 */
@Component
public class StatusUpdateJob {

    private static final Logger log = LoggerFactory.getLogger(StatusUpdateJob.class);

    private final OfferJpaRepository offerRepository;

    public StatusUpdateJob(OfferJpaRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    /**
     * Recalcula el estado de todas las ofertas que no están VENCIDAS.
     *
     * @Scheduled(fixedRate = 3600000):
     * - fixedRate: ejecutar cada N milisegundos desde el inicio de la ejecución anterior.
     * - 3600000 ms = 1 hora.
     * - Alternativa: fixedDelay (espera N ms después de que termina la ejecución).
     *   Usamos fixedRate para que el job corra a intervalos regulares independientemente
     *   de cuánto tarde en ejecutarse.
     *
     * @Transactional: todas las actualizaciones de estado son atómicas.
     * Si el job falla a mitad, se revierten todos los cambios de esa ejecución.
     *
     * Estrategia de actualización:
     * Solo procesamos ofertas PROXIMA y ACTIVA (no VENCIDA).
     * Las VENCIDAS ya tienen el estado final y no pueden cambiar.
     * Esto reduce el número de registros a procesar significativamente.
     *
     * Requerimiento: 7.1, 7.2, 7.3
     */
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void recalculateStatuses() {
        log.info("StatusUpdateJob: iniciando recálculo de estados de ofertas...");

        Instant now = Instant.now();
        AtomicInteger updatedCount = new AtomicInteger(0);

        // Procesar solo ofertas PROXIMA (pueden pasar a ACTIVA)
        // y ACTIVA (pueden pasar a VENCIDA).
        // Las VENCIDAS no cambian de estado.
        List<OfferJpaEntity> proximasYActivas = offerRepository.findByStatus(OfferStatus.PROXIMA);
        proximasYActivas.addAll(offerRepository.findByStatus(OfferStatus.ACTIVA));

        proximasYActivas.forEach(entity -> {
            // Calcular el estado correcto según las fechas actuales
            OfferStatus newStatus = StatusEngine.calculate(
                    entity.getStartsAt(),
                    entity.getEndsAt(),
                    now
            );

            // Solo actualizar si el estado cambió (evitar writes innecesarios a la BD)
            if (newStatus != entity.getStatus()) {
                log.debug("StatusUpdateJob: oferta {} cambia de {} a {}",
                        entity.getId(), entity.getStatus(), newStatus);

                entity.setStatus(newStatus);
                offerRepository.save(entity);
                updatedCount.incrementAndGet();
            }
        });

        log.info("StatusUpdateJob: completado. {} ofertas actualizadas de {} procesadas.",
                updatedCount.get(), proximasYActivas.size());
    }
}
