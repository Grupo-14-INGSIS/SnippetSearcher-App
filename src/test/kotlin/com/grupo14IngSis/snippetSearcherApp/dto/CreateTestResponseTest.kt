package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreateTestResponseTest {

    @Test
    fun `CreateTestResponse should have correct properties`() {
        val testId = "tid"
        
        val response = CreateTestResponse(
            testId = testId
        )

        assertEquals(testId, response.testId)
    }
}