package com.grupo14IngSis.snippetSearcherApp.jobs

/**
 * Valor que ve la UI (`ComplianceEnum` en printscript-ui): pending | failed | not-compliant | compliant.
 *
 * Se arma combinando dos fuentes que guardan cosas distintas:
 *   - Postgres (`snippet.compliant`): el RESULTADO del último linteo que terminó (true / false).
 *   - Redis (`snippet:lint-status`):  el ESTADO del trabajo en curso (pending / failed).
 *
 * Si hay un trabajo en curso o fallido manda ese estado; si no, el resultado guardado. Un snippet
 * que nunca se linteó (compliant = null) se muestra "pending": el reconciliador lo va a encolar.
 */
object Compliance {
    const val PENDING = "pending"
    const val FAILED = "failed"
    const val COMPLIANT = "compliant"
    const val NOT_COMPLIANT = "not-compliant"

    fun resolve(
        compliant: Boolean?,
        status: LintStatusStore.Status?,
    ): String =
        when (status) {
            LintStatusStore.Status.PENDING -> PENDING
            LintStatusStore.Status.FAILED -> FAILED
            null ->
                when (compliant) {
                    true -> COMPLIANT
                    false -> NOT_COMPLIANT
                    null -> PENDING
                }
        }
}
