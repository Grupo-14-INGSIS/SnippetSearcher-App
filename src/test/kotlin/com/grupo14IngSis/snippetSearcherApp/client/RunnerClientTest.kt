package com.grupo14IngSis.snippetSearcherApp.client

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate
import kotlin.test.assertEquals
/*
class RunnerClientTest {
    private val restTemplate: RestTemplate = mock(RestTemplate::class.java)
    private val client =
        RunnerClient(restTemplate).apply {
            this.runnerServiceUrl = "http://localhost"
        }

    @Test
    fun `executeSnippet returns outputs`() {
        val response = ExecuteSnippetResponse(outputs = listOf("out1"))
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ExecuteSnippetResponse::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(response))

        val result = client.executeSnippet("code", listOf("in"))
        assertEquals(listOf("out1"), result)
    }

    @Test
    fun `executeSnippet returns empty list when body is null`() {
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ExecuteSnippetResponse::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = client.executeSnippet("code", listOf("in"))
        assertTrue(result.isEmpty())
    }

    @Test
    fun `executeSnippet throws RunnerExecutionException on HttpClientErrorException`() {
        val ex = HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad", null, "error".toByteArray(), null)
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ExecuteSnippetResponse::class.java),
            ),
        ).thenThrow(ex)

        val thrown =
            assertThrows(RunnerExecutionException::class.java) {
                client.executeSnippet("code", listOf())
            }
        assertTrue(thrown.message!!.contains("Error ejecutando snippet"))
    }

    @Test
    fun `executeSnippet throws RunnerExecutionException on HttpServerErrorException`() {
        val ex = HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Server", null, "serverErr".toByteArray(), null)
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ExecuteSnippetResponse::class.java),
            ),
        ).thenThrow(ex)

        val thrown =
            assertThrows(RunnerExecutionException::class.java) {
                client.executeSnippet("code", listOf())
            }
        assertTrue(thrown.message!!.contains("Error del servidor runner"))
    }

    @Test
    fun `executeSnippet throws RunnerExecutionException on generic Exception`() {
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ExecuteSnippetResponse::class.java),
            ),
        ).thenThrow(RuntimeException("boom"))

        val thrown =
            assertThrows(RunnerExecutionException::class.java) {
                client.executeSnippet("code", listOf())
            }
        assertTrue(thrown.message!!.contains("Error inesperado"))
    }

    @Test
    fun `validateSnippet returns valid result`() {
        val response = ValidationResponse(isValid = true, errors = listOf())
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ValidationResponse::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(response))

        val result = client.validateSnippet("code")
        assertTrue(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validateSnippet returns false when body is null`() {
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ValidationResponse::class.java),
            ),
        ).thenReturn(ResponseEntity.ok(null))

        val result = client.validateSnippet("code")
        assertFalse(result.isValid)
        assertTrue(result.errors.isEmpty())
    }

    @Test
    fun `validateSnippet returns error result on exception`() {
        `when`(
            restTemplate.postForEntity(
                anyString(),
                any(HttpEntity::class.java),
                eq(ValidationResponse::class.java),
            ),
        ).thenThrow(RuntimeException("fail"))

        val result = client.validateSnippet("code")
        assertFalse(result.isValid)
        assertTrue(result.errors[0].contains("Error validating snippet"))
    }

    @Test
    fun `DTOs equality and toString`() {
        val req1 = ExecuteSnippetRequest("c", listOf("i"))
        val req2 = ExecuteSnippetRequest("c", listOf("i"))
        assertEquals(req1, req2)
        assertEquals(req1.hashCode(), req2.hashCode())
        assertTrue(req1.toString().contains("c"))

        val resp = ExecuteSnippetResponse(listOf("o"))
        assertEquals(listOf("o"), resp.outputs)

        val valReq = ValidateSnippetRequest("c")
        assertEquals("c", valReq.code)

        val valResp = ValidationResponse(true, listOf("err"))
        assertTrue(valResp.isValid)
        assertEquals(listOf("err"), valResp.errors)

        val valRes = ValidationResult(false, listOf("err"))
        assertFalse(valRes.isValid)
        assertEquals(listOf("err"), valRes.errors)
    }

    @Test
    fun `RunnerExecutionException works`() {
        val ex = RunnerExecutionException("msg", RuntimeException("cause"))
        assertEquals("msg", ex.message)
        assertNotNull(ex.cause)
    }
}
*/