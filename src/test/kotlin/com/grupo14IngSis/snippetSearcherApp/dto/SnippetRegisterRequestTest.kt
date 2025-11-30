package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetRegisterRequestTest {
    @Test
    fun `SnippetRegisterRequest should have correct properties`() {
        val snippetId = "sid"
        val language = "kotlin"
        val ownerId = "uid"

        val request =
            SnippetRegisterRequest(
                snippetId = snippetId,
                language = language,
                ownerId = ownerId,
            )

        assertEquals(snippetId, request.snippetId)
        assertEquals(language, request.language)
        assertEquals(ownerId, request.ownerId)
    }
}
