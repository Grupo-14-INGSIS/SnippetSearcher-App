package com.grupo14IngSis.snippetSearcherApp.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class ValidationResponseTest {
    @Test
    fun `should create ValidationResponse with all fields`() {
        val response =
            ValidationResponse(
                isValid = false,
                message = "Syntax error",
                rule = "no-tabs",
                line = 10,
                column = 5,
            )

        assertFalse(response.isValid)
        assertEquals("Syntax error", response.message)
        assertEquals("no-tabs", response.rule)
        assertEquals(10, response.line)
        assertEquals(5, response.column)
    }

    @Test
    fun `should allow null optional fields`() {
        val response =
            ValidationResponse(
                isValid = true,
                message = "All good",
                rule = null,
                line = null,
                column = null,
            )

        assertTrue(response.isValid)
        assertEquals("All good", response.message)
        assertNull(response.rule)
        assertNull(response.line)
        assertNull(response.column)
    }

    @Test
    fun `equals and hashCode should be consistent`() {
        val r1 = ValidationResponse(false, "Error", "rule1", 1, 2)
        val r2 = ValidationResponse(false, "Error", "rule1", 1, 2)

        assertEquals(r1, r2)
        assertEquals(r1.hashCode(), r2.hashCode())
    }

    @Test
    fun `equals should differ when any field changes`() {
        val base = ValidationResponse(false, "Error", "rule1", 1, 2)

        assertNotEquals(base, base.copy(isValid = true))
        assertNotEquals(base, base.copy(message = "Other"))
        assertNotEquals(base, base.copy(rule = "rule2"))
        assertNotEquals(base, base.copy(line = 99))
        assertNotEquals(base, base.copy(column = 99))
    }

    @Test
    fun `copy should update only specified fields`() {
        val original = ValidationResponse(false, "Error", "rule1", 1, 2)
        val copy = original.copy(message = "Updated", column = 10)

        assertEquals(original.isValid, copy.isValid)
        assertEquals("Updated", copy.message)
        assertEquals(original.rule, copy.rule)
        assertEquals(original.line, copy.line)
        assertEquals(10, copy.column)
    }

    @Test
    fun `toString should contain field values`() {
        val response = ValidationResponse(true, "OK", "ruleX", 3, 4)
        val s = response.toString()

        assertTrue(s.contains("OK"))
        assertTrue(s.contains("ruleX"))
        assertTrue(s.contains("3"))
        assertTrue(s.contains("4"))
    }

    @Test
    fun `jackson serialization should produce expected json`() {
        val mapper = jacksonObjectMapper()
        val response = ValidationResponse(false, "Error", "rule1", 1, 2)

        val json = mapper.writeValueAsString(response)

        assertTrue(json.contains("\"isValid\":false"))
        assertTrue(json.contains("\"message\":\"Error\""))
        assertTrue(json.contains("\"rule\":\"rule1\""))
        assertTrue(json.contains("\"line\":1"))
        assertTrue(json.contains("\"column\":2"))
    }

    @Test
    fun `jackson deserialization should reconstruct object`() {
        val mapper = jacksonObjectMapper()
        val json = """{"isValid":true,"message":"OK","rule":null,"line":null,"column":null}"""

        val response: ValidationResponse = mapper.readValue(json)

        assertTrue(response.isValid)
        assertEquals("OK", response.message)
        assertNull(response.rule)
        assertNull(response.line)
        assertNull(response.column)
    }

    @Test
    fun `jackson roundtrip should preserve values`() {
        val mapper = jacksonObjectMapper()
        val original = ValidationResponse(false, "Error", "rule1", 42, 99)

        val json = mapper.writeValueAsString(original)
        val roundtrip: ValidationResponse = mapper.readValue(json)

        assertEquals(original, roundtrip)
    }
}
