package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.config.SecurityConfig
import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import com.grupo14IngSis.snippetSearcherApp.domain.UserData
import com.grupo14IngSis.snippetSearcherApp.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForSnippetResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import com.grupo14IngSis.snippetSearcherApp.dto.RunTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetData
import com.grupo14IngSis.snippetSearcherApp.dto.StartExecutionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TestResult
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import com.grupo14IngSis.snippetSearcherApp.repository.TestRepository
import com.grupo14IngSis.snippetSearcherApp.repository.UserDataRepository
import com.grupo14IngSis.snippetSearcherApp.service.SnippetTaskProducer
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.HttpClientErrorException
import java.util.Optional
import com.grupo14IngSis.snippetSearcherApp.domain.Test as DomainTest

@WebMvcTest(SnippetController::class)
@Import(SecurityConfig::class)
@TestPropertySource(properties = ["redis.stream.key=test-stream"])
class SnippetControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean(enforceOverride = false)
    private lateinit var accessManagerClient: AccessManagerClient

    @MockitoBean(enforceOverride = false)
    private lateinit var runnerClient: RunnerClient

    @MockitoBean(enforceOverride = false)
    private lateinit var snippetRepository: SnippetRepository

    @MockitoBean(enforceOverride = false)
    private lateinit var testRepository: TestRepository

    @MockitoBean(enforceOverride = false)
    private lateinit var userDataRepository: UserDataRepository

    @MockitoBean(enforceOverride = false)
    private lateinit var snippetTaskProducer: SnippetTaskProducer

    @MockitoBean(enforceOverride = false)
    private lateinit var redisTemplate: RedisTemplate<String, String>

    @MockitoBean(enforceOverride = false)
    private lateinit var jwtDecoder: JwtDecoder

    private val userId = "testUser"

    @Test
    fun `getAllSnippets returns 200`() {
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, listOf("snippet1"), emptyList()))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))

        mockMvc
            .perform(
                get("/api/v1/snippets")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `getAllSnippets registers shared snippets missing in local db`() {
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, emptyList(), listOf("shared1", "missing")))
        `when`(snippetRepository.findById("shared1")).thenReturn(Optional.empty())
        `when`(snippetRepository.findById("missing")).thenReturn(Optional.empty())
        `when`(runnerClient.getSnippetData("shared1"))
            .thenReturn(SnippetData("shared1", "name", "printscript"))
        `when`(runnerClient.getSnippetData("missing")).thenReturn(null)

        mockMvc
            .perform(
                get("/api/v1/snippets")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `deleteSnippet returns 401 when user is not owner`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))

        mockMvc
            .perform(
                delete("/api/v1/snippets/snippet1")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `shareSnippet returns 404 when target user does not exist`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(userDataRepository.findByUserName("nobody")).thenReturn(null)

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/permissions")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"nobody"}"""),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `shareSnippet returns 409 when already shared`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(userDataRepository.findByUserName("otherUser"))
            .thenReturn(UserData("otherUserId", "otherUser"))
        `when`(accessManagerClient.postPermission("otherUserId", "snippet1", "shared"))
            .thenThrow(HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "bad", HttpHeaders(), ByteArray(0), null))

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/permissions")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"otherUser"}"""),
            ).andExpect(status().isConflict)
    }

    @Test
    fun `getRules registers user in runner when rules are missing`() {
        `when`(runnerClient.getRules(userId, "formatting", "printscript"))
            .thenThrow(HttpClientErrorException.create(HttpStatus.NOT_FOUND, "nf", HttpHeaders(), ByteArray(0), null))
            .thenReturn(mapOf("rule1" to true))

        mockMvc
            .perform(
                get("/api/v1/rules")
                    .with(jwt().jwt { it.subject(userId) })
                    .param("task", "formatting")
                    .param("language", "printscript"),
            ).andExpect(status().isOk)
    }

    @Test
    fun `updateRules publishes tasks when applyToSnippets is true`() {
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, listOf("s1"), emptyList()))

        mockMvc
            .perform(
                put("/api/v1/rules")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"formatting","language":"printscript","rules":{"rule1":true},"applyToSnippets":true}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `updateSnippetStatus handles formatting task and missing snippet`() {
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))
        `when`(snippetRepository.findById("missing")).thenReturn(Optional.empty())

        mockMvc
            .perform(
                patch("/api/v1/snippets/snippet1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"formatting","status":false}"""),
            ).andExpect(status().isOk)
        mockMvc
            .perform(
                patch("/api/v1/snippets/snippet1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"linting","status":false}"""),
            ).andExpect(status().isOk)
        mockMvc
            .perform(
                patch("/api/v1/snippets/missing/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"linting","status":true}"""),
            ).andExpect(status().isNotFound)
    }

    @Test
    fun `getSnippetData returns 404 when snippet is missing`() {
        `when`(snippetRepository.findById("missing")).thenReturn(Optional.empty())

        mockMvc
            .perform(get("/api/v1/snippets/missing"))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `testing endpoints return 200`() {
        mockMvc.perform(post("/api/v1/testing/separator")).andExpect(status().isOk)
        mockMvc.perform(post("/api/v1/testing")).andExpect(status().isOk)
    }

    @Test
    fun `getSnippetData returns 200`() {
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1", description = "desc", version = "1.1")))

        mockMvc
            .perform(
                get("/api/v1/snippets/snippet1"),
            ).andExpect(status().isOk)
    }

    @Test
    fun `registerSnippet returns 200`() {
        mockMvc
            .perform(
                put("/api/v1/snippets/snippet1")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"$userId","name":"snippet1","language":"kotlin","description":"desc","version":"1.1"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `deleteSnippet returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))

        mockMvc
            .perform(
                delete("/api/v1/snippets/snippet1")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `getUsersWithPermission returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(accessManagerClient.getPermissionsForSnippet("snippet1"))
            .thenReturn(GetPermissionsForSnippetResponse("snippet1", userId, listOf("otherUser")))
        `when`(userDataRepository.findById("otherUser"))
            .thenReturn(Optional.of(UserData("otherUser", "other@example.com")))

        mockMvc
            .perform(
                get("/api/v1/snippets/snippet1/permission")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `shareSnippet returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(userDataRepository.findByUserName("otherUser"))
            .thenReturn(UserData("otherUserId", "otherUser"))

        mockMvc
            .perform(
                put("/api/v1/snippets/snippet1/permission")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"otherUser"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `removeSnippetPermission returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))

        mockMvc
            .perform(
                delete("/api/v1/snippets/snippet1/permission/otherUser")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `createUser returns 200`() {
        mockMvc
            .perform(
                put("/api/v1/users")
                    .with(jwt().jwt { it.subject(userId).claim("email", "test@example.com") }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `deleteUser returns 200`() {
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, emptyList(), emptyList()))

        mockMvc
            .perform(
                delete("/api/v1/users")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `listUsers returns 200`() {
        `when`(userDataRepository.findAll())
            .thenReturn(listOf(UserData("u1", "Alice"), UserData("u2", "Bob")))

        mockMvc
            .perform(
                get("/api/v1/users")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `getAllTests returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(testRepository.findBySnippetId("snippet1"))
            .thenReturn(emptyList())

        mockMvc
            .perform(
                get("/api/v1/snippets/snippet1/tests")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `createTest returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/tests")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"input":["1"],"expected":["2"],"version":"1.1","environment":{}}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `runTest returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(testRepository.findById("test1"))
            .thenReturn(Optional.of(DomainTest("test1", "snippet1", listOf("1"), listOf("2"), "1.1", emptyMap())))
        `when`(runnerClient.runTest("snippet1", userId, "1.1", emptyMap(), listOf("1"), listOf("2")))
            .thenReturn(RunTestResponse(listOf("2"), TestResult.SUCCESS, "ok"))

        mockMvc
            .perform(
                put("/api/v1/snippets/snippet1/tests/test1")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `removeTest returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))

        mockMvc
            .perform(
                delete("/api/v1/snippets/snippet1/tests/test1")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `runSnippet returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))
        `when`(runnerClient.runSnippet("snippet1", userId, "1.1", emptyMap(), emptyList()))
            .thenReturn(StartExecutionResponse(ExecutionEventType.COMPLETED, listOf("hello")))

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/execution")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"version":"1.1","environment":{}}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `sendInput returns 204`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/execution/input")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"$userId","input":"line 1"}"""),
            ).andExpect(status().isNoContent)
    }

    @Test
    fun `cancelSnippetExecution returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))

        mockMvc
            .perform(
                delete("/api/v1/snippets/snippet1/execution")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"$userId"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `getExecutionStatus returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))
        `when`(runnerClient.getExecutionStatus("snippet1", userId))
            .thenReturn(StartExecutionResponse(ExecutionEventType.COMPLETED, listOf("done")))

        mockMvc
            .perform(
                get("/api/v1/snippets/snippet1/run/status")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `updateRules returns 200`() {
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, emptyList(), emptyList()))

        mockMvc
            .perform(
                put("/api/v1/rules")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"linting","language":"kotlin","rules":{"rule1":"val1"},"applyToSnippets":false}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `getRules returns 200`() {
        `when`(runnerClient.getRules(userId, "linting", "kotlin"))
            .thenReturn(mapOf("rule1" to "val1"))

        mockMvc
            .perform(
                get("/api/v1/rules")
                    .with(jwt().jwt { it.subject(userId) })
                    .param("task", "linting")
                    .param("language", "kotlin"),
            ).andExpect(status().isOk)
    }

    @Test
    fun `updateSnippetStatus returns 200`() {
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))

        mockMvc
            .perform(
                patch("/api/v1/snippets/snippet1/status")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"linting","status":true,"compliance":"compliant"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `updateSnippetMetadata returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "kotlin", "snippet1")))

        mockMvc
            .perform(
                patch("/api/v1/snippets/snippet1")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"description":"new desc","version":"1.2"}"""),
            ).andExpect(status().isOk)
    }

    @Test
    fun `synchronousTask returns 200`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(runnerClient.callTask("snippet1", "format"))
            .thenReturn("formatted content")

        mockMvc
            .perform(
                put("/api/v1/snippets/snippet1/task/format")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }
}
