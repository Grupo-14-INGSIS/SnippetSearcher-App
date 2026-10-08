package com.grupo14IngSis.snippetSearcherApp.model

import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetTest {
    @Test
    fun `Snippet should have correct properties`() {
        val snippet =
            Snippet(
                snippetId = "s1",
                name = "sname",
                language = "kotlin",
                bucketId = "s1",
                formatterApplied = true,
                linterApplied = true,
                compliance = "compliant",
                description = "sdescription",
                version = "1.1",
            )

        assertEquals("s1", snippet.snippetId)
        assertEquals("sname", snippet.name)
        assertEquals("kotlin", snippet.language)
        assertEquals("s1", snippet.bucketId)
        assertEquals(true, snippet.formatterApplied)
        assertEquals(true, snippet.linterApplied)
        assertEquals("compliant", snippet.compliance)
        assertEquals("sdescription", snippet.description)
        assertEquals("1.1", snippet.version)
    }
}
