package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals

class TestResultTest {
    @Test
    fun `should create TestResult with all fields`() {
        val result =
            TestResult(
                testCaseId = "tc1",
                testCaseName = "simple case",
                status = TestStatus.PASSED,
                actualOutput = listOf("output1"),
                expectedOutput = listOf("output1"),
                errorMessage = null,
                executionTime = 120L,
            )

        assertEquals("tc1", result.testCaseId)
        assertEquals("simple case", result.testCaseName)
        assertEquals(TestStatus.PASSED, result.status)
        assertEquals(listOf("output1"), result.actualOutput)
        assertEquals(listOf("output1"), result.expectedOutput)
        assertNull(result.errorMessage)
        assertEquals(120L, result.executionTime)
    }

    @Test
    fun `should compare equality of two identical TestResults`() {
        val r1 = TestResult("tc1", "case", TestStatus.FAILED, listOf("a"), listOf("b"), "error", 50L)
        val r2 = TestResult("tc1", "case", TestStatus.FAILED, listOf("a"), listOf("b"), "error", 50L)

        assertEquals(r1, r2)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    @Test
    fun `should copy TestResult with modified fields`() {
        val original = TestResult("tc1", "case", TestStatus.FAILED, listOf("a"), listOf("b"), "error", 50L)
        val copy = original.copy(status = TestStatus.PASSED, executionTime = 100L)

        assertEquals("tc1", copy.testCaseId)
        assertEquals("case", copy.testCaseName)
        assertEquals(TestStatus.PASSED, copy.status)
        assertEquals(listOf("a"), copy.actualOutput)
        assertEquals(listOf("b"), copy.expectedOutput)
        assertEquals("error", copy.errorMessage)
        assertEquals(100L, copy.executionTime)
    }

    @Test
    fun `should handle null optional fields`() {
        val result = TestResult("tc2", "case2", TestStatus.FAILED, listOf("x"), listOf("y"))

        assertNull(result.errorMessage)
        assertNull(result.executionTime)
    }
}
