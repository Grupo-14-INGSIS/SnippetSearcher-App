package com.grupo14IngSis.snippetSearcherApp.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TestTest {

    @Test
    fun `Test should have correct properties`() {
        val id = "tid"
        val snippetId = "sid"
        val name = "tname"
        val inputs = listOf("a", "b")
        val expectedOutputs = listOf("c")
        val now = LocalDateTime.now()

        val test = Test(
            id = id,
            snippetId = snippetId,
            name = name,
            inputs = inputs,
            expectedOutputs = expectedOutputs,
            createdAt = now,
            updatedAt = now
        )

        assertEquals(id, test.id)
        assertEquals(snippetId, test.snippetId)
        assertEquals(name, test.name)
        assertEquals(inputs, test.inputs)
        assertEquals(expectedOutputs, test.expectedOutputs)
        assertEquals(now, test.createdAt)
        assertEquals(now, test.updatedAt)
    }

    @Test
    fun `TestResult should have correct properties`() {
        val testId = "tid"
        val passed = true
        val actualOutputs = listOf("c")
        val expectedOutputs = listOf("c")
        val error = "error"

        val result = TestResult(
            testId = testId,
            passed = passed,
            actualOutputs = actualOutputs,
            expectedOutputs = expectedOutputs,
            error = error
        )

        assertEquals(testId, result.testId)
        assertEquals(passed, result.passed)
        assertEquals(actualOutputs, result.actualOutputs)
        assertEquals(expectedOutputs, result.expectedOutputs)
        assertEquals(error, result.error)
    }
}