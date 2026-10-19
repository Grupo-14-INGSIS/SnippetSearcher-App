package com.grupo14IngSis.snippetSearcherApp.jobs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ComplianceTest {
    @Test
    fun `without a running job the stored result is shown`() {
        assertEquals("compliant", Compliance.resolve(true, null))
        assertEquals("not-compliant", Compliance.resolve(false, null))
    }

    @Test
    fun `a running or failed job wins over the stored result`() {
        assertEquals("pending", Compliance.resolve(true, LintStatusStore.Status.PENDING))
        assertEquals("failed", Compliance.resolve(false, LintStatusStore.Status.FAILED))
    }

    @Test
    fun `a never linted snippet is pending`() {
        assertEquals("pending", Compliance.resolve(null, null))
    }
}
