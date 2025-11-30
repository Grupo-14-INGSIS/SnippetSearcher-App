package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LintingConfigTest {

    @Test
    fun `LintingConfig should have correct properties`() {
        val rules = listOf(LintingRule("ruleName", true, "description"))
        
        val config = LintingConfig(
            userId = "testUser",
            rules = rules
        )

        assertEquals(rules, config.rules)
        assertEquals("testUser", config.userId)
    }
}