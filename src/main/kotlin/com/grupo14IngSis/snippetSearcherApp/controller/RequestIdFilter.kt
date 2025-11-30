package com.grupo14IngSis.snippetSearcher

import com.newrelic.api.agent.NewRelic
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class RequestIdFilter {
    fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse

        val requestId = httpRequest.getHeader("X-Request-Id") ?: UUID.randomUUID().toString()

        MDC.put("request_id", requestId)

        NewRelic.addCustomParameter("request_id", requestId)

        httpResponse.setHeader("X-Request-Id", requestId)

        try {
            chain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
