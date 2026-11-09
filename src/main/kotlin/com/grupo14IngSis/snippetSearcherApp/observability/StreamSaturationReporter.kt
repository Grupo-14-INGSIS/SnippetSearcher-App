package com.grupo14IngSis.snippetSearcherApp.observability

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Mide la saturación de Redis Streams, la cuarta señal dorada: cuántos mensajes hay en cada stream y
 * cuántos entregó un consumidor sin confirmar (lag). Lo manda a New Relic como `RedisStreamStatus`.
 *
 * - `snippet-jobs` / `runner_group`: trabajos de formateo y linteo que el Runner no terminó.
 * - `snippet-job-results` / `app_group`: resultados que App no procesó.
 */
@Component
class StreamSaturationReporter(
    private val redis: StringRedisTemplate,
    private val businessEvents: BusinessEvents,
    @Value("\${redis.streams.jobs}") private val jobsStream: String,
    @Value("\${redis.streams.jobs-group:runner_group}") private val jobsGroup: String,
    @Value("\${redis.streams.results}") private val resultsStream: String,
    @Value("\${redis.streams.results-group}") private val resultsGroup: String,
) {
    private val logger = LoggerFactory.getLogger(StreamSaturationReporter::class.java)

    @Scheduled(
        initialDelayString = "\${observability.stream-status.initial-delay-ms:60000}",
        fixedDelayString = "\${observability.stream-status.interval-ms:60000}",
    )
    fun report() {
        report(jobsStream, jobsGroup)
        report(resultsStream, resultsGroup)
    }

    fun report(
        stream: String,
        group: String,
    ) {
        // Si Redis no responde o el grupo todavía no existe, se saltea esta medición sin cortar nada
        @Suppress("TooGenericExceptionCaught")
        try {
            val length = redis.opsForStream<String, String>().size(stream) ?: 0L
            val pending = redis.opsForStream<String, String>().pending(stream, group)?.totalPendingMessages ?: 0L
            businessEvents.streamStatus(stream, group, length, pending)
        } catch (e: Exception) {
            logger.warn("Could not read stream status stream=$stream group=$group: ${e.message}")
        }
    }
}
