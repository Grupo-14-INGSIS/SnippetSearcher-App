package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetCreationResponseTest {

    @Test
    fun `SnippetCreationResponse should have correct properties`() {
        val success = true
        val message = "Snippet created successfully"
        
        val response = SnippetCreationResponse(
            success = success,
            message = message
        )

        assertEquals(success, response.success)
        assertEquals(message, response.message)
    }
}