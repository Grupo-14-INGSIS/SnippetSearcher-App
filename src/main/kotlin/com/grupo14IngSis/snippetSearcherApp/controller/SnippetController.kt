package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient // Debes crear esta clase
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/snippets")
class SnippetController(
    private val accessManagerClient: AccessManagerClient, // Cliente para AccessManager
    private val runnerClient: RunnerClient
) {

    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun createSnippet(
        @RequestPart("file") file: MultipartFile,
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam language: String,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<SnippetCreationResponse> {

        // Delego la verificación del token del usuario
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
}