package com.grupo14IngSis.snippetSearcherApp.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class CreateTestRequestDtoTest {

    companion object {
        @JvmStatic
        fun validInputs(): List<CreateTestRequestDto> = listOf(
            CreateTestRequestDto(snippetId = "sid-1", input = listOf("1", "2", "3"), expected = "6"),
            CreateTestRequestDto(snippetId = "alpha", input = listOf("hello", "world"), expected = "hello world"),
            CreateTestRequestDto(snippetId = "with-empty-elements", input = listOf("", "non-empty", ""), expected = "ok"),
            CreateTestRequestDto(snippetId = "unicode", input = listOf("á", "β", "漢字"), expected = "mixed"),
            CreateTestRequestDto(snippetId = "long-expected", input = listOf("a", "b"), expected = "x".repeat(1024))
        )

        @JvmStatic
        fun edgeCases(): List<CreateTestRequestDto> = listOf(
            CreateTestRequestDto(snippetId = "", input = listOf("data"), expected = "non-empty"),
            CreateTestRequestDto(snippetId = "sid", input = emptyList(), expected = ""),
            CreateTestRequestDto(snippetId = "spaces", input = listOf("   ", "\t"), expected = "   \t"),
            CreateTestRequestDto(snippetId = "very-long-id", input = listOf("x"), expected = "y").copy(snippetId = "s".repeat(512)),
            CreateTestRequestDto(snippetId = "control-chars", input = listOf("\n", "\r", "\u0000"), expected = "\n\r\u0000")
        )
    }

    @ParameterizedTest
    @MethodSource("validInputs")
    fun `should construct dto with valid inputs`(dto: CreateTestRequestDto) {
        assertNotNull(dto)
        assertTrue(dto.snippetId.isNotEmpty())
        assertNotNull(dto.input)
        assertNotNull(dto.expected)
    }

    @ParameterizedTest
    @MethodSource("edgeCases")
    fun `should handle edge cases without throwing`(dto: CreateTestRequestDto) {
        // Data classes have no built-in validation: ensure construction and field access are safe
        assertNotNull(dto)
        // Access fields to ensure no surprises
        dto.input.forEach { assertNotNull(it) }
        assertNotNull(dto.expected)
    }

    @Test
    fun `equals and hashCode should be consistent for identical instances`() {
        val a = CreateTestRequestDto("sid", listOf("1", "2"), "3")
        val b = CreateTestRequestDto("sid", listOf("1", "2"), "3")

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `equals should differ when any field changes`() {
        val base = CreateTestRequestDto("sid", listOf("1", "2"), "3")

        val diffId = base.copy(snippetId = "other")
        val diffInput = base.copy(input = listOf("x"))
        val diffExpected = base.copy(expected = "4")

        assertNotEquals(base, diffId)
        assertNotEquals(base, diffInput)
        assertNotEquals(base, diffExpected)
    }

    @Test
    fun `copy should keep unchanged fields and update only the provided ones`() {
        val original = CreateTestRequestDto("sid", listOf("in1", "in2"), "exp")
        val copied = original.copy(expected = "new-exp")

        assertEquals(original.snippetId, copied.snippetId)
        assertEquals(original.input, copied.input)
        assertEquals("new-exp", copied.expected)
    }

    @Test
    fun `toString should include field values for debugging`() {
        val dto = CreateTestRequestDto("sid", listOf("a", "b"), "c")
        val s = dto.toString()
        assertTrue(s.contains("sid"))
        assertTrue(s.contains("a"))
        assertTrue(s.contains("c"))
    }

    @Test
    fun `jackson serialization should produce expected json`() {
        val mapper = jacksonObjectMapper()
        val dto = CreateTestRequestDto("sid", listOf("a", "b"), "c")

        val json = mapper.writeValueAsString(dto)
        // Basic structure assertions
        assertTrue(json.contains("\"snippetId\":\"sid\""))
        assertTrue(json.contains("\"input\":[\"a\",\"b\"]"))
        assertTrue(json.contains("\"expected\":\"c\""))
    }

    @Test
    fun `jackson deserialization should reconstruct dto`() {
        val mapper = jacksonObjectMapper()
        val json = """{"snippetId":"sid","input":["a","b"],"expected":"c"}"""

        val dto: CreateTestRequestDto = mapper.readValue(json)

        assertEquals("sid", dto.snippetId)
        assertEquals(listOf("a", "b"), dto.input)
        assertEquals("c", dto.expected)
    }

    @Test
    fun `jackson roundtrip should preserve values`() {
        val mapper = jacksonObjectMapper()
        val original = CreateTestRequestDto("sid", listOf("", "β", "漢字"), "mixed")

        val json = mapper.writeValueAsString(original)
        val roundtrip: CreateTestRequestDto = mapper.readValue(json)

        assertEquals(original, roundtrip)
    }

    @Test
    fun `immutability expectations - input reference is the same list instance`() {
        // Kotlin data class does not defensively copy constructor arguments
        val inputList = mutableListOf("a", "b")
        val dto = CreateTestRequestDto("sid", inputList, "c")

        inputList.add("c") // mutate after construction

        // The dto will reflect the mutation since the list reference is shared
        assertEquals(listOf("a", "b", "c"), dto.input)
    }
}