package com.grupo14IngSis.snippetSearcherApp.model

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class TestTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `TestResult roundtrip serialization`() {
        val original =
            TestResult(
                testId = "tid",
                passed = true,
                actualOutputs = listOf("3"),
                expectedOutputs = listOf("3"),
                error = null,
            )

        val json = mapper.writeValueAsString(original)
        val roundtrip: TestResult = mapper.readValue(json)

        assertEquals(original, roundtrip)
    }
}
