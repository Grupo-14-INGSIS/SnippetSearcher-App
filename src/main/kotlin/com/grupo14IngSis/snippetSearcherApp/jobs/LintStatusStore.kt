package com.grupo14IngSis.snippetSearcherApp.jobs

import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

/**
 * Estado del PROCESAMIENTO del linteo, que no es un dato del snippet y por eso no va a la tabla.
 *
 * Hash de Redis `snippet:lint-status` (campo = snippetId):
 *   - "pending": hay un trabajo de linteo encolado o en curso.
 *   - "failed":  el último trabajo no se pudo completar (código inválido, snippet sin contenido,
 *                reintentos agotados).
 *   - sin campo: no hay nada en curso; vale lo que diga `snippet.compliant` en Postgres.
 */
@Component
class LintStatusStore(
    private val redis: StringRedisTemplate,
) {
    enum class Status(
        val wire: String,
    ) {
        PENDING("pending"),
        FAILED("failed"),
    }

    private val logger = LoggerFactory.getLogger(LintStatusStore::class.java)
    private val key = "snippet:lint-status"

    private fun hash() = redis.opsForHash<String, String>()

    fun markPending(snippetId: String) = hash().put(key, snippetId, Status.PENDING.wire)

    /** Marca "pending" solo si no había estado. Devuelve true si lo marcó (evita encolar dos veces). */
    fun markPendingIfAbsent(snippetId: String): Boolean = hash().putIfAbsent(key, snippetId, Status.PENDING.wire)

    fun markFailed(snippetId: String) = hash().put(key, snippetId, Status.FAILED.wire)

    fun clear(snippetId: String) {
        hash().delete(key, snippetId)
    }

    fun get(snippetId: String): Status? = getAll(listOf(snippetId))[snippetId]

    /**
     * Un solo HMGET para toda la lista de snippets del usuario. Si Redis no responde se devuelve
     * vacío: el listado sigue funcionando con el último resultado guardado en la tabla.
     */
    fun getAll(snippetIds: Collection<String>): Map<String, Status> {
        if (snippetIds.isEmpty()) return emptyMap()
        val ids = snippetIds.toList()
        val values =
            try {
                hash().multiGet(key, ids)
            } catch (e: Exception) {
                logger.warn("Could not read lint status from Redis: ${e.message}")
                return emptyMap()
            }
        return ids
            .zip(values)
            .mapNotNull { (id, value) -> Status.entries.firstOrNull { it.wire == value }?.let { id to it } }
            .toMap()
    }
}
