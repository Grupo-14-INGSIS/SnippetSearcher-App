package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateLintingRulesRequestTest {

    @Test
    fun `UpdateLintingRulesRequest should have correct properties`() {
        val rules = emptyList<LintingRule>()
        
        val request = UpdateLintingRulesRequest(
            rules = rules
        )

        assertEquals(rules, request.rules)
    }
}