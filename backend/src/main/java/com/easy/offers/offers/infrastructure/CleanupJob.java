package com.easy.offers.offers.infrastructure;

import com.easy.offers.audit.application.AuditLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * CleanupJob — Job programado que elimina ofertas vencidas pasado el período de retención.
 *
 * ¿Por qué eliminar ofertas vencidas?
 * Las ofertas vencidas acumulan datos históricos que ya no son útiles para la
 * operación diaria. Después de 21 días, una oferta vencida no tiene valor
 * operativo y solo ocupa espacio en la BD.
 *
 * ¿Por qué 21 días y no eliminar inmediatamente al vencer?
 * El período de retención permite:
 * 1. Consultar ofertas recién vencidas para análisis o corrección de errores.
 * 2. Dar tiempo a los usuarios para revisar el historial antes de que desaparezca.
 * 3. Evitar eliminaciones accidentales de ofertas que acaban de vencer.
 *
 * ¿Por qué a las 2:00 AM?
 * Es el horario de menor tráfico en un sistema de gestión comercial.
 * Las operaciones de DELETE masivo pueden ser costosas en BD y es mejor
 * ejecutarlas cuando hay menos usuarios activos.
 *
 * Expresión cron: "0 0 2 * * *"
 * Formato: segundos minutos horas día-del-mes mes día-de-la-semana
 * - 0: segundo 0
 * - 0: minuto 0
 * - 2: hora 2 (2:00 AM)
 * - *: cualquier día del mes
 * - *: cualquier mes
 * - *: cualquier día de la semana
 * → Se ejecuta todos los días a las 2:00:00 AM UTC
 *
 * Requerimiento: 8.1, 8.2, 8.3
 */
@Component
public class CleanupJob {

    private static final Logger log = LoggerFactory.getLogger(CleanupJob.class);

    /**
     * Período de retención en días (Requerimiento 8.1: 21 días).
     * Constante para facilitar cambios futuros y hacer el código más legible.
     */
    private static final int RETENTION_DAYS = 21;

    private final OfferJpaRepository offerRepository;
    private final AuditLogger auditLogger;

    public CleanupJob(OfferJpaRepository offerRepository, AuditLogger auditLogger) {
        this.offerRepository = offerRepository;
        this.auditLogger = auditLogger;
    }

    /**
     * Elimina todas las ofertas cuyo ends_at es anterior a (now - 21 días).
     *
     * Flujo:
     * 1. Calcular la fecha de corte: now - 21 días
     * 2. Buscar todas las ofertas VENCIDAS con ends_at < fecha de corte
     * 3. Por cada oferta: registrar AUTO_DELETE en auditoría, luego eliminar
     * 4. Loguear el resultado
     *
     * Orden de operaciones (importante):
     * Registramos la auditoría ANTES de eliminar la oferta.
     * Si lo hiciéramos después, la FK offer_id en audit_log quedaría NULL
     * inmediatamente (ON DELETE SET NULL). Al registrar antes, la FK es válida
     * en el momento del INSERT, y luego el DELETE la pone en NULL.
     * Esto garantiza que el registro de auditoría tenga el offer_id correcto
     * en el campo, aunque luego quede NULL por la eliminación.
     *
     * @Transactional: si algo falla, se revierten tanto las eliminaciones
     * como los registros de auditoría de esa ejecución.
     *
     * Requerimiento: 8.1 — Eliminar ofertas con ends_at < now - 21 días.
     * Requerimiento: 8.2 — Registrar AUTO_DELETE en auditoría.
     * Requerimiento: 8.3 — El sistema sigue respondiendo durante la ejecución.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void deleteExpiredOffers() {
        // Calcular la fecha de corte: hoy - 21 días en UTC
        // LocalDateTime.now(ZoneOffset.UTC): fecha y hora actual en UTC
        // minusDays(RETENTION_DAYS): restar 21 días
        LocalDateTime cutoffDate = LocalDateTime.now(ZoneOffset.UTC).minusDays(RETENTION_DAYS);

        log.info("CleanupJob: iniciando limpieza de ofertas vencidas antes de {}...", cutoffDate);

        // Buscar ofertas VENCIDAS con ends_at anterior a la fecha de corte
        List<OfferJpaEntity> toDelete = offerRepository.findExpiredBefore(cutoffDate);

        if (toDelete.isEmpty()) {
            log.info("CleanupJob: no hay ofertas para eliminar.");
            return;
        }

        log.info("CleanupJob: {} ofertas a eliminar.", toDelete.size());

        // Procesar cada oferta: auditoría primero, luego eliminación
        toDelete.forEach(entity -> {
            log.debug("CleanupJob: eliminando oferta {} (ends_at: {})",
                    entity.getId(), entity.getEndsAt());

            // Paso 1: Registrar AUTO_DELETE en auditoría ANTES de eliminar (Req 8.2)
            // changedBy = null porque es el sistema, no un usuario
            auditLogger.logAutoDelete(entity.getId());

            // Paso 2: Eliminar la oferta
            // Después de este DELETE, el offer_id en audit_log queda NULL (ON DELETE SET NULL)
            offerRepository.delete(entity);
        });

        log.info("CleanupJob: completado. {} ofertas eliminadas.", toDelete.size());
    }
}
