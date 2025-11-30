package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

@WebMvcTest(SnippetController::class)
class SnippetControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean private lateinit var accessManagerClient: AccessManagerClient
    @MockBean private lateinit var runnerClient: RunnerClient
    @MockBean private lateinit var jwtDecoder: JwtDecoder
    @MockBean private lateinit var jwtAuthenticationConverter: JwtAuthenticationConverter

    private val USER_ID = "testUser"

    private fun setupJwt() {
        val jwt = Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            mapOf("alg" to "none"),
            mapOf("sub" to USER_ID)
        )
        `when`(jwtDecoder.decode("token")).thenReturn(jwt)
        `when`(jwtAuthenticationConverter.convert(jwt))
            .thenReturn(JwtAuthenticationToken(jwt, listOf(SimpleGrantedAuthority("SCOPE_openid"))))
    }

    @Test fun `getAllSnippets returns 200`() {
        setupJwt()
        mockMvc.perform(get("/api/v1/snippets")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `registerSnippet returns 200`() {
        setupJwt()
        mockMvc.perform(put("/api/v1/snippets/snippet1")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"snippetId":"snippet1","language":"kotlin","ownerId":"$USER_ID"}"""))
            .andExpect(status().isOk)
    }

    @Test fun `deleteSnippet returns 200`() {
        setupJwt()
        mockMvc.perform(delete("/api/v1/snippets/snippet1")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `shareSnippet returns 200`() {
        setupJwt()
        mockMvc.perform(put("/api/v1/snippets/snippet1/permission")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"userId":"otherUser"}"""))
            .andExpect(status().isOk)
    }

    @Test fun `removeSnippetPermission returns 200`() {
        setupJwt()
        mockMvc.perform(delete("/api/v1/snippets/snippet1/permission")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `deleteUser returns 200`() {
        setupJwt()
        mockMvc.perform(delete("/api/v1/users")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `getAllTests returns 200`() {
        setupJwt()
        mockMvc.perform(get("/api/v1/snippets/snippet1/tests")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `createTest returns 200`() {
        setupJwt()
        mockMvc.perform(post("/api/v1/snippets/snippet1/tests")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"snippetId":"snippet1","input":["1"],"expected":"2"}"""))
            .andExpect(status().isOk)
    }

    @Test fun `runTest returns 200`() {
        setupJwt()
        mockMvc.perform(put("/api/v1/snippets/snippet1/tests/test1")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `removeTest returns 200`() {
        setupJwt()
        mockMvc.perform(delete("/api/v1/snippets/snippet1/tests/test1")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `runSnippet returns 200`() {
        setupJwt()
        mockMvc.perform(post("/api/v1/snippets/snippet1/run")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"input":"hello"}"""))
            .andExpect(status().isOk)
    }

    @Test fun `cancelSnippetExecution returns 200`() {
        setupJwt()
        mockMvc.perform(delete("/api/v1/snippets/snippet1/run")
            .header("Authorization", "Bearer token"))
            .andExpect(status().isOk)
    }

    @Test fun `updateRules returns 200`() {
        setupJwt()
        mockMvc.perform(put("/api/v1/rules")
            .header("Authorization", "Bearer token")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"task":"linting","language":"kotlin","rules":{"rule1":"val1"}}"""))
            .andExpect(status().isOk)
    }

    @Test fun `getRules returns 200`() {
        setupJwt()
        mockMvc.perform(get("/api/v1/rules")
            .header("Authorization", "Bearer token")
            .param("task", "linting")
            .param("language", "kotlin"))
            .andExpect(status().isOk)
    }
}