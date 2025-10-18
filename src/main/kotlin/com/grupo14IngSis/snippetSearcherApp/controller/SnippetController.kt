package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateResponse
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
    private val snippetService: SnippetService  // AGREGAR esta dependencia
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createSnippet(
        @RequestPart("file") file: MultipartFile,
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam language: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<SnippetCreationResponse> {

        // 1. Autorización (Llama a AccessManager para verificar token/usuario)
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        // 2. Preparación de la solicitud
        val runnerRequest = SnippetCreationRequest(
            userId = userId,
            name = name,
            description = description,
            language = language,
            code = String(file.bytes) // Lectura simple del contenido del archivo
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

    // Endpoint para actualizar snippet con archivo
    @PutMapping("/{id}/file")
    fun updateSnippetByFile(
        @PathVariable id: String,
        @RequestParam("file") file: MultipartFile,
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) description: String?,
        @RequestParam(required = false) language: String?,
        @RequestParam(required = false) version: String?,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        // Autorización
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Unauthorized"))

        return try {
            val content = file.inputStream.bufferedReader().use { it.readText() }

            val response = snippetService.updateSnippet(
                id = id,
                name = name,
                description = description,
                language = language,
                version = version,
                content = content
            )

            ResponseEntity.ok(response)
        } catch (e: InvalidSnippetException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf(
                    "error" to "Invalid snippet",
                    "message" to e.message,
                    "rule" to e.rule,
                    "line" to e.line,
                    "column" to e.column
                ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Snippet not found", "message" to e.message))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error updating snippet", "message" to e.message))
        }
    }

    // Endpoint para actualizar snippet con JSON (para editor)
    @PutMapping("/{id}")
    fun updateSnippet(
        @PathVariable id: String,
        @RequestBody request: SnippetUpdateRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Any> {
        // Autorización
        val userId = accessManagerClient.authorize(authHeader)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Unauthorized"))

        return try {
            val response = snippetService.updateSnippet(
                id = id,
                name = request.name,
                description = request.description,
                language = request.language,
                version = request.version,
                content = request.content
            )

            ResponseEntity.ok(response)
        } catch (e: InvalidSnippetException) {
            ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf(
                    "error" to "Invalid snippet",
                    "message" to e.message,
                    "rule" to e.rule,
                    "line" to e.line,
                    "column" to e.column
                ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(mapOf("error" to "Snippet not found", "message" to e.message))
        } catch (e: Exception) {
            ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Error updating snippet", "message" to e.message))
        }
    }
}