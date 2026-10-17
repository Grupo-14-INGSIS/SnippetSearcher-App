package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * Encola el linteo de los snippets que nunca obtuvieron resultado (`compliant = null`) y no tienen
 * trabajo en curso: snippets creados antes de este cambio o cuyo mensaje nunca llegó a publicarse.
 *
 * Con varias réplicas del App corriendo a la vez no se duplica: `requestLintIfIdle` usa HSETNX
 * sobre el estado, así que solo la primera réplica encola cada snippet.
 */
@Component
@Profile("!test")
class PendingLintReconciler(
    private val snippetRepository: SnippetRepository,
    private val accessManagerClient: AccessManagerClient,
    private val jobProducer: SnippetJobProducer,
) {
    private val logger = LoggerFactory.getLogger(PendingLintReconciler::class.java)

    @Scheduled(
        initialDelayString = "\${redis.streams.reconcile-initial-delay-ms:120000}",
        fixedDelayString = "\${redis.streams.reconcile-interval-ms:300000}",
    )
    fun reconcile() {
        val neverLinted = snippetRepository.findByCompliantIsNull()
        var queued = 0
        for (snippet in neverLinted) {
            try {
                val ownerId = accessManagerClient.getPermissionsForSnippet(snippet.snippetId)?.ownerId ?: continue
                if (jobProducer.requestLintIfIdle(snippet, ownerId)) queued++
            } catch (e: Exception) {
                logger.warn("Could not queue lint for snippet ${snippet.snippetId}: ${e.message}")
            }
        }
        if (queued > 0) logger.info("Reconciler queued $queued lint job(s) for never-linted snippets")
    }
}
