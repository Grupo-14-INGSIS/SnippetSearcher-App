package com.grupo14IngSis.snippetSearcherApp.config

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@WebMvcTest(controllers = [DummyController::class])
@Import(SecurityConfig::class)
class SecurityConfigTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean(enforceOverride = false)
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `permitAll should allow access to non-api paths`() {
        mockMvc
            .get("/public")
            .andExpect {
                status { isOk() }
            }
    }

    @Test
    fun `api path should be unauthorized without authentication`() {
        mockMvc
            .get("/api/v1/resource")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `api path should be accessible with authentication`() {
        mockMvc
            .get("/api/v1/resource") {
                with(jwt())
            }.andExpect {
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
