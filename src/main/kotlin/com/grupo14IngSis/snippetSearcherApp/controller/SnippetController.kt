package com.grupo14IngSis.snippetSearcherApp.controller
import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import com.grupo14IngSis.snippetSearcherApp.domain.Test
import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestRequest
import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import com.grupo14IngSis.snippetSearcherApp.dto.ShareSnippetRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetRunRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import com.grupo14IngSis.snippetSearcherApp.repository.TestRepository
import com.grupo14IngSis.snippetSearcherApp.service.SnippetTaskProducer
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class SnippetController(
    private val accessManagerClient: AccessManagerClient,
    private val runnerClient: RunnerClient,
    private val snippetRepository: SnippetRepository,
    private val testRepository: TestRepository,
    private val snippetTaskProducer: SnippetTaskProducer,
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${redis.stream.key}") private val streamKey: String,
) {
    private fun authorize(
        userId: String,
        snippetId: String,
    ): Boolean {
        val permission = accessManagerClient.getPermission(userId, snippetId) ?: return false
        return permission.role.lowercase() == "owner"
    }

    /**
     * GET /api/v1/snippets
     *
     * Get all snippets available for a user
     *
     * Response:
     *
     *     {
     *         [snippetId]
     *     }
     *     */
    @GetMapping("/snippets")
    @PreAuthorize("isAuthenticated()")
    fun getAllSnippets(authentication: Authentication): ResponseEntity<Map<String, String>> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        val snippetPermissions: GetPermissionsForUserResponse =
            accessManagerClient.getPermissionsForUser(userId) ?: return ResponseEntity.status(404).build()
        val output = mutableMapOf<String, String>()
        snippetPermissions.owned.forEach { output[it] = "owner" }
        snippetPermissions.shared.forEach { output[it] = "shared" }
        return ResponseEntity.ok().body(output)
    }

    /**
     * PUT    /api/v1/snippets/{snippetId}
     *
     * Register a snippet
     *
     * Request:
     *
     *     {
     *       snippetId: {snippetId}
     *       language: {language}
     *       ownerId: {userId}
     *     }
     */
    @PutMapping("/snippets/{snippetId}?language={language}")
    @PreAuthorize("isAuthenticated()")
    fun registerSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable language: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        accessManagerClient.postPermission(userId, snippetId, "owner")
        snippetRepository.save(Snippet(snippetId, language, snippetId))
        return ResponseEntity.ok().build()
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}
     *
     * Delete a snippet
     */
    @DeleteMapping("/snippets/{snippetId}")
    @PreAuthorize("isAuthenticated()")
    fun deleteSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (!authorize(userId, snippetId)) return ResponseEntity.status(401).build()
        accessManagerClient.deletePermissionForSnippet(snippetId)
        runnerClient.deleteSnippet("snippets", snippetId)
        snippetRepository.deleteById(snippetId)
        testRepository.deleteBySnippetId(snippetId)
        return ResponseEntity.ok().build()
    }

    /**
     * PUT    /api/v1/snippets/{snippetId}/permission
     *
     * Share a snippet with another user
     *
     * Request:
     *
     *     {
     *       userId: {userId}
     *     }
     */
    @PutMapping("/snippets/{snippetId}/permission")
    @PreAuthorize("isAuthenticated()")
    fun shareSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody snippetData: ShareSnippetRequest,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val ownerId = jwt.subject
        if (!authorize(ownerId, snippetId)) return ResponseEntity.status(401).build()
        accessManagerClient.postPermission(snippetData.userId, snippetId, "shared")
        return ResponseEntity.ok().build()
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}/permission
     *
     * Remove permission for another user
     */
    @DeleteMapping("/snippets/{snippetId}/permission/{userId}")
    @PreAuthorize("isAuthenticated()")
    fun removeSnippetPermission(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable userId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val ownerId = jwt.subject
        if (!authorize(ownerId, snippetId)) return ResponseEntity.status(401).build()
        accessManagerClient.deletePermission(userId, snippetId)
        return ResponseEntity.ok().build()
    }

    /**
     * DELETE /api/v1/users
     *
     * Delete a user
     */
    @DeleteMapping("/users")
    @PreAuthorize("isAuthenticated()")
    fun deleteUser(authentication: Authentication): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        accessManagerClient.getPermissionsForUser(userId)
        runnerClient.deleteUser(userId)
        // TODO delete all snippets and tests from user
        // get all snippets
        // for each snippet, get all tests
        // delete each test
        // delete each snippet
        return ResponseEntity.ok().build()
    }

    /**
     * GET    /api/v1/snippets/{snippetId}/tests
     *
     * Get all tests for a snippet
     *
     * Response:
     *
     *     {
     *       [testId]
     *     }
     */
    @GetMapping("/snippets/{snippetId}/tests")
    @PreAuthorize("isAuthenticated()")
    fun getAllTests(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<List<String>> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (!authorize(userId, snippetId)) return ResponseEntity.status(401).build()
        val tests = testRepository.findTestIdsBySnippetId(snippetId)
        return ResponseEntity.ok(tests)
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/tests
     *
     * Create a test
     *
     * Request:
     *
     *     {
     *       snippetId: {snippetId}
     *       input: [String]
     *       expected: {String}
     *     }
     * Response
     *
     *     {
     *       testId: {testId}
     *     }
     */
    @PostMapping("/snippets/{snippetId}/tests")
    @PreAuthorize("isAuthenticated()")
    fun createTest(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody testData: CreateTestRequest,
    ): ResponseEntity<CreateTestResponse> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (!authorize(userId, snippetId)) return ResponseEntity.status(401).build()
        val testId = UUID.randomUUID().toString()
        val test =
            Test(
                testId,
                snippetId,
                testData.input,
                testData.expected,
            )
        testRepository.save(test)
        return ResponseEntity.ok(CreateTestResponse(testId))
    }

    /**
     * PUT    /api/v1/snippets/{snippetId}/tests/{testId}
     *
     * Start execution of a test
     */
    @PutMapping("/snippets/{snippetId}/tests/{testId}")
    @PreAuthorize("isAuthenticated()")
    fun runTest(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable testId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        // TODO
        return ResponseEntity.ok().body(userId)
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}/tests/{testId}
     *
     * Delete a test
     */
    @DeleteMapping("/snippets/{snippetId}/tests/{testId}")
    @PreAuthorize("isAuthenticated()")
    fun removeTest(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable testId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (!authorize(userId, snippetId)) return ResponseEntity.status(401).build()
        testRepository.deleteById(testId)
        return ResponseEntity.ok().build()
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/run
     *
     * Start execution of a snippet or provide input
     *
     * Request:
     *
     *     {
     *       input: {String?}
     *     }
     */
    @PostMapping("/snippets/{snippetId}/run")
    @PreAuthorize("isAuthenticated()")
    fun runSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetRunRequest,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (!authorize(userId, snippetId)) return ResponseEntity.status(401).build()

        val snippet = snippetRepository.findById(snippetId)
        if (snippet.isEmpty) {
            return ResponseEntity.notFound().build()
        }

        snippetTaskProducer.publish(userId, listOf(), "language", "test")

        return ResponseEntity.ok().build()
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}/run
     *
     * Cancel execution of a snippet
     */
    @DeleteMapping("/snippets/{snippetId}/run")
    @PreAuthorize("isAuthenticated()")
    fun cancelSnippetExecution(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (!authorize(userId, snippetId)) return ResponseEntity.status(401).build()
        // TODO
        return ResponseEntity.ok().build()
    }

    /**
     * PUT    /api/v1/rules
     *
     * Modify task rules
     *
     * Request:
     *
     *     {
     *         task: {formatting/linting}
     *         language: {language}
     *         rules: {
     *             rule1: {var1}
     *             rule2: {val2}
     *             ...
     *         }
     *     }
     */
    @PutMapping("/rules")
    @PreAuthorize("isAuthenticated()")
    fun updateRules(
        authentication: Authentication,
        @RequestBody request: SnippetUpdateRequest,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject

        val userSnippets = accessManagerClient.getPermissionsForUser(userId) ?: return ResponseEntity.status(404).build()
        runnerClient.patchRules(userId, request.task, request.language, request.rules)
        snippetTaskProducer.publish(userId, userSnippets.owned, request.language, request.task)
        return ResponseEntity.ok().build()
    }

    /**
     * GET    /api/v1/rules?task={task}&language={language}
     *
     * Get all rules for a user
     *
     * Response:
     *
     *     {
     *         rule1: {val1}
     *         rule2: {val2}
     *         ...
     *     }
     */
    @GetMapping("/rules")
    @PreAuthorize("isAuthenticated()")
    fun getRules(
        authentication: Authentication,
        @RequestParam task: String,
        @RequestParam language: String,
    ): ResponseEntity<Map<String, Any>> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        runnerClient.getRules(userId, task, language)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/testing/separator")
    fun printSeparator() {
        println(
            "###############################################################\n" +
                "# SEPARATOR SEPARATOR SEPARATOR SEPARATOR SEPARATOR SEPARATOR #\n" +
                "###############################################################",
        )
    }

    @PostMapping("/testing")
    fun sendTestingMessage() {
        val payload: Map<String, String> =
            mapOf(
                "task" to "test",
                "userId" to "userId",
                "snippetId" to "it",
                "language" to "language",
            )

        redisTemplate.opsForStream<String, String>().add(streamKey, payload)
    }
}
