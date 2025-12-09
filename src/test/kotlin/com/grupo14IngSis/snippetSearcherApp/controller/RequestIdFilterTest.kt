package com.grupo14IngSis.snippetSearcher

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.slf4j.MDC

class RequestIdFilterTest {
    private val filter = RequestIdFilter()

    @Test
    fun `should reuse existing X-Request-Id header`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val chain = mock(FilterChain::class.java)

        `when`(request.getHeader("X-Request-Id")).thenReturn("existing-id")

        filter.doFilter(request, response, chain)

        // Verifica que se setea el mismo ID en la respuesta
        verify(response).setHeader("X-Request-Id", "existing-id")
        // Verifica que se ejecuta el chain
        verify(chain).doFilter(request, response)
        // El MDC se limpia al final
        assertNull(MDC.get("request_id"))
    }

    @Test
    fun `should generate new request id if header missing`() {
        val request = mock(HttpServletRequest::class.java)
        val response = mock(HttpServletResponse::class.java)
        val chain = mock(FilterChain::class.java)

        `when`(request.getHeader("X-Request-Id")).thenReturn(null)

        filter.doFilter(request, response, chain)

        // Verifica que se setea algún UUID en la respuesta
        verify(response).setHeader(eq("X-Request-Id"), anyString())
        verify(chain).doFilter(request, response)
        assertNull(MDC.get("request_id"))
    }
}
