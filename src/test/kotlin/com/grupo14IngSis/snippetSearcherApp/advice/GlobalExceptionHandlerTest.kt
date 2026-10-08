package com.grupo14IngSis.snippetSearcherApp.advice

import jakarta.servlet.http.HttpServletRequest
import org.apache.coyote.BadRequestException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.HttpStatus
import org.springframework.web.context.request.WebRequest

class GlobalExceptionHandlerTest {
    private val handler = GlobalExceptionHandler()

    private fun httpRequest(): HttpServletRequest {
        val request = mock(HttpServletRequest::class.java)
        `when`(request.method).thenReturn("GET")
        `when`(request.requestURI).thenReturn("/api/v1/test")
        return request
    }

    @Test
    fun `handleBadRequestException returns 400 with details`() {
        val response =
            handler.handleBadRequestException(
                BadRequestException("bad input"),
                mock(WebRequest::class.java),
                httpRequest(),
            )

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(400, response.body?.status)
        assertEquals("Invalid request", response.body?.message)
        assertEquals("bad input", response.body?.details)
    }

    @Test
    fun `handleAllExceptions returns 500 with details`() {
        val response =
            handler.handleAllExceptions(
                RuntimeException("boom"),
                mock(WebRequest::class.java),
                httpRequest(),
            )

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.statusCode)
        assertEquals(500, response.body?.status)
        assertEquals("boom", response.body?.details)
    }

    @Test
    fun `handleAllExceptions uses default details when message is null`() {
        val response =
            handler.handleAllExceptions(
                RuntimeException(),
                mock(WebRequest::class.java),
                httpRequest(),
            )

        assertEquals("No details available", response.body?.details)
    }
}
