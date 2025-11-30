package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.config.SecurityConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(SnippetController::class)
@Import(SecurityConfig::class, SnippetControllerTest.TestBeans::class)
@AutoConfigureMockMvc(addFilters = false)
class SnippetControllerTest {


    @Autowired
    lateinit var mockMvc: MockMvc

    private fun testJwt(): Jwt = Jwt
        .withTokenValue("token")
        .header("alg", "none")
        .claim("sub", "user123")
        .build()

    @Test fun `GET snippets`() {
        mockMvc.perform(get("/api/v1/snippets").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET registerSnippet`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1")
            .content("""{"snippetId":"snippet1","language":"kotlin","ownerId":"user123"}""")
            .with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET deleteSnippet`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET shareSnippet`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/permission")
            .content("""{"userId":"other"}""")
            .with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET removeSnippetPermission`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/permission").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET deleteUser`() {
        mockMvc.perform(get("/api/v1/users").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET getAllTests`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/tests").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET createTest`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/tests")
            .content("""{"snippetId":"snippet1","input":["1"],"expected":"2"}""")
            .with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET runTest`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/tests/test1").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET removeTest`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/tests/test1").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET runSnippet`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/run")
            .content("""{"input":"hello"}""")
            .with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET cancelSnippetExecution`() {
        mockMvc.perform(get("/api/v1/snippets/snippet1/run").with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET updateRules`() {
        mockMvc.perform(get("/api/v1/rules")
            .content("""{"task":"linting","language":"kotlin","rules":{"rule1":"val1"}}""")
            .with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @Test fun `GET getRules`() {
        mockMvc.perform(get("/api/v1/rules")
            .param("task", "linting")
            .param("language", "kotlin")
            .with(jwt().jwt(testJwt())))
            .andExpect(status().isOk)
    }

    @TestConfiguration
    class TestBeans {
        @Bean
        fun restTemplateBuilder(): RestTemplateBuilder = RestTemplateBuilder()

        @Bean
        fun accessManagerClient(restTemplateBuilder: RestTemplateBuilder): AccessManagerClient =
            AccessManagerClient(
                accessManagerUrl = "http://localhost/access",
                restTemplateBuilder = restTemplateBuilder
            )

        @Bean
        fun runnerClient(): RunnerClient = RunnerClient()
    }
}