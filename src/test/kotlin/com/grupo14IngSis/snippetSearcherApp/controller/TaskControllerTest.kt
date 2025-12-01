package com.grupo14IngSis.snippetSearcherApp.controller
/*
import com.fasterxml.jackson.databind.ObjectMapper
import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TaskRequest
import com.grupo14IngSis.snippetSearcherApp.service.SnippetTaskProducer
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(TaskController::class)
@Import(TaskControllerTest.NoCsrfConfig::class, TaskControllerTest.StubConfig::class)
class TaskControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var snippetTaskProducer: SnippetTaskProducer

    @Autowired
    private lateinit var accessManagerClient: AccessManagerClient

    @TestConfiguration
    class StubConfig {
        @Bean
        fun snippetTaskProducer(): SnippetTaskProducer {
            return Mockito.mock(SnippetTaskProducer::class.java)
        }

        @Bean
        fun accessManagerClient(): AccessManagerClient {
            return Mockito.mock(AccessManagerClient::class.java)
        }
    }

    @TestConfiguration
    class NoCsrfConfig {
        @Bean
        fun filterChain(http: HttpSecurity): SecurityFilterChain {
            http.csrf().disable()
                .authorizeHttpRequests { it.anyRequest().permitAll() }
            return http.build()
        }
    }

    @Test
    fun `startTask should return bad request for invalid task`() {
        val taskRequest = TaskRequest(userId = "user123")
        val invalidTask = "invalidTask"

        mockMvc.perform(
            post("/api/v1/snippets/{task}", invalidTask)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().string("Invalid task '$invalidTask'. Valid tasks: [formatting, linting]"))
    }

    @Test
    fun `startTask should return accepted when user has snippets`() {
        val taskRequest = TaskRequest(userId = "user123")
        val validTask = "linting"
        val ownedSnippets = listOf("snippet1", "snippet2")
        val permissions = GetPermissionsForUserResponse(userId = taskRequest.userId, owned = ownedSnippets, shared = emptyList())

        Mockito.`when`(accessManagerClient.getPermissionsForUser(taskRequest.userId)).thenReturn(permissions)

        mockMvc.perform(
            post("/api/v1/snippets/{task}", validTask)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)),
        )
            .andExpect(status().isAccepted)
            .andExpect(content().string("$validTask tasks queued: ${ownedSnippets.size}"))

        ownedSnippets.forEach { snippetId ->
            Mockito.verify(snippetTaskProducer).requestTask(snippetId, validTask)
        }
    }

    @Test
    fun `startTask should return no content when user has no snippets`() {
        val taskRequest = TaskRequest(userId = "user123")
        val validTask = "linting"
        val permissions = GetPermissionsForUserResponse(userId = taskRequest.userId, owned = emptyList(), shared = emptyList())

        Mockito.`when`(accessManagerClient.getPermissionsForUser(taskRequest.userId)).thenReturn(permissions)

        mockMvc.perform(
            post("/api/v1/snippets/{task}", validTask)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskRequest)),
        )
            .andExpect(status().isNoContent)
    }
}
*/