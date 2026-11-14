package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import com.grupo14IngSis.snippetSearcherApp.domain.Test
import com.grupo14IngSis.snippetSearcherApp.domain.UserData
import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestRequest
import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import com.grupo14IngSis.snippetSearcherApp.dto.InputSendRequest
import com.grupo14IngSis.snippetSearcherApp.dto.RunTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TestResult
import com.grupo14IngSis.snippetSearcherApp.dto.ShareSnippetRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetData
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetPermissionData
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetRunRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.dto.StartExecutionResponse
import com.grupo14IngSis.snippetSearcherApp.jobs.Compliance
import com.grupo14IngSis.snippetSearcherApp.jobs.JobTask
import com.grupo14IngSis.snippetSearcherApp.jobs.LintStatusStore
import com.grupo14IngSis.snippetSearcherApp.jobs.SnippetJobProducer
import com.grupo14IngSis.snippetSearcherApp.observability.BusinessEvents
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import com.grupo14IngSis.snippetSearcherApp.repository.TestRepository
import com.grupo14IngSis.snippetSearcherApp.repository.UserDataRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
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
import org.springframework.web.client.HttpClientErrorException
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class SnippetController(
    private val accessManagerClient: AccessManagerClient,
    private val runnerClient: RunnerClient,
    private val snippetRepository: SnippetRepository,
    private val testRepository: TestRepository,
    private val userDataRepository: UserDataRepository,
    private val jobProducer: SnippetJobProducer,
    private val lintStatus: LintStatusStore,
    private val businessEvents: BusinessEvents,
) {
    private val logger = LoggerFactory.getLogger(SnippetController::class.java)
    private val ownerPermission = 2
    private val sharedPermission = 1
    private val noPermission = 0
    private val supportedLanguages = setOf("printscript")
    private val supportedVersions = setOf("1.0", "1.1")

    private fun getAuthorization(
        userId: String,
        snippetId: String,
    ): Int {
        val permission = accessManagerClient.getPermission(userId, snippetId) ?: return noPermission
        return when {
            permission.role.equals("owner", ignoreCase = true) -> ownerPermission
            permission.role.equals("shared", ignoreCase = true) -> sharedPermission
            else -> noPermission
        }
    }

    /**
     * GET /api/v1/snippets
     *
     * Get all snippets available for a user
     *
     * Response: a map containing the id of each snippet associated with the snippet permission data
     *
     *     {
     *         snippet1: {
     *             name: String,
     *             language: String,
     *             permission: owner
     *         },
     *         snippet2: {
     *             name: String,
     *             language: String,
     *             permission: shared
     *         },
     *         ...
     *     }
     *     */
    @GetMapping("/snippets")
    @PreAuthorize("isAuthenticated()")
    fun getAllSnippets(authentication: Authentication): ResponseEntity<Map<String, SnippetPermissionData>> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        val snippetPermissions: GetPermissionsForUserResponse =
            accessManagerClient.getPermissionsForUser(userId) ?: return ResponseEntity.status(404).build()
        // Estado de los trabajos de linteo de todos los snippets en un solo HMGET a Redis
        val statuses = lintStatus.getAll(snippetPermissions.owned + snippetPermissions.shared)
        val output = mutableMapOf<String, SnippetPermissionData>()
        snippetPermissions.owned.forEach {
            val snippet = snippetRepository.findById(it).orElse(null)
            if (snippet != null) {
                val compliance = Compliance.resolve(snippet.compliant, statuses[it])
                output[it] =
                    SnippetPermissionData(
                        snippet.name,
                        snippet.language,
                        "owner",
                        compliance = compliance,
                        status = compliance,
                    )
            }
        }
        snippetPermissions.shared.forEach {
            var snippet = snippetRepository.findById(it).orElse(null)
            if (snippet == null) {
                val snippetData = runnerClient.getSnippetData(it)
                if (snippetData != null) {
                    snippet = Snippet(snippetData.snippetId, snippetData.name, snippetData.language, it)
                    snippetRepository.save(snippet)
                }
            }
            if (snippet != null) {
                val compliance = Compliance.resolve(snippet.compliant, statuses[it])
                output[it] =
                    SnippetPermissionData(
                        snippet.name,
                        snippet.language,
                        "shared",
                        compliance = compliance,
                        status = compliance,
                    )
            }
        }
        // Evento de negocio: permite ver en New Relic si un usuario pasó de tener snippets a ver 0
        val owned = output.values.count { it.permission == "owner" }
        logger.info("snippets listed user=$userId count=${output.size} owned=$owned shared=${output.size - owned}")
        businessEvents.snippetsListed(userId, output.size, owned)
        return ResponseEntity.ok().body(output)
    }

    /**
     * GET    /api/v1/snippets/{snippetId}
     *
     * Get metadata and content associated with the snippet (BFF pattern)
     *
     * Response:
     *
     *     {
     *         snippetId: String,
     *         name: String,
     *         language: String,
     *         content: String
     *     }
     */
    @GetMapping("/snippets/{snippetId}")
    @PreAuthorize("isAuthenticated()")
    fun getSnippetData(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<SnippetData> {
        val jwt = authentication.principal as Jwt
        val permission = getAuthorization(jwt.subject, snippetId)
        if (permission < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippetOptional = snippetRepository.findById(snippetId)
        if (snippetOptional.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        val snippet = snippetOptional.get()
        val content = runnerClient.getSnippetContent(snippetId)
        val compliance = Compliance.resolve(snippet.compliant, lintStatus.get(snippetId))
        val response =
            SnippetData(
                snippetId = snippet.snippetId,
                name = snippet.name,
                language = snippet.language,
                compliance = compliance,
                status = compliance,
                description = snippet.description,
                version = snippet.version,
                content = content,
                permission = if (permission == ownerPermission) "owner" else "shared",
            )
        return ResponseEntity.ok().body(response)
    }

    /**
     * PUT    /api/v1/snippets/{snippetId}
     *
     * Crea un snippet. El dueño es siempre el usuario del token (el `userId` del body se ignora).
     * Con `content`, App orquesta: primero el Runner valida el código y lo guarda, y después App lo
     * registra. El Runner nunca llama a App. Sin `content`, se registra directo.
     *
     * Request: `{ name, language, content?, description?, version? }`
     * Response: el snippet creado (con contenido) o `{ success, message }` (sin contenido)
     */
    @PutMapping("/snippets/{snippetId}")
    @PreAuthorize("isAuthenticated()")
    fun createSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<Any> {
        val ownerId = (authentication.principal as Jwt).subject
        val content = request.content
        if (content.isNullOrBlank()) {
            val result = registerSnippet(snippetId, ownerId, request)
            return ResponseEntity.status(result.statusCode).body(result.body)
        }
        val stored =
            try {
                runnerClient.createSnippet(snippetId, request.name, request.language, content, request.version ?: "1.1")
            } catch (e: HttpClientErrorException) {
                // 400 del Runner: el código no parsea. Se reenvía el detalle (regla, línea, columna).
                return ResponseEntity.status(e.statusCode).body(e.responseBodyAsString)
            }
        val registered = registerStoredSnippet(snippetId, ownerId, request)
        return if (registered.statusCode.is2xxSuccessful) stored else ResponseEntity.status(registered.statusCode).body(registered.body)
    }

    /**
     * Registra un snippet cuyo código el Runner ya guardó. Si el registro falla, borra ese código: si no,
     * quedaría en el bucket sin metadatos ni dueño, y ningún usuario lo vería.
     */
    private fun registerStoredSnippet(
        snippetId: String,
        ownerId: String,
        request: SnippetCreationRequest,
    ): ResponseEntity<SnippetCreationResponse> {
        val registered =
            try {
                registerSnippet(snippetId, ownerId, request)
            } catch (e: Exception) {
                // Por ejemplo, AccessManager no respondió después de guardar los metadatos
                logger.warn("Could not register snippet $snippetId: ${e.message}")
                runCatching { snippetRepository.deleteById(snippetId) }
                ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(SnippetCreationResponse(false, "Could not register snippet"))
            }
        if (!registered.statusCode.is2xxSuccessful) {
            runCatching { runnerClient.deleteSnippet(snippetId) }
                .onFailure { logger.warn("Could not delete stored content of unregistered snippet $snippetId: ${it.message}") }
        }
        return registered
    }

    /**
     * Registra el snippet en la base de App, le da permiso de dueño a [ownerId] y encola su primer
     * linteo.
     */
    fun registerSnippet(
        snippetId: String,
        ownerId: String,
        request: SnippetCreationRequest,
    ): ResponseEntity<SnippetCreationResponse> {
        val snippet =
            Snippet(
                snippetId,
                request.name,
                request.language,
                snippetId,
                description = request.description ?: "",
                version = request.version ?: "1.1",
            )
        // save() de JPA sobre un id existente lo PISA (merge) en lugar de fallar: hay que chequearlo antes
        if (snippetRepository.existsById(snippetId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(SnippetCreationResponse(false, "Snippet already exists"))
        }
        try {
            snippetRepository.save(snippet)
        } catch (e: DataIntegrityViolationException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                SnippetCreationResponse(
                    false,
                    "Snippet already exists",
                ),
            )
        } catch (e: Exception) {
            return ResponseEntity.status(500).body(
                SnippetCreationResponse(
                    false,
                    "Error creating snippet: ${e.message}",
                ),
            )
        }
        accessManagerClient.postPermission(ownerId, snippetId, "owner")
        logger.info("snippet created snippetId=$snippetId owner=$ownerId language=${snippet.language}")
        // El snippet nuevo se lintea en segundo plano con las reglas de su dueño
        requestLintQuietly(snippet, ownerId)
        return ResponseEntity.ok().body(
            SnippetCreationResponse(
                true,
                "Snippet with ID $snippetId created successfully",
            ),
        )
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
        if (getAuthorization(userId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        accessManagerClient.deletePermissionForSnippet(snippetId)
        try {
            runnerClient.deleteSnippet(snippetId)
        } catch (e: Exception) {
            // Si el contenido nunca llegó al asset service (404) igual queremos borrar el snippet
            logger.warn("Could not delete snippet content for $snippetId in Runner: ${e.message}")
        }
        snippetRepository.deleteById(snippetId)
        testRepository.deleteBySnippetId(snippetId)
        runCatching { lintStatus.clear(snippetId) }
        logger.info("snippet deleted snippetId=$snippetId by=$userId")
        return ResponseEntity.ok().build()
    }

    /**
     * GET    /api/v1/snippets/{snippetId}/permission
     *
     * Get all users with permission for a snippet
     *
     * Request:
     *
     *     {
     *       userId: name...
     *       userId2: name,
     *       ...
     *     }
     */
    @GetMapping("/snippets/{snippetId}/permissions")
    @PreAuthorize("isAuthenticated()")
    fun getUsersWithPermission(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<List<Map<String, String>>> {
        val jwt = authentication.principal as Jwt
        val ownerId = jwt.subject
        if (getAuthorization(ownerId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val users =
            accessManagerClient.getPermissionsForSnippet(snippetId)
                ?: return ResponseEntity.notFound().build()
        val shared = users.shared
        val userList = mutableListOf<Map<String, String>>()
        for (user in shared) {
            val userData = userDataRepository.findById(user).orElse(null)
            userList.add(mapOf("id" to user, "email" to (userData?.userName ?: user)))
        }
        return ResponseEntity.ok(userList)
    }

    /**
     * POST / PUT    /api/v1/snippets/{snippetId}/permissions
     *
     * Share a snippet with another user
     */
    @PostMapping("/snippets/{snippetId}/permissions")
    @PreAuthorize("isAuthenticated()")
    fun shareSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody snippetData: ShareSnippetRequest,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val ownerId = jwt.subject
        if (getAuthorization(ownerId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        val targetUser =
            userDataRepository.findByUserName(snippetData.userId)
                ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with email ${snippetData.userId} not found.")

        try {
            accessManagerClient.postPermission(targetUser.userId, snippetId, "shared")
        } catch (e: HttpClientErrorException.BadRequest) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("User already has permission for this snippet.")
        }
        logger.info("snippet shared snippetId=$snippetId by=$ownerId with=${targetUser.userId}")
        return ResponseEntity.ok().build()
    }

    /**
     * DELETE /api/v1/snippets/{snippetId}/permissions/{userId}
     *
     * Remove permission for another user
     */
    @DeleteMapping("/snippets/{snippetId}/permissions/{userId}")
    @PreAuthorize("isAuthenticated()")
    fun removeSnippetPermission(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable userId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val ownerId = jwt.subject
        if (getAuthorization(ownerId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        accessManagerClient.deletePermission(userId, snippetId)
        logger.info("snippet unshared snippetId=$snippetId by=$ownerId from=$userId")
        return ResponseEntity.ok().build()
    }

    /**
     * PUT /api/v1/users
     *
     * Create or update the authenticated user. PUT porque es idempotente: la UI lo llama en cada login.
     */
    @PutMapping("/users")
    @PreAuthorize("isAuthenticated()")
    fun createUser(
        authentication: Authentication,
        @RequestBody(required = false) body: Map<String, String>?,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        val userName =
            body?.get("email")
                ?: jwt.getClaimAsString("email")
                ?: jwt.getClaimAsString("nickname")
                ?: jwt.getClaimAsString("name")
                ?: "Unknown User"

        userDataRepository.save(UserData(userId, userName))
        try {
            runnerClient.createUser(userId)
        } catch (e: Exception) {
            logger.warn("El usuario ya existe en el Runner o hubo un error, ignorando: ${e.message}")
        }
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
        val snippets = accessManagerClient.getPermissionsForUser(userId)
        if (snippets != null) {
            for (snippet in snippets.owned) {
                testRepository.deleteBySnippetId(snippet)
                snippetRepository.deleteBySnippetId(snippet)
                accessManagerClient.deletePermissionForSnippet(snippet)
            }
        }
        accessManagerClient.deletePermissionForUser(userId)
        runnerClient.deleteUser(userId)
        return ResponseEntity.ok().build()
    }

    /**
     * GET /api/v1/users
     *
     * List all registered users, optionally filtered by name.
     * Used by the share snippet autocomplete in the frontend.
     */
    @GetMapping("/users")
    @PreAuthorize("isAuthenticated()")
    fun listUsers(
        @RequestParam(required = false) name: String?,
    ): ResponseEntity<List<Map<String, String>>> {
        val allUsers = userDataRepository.findAll()
        val filtered =
            if (!name.isNullOrBlank()) {
                allUsers.filter { it.userName.contains(name, ignoreCase = true) }
            } else {
                allUsers
            }
        val result = filtered.map { mapOf("id" to it.userId, "name" to it.userName) }
        return ResponseEntity.ok(result)
    }

    /**
     * GET    /api/v1/snippets/{snippetId}/tests
     *
     * Get all tests for a snippet
     *
     * Response: a Map containing all test
     *
     *     {
     *       testId1: {
     *         testId: String
     *         snippetId: String,
     *         input: List<String>,
     *         output: List<String>,
     *         version: String,
     *         environment: Map<String, String>
     *       },
     *       testId2: {...},
     *       ...
     *     }
     */
    @GetMapping("/snippets/{snippetId}/tests")
    @PreAuthorize("isAuthenticated()")
    fun getAllTests(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<Map<String, Test>> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val response = mutableMapOf<String, Test>()
        val tests = testRepository.findBySnippetId(snippetId)
        for (test in tests) {
            response[test.testId] = test
        }
        return ResponseEntity.ok(response)
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/tests
     *
     * Create a test
     *
     * Request:
     *
     *     {
     *       input: List<String>,
     *       expected: List<String>,
     *       version: String,
     *       environment: Map<String, String>
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
        if (getAuthorization(userId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val testId = UUID.randomUUID().toString()
        val test =
            Test(
                testId,
                snippetId,
                testData.input,
                testData.expected,
                testData.version,
                testData.environment,
            )
        testRepository.save(test)
        return ResponseEntity.ok(CreateTestResponse(testId))
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/tests/{testId}/runs
     *
     * Start execution of a test. Una "corrida" es un recurso nuevo (POST), no un reemplazo del test (PUT).
     *
     * Response:
     *
     *     {
     *       actual: List<String>,
     *       result: TestResult (String enum),
     *       message: String
     *     }
     */
    @PostMapping("/snippets/{snippetId}/tests/{testId}/runs")
    @PreAuthorize("isAuthenticated()")
    fun runTest(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable testId: String,
    ): ResponseEntity<RunTestResponse> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val test =
            testRepository.findById(testId).orElse(null)
                ?: return ResponseEntity.notFound().build()
        val result =
            runnerClient.runTest(
                snippetId,
                userId,
                test.version,
                test.environment,
                test.input,
                test.output,
            )
        return ResponseEntity.ok().body(result)
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
        if (getAuthorization(userId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        testRepository.deleteById(testId)
        return ResponseEntity.ok().build()
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/execution
     *
     * Start execution of a snippet
     *
     * Request:
     *
     *     {
     *       environment: Map<String, String>
     *       version: String
     *     }
     *
     * Response:
     *
     *     {
     *       status: String (COMPLETED/OUTPUT/WAITING/ERROR),
     *       message: List<String>
     *     }
     */
    @PostMapping("/snippets/{snippetId}/executions")
    @PreAuthorize("isAuthenticated()")
    fun runSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody request: SnippetRunRequest,
    ): ResponseEntity<StartExecutionResponse> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippet = snippetRepository.findById(snippetId)
        if (snippet.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        val output = runnerClient.runSnippet(snippetId, userId, request.version, request.environment, request.inputs)

        return ResponseEntity.ok().body(output)
    }

    /**
     * POST   /api/v1/snippets/{snippetId}/executions/input
     *
     * Send input to snippet execution
     *
     * Request:
     *
     *     {
     *       input: String
     *     }
     */
    @PostMapping("/snippets/{snippetId}/executions/input")
    @PreAuthorize("isAuthenticated()")
    fun sendInput(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody request: InputSendRequest,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippet = snippetRepository.findById(snippetId)
        if (snippet.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        runnerClient.sendInput(snippetId, userId, request.input)
        return ResponseEntity.noContent().build()
    }

    // Sin body: un DELETE no debería llevarlo (proxies y clientes lo descartan) y el usuario sale del JWT
    @DeleteMapping("/snippets/{snippetId}/executions")
    @PreAuthorize("isAuthenticated()")
    fun cancelSnippetExecution(
        authentication: Authentication,
        @PathVariable snippetId: String,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippet = snippetRepository.findById(snippetId)
        if (snippet.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        runnerClient.cancelExecution(snippetId, userId)
        return ResponseEntity.ok().build()
    }

    /**
     * GET    /api/v1/snippets/{snippetId}/executions/status
     *
     * Get the current status of a snippet execution.
     *
     * Response:
     *
     *     {
     *       status: String (COMPLETED/OUTPUT/WAITING/ERROR),
     *       message: List<String>
     *     }
     */
    @GetMapping("/snippets/{snippetId}/executions/status")
    @PreAuthorize("isAuthenticated()")
    fun getExecutionStatus(
        authentication: Authentication,
        @PathVariable snippetId: String,
        // run = ejecución interactiva, test = corrida de un test. La UI lo consulta mientras corre
        // para mostrar el output a medida que se imprime (US #9 y #10).
        @RequestParam(defaultValue = "run") kind: String,
    ): ResponseEntity<StartExecutionResponse> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippet = snippetRepository.findById(snippetId)
        if (snippet.isEmpty) {
            return ResponseEntity.notFound().build()
        }
        if (kind != "run" && kind != "test") {
            return ResponseEntity.badRequest().build()
        }
        val output = runnerClient.getExecutionStatus(snippetId, userId, kind)
        return ResponseEntity.ok().body(output)
    }

    /**
     * GET /api/v1/snippets/{snippetId}/download?variant=original|formatted
     *
     * US #13: descargar el snippet. `original` es el código como lo escribió el usuario (aunque después
     * se haya formateado); `formatted` es ese snippet formateado con las reglas de su dueño. Alcanza con
     * tenerlo compartido (lectura). Responde el archivo `.ps` como adjunto.
     */
    @GetMapping("/snippets/{snippetId}/download")
    @PreAuthorize("isAuthenticated()")
    fun downloadSnippet(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestParam(defaultValue = "original") variant: String,
    ): ResponseEntity<String> {
        val userId = (authentication.principal as Jwt).subject
        if (getAuthorization(userId, snippetId) < sharedPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        if (variant != "original" && variant != "formatted") {
            return ResponseEntity.badRequest().body("variant must be original or formatted")
        }
        val snippet = snippetRepository.findById(snippetId).orElse(null) ?: return ResponseEntity.notFound().build()
        val ownerId = accessManagerClient.getPermissionsForSnippet(snippetId)?.ownerId?.takeIf { it.isNotBlank() } ?: userId
        val code =
            try {
                runnerClient.downloadSnippet(snippetId, variant, ownerId, snippet.language, snippet.version)
            } catch (e: HttpClientErrorException) {
                return ResponseEntity.status(e.statusCode).body(e.responseBodyAsString)
            }
        val fileName = if (variant == "formatted") "${snippet.name}.formatted.ps" else "${snippet.name}.ps"
        // Con un nombre no ASCII (tildes, ñ) se codifica en UTF-8; si no, va tal cual
        val disposition =
            if (fileName.all { it.code < 128 }) {
                ContentDisposition.attachment().filename(fileName).build()
            } else {
                ContentDisposition.attachment().filename(fileName, Charsets.UTF_8).build()
            }
        return ResponseEntity
            .ok()
            .contentType(MediaType("text", "plain", Charsets.UTF_8))
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(code)
    }

    /**
     * PUT    /api/v1/rules
     *
     * Modify task rules
     *
     * Request:
     *
     *     {
     *         task: String (formatting/linting)
     *         language: String
     *         rules: Map<String, Any>
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

        val userSnippets =
            accessManagerClient.getPermissionsForUser(userId) ?: return ResponseEntity.status(404).build()
        runnerClient.patchRules(userId, request.task, request.language, request.rules)
        val task = JobTask.fromRuleTask(request.task)
        if (!request.applyToSnippets || task == null) {
            return ResponseEntity.ok().build()
        }
        // Las reglas ya quedaron guardadas; la revalidación de cada snippet es asíncrona (202).
        // Solo se reprocesan los snippets de este lenguaje: las reglas son por lenguaje.
        snippetRepository
            .findAllById(userSnippets.owned)
            .filter { it.language.equals(request.language, ignoreCase = true) }
            .forEach {
                try {
                    when (task) {
                        JobTask.LINT -> jobProducer.requestLint(it, userId)
                        // Al terminar el formateo el App encola el linteo (ver SnippetJobResultConsumer)
                        JobTask.FORMAT -> jobProducer.requestFormat(it, userId)
                    }
                } catch (e: Exception) {
                    logger.warn("Could not queue ${task.wire} job for snippet ${it.snippetId}: ${e.message}")
                }
            }
        return ResponseEntity.accepted().build()
    }

    private fun requestLintQuietly(
        snippet: Snippet,
        ownerId: String,
    ) {
        try {
            jobProducer.requestLint(snippet, ownerId)
        } catch (e: Exception) {
            // No se pierde: queda "failed" (o compliant = null y lo toma el reconciliador)
            logger.warn("Could not queue lint for new snippet ${snippet.snippetId}: ${e.message}")
        }
    }

    /**
     * PATCH /api/v1/snippets/{snippetId}
     *
     * Edita el snippet (US #2). Solo el dueño. Body con los campos a cambiar:
     * `{ name?, description?, language?, version?, content? }`. Con `content`, el Runner valida y guarda el
     * código, y App encola el linteo y corre los tests. Cambiar el lenguaje o la versión también invalida el
     * linteo anterior (el mismo código puede parsear distinto).
     */
    @org.springframework.web.bind.annotation.PatchMapping("/snippets/{snippetId}")
    @PreAuthorize("isAuthenticated()")
    fun updateSnippetMetadata(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @RequestBody body: Map<String, String>,
    ): ResponseEntity<Any> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        if (getAuthorization(userId, snippetId) < ownerPermission) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippetOptional = snippetRepository.findById(snippetId)
        if (snippetOptional.isEmpty) return ResponseEntity.notFound().build()
        val snippet = snippetOptional.get()
        val name = body["name"]?.trim()
        if (name != null && name.isEmpty()) return ResponseEntity.badRequest().body("name must not be blank")
        val language = body["language"]?.trim()?.lowercase()
        if (language != null && language !in supportedLanguages) {
            return ResponseEntity.badRequest().body("Unsupported language: $language")
        }
        val version = body["version"]?.trim()
        if (version != null && version !in supportedVersions) {
            return ResponseEntity.badRequest().body("Unsupported version: $version")
        }
        val parsingChanged = (language != null && language != snippet.language) || (version != null && version != snippet.version)
        name?.let { snippet.name = it }
        body["description"]?.let { snippet.description = it }
        language?.let { snippet.language = it }
        version?.let { snippet.version = it }
        val content = body["content"]
        if (content != null) {
            // App orquesta: el Runner solo valida y guarda el código. Si lo rechaza, no se guarda ningún
            // cambio (tampoco el nombre ni la descripción).
            try {
                runnerClient.updateSnippetContent(snippetId, content, snippet.version)
            } catch (e: HttpClientErrorException) {
                return ResponseEntity.status(e.statusCode).body(e.responseBodyAsString)
            }
            logger.info("snippet content updated snippetId=$snippetId by=$userId")
        }
        val relint = content != null || parsingChanged
        // El resultado de linteo anterior ya no vale: queda "pending" y, si encolar falla, lo retoma el reconciliador
        if (relint) snippet.compliant = null
        snippetRepository.save(snippet)
        if (!relint) return ResponseEntity.ok().build()
        // Siempre con las reglas del dueño (hoy solo el dueño puede editar, pero no depende de eso)
        requestLintQuietly(snippet, accessManagerClient.getPermissionsForSnippet(snippetId)?.ownerId ?: userId)
        if (content == null) return ResponseEntity.ok().build()
        return ResponseEntity.ok().body(runTestsAfterUpdate(snippetId, userId))
    }

    /**
     * Testing automático (US #16): corre los tests del snippet contra el código recién guardado. El resultado
     * no cambia el guardado: solo se informa. La UI muestra el aviso amarillo si el texto dice "test results".
     */
    private fun runTestsAfterUpdate(
        snippetId: String,
        userId: String,
    ): String {
        val failures =
            try {
                testRepository.findBySnippetId(snippetId).mapNotNull { test ->
                    val result = runnerClient.runTest(snippetId, userId, test.version, test.environment, test.input, test.output)
                    result.message.takeIf { result.result != TestResult.SUCCESS }
                }
            } catch (e: Exception) {
                return "Snippet updated successfully, but tests could not be run: ${e.message}"
            }
        if (failures.isEmpty()) return "Snippet updated successfully."
        return "Snippet updated successfully with the following test results:\n" + failures.joinToString("") { "\n- $it" }
    }

    /**
     * POST /api/v1/snippets/{snippetId}/tasks/{task}
     *
     * Apply a synchronous task to a snippet. POST y no PUT: es una acción que devuelve un resultado,
     * no el reemplazo de un recurso.
     *
     * Returns the raw content of the processed snippet (format) or the analyzer report (lint).
     *
     * Lint usa SIEMPRE el analizador de PrintScript con las reglas del dueño: es exactamente el mismo
     * cálculo que da `compliant` en la lista, así el botón "Lint" de la UI no puede contradecirla.
     * Format también usa el formatter de PrintScript del Runner con las reglas del dueño.
     * Body opcional: `{content, version}` para procesar lo que hay en el editor sin guardarlo (vista previa:
     * el botón Format de la UI). Lintear y la vista previa solo leen, así que alcanza con tener el snippet
     * compartido; formatear sin body reemplaza el código guardado y es solo del dueño.
     */
    @PostMapping("/snippets/{snippetId}/tasks/{task}")
    fun synchronousTask(
        authentication: Authentication,
        @PathVariable snippetId: String,
        @PathVariable task: String,
        @RequestBody(required = false) body: Map<String, String>?,
    ): ResponseEntity<String> {
        val jwt = authentication.principal as Jwt
        val userId = jwt.subject
        val content = body?.get("content")?.takeIf { it.isNotBlank() }
        // Sin content, el formateo guarda el resultado: eso modifica el snippet
        val modifies = JobTask.fromRuleTask(task) == JobTask.FORMAT && content == null
        val permission = getAuthorization(userId, snippetId)
        if (permission < (if (modifies) ownerPermission else sharedPermission)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }
        val snippet = snippetRepository.findById(snippetId).orElse(null) ?: return ResponseEntity.notFound().build()
        // Las reglas que valen son las del dueño (las mismas con las que se calcula compliant)
        val ownerId =
            if (permission == ownerPermission) {
                userId
            } else {
                accessManagerClient.getPermissionsForSnippet(snippetId)?.ownerId?.takeIf { it.isNotBlank() } ?: userId
            }
        val version = body?.get("version")?.takeIf { it.isNotBlank() } ?: snippet.version
        val processedSnippet =
            try {
                runnerClient.callTask(snippetId, task, ownerId, snippet.language, version, content)
            } catch (e: HttpClientErrorException) {
                // 400 del Runner (sin reglas, código que no parsea, tarea desconocida) o 404: se reenvía tal cual
                return ResponseEntity.status(e.statusCode).body(e.responseBodyAsString)
            }
        // El formateo reemplazó el contenido: el resultado de linteo anterior ya no vale
        if (modifies) requestLintQuietly(snippet, userId)
        return ResponseEntity.ok().body(processedSnippet)
    }

    // ...

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
        try {
            val rules = runnerClient.getRules(userId, task, language)
            return ResponseEntity.ok(rules)
        } catch (e: HttpClientErrorException.NotFound) {
            runnerClient.registerUser(userId)
            val rules = runnerClient.getRules(userId, task, language)
            return ResponseEntity.ok(rules)
        }
    }
}
