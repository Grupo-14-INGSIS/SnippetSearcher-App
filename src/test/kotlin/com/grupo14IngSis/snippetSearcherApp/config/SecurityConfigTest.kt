package com.grupo14IngSis.snippetSearcherApp.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest
@Import(SecurityConfig::class)
class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `unauthenticated access to api endpoint should be unauthorized`() {
        mockMvc.perform(get("/api/v1/snippets"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @WithMockUser
    fun `authenticated access to api endpoint should be authorized`() {
        // This will likely fail with 404 since there is no actual controller registered
        // for this test, but it will pass the security filter. A 401 or 403 would indicate
        // a security configuration issue.
        mockMvc.perform(get("/api/v1/snippets"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `access to non-api endpoint should be permitted`() {
        mockMvc.perform(get("/actuator/health"))
            .andExpect(status().isNotFound) // Or OK if the endpoint was configured
    }
}