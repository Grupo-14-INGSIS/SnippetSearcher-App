package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetCreationRequestTest {

    @Test
    fun `SnippetCreationRequest should have correct properties`() {
        val name = "snippetName"
        val code = "snippetContent"
        
        val request = SnippetCreationRequest(
            userId = "testUser",
            name = name,
            description = "description",
            language = "kotlin",
            code = code
        )

        assertEquals(name, request.name)
        assertEquals(code, request.code)
    }
}