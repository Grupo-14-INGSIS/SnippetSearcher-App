package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TestExecutionRequestTest {
    @Test
    fun `TestExecutionRequest should have correct properties`() {
        val testCaseIds = listOf("tid1", "tid2")

        val request =
            TestExecutionRequest(
                testCaseIds = testCaseIds,
            )

        assertEquals(testCaseIds, request.testCaseIds)
    }
}
