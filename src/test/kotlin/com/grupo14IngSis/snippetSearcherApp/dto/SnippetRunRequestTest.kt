package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetRunRequestTest {
    @Test
    fun `SnippetRunRequest should have correct properties`() {
        val input = "input"

        val request =
            SnippetRunRequest(
                input = input,
            )

        assertEquals(input, request.input)
    }
}
