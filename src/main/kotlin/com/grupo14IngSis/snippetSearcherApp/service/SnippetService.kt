package com.grupo14IngSis.snippetSearcherApp.service

import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.dto.*
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import org.springframework.stereotype.Service

@Service
class SnippetService(
    private val runnerClient: RunnerClient,
) {
    fun updateSnippet(
        snippetId: Long,
        userId: String,
        updateRequest: SnippetUpdateRequest,
    ): SnippetUpdateResponse {
        return runnerClient.updateSnippet(snippetId, userId, updateRequest)
    }

    fun getSnippetById(
        snippetId: Long,
        userId: String,
    ): Snippet {
        return runnerClient.getSnippet(snippetId, userId)
    }

    fun getAllSnippetsByUser(userId: String): List<Snippet> {
        return runnerClient.getAllSnippets(userId)
    }

    // ========== NUEVOS MÉTODOS PARA USER STORY #6 ==========

    fun getSnippetDetail(snippetId: Long, userId: String): SnippetDetailResponse {
        return runnerClient.getSnippetDetail(snippetId, userId)
    }

    fun executeTests(
        snippetId: Long,
        userId: String,
        request: TestExecutionRequest
    ): TestExecutionResponse {
        return runnerClient.executeTests(snippetId, userId, request)
    }

    fun getLintingErrors(snippetId: Long, userId: String): List<LintingError> {
        return runnerClient.getLintingErrors(snippetId, userId)
    }
}