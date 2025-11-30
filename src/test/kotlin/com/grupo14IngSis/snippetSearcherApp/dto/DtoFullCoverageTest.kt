package com.grupo14IngSis.snippetSearcherApp.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DtoFullCoverageTest {
    private val mapper = jacksonObjectMapper()

    @Test
    fun `should create CreateTestRequest and validate fields`() {
        val dto = CreateTestRequest("sum test", listOf("1", "2"), listOf("3"))
        assertEquals("sum test", dto.name)
        assertEquals(listOf("1", "2"), dto.inputs)
        assertEquals(listOf("3"), dto.expectedOutputs)
    }

    @Test
    fun `CreateTestRequest equals and hashCode consistency`() {
        val a = CreateTestRequest("n", listOf("x"), listOf("y"))
        val b = CreateTestRequest("n", listOf("x"), listOf("y"))
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, a.copy(name = "other"))
    }

    @Test
    fun `UpdateTestRequest should allow nulls`() {
        val dto = UpdateTestRequest(null, null, null)
        assertNull(dto.name)
        assertNull(dto.inputs)
        assertNull(dto.expectedOutputs)
    }

    @Test
    fun `UpdateTestRequest copy should update only specified fields`() {
        val base = UpdateTestRequest("n", listOf("a"), listOf("b"))
        val copy = base.copy(inputs = listOf("c"))
        assertEquals("n", copy.name)
        assertEquals(listOf("c"), copy.inputs)
        assertEquals(listOf("b"), copy.expectedOutputs)
    }

    @Test
    fun `TestResponse should create and validate fields`() {
        val dto = TestResponse("id1", "sid1", "case1", listOf("in"), listOf("out"), "created", "updated")
        assertEquals("id1", dto.id)
        assertEquals("sid1", dto.snippetId)
        assertEquals("case1", dto.name)
        assertEquals(listOf("in"), dto.inputs)
        assertEquals(listOf("out"), dto.expectedOutputs)
        assertEquals("created", dto.createdAt)
        assertEquals("updated", dto.updatedAt)
    }

    @Test
    fun `TestResponse equals and copy`() {
        val a = TestResponse("id", "sid", "n", listOf("i"), listOf("o"), "c", "u")
        val b = TestResponse("id", "sid", "n", listOf("i"), listOf("o"), "c", "u")
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        val copy = a.copy(name = "new")
        assertEquals("new", copy.name)
    }

    @Test
    fun `TestResultResponse should create and validate fields`() {
        val dto = TestResultResponse("tid", true, listOf("a"), listOf("a"), null, true)
        assertEquals("tid", dto.testId)
        assertTrue(dto.passed)
        assertEquals(listOf("a"), dto.actualOutputs)
        assertEquals(listOf("a"), dto.expectedOutputs)
        assertNull(dto.error)
        assertTrue(dto.isValid)
    }

    @Test
    fun `TestResultResponse equals and copy`() {
        val a = TestResultResponse("tid", false, listOf("x"), listOf("y"), "err", false)
        val b = TestResultResponse("tid", false, listOf("x"), listOf("y"), "err", false)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        val copy = a.copy(passed = true, error = null)
        assertTrue(copy.passed)
        assertNull(copy.error)
    }

    @Test
    fun `toString should contain values`() {
        val dto = CreateTestRequest("n", listOf("i"), listOf("o"))
        val s = dto.toString()
        assertTrue(s.contains("n"))
        assertTrue(s.contains("i"))
        assertTrue(s.contains("o"))
    }

    @Test
    fun `jackson serialization and deserialization roundtrip`() {
        val original = TestResponse("id", "sid", "n", listOf("i"), listOf("o"), "c", "u")
        val json = mapper.writeValueAsString(original)
        val roundtrip: TestResponse = mapper.readValue(json)
        assertEquals(original, roundtrip)
    }

    @Test
    fun `jackson serialization should include fields`() {
        val dto = TestResultResponse("tid", true, listOf("a"), listOf("b"), "err", false)
        val json = mapper.writeValueAsString(dto)
        assertTrue(json.contains("\"testId\":\"tid\""))
        assertTrue(json.contains("\"passed\":true"))
        assertTrue(json.contains("\"actualOutputs\":[\"a\"]"))
        assertTrue(json.contains("\"expectedOutputs\":[\"b\"]"))
        assertTrue(json.contains("\"error\":\"err\""))
        assertTrue(json.contains("\"isValid\":false"))
    }

    @Test
    fun `edge cases with empty lists and blank strings`() {
        val dto = CreateTestRequest("", emptyList(), emptyList())
        assertEquals("", dto.name)
        assertTrue(dto.inputs.isEmpty())
        assertTrue(dto.expectedOutputs.isEmpty())
    }
}
