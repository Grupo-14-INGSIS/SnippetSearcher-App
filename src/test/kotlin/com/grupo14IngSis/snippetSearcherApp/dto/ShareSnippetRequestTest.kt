package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ShareSnippetRequestTest {

    @Test
    fun `ShareSnippetRequest should have correct properties`() {
        val userId = "uid"
        
        val request = ShareSnippetRequest(
            userId = userId
        )

        assertEquals(userId, request.userId)
    }
}