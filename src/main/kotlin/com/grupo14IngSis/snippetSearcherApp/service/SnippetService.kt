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

    fun downloadOriginalSnippet(id: String, token: String): SnippetDownloadDTO {
        val userId = accessManagerClient.validateToken(token)
        val snippet = snippetRepository.findById(id)
            .orElseThrow { InvalidSnippetException("Snippet not found") }

        // Verificar permisos de lectura
        if (snippet.author != userId) {
            throw InvalidSnippetException("No permission to download this snippet")
        }

        return SnippetDownloadDTO(
            id = snippet.id,
            name = snippet.name,
            content = snippet.content,
            language = snippet.language,
            extension = getExtensionForLanguage(snippet.language)
        )
    }

    fun downloadFormattedSnippet(id: String, token: String): SnippetDownloadDTO {
        val userId = accessManagerClient.validateToken(token)
        val snippet = snippetRepository.findById(id)
            .orElseThrow { InvalidSnippetException("Snippet not found") }

        // Verificar permisos de lectura
        if (snippet.author != userId) {
            throw InvalidSnippetException("No permission to download this snippet")
        }

        // Aquí deberías llamar al servicio de formateo
        val formattedContent = formatSnippet(snippet.content, snippet.language)

        return SnippetDownloadDTO(
            id = snippet.id,
            name = snippet.name,
            content = formattedContent,
            language = snippet.language,
            extension = getExtensionForLanguage(snippet.language)
        )
    }

    private fun getExtensionForLanguage(language: String): String {
        return when (language.lowercase()) {
            "kotlin" -> "kt"
            "java" -> "java"
            "python" -> "py"
            "javascript" -> "js"
            "typescript" -> "ts"
            else -> "txt"
        }
    }

    private fun formatSnippet(content: String, language: String): String {
        // Aquí deberías integrar con tu servicio de formateo
        // Por ahora retorno el contenido original
        // TODO: Implementar llamada al formatter service
        return content
    }
}