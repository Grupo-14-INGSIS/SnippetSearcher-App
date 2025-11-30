package com.grupo14IngSis.snippetSearcherApp.controller
import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestRequest
import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import com.grupo14IngSis.snippetSearcherApp.dto.LintingError
import com.grupo14IngSis.snippetSearcherApp.dto.ShareSnippetRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetDetailResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetRegisterRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetRunRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TestExecutionRequest
import com.grupo14IngSis.snippetSearcherApp.dto.TestExecutionResponse
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

@RestController
@RequestMapping("/api/v1")
class SnippetController(
   private val accessManagerClient: AccessManagerClient,
   private val runnerClient: RunnerClient,
) {

  private fun authorize(userId: String, snippetId: String): Boolean {
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
    val snippetPermissions: GetPermissionsForUserResponse = accessManagerClient.getPermissionsForUser(userId)?:
      return ResponseEntity.status(404).build()
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
  @PutMapping("/snippets/{snippetId}")
  @PreAuthorize("isAuthenticated()")
  fun registerSnippet(
    authentication: Authentication,
    @PathVariable snippetId: String,
  ): ResponseEntity<Any> {
    val jwt = authentication.principal as Jwt
    val userId = jwt.subject
    accessManagerClient.postPermission(userId, snippetId, "owner")
    // Add to db
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
    @RequestBody snippetData: ShareSnippetRequest
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
  fun deleteUser(
    authentication: Authentication,
  ): ResponseEntity<Any> {
    val jwt = authentication.principal as Jwt
    val userId = jwt.subject
    accessManagerClient.getPermissionsForUser(userId)
    runnerClient.deleteUser(userId)
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
    // TODO
    return ResponseEntity.ok().build()
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
    @RequestBody testData: CreateTestRequest
  ): ResponseEntity<CreateTestResponse> {
    val jwt = authentication.principal as Jwt
    val userId = jwt.subject
    // TODO
    return ResponseEntity.ok().build()
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
    return ResponseEntity.ok().build()
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
    // TODO
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
    @RequestBody request: SnippetRunRequest
  ): ResponseEntity<Any> {
    val jwt = authentication.principal as Jwt
    val userId = jwt.subject
    // TODO
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
    @RequestBody request: SnippetUpdateRequest
  ): ResponseEntity<Any> {
    val jwt = authentication.principal as Jwt
    val userId = jwt.subject
    runnerClient.patchRules(userId, request.task, request.language, request.rules)
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
}