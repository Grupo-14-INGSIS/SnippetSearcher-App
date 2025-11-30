package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetUpdateResponseTest {

    @Test
    fun `SnippetUpdateResponse should have correct properties`() {
        val id = "sid"
        val name = "sname"
        val content = "scontent"
        
        val response = SnippetUpdateResponse(
            id = id,
            name = name,
            description = "description",
            language = "kotlin",
            version = "1.0",
            content = content,
            isValid = true
        )

        assertEquals(id, response.id)
        assertEquals(name, response.name)
        assertEquals(content, response.content)
    }
}