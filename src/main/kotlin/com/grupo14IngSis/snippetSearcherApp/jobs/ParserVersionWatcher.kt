package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Consigna: "Con cada versión nueva del parser del lenguaje, se deberán re-evaluar y validar todos los snippets."
 *
 * Le pregunta al Runner con qué versión de PrintScript corre y la compara con la última vista, guardada en
 * Redis (`printscript:parser-version`). Si cambió, todos los snippets pierden su resultado de linteo
 * (`compliant = null`) y se borran los "failed"; el reconciliador los vuelve a encolar con las reglas de cada
 * dueño. Con el parser nuevo, cada snippet se vuelve a parsear (validar) y lintear.
 *
 * GETSET en Redis: con varias réplicas de App, solo la primera que ve la versión nueva hace el reset.
 */
@Component
class ParserVersionWatcher(
    private val runnerClient: RunnerClient,
    private val snippetRepository: SnippetRepository,
    private val lintStatus: LintStatusStore,
    private val redis: StringRedisTemplate,
) {
    private val logger = LoggerFactory.getLogger(ParserVersionWatcher::class.java)
    private val key = "printscript:parser-version"

    /** Devuelve true si detectó una versión nueva y marcó todos los snippets para re-evaluar. */
    fun checkForNewVersion(): Boolean {
        val current =
            try {
                runnerClient.getParserVersion()
            } catch (e: Exception) {
                logger.warn("Could not read PrintScript version from Runner: ${e.message}")
                null
            }
        if (current.isNullOrBlank() || current == "unknown") return false
        val previous = redis.opsForValue().getAndSet(key, current)
        // Primera vez (no había versión guardada): es la línea de base, no hay nada que re-evaluar
        if (previous == null || previous == current) return false
        val reset = snippetRepository.resetAllCompliance()
        val failedCleared = lintStatus.clearFailed()
        logger.info(
            "PrintScript version changed $previous -> $current: $reset snippet(s) marked for re-evaluation, " +
                "$failedCleared failed status(es) cleared",
        )
        return true
    }
}
