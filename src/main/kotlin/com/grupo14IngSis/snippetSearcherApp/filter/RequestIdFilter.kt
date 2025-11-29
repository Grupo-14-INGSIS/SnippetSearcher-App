package com.grupo14IngSis.snippetSearcherApp.filter

import com.newrelic.api.agent.NewRelic
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.*

@Component
class RequestIdFilter : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(RequestIdFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestId =
            request.getHeader("X-Request-ID")
                ?: UUID.randomUUID().toString()

        MDC.put("request_id", requestId)

        response.setHeader("X-Request-ID", requestId)

        NewRelic.addCustomParameter("request_id", requestId)
        NewRelic.addCustomParameter("service", "SnippetSearcher")

        logger.info("[SNIPPET-SEARCHER] Request $requestId - ${request.method} ${request.requestURI}")

        try {
            filterChain.doFilter(request, response)
        } finally {
            MDC.clear()
        }
    }
}
