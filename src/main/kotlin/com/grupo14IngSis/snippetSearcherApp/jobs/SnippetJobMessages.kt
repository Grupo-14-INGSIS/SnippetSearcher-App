package com.grupo14IngSis.snippetSearcherApp.jobs

/*
 * Esquema de los mensajes que viajan por Redis Streams. Es el único acoplamiento entre
 * productor y consumidor, por eso la misma definición existe (idéntica) en el Runner.
 *
 *   snippet-jobs         App -> Runner   "procesá este snippet"
 *   snippet-job-results  Runner -> App   "este es el resultado"
 */

enum class JobTask(
    val wire: String,
) {
    LINT("lint"),
    FORMAT("format"),
    ;

    companion object {
        fun fromWire(value: String?): JobTask? = entries.firstOrNull { it.wire == value?.lowercase() }

        /** Acepta los nombres que usa la UI en `PUT /rules` ("linting" / "formatting"). */
        fun fromRuleTask(task: String): JobTask? =
            when {
                task.contains("lint", ignoreCase = true) -> LINT
                task.contains("format", ignoreCase = true) -> FORMAT
                else -> null
            }
    }
}

data class SnippetJob(
    val task: JobTask,
    val snippetId: String,
    // Dueño del snippet: el Runner aplica las reglas de linteo/formateo de este usuario
    val userId: String,
    val language: String,
    val version: String,
) {
    fun toFields(): Map<String, String> =
        mapOf(
            "task" to task.wire,
            "snippetId" to snippetId,
            "userId" to userId,
            "language" to language,
            "version" to version,
        )
}

enum class JobOutcome(
    val wire: String,
) {
    COMPLIANT("compliant"),
    NOT_COMPLIANT("not-compliant"),
    FORMATTED("formatted"),
    FAILED("failed"),
    ;

    companion object {
        fun fromWire(value: String?): JobOutcome? = entries.firstOrNull { it.wire == value }
    }
}

data class SnippetJobResult(
    val task: JobTask,
    val snippetId: String,
    val outcome: JobOutcome,
    val detail: String = "",
) {
    companion object {
        fun fromFields(fields: Map<String, String>): SnippetJobResult? {
            val task = JobTask.fromWire(fields["task"]) ?: return null
            val snippetId = fields["snippetId"]?.takeIf { it.isNotBlank() } ?: return null
            val outcome = JobOutcome.fromWire(fields["outcome"]) ?: return null
            return SnippetJobResult(task, snippetId, outcome, fields["detail"] ?: "")
        }
    }
}
