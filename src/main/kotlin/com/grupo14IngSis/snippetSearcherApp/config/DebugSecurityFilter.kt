package com.grupo14IngSis.snippetSearcherApp.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Profile("dev")
@Component
class DebugSecurityFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        println("==========================================")
        println("🔵 REQUEST: ${request.method} ${request.requestURI}")
        println("🔵 Headers:")
        request.headerNames.asIterator().forEach { headerName ->
            println("   $headerName: ${request.getHeader(headerName)}")
        }
        println("🔵 Profile active: ${System.getProperty("spring.profiles.active")}")
        println("==========================================")

        try {
            filterChain.doFilter(request, response)
            println("✅ Response status: ${response.status}")
        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
}
