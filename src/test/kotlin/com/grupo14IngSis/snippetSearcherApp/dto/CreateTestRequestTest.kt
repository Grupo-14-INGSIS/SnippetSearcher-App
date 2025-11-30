package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateTestRequestTest {

    @Test
    fun `CreateTestRequest should have correct properties`() {
        val snippetId = "sid"
        val input = listOf("a", "b")
        val expected = "c"
        
        val request = CreateTestRequestDto(
            snippetId = snippetId,
            input = input,
            expected = expected
        )

        assertEquals(snippetId, request.snippetId)
        assertEquals(input, request.input)
        assertEquals(expected, request.expected)
    }
}