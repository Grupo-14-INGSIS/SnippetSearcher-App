package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestResponseTest {
    @Test
    fun `TestResponse should have correct properties`() {
        val id = "tid"
        val snippetId = "sid"
        val name = "tname"
        val inputs = listOf("input")
        val expectedOutputs = listOf("output")
        val createdAt = "2023-11-30T10:00:00"
        val updatedAt = "2023-11-30T10:00:00"

        val testResponse =
            TestResponse(
                id = id,
                snippetId = snippetId,
                name = name,
                inputs = inputs,
                expectedOutputs = expectedOutputs,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )

        assertEquals(id, testResponse.id)
        assertEquals(snippetId, testResponse.snippetId)
        assertEquals(name, testResponse.name)
        assertEquals(inputs, testResponse.inputs)
        assertEquals(expectedOutputs, testResponse.expectedOutputs)
        assertEquals(createdAt, testResponse.createdAt)
        assertEquals(updatedAt, testResponse.updatedAt)
    }
}
