package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetUpdateRequestTest {

    @Test
    fun `SnippetUpdateRequest should have correct properties`() {
        val task = "formatting"
        val language = "kotlin"
        val rules = emptyMap<String, Any>()
        
        val request = SnippetUpdateRequest(
            task = task,
            language = language,
            rules = rules
        )

        assertEquals(task, request.task)
        assertEquals(language, request.language)
        assertEquals(rules, request.rules)
    }
}