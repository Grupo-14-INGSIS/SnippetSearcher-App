package com.grupo14IngSis.snippetSearcherApp.observability

import com.newrelic.api.agent.NewRelic
import org.springframework.stereotype.Component

/**
 * Eventos de negocio para New Relic (custom events). Sin el agente (tests, local) no hacen nada.
 */
@Component
class BusinessEvents {
    /**
     * `SnippetsListed`: cuántos snippets vio un usuario al pedir su lista. Permite detectar el caso
     * "mis snippets desaparecieron", que responde 200 con una lista vacía y no dispara ningún error.
     */
    fun snippetsListed(
        userId: String,
        count: Int,
        owned: Int,
    ) {
        NewRelic.getAgent().insights.recordCustomEvent(
            "SnippetsListed",
            mapOf("userId" to userId, "count" to count, "owned" to owned, "shared" to count - owned),
        )
    }

    /** `RedisStreamStatus`: largo y mensajes sin confirmar de un stream (la señal de saturación). */
    fun streamStatus(
        stream: String,
        group: String,
        length: Long,
        pending: Long,
    ) {
        NewRelic.getAgent().insights.recordCustomEvent(
            "RedisStreamStatus",
            mapOf("stream" to stream, "group" to group, "length" to length, "pending" to pending),
        )
    }
}
