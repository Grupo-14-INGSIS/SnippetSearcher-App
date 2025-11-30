package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SnippetDownloadTest {

    @Test
    fun `SnippetDownload should have correct properties`() {
        val content = "scontent"
        
        val download = SnippetDownload(
            id = "sid",
            name = "sname",
            content = content,
            language = "kotlin",
            extension = ".kt"
        )

        assertEquals(content, download.content)
    }
}