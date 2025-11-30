package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestExecutionResponseTest {

    @Test
    fun `TestExecutionResponse should have correct properties`() {
        val results = emptyList<TestResult>()
        val summary = TestSummary(0, 0, 0, 0, 0)
        
        val response = TestExecutionResponse(
            results = results,
            summary = summary
        )

        assertEquals(results, response.results)
        assertEquals(summary, response.summary)
    }
}