package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import com.grupo14IngSis.snippetSearcherApp.redis.RedisStreamProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Publica trabajos en `snippet-jobs`. El App es el único productor: es quien conoce al dueño
 * del snippet (cuyas reglas se aplican) y su versión, y quien es dueño del estado "pending".
 */
@Component
class SnippetJobProducer(
    @Value("\${redis.streams.jobs}") streamKey: String,
    @Value("\${redis.streams.max-length:10000}") maxLength: Long,
    redis: StringRedisTemplate,
    private val lintStatus: LintStatusStore,
) : RedisStreamProducer(streamKey, redis, maxLength) {
    private val logger = LoggerFactory.getLogger(SnippetJobProducer::class.java)

    fun requestLint(
        snippet: Snippet,
        ownerId: String,
    ) {
        if (rejectOwnerless(snippet, ownerId)) return
        // Primero "pending" y después el XADD: si el Runner responde rápido, su resultado ya
        // encuentra el estado y lo limpia. Al revés el resultado podría llegar antes y quedar "pending".
        lintStatus.markPending(snippet.snippetId)
        publish(SnippetJob(JobTask.LINT, snippet.snippetId, ownerId, snippet.language, snippet.version))
    }

    /** Variante del reconciliador: no encola si el snippet ya tiene un trabajo en curso o fallido. */
    fun requestLintIfIdle(
        snippet: Snippet,
        ownerId: String,
    ): Boolean {
        if (!lintStatus.markPendingIfAbsent(snippet.snippetId)) return false
        if (rejectOwnerless(snippet, ownerId)) return false
        publish(SnippetJob(JobTask.LINT, snippet.snippetId, ownerId, snippet.language, snippet.version))
        return true
    }

    fun requestFormat(
        snippet: Snippet,
        ownerId: String,
    ) {
        if (ownerId.isBlank()) {
            logger.warn("Not formatting snippet ${snippet.snippetId}: it has no owner")
            return
        }
        publish(SnippetJob(JobTask.FORMAT, snippet.snippetId, ownerId, snippet.language, snippet.version))
    }

    /**
     * Sin dueño no hay reglas que aplicar y el Runner descartaría el mensaje: el snippet quedaría
     * "pending" para siempre. Se marca "failed" sin publicar nada.
     */
    private fun rejectOwnerless(
        snippet: Snippet,
        ownerId: String,
    ): Boolean {
        if (ownerId.isNotBlank()) return false
        logger.warn("Not linting snippet ${snippet.snippetId}: it has no owner")
        lintStatus.markFailed(snippet.snippetId)
        return true
    }

    private fun publish(job: SnippetJob) {
        try {
            val id = emit(job.toFields())
            logger.info("Published ${job.task.wire} job for snippet ${job.snippetId} to '$streamKey' (id $id)")
        } catch (e: Exception) {
            // Sin el mensaje nadie va a lintear: el estado queda "failed" para que se vea en la UI
            // (si Redis está caído esto también falla: por eso runCatching)
            if (job.task == JobTask.LINT) runCatching { lintStatus.markFailed(job.snippetId) }
            throw JobPublishException("Could not publish ${job.task.wire} job for snippet ${job.snippetId}", e)
        }
    }
}

class JobPublishException(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause)
