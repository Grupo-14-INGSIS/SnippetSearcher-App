package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LintingRuleTest {
    @Test
    fun `LintingRule should have correct properties`() {
        val name = "ruleName"
        val isActive = true

        val rule =
            LintingRule(
                name = name,
                isActive = isActive,
            )

        assertEquals(name, rule.name)
        assertEquals(isActive, rule.isActive)
    }
}
