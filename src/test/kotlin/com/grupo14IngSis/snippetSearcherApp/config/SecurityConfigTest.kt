package com.grupo14IngSis.snippetSearcherApp.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(controllers = [DummyController::class])
@Import(SecurityConfig::class)
class SecurityConfigTest(
    @Autowired val mockMvc: MockMvc,
) {
    @Test
    fun `permitAll should allow access to non-api paths`() {
        mockMvc.get("/public")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `api path should be unauthorized without authentication`() {
        mockMvc.get("/api/v1/resource")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    @WithMockUser
    fun `api path should be accessible with authentication`() {
        mockMvc.get("/api/v1/resource")
            .andExpect {
                status { isOk() }
            }
    }
}

@RestController
class DummyController {
    @GetMapping("/public")
    fun publicEndpoint(): String = "ok"

    @GetMapping("/api/v1/resource")
    fun apiEndpoint(): String = "secured"

    @PostMapping("/public")
    fun publicPost(): String = "posted"
}
