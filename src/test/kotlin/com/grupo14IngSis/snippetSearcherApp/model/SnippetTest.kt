package com.grupo14IngSis.snippetSearcherApp.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetTest {
    @Test
    fun `Snippet should have correct properties`() {
        val id = 1L
        val name = "sname"
        val description = "sdescription"
        val language = "kotlin"
        val version = "1.0"
        val code = "scode"

        val snippet =
            Snippet(
                id = id,
                name = name,
                description = description,
                language = language,
                version = version,
                code = code,
            )

        assertEquals(id, snippet.id)
        assertEquals(name, snippet.name)
        assertEquals(description, snippet.description)
        assertEquals(language, snippet.language)
        assertEquals(version, snippet.version)
        assertEquals(code, snippet.code)
    }
}
