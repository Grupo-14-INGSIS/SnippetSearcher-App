package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.redis.RedisStreamConsumer
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Consume `snippet-job-results` (lo que produce el Runner) y aplica el resultado:
 *   - lint compliant / not-compliant -> se guarda `snippet.compliant` en Postgres y se borra el estado
 *   - lint failed                    -> estado "failed" en Redis (la tabla conserva el último resultado)
 *   - format formatted               -> el contenido cambió, así que se encola un linteo nuevo
 *
 * Reemplaza al viejo `PATCH /snippets/{id}/status` que el Runner llamaba por HTTP sin autenticación:
 * ahora el Runner no conoce al App, solo publica en el stream.
 */
@Component
@Profile("!test")
class SnippetJobResultConsumer(
    @Value("\${redis.streams.results}") streamKey: String,
    @Value("\${redis.streams.results-group:app_group}") groupId: String,
    redis: StringRedisTemplate,
    private val snippetRepository: SnippetRepository,
    private val lintStatus: LintStatusStore,
    private val jobProducer: SnippetJobProducer,
    private val accessManagerClient: AccessManagerClient,
) : RedisStreamConsumer(streamKey, groupId, redis) {
    private val logger = LoggerFactory.getLogger(SnippetJobResultConsumer::class.java)

    public override fun onMessage(record: MapRecord<String, String, String>) {
        val result = SnippetJobResult.fromFields(record.value)
        if (result == null) {
            logger.warn("Discarding malformed result ${record.id}: ${record.value}")
            return
        }
        val snippet = snippetRepository.findById(result.snippetId).orElse(null)
        if (snippet == null) {
            // El snippet se borró mientras se procesaba: no hay nada que actualizar
            lintStatus.clear(result.snippetId)
            return
        }
        logger.info("Result for snippet ${result.snippetId}: ${result.task.wire} -> ${result.outcome.wire} ${result.detail}")
        when (result.task) {
            JobTask.LINT ->
                when (result.outcome) {
                    JobOutcome.COMPLIANT, JobOutcome.NOT_COMPLIANT -> {
                        snippet.compliant = result.outcome == JobOutcome.COMPLIANT
                        snippetRepository.save(snippet)
                        lintStatus.clear(snippet.snippetId)
                    }
                    else -> lintStatus.markFailed(snippet.snippetId)
                }
            JobTask.FORMAT -> {
                snippet.formatterApplied = result.outcome == JobOutcome.FORMATTED
                snippetRepository.save(snippet)
                if (result.outcome == JobOutcome.FORMATTED) {
                    val ownerId = accessManagerClient.getPermissionsForSnippet(snippet.snippetId)?.ownerId
                    if (ownerId != null) jobProducer.requestLint(snippet, ownerId)
                }
            }
        }
    }

    public override fun onDeadLetter(record: MapRecord<String, String, String>) {
        logger.error("Could not apply result ${record.id} after several attempts: ${record.value}")
    }
}
