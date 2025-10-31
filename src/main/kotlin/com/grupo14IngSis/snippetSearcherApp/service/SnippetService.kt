package com.grupo14IngSis.snippetSearcherApp.service

import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateResponse
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import org.springframework.stereotype.Service

@Service
class SnippetService(
    private val runnerClient: RunnerClient
) {

    fun updateSnippet(
        snippetId: Long,
        userId: String,
        updateRequest: SnippetUpdateRequest
    ): SnippetUpdateResponse {
        // Delegar todo al Runner (validación, permisos, persistencia)
        return runnerClient.updateSnippet(snippetId, userId, updateRequest)
    }

    fun getSnippetById(snippetId: Long, userId: String): Snippet {
        // Obtener snippet del Runner
        return runnerClient.getSnippet(snippetId, userId)
    }
}