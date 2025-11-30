package com.grupo14IngSis.snippetSearcherApp.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InvalidSnippetExceptionTest {
    @Test
    fun `InvalidSnippetException should have correct properties`() {
        val message = "error"
        val rule = "rule"
        val line = 1
        val column = 1

        val exception =
            InvalidSnippetException(
                message = message,
                rule = rule,
                line = line,
                column = column,
            )

        assertEquals(message, exception.message)
        assertEquals(rule, exception.rule)
        assertEquals(line, exception.line)
        assertEquals(column, exception.column)
    }
}
