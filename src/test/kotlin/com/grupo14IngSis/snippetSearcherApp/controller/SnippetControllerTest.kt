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
import com.grupo14IngSis.snippetSearcherApp.jobs.LintStatusStore
import com.grupo14IngSis.snippetSearcherApp.jobs.SnippetJobProducer
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import com.grupo14IngSis.snippetSearcherApp.repository.TestRepository
import com.grupo14IngSis.snippetSearcherApp.repository.UserDataRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.client.HttpClientErrorException
import java.util.Optional
import com.grupo14IngSis.snippetSearcherApp.domain.Test as DomainTest

@WebMvcTest(SnippetController::class)
@Import(SecurityConfig::class)
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
    private lateinit var jobProducer: SnippetJobProducer

    @MockitoBean(enforceOverride = false)
    private lateinit var lintStatus: LintStatusStore

    @MockitoBean(enforceOverride = false)
    private lateinit var jwtDecoder: JwtDecoder

    private val userId = "testUser"

    @Test
    fun `CORS preflight from the UI domain is allowed`() {
        mockMvc
            .perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .options("/api/v1/snippets")
                    .header(HttpHeaders.ORIGIN, "https://snippet26dev.duckdns.org")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,x-request-id"),
            ).andExpect(status().isOk)
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .header()
                    .string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://snippet26dev.duckdns.org"),
            )
    }

    @Test
    fun `CORS preflight from another site is rejected`() {
        mockMvc
            .perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                    .options("/api/v1/snippets")
                    .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                    .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"),
            ).andExpect(status().isForbidden)
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .header()
                    .doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN),
            )
    }

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
    fun `deleteSnippet returns 403 when user is not owner`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))

        mockMvc
            .perform(
                delete("/api/v1/snippets/snippet1")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isForbidden)
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
    fun `updateRules queues a format job per owned snippet of that language and returns 202`() {
        val ps = Snippet("s1", "a", "printscript", "s1")
        val other = Snippet("s2", "b", "kotlin", "s2")
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, listOf("s1", "s2"), emptyList()))
        `when`(snippetRepository.findAllById(listOf("s1", "s2"))).thenReturn(listOf(ps, other))

        mockMvc
            .perform(
                put("/api/v1/rules")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"formatting","language":"printscript","rules":{"rule1":true},"applyToSnippets":true}"""),
            ).andExpect(status().isAccepted)

        verify(jobProducer).requestFormat(ps, userId)
        verify(jobProducer, never()).requestFormat(other, userId)
    }

    @Test
    fun `updateRules queues lint jobs for linting rules`() {
        val ps = Snippet("s1", "a", "printscript", "s1")
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, listOf("s1"), emptyList()))
        `when`(snippetRepository.findAllById(listOf("s1"))).thenReturn(listOf(ps))

        mockMvc
            .perform(
                put("/api/v1/rules")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"linting","language":"printscript","rules":{"rule1":true},"applyToSnippets":true}"""),
            ).andExpect(status().isAccepted)

        verify(jobProducer).requestLint(ps, userId)
    }

    @Test
    fun `the old unauthenticated status endpoint no longer exists`() {
        mockMvc
            .perform(
                patch("/api/v1/snippets/snippet1/status")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"task":"linting","status":true}"""),
            ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `requestLint queues a lint job with the owner rules`() {
        val snippet = Snippet("snippet1", "testSnippet", "printscript", "snippet1")
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))
        `when`(snippetRepository.findById("snippet1")).thenReturn(Optional.of(snippet))
        `when`(accessManagerClient.getPermissionsForSnippet("snippet1"))
            .thenReturn(GetPermissionsForSnippetResponse("snippet1", "ownerId", listOf(userId)))

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/lint-jobs")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isAccepted)

        verify(jobProducer).requestLint(snippet, "ownerId")
    }

    @Test
    fun `requestLint returns 403 without permission`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1")).thenReturn(null)

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/lint-jobs")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isForbidden)

        verifyNoInteractions(jobProducer)
    }

    @Test
    fun `getAllSnippets combines the stored result with the job status`() {
        `when`(accessManagerClient.getPermissionsForUser(userId))
            .thenReturn(GetPermissionsForUserResponse(userId, listOf("ok", "bad", "running", "broken", "new"), emptyList()))
        `when`(snippetRepository.findById("ok")).thenReturn(Optional.of(Snippet("ok", "a", "printscript", "ok", compliant = true)))
        `when`(snippetRepository.findById("bad")).thenReturn(Optional.of(Snippet("bad", "b", "printscript", "bad", compliant = false)))
        `when`(snippetRepository.findById("running"))
            .thenReturn(Optional.of(Snippet("running", "c", "printscript", "running", compliant = true)))
        `when`(snippetRepository.findById("broken")).thenReturn(Optional.of(Snippet("broken", "d", "printscript", "broken")))
        `when`(snippetRepository.findById("new")).thenReturn(Optional.of(Snippet("new", "e", "printscript", "new")))
        `when`(lintStatus.getAll(listOf("ok", "bad", "running", "broken", "new")))
            .thenReturn(mapOf("running" to LintStatusStore.Status.PENDING, "broken" to LintStatusStore.Status.FAILED))

        mockMvc
            .perform(
                get("/api/v1/snippets")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
            .andExpect(jsonPath("$.ok.compliance").value("compliant"))
            .andExpect(jsonPath("$.bad.compliance").value("not-compliant"))
            .andExpect(jsonPath("$.running.compliance").value("pending"))
            .andExpect(jsonPath("$.broken.compliance").value("failed"))
            .andExpect(jsonPath("$.new.compliance").value("pending"))
    }

    @Test
    fun `getSnippetData returns 404 when snippet is missing`() {
        `when`(snippetRepository.findById("missing")).thenReturn(Optional.empty())

        mockMvc
            .perform(get("/api/v1/snippets/missing"))
            .andExpect(status().isNotFound)
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
    fun `registerSnippet returns 409 when the snippet already exists and does not overwrite it`() {
        `when`(snippetRepository.existsById("snippet1")).thenReturn(true)

        mockMvc
            .perform(
                put("/api/v1/snippets/snippet1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"$userId","name":"other","language":"kotlin"}"""),
            ).andExpect(status().isConflict)

        verifyNoInteractions(jobProducer, accessManagerClient)
    }

    @Test
    fun `PUT on a test no longer runs it - running is POST tests id runs`() {
        mockMvc.perform(
            put("/api/v1/snippets/snippet1/tests/test1")
                .with(jwt().jwt { it.subject(userId) }),
        )

        verifyNoInteractions(runnerClient, accessManagerClient)
    }

    @Test
    fun `old route aliases are gone`() {
        mockMvc.perform(
            post("/api/v1/snippets/snippet1/permission")
                .with(jwt().jwt { it.subject(userId) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"userId":"otherUser"}"""),
        )
        mockMvc.perform(
            post("/api/v1/snippets/snippet1/run")
                .with(jwt().jwt { it.subject(userId) })
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"version":"1.1","environment":{}}"""),
        )

        verifyNoInteractions(runnerClient, accessManagerClient)
    }

    @Test
    fun `registerSnippet returns 200 and queues the first lint`() {
        mockMvc
            .perform(
                put("/api/v1/snippets/snippet1")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"$userId","name":"snippet1","language":"kotlin","description":"desc","version":"1.1"}"""),
            ).andExpect(status().isOk)

        verify(jobProducer).requestLint(Snippet("snippet1", "snippet1", "kotlin", "snippet1", description = "desc"), userId)
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
                get("/api/v1/snippets/snippet1/permissions")
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
                post("/api/v1/snippets/snippet1/permissions")
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
                delete("/api/v1/snippets/snippet1/permissions/otherUser")
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
                post("/api/v1/snippets/snippet1/tests/test1/runs")
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
                post("/api/v1/snippets/snippet1/executions")
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
                post("/api/v1/snippets/snippet1/executions/input")
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
                delete("/api/v1/snippets/snippet1/executions")
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
                get("/api/v1/snippets/snippet1/executions/status")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
    }

    @Test
    fun `live output of a test run is read with kind=test`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "printscript", "snippet1")))
        `when`(runnerClient.getExecutionStatus("snippet1", userId, "test"))
            .thenReturn(StartExecutionResponse(ExecutionEventType.OUTPUT, listOf("hola")))

        mockMvc
            .perform(get("/api/v1/snippets/snippet1/executions/status?kind=test").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value("OUTPUT"))
            .andExpect(jsonPath("$.message[0]").value("hola"))
    }

    @Test
    fun `unknown execution kind is 400`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "testSnippet", "printscript", "snippet1")))

        mockMvc
            .perform(get("/api/v1/snippets/snippet1/executions/status?kind=zzz").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `download formatted by a shared user uses the owner rules and is an attachment`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))
        `when`(accessManagerClient.getPermissionsForSnippet("snippet1"))
            .thenReturn(GetPermissionsForSnippetResponse("snippet1", "theOwner", listOf(userId)))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "hello", "printscript", "snippet1", version = "1.1")))
        `when`(runnerClient.downloadSnippet("snippet1", "formatted", "theOwner", "printscript", "1.1"))
            .thenReturn("let a : number = 1;")

        mockMvc
            .perform(get("/api/v1/snippets/snippet1/download?variant=formatted").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isOk)
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .header()
                    .string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("hello.formatted.ps")),
            ).andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .content()
                    .string("let a : number = 1;"),
            )
    }

    @Test
    fun `download original is the default variant`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1"))
            .thenReturn(Optional.of(Snippet("snippet1", "hello", "printscript", "snippet1", version = "1.1")))
        `when`(runnerClient.downloadSnippet("snippet1", "original", userId, "printscript", "1.1"))
            .thenReturn("let a:number=1;")

        mockMvc
            .perform(get("/api/v1/snippets/snippet1/download").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isOk)
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .content()
                    .string("let a:number=1;"),
            )
    }

    @Test
    fun `download without access is 403 and with a bad variant is 400`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "none"))
        mockMvc
            .perform(get("/api/v1/snippets/snippet1/download").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isForbidden)

        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        mockMvc
            .perform(get("/api/v1/snippets/snippet1/download?variant=zip").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isBadRequest)
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
    fun `synchronousTask formats with the owner rules and re-lints the new content`() {
        val snippet = Snippet("snippet1", "testSnippet", "printscript", "snippet1", version = "1.1")
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1")).thenReturn(Optional.of(snippet))
        `when`(runnerClient.callTask("snippet1", "format", userId, "printscript", "1.1"))
            .thenReturn("formatted content")

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/tasks/format")
                    .with(jwt().jwt { it.subject(userId) }),
            ).andExpect(status().isOk)
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .content()
                    .string("formatted content"),
            )

        verify(jobProducer).requestLint(snippet, userId)
    }

    @Test
    fun `synchronousTask lint does not queue another lint`() {
        val snippet = Snippet("snippet1", "testSnippet", "printscript", "snippet1", version = "1.1")
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1")).thenReturn(Optional.of(snippet))
        `when`(runnerClient.callTask("snippet1", "lint", userId, "printscript", "1.1")).thenReturn("SUCCESS")

        mockMvc
            .perform(post("/api/v1/snippets/snippet1/tasks/lint").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isOk)

        verifyNoInteractions(jobProducer)
    }

    @Test
    fun `lint task by a shared user lints the editor content with the OWNER rules`() {
        // Mismo cálculo que compliant en la lista: reglas del dueño, no de quien mira
        val snippet = Snippet("snippet1", "testSnippet", "printscript", "snippet1", version = "1.1")
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))
        `when`(accessManagerClient.getPermissionsForSnippet("snippet1"))
            .thenReturn(GetPermissionsForSnippetResponse("snippet1", "theOwner", listOf(userId)))
        `when`(snippetRepository.findById("snippet1")).thenReturn(Optional.of(snippet))
        `when`(runnerClient.callTask("snippet1", "lint", "theOwner", "printscript", "1.1", "println(a + b);"))
            .thenReturn("ANALYSIS RESULTS: 1 issue(s) found")

        mockMvc
            .perform(
                post("/api/v1/snippets/snippet1/tasks/lint")
                    .with(jwt().jwt { it.subject(userId) })
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"content":"println(a + b);"}"""),
            ).andExpect(status().isOk)
            .andExpect(
                org.springframework.test.web.servlet.result.MockMvcResultMatchers
                    .content()
                    .string("ANALYSIS RESULTS: 1 issue(s) found"),
            )
    }

    @Test
    fun `format task is owner only - a shared user gets 403`() {
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "shared"))

        mockMvc
            .perform(post("/api/v1/snippets/snippet1/tasks/format").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isForbidden)
    }

    @Test
    fun `synchronousTask forwards a 400 from the Runner instead of a 500`() {
        val snippet = Snippet("snippet1", "testSnippet", "printscript", "snippet1", version = "1.1")
        `when`(accessManagerClient.getPermission(userId, "snippet1"))
            .thenReturn(GetPermissionResponse("snippet1", userId, "owner"))
        `when`(snippetRepository.findById("snippet1")).thenReturn(Optional.of(snippet))
        `when`(runnerClient.callTask("snippet1", "format", userId, "printscript", "1.1"))
            .thenThrow(
                HttpClientErrorException.create(HttpStatus.BAD_REQUEST, "bad", HttpHeaders(), "no rules".toByteArray(), null),
            )

        mockMvc
            .perform(post("/api/v1/snippets/snippet1/tasks/format").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isBadRequest)

        verifyNoInteractions(jobProducer)
    }

    @Test
    fun `synchronousTask on an unknown snippet is 404`() {
        `when`(accessManagerClient.getPermission(userId, "missing"))
            .thenReturn(GetPermissionResponse("missing", userId, "owner"))

        mockMvc
            .perform(post("/api/v1/snippets/missing/tasks/format").with(jwt().jwt { it.subject(userId) }))
            .andExpect(status().isNotFound)
    }
}
