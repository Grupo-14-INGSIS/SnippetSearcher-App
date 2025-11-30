package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcher.RequestIdFilter
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.slf4j.MDC
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class RequestIdFilterTest {

    private val filter = RequestIdFilter()
    private val filterChain = mock(FilterChain::class.java)

    @Test
    fun `should use request id from header if present`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()
        val requestId = "my-test-request-id"
        request.addHeader("X-Request-Id", requestId)

        filter.doFilter(request, response, filterChain)

        assertEquals(requestId, response.getHeader("X-Request-Id"))
        assertEquals(requestId, MDC.get("request_id"))
        verify(filterChain).doFilter(request, response)
        MDC.clear()
    }

    @Test
    fun `should generate request id if not present in header`() {
        val request = MockHttpServletRequest()
        val response = MockHttpServletResponse()

        filter.doFilter(request, response, filterChain)

        assertNotNull(response.getHeader("X-Request-Id"))
        assertNotNull(MDC.get("request_id"))
        verify(filterChain).doFilter(request, response)
        MDC.clear()
    }
}