package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LintingErrorTest {

    @Test
    fun `LintingError should have correct properties`() {
        val line = 1
        val column = 1
        val message = "error"
        
        val error = LintingError(
            rule = "test-rule",
            line = line,
            column = column,
            message = message
        )

        assertEquals(line, error.line)
        assertEquals(column, error.column)
        assertEquals(message, error.message)
    }
}