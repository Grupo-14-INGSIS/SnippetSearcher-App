package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Rutas para otros servicios, sin token. No están expuestas a internet: nginx solo le reenvía a App lo
 * que empieza con `/api/`, así que todo lo que está bajo `/internal/` solo se alcanza desde la red
 * interna de Swarm.
 */
@RestController
@RequestMapping("/internal")
class InternalSnippetController(
    private val snippetController: SnippetController,
) {
    /**
     * PUT /internal/snippets/{snippetId}
     *
     * Lo llama el Runner después de guardar el contenido de un snippet nuevo. Acá sí vale el `userId`
     * del body: es el dueño que App le pasó al Runner, tomado del token del usuario.
     */
    @PutMapping("/snippets/{snippetId}")
    fun registerSnippet(
        @PathVariable snippetId: String,
        @RequestBody request: SnippetCreationRequest,
    ): ResponseEntity<SnippetCreationResponse> {
        if (request.userId.isBlank()) {
            return ResponseEntity.badRequest().body(SnippetCreationResponse(false, "userId is required"))
        }
        return snippetController.registerSnippet(snippetId, request.userId, request)
    }
}
