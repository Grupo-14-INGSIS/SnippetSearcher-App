package com.grupo14IngSis.snippetSearcherApp.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TestCaseTest {

    @Test
    fun `should create TestCase with all fields`() {
        val tc = TestCase(
            id = "tc1",
            name = "Addition test",
            input = listOf("1", "2"),
            expectedOutput = listOf("3"),
            status = TestStatus.PASSED
        )

        assertEquals("tc1", tc.id)
        assertEquals("Addition test", tc.name)
        assertEquals(listOf("1", "2"), tc.input)
        assertEquals(listOf("3"), tc.expectedOutput)
        assertEquals(TestStatus.PASSED, tc.status)
    }

    @Test
    fun `should default status to PENDING`() {
        val tc = TestCase("tc2", "Default status", listOf("a"), listOf("b"))
        assertEquals(TestStatus.PENDING, tc.status)
    }

    @Test
    fun `equals and hashCode should be consistent`() {
        val a = TestCase("id", "name", listOf("x"), listOf("y"), TestStatus.FAILED)
        val b = TestCase("id", "name", listOf("x"), listOf("y"), TestStatus.FAILED)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `equals should differ when any field changes`() {
        val base = TestCase("id", "name", listOf("x"), listOf("y"), TestStatus.PENDING)

        assertNotEquals(base, base.copy(id = "other"))
        assertNotEquals(base, base.copy(name = "other"))
        assertNotEquals(base, base.copy(input = listOf("z")))
        assertNotEquals(base, base.copy(expectedOutput = listOf("z")))
        assertNotEquals(base, base.copy(status = TestStatus.PASSED))
    }

    @Test
    fun `copy should update only specified fields`() {
        val original = TestCase("id", "name", listOf("x"), listOf("y"), TestStatus.PENDING)
        val copy = original.copy(name = "new-name", status = TestStatus.FAILED)

        assertEquals("id", copy.id)
        assertEquals("new-name", copy.name)
        assertEquals(listOf("x"), copy.input)
        assertEquals(listOf("y"), copy.expectedOutput)
        assertEquals(TestStatus.FAILED, copy.status)
    }

    @Test
    fun `toString should contain field values`() {
        val tc = TestCase("id", "name", listOf("a"), listOf("b"), TestStatus.PASSED)
        val s = tc.toString()

        assertTrue(s.contains("id"))
        assertTrue(s.contains("name"))
        assertTrue(s.contains("a"))
        assertTrue(s.contains("b"))
        assertTrue(s.contains("PASSED"))
    }

    @Test
    fun `jackson serialization should produce expected json`() {
        val mapper = jacksonObjectMapper()
        val tc = TestCase("id", "name", listOf("a"), listOf("b"), TestStatus.FAILED)

        val json = mapper.writeValueAsString(tc)

        assertTrue(json.contains("\"id\":\"id\""))
        assertTrue(json.contains("\"name\":\"name\""))
        assertTrue(json.contains("\"input\":[\"a\"]"))
        assertTrue(json.contains("\"expectedOutput\":[\"b\"]"))
        assertTrue(json.contains("\"status\":\"FAILED\""))
    }

    @Test
    fun `jackson deserialization should reconstruct object`() {
        val mapper = jacksonObjectMapper()
        val json = """{"id":"id","name":"name","input":["a"],"expectedOutput":["b"],"status":"PASSED"}"""

        val tc: TestCase = mapper.readValue(json)

        assertEquals("id", tc.id)
        assertEquals("name", tc.name)
        assertEquals(listOf("a"), tc.input)
        assertEquals(listOf("b"), tc.expectedOutput)
        assertEquals(TestStatus.PASSED, tc.status)
    }

    @Test
    fun `jackson roundtrip should preserve values`() {
        val mapper = jacksonObjectMapper()
        val original = TestCase("id", "name", listOf("x", "y"), listOf("z"), TestStatus.PENDING)

        val json = mapper.writeValueAsString(original)
        val roundtrip: TestCase = mapper.readValue(json)

        assertEquals(original, roundtrip)
    }

    @Test
    fun `enum TestStatus should contain all values`() {
        val values = TestStatus.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(TestStatus.PENDING))
        assertTrue(values.contains(TestStatus.PASSED))
        assertTrue(values.contains(TestStatus.FAILED))
    }

    @Test
    fun `should handle empty lists and blank fields`() {
        val tc = TestCase("", "", emptyList(), emptyList())
        assertEquals("", tc.id)
        assertEquals("", tc.name)
        assertTrue(tc.input.isEmpty())
        assertTrue(tc.expectedOutput.isEmpty())
        assertEquals(TestStatus.PENDING, tc.status)
    }
}