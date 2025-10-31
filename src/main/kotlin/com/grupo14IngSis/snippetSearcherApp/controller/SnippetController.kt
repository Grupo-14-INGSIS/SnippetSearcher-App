package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateResponse
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import com.grupo14IngSis.snippetSearcherApp.service.SnippetService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/snippets")
class SnippetController(
    private val accessManagerClient: AccessManagerClient,
    private val runnerClient: RunnerClient,
    private val snippetService: SnippetService
) {

    // ========== CREAR SNIPPET DESDE ARCHIVO ==========
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createSnippet(
        @RequestPart("file") file: MultipartFile,
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam language: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<SnippetCreationResponse> {

        // 1. Verificación del token del usuario
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 2. Preparación de la solicitud
        val runnerRequest = SnippetCreationRequest(
            userId = userId,
            name = name,
            description = description,
            language = language,
            code = String(file.bytes) // Lectura del contenido del archivo
        )

        // 3. Procesamiento y Guardado (Llama a Runner)
        return try {
            val response = runnerClient.processAndSaveSnippet(runnerRequest)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: InvalidSnippetException) {
            // 4. Manejo del error de validación de Snippet
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SnippetCreationResponse(
                    success = false,
                    message = "Error en el snippet: ${e.message}"
                ))
        }
    }

    // ========== CREAR SNIPPET DESDE EDITOR (código directo) ==========
    @PostMapping("/code", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createSnippetFromCode(
        @RequestBody request: SnippetCreationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<SnippetCreationResponse> {

        // 1. Verificación del token del usuario
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 2. Asegurar que el userId del request coincida con el del token
        val requestWithUserId = request.copy(userId = userId)

        // 3. Procesamiento y Guardado (Llama a Runner)
        return try {
            val response = runnerClient.processAndSaveSnippet(requestWithUserId)
            ResponseEntity.status(HttpStatus.CREATED).body(response)
        } catch (e: InvalidSnippetException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SnippetCreationResponse(
                    success = false,
                    message = "Error en el snippet: ${e.message}"
                ))
        }
    }

    // ========== ACTUALIZAR SNIPPET DESDE EDITOR ==========
    @PutMapping("/{snippetId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateSnippet(
        @PathVariable snippetId: Long,
        @RequestBody updateRequest: SnippetUpdateRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<SnippetUpdateResponse> {

        // 1. Verificar autorización
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 2. Actualizar snippet (el Runner maneja validación, permisos y persistencia)
        return try {
            val response = snippetService.updateSnippet(snippetId, userId, updateRequest)
            ResponseEntity.ok(response)
        } catch (e: InvalidSnippetException) {
            // Error de validación del código
            ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(SnippetUpdateResponse(
                    id = snippetId.toString(),
                    name = "",
                    description = "",
                    language = "",
                    version = "",
                    content = "",
                    isValid = false,
                    validationErrors = listOf(e.message ?: "Error desconocido")
                ))
        } catch (e: RuntimeException) {
            // Snippet no encontrado o sin permisos
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    // ========== OBTENER SNIPPET POR ID ==========
    @GetMapping("/{snippetId}")
    fun getSnippet(
        @PathVariable snippetId: Long,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Snippet> {

        // 1. Verificar autorización
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 2. Obtener snippet del Runner
        return try {
            val snippet = snippetService.getSnippetById(snippetId, userId)
            ResponseEntity.ok(snippet)
        } catch (e: RuntimeException) {
            // Snippet no encontrado o sin permisos
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }
}