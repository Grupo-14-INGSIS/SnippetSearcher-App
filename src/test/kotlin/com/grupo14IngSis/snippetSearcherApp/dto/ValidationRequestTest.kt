package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ValidationRequestTest {

    @Test
    fun `ValidationRequest should have correct properties`() {
        val content = "scontent"
        
        val request = ValidationRequest(
            content = content,
            language = "kotlin",
            version = "1.0"
        )

        assertEquals(content, request.content)
    }
}