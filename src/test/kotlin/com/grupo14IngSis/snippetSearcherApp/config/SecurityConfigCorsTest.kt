package com.grupo14IngSis.snippetSearcherApp.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest

class SecurityConfigCorsTest {
    private fun corsFor(origins: List<String>) =
        SecurityConfig(origins)
            .corsConfigurationSource()
            .getCorsConfiguration(MockHttpServletRequest("GET", "/api/v1/snippets"))!!

    private val defaults = SecurityConfig.DEFAULT_ALLOWED_ORIGINS.split(",")

    @Test
    fun `the UI domains and localhost are allowed`() {
        val cors = corsFor(defaults)

        assertEquals("https://snippet26dev.duckdns.org", cors.checkOrigin("https://snippet26dev.duckdns.org"))
        assertEquals("https://snippet26prod.duckdns.org", cors.checkOrigin("https://snippet26prod.duckdns.org"))
        assertEquals("http://localhost:5173", cors.checkOrigin("http://localhost:5173"))
    }

    @Test
    fun `any other site is rejected (antes con * se aceptaba cualquiera)`() {
        val cors = corsFor(defaults)

        assertNull(cors.checkOrigin("https://evil.example.com"))
        assertNull(cors.checkOrigin("http://localhost:3000"))
        assertNull(cors.checkOrigin("http://snippet26dev.duckdns.org")) // http, no https
    }

    @Test
    fun `no credentials and no wildcard`() {
        val cors = corsFor(defaults)

        assertFalse(cors.allowCredentials ?: false)
        assertFalse(cors.allowedOrigins!!.contains("*"))
        assertNull(cors.allowedOriginPatterns)
    }

    @Test
    fun `origins from the property are trimmed and a trailing slash is ignored`() {
        val cors = corsFor(listOf(" https://a.example.com/ ", "", "http://localhost:5173"))

        assertEquals(listOf("https://a.example.com", "http://localhost:5173"), cors.allowedOrigins)
    }
}
