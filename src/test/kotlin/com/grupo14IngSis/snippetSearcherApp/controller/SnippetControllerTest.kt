package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SnippetController::class)
class SnippetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var accessManagerClient: AccessManagerClient

    private lateinit var runnerClient: RunnerClient

    @Test
    fun `getAllSnippets should return an empty list when authenticated`() {
        val jwt = Jwt.withTokenValue("token")
            .header("alg", "none")
            .claim("sub", "user123")
            .build()
        val auth: Authentication = Mockito.mock(Authentication::class.java)
        Mockito.`when`(auth.principal).thenReturn(jwt)

        mockMvc.perform(get("/api/v1/snippets").with(authentication(auth)))
            .andExpect(status().isOk)
            .andExpect(content().json("[]"))
    }
}
