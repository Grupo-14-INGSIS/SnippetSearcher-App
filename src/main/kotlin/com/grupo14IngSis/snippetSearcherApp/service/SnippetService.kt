package com.grupo14IngSis.snippetSearcherApp.service

import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateResponse
import org.springframework.transaction.annotation.Transactional

@Service
class SnippetService(
    private val repository: SnippetRepository,
    private val runnerClient: RunnerClient
) {

    fun createSnippet(
        file: MultipartFile,
        language: String,
        version: String,
        name: String,
        description: String
    ): Snippet {
        val code = String(file.bytes)

        val validation = runnerClient.validateCode(language, code)

        if (!validation.valid) {
            throw InvalidSnippetException(
                "Código inválido: ${validation.error} (línea ${validation.line}, columna ${validation.column})"
            )
        }

        val snippet = Snippet(
            name = name,
            description = description,
            language = language,
            version = version,
            code = code
        )

        return repository.save(snippet)
    }
    @Transactional
    fun updateSnippet(
        id: String,
        name: String? = null,
        description: String? = null,
        language: String? = null,
        version: String? = null,
        content: String? = null
    ): SnippetUpdateResponse {
        // Buscar el snippet existente
        val snippet = snippetRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Snippet with id $id not found") }

        // Validar que el usuario actual sea el owner del snippet
        // TODO: Implementar verificación de ownership cuando se agregue Auth0
        // if (snippet.ownerId != currentUserId) throw UnauthorizedException()

        // Actualizar los campos que se proporcionaron
        val updatedName = name ?: snippet.name
        val updatedDescription = description ?: snippet.description
        val updatedLanguage = language ?: snippet.language
        val updatedVersion = version ?: snippet.version
        val updatedContent = content ?: snippet.content

        // Validar el contenido actualizado si se proporcionó nuevo contenido
        if (content != null) {
            val validationResponse = validateSnippetContent(
                content = updatedContent,
                language = updatedLanguage,
                version = updatedVersion
            )

            if (!validationResponse.isValid) {
                throw InvalidSnippetException(
                    message = "El snippet no es válido según las reglas del parser",
                    rule = validationResponse.rule ?: "Unknown rule",
                    line = validationResponse.line ?: 0,
                    column = validationResponse.column ?: 0
                )
            }
        }

        // Actualizar el snippet
        snippet.name = updatedName
        snippet.description = updatedDescription
        snippet.language = updatedLanguage
        snippet.version = updatedVersion
        snippet.content = updatedContent

        // Guardar cambios
        val savedSnippet = snippetRepository.save(snippet)

        return SnippetUpdateResponse(
            id = savedSnippet.id!!,
            name = savedSnippet.name,
            description = savedSnippet.description,
            language = savedSnippet.language,
            version = savedSnippet.version,
            content = savedSnippet.content,
            isValid = true,
            validationErrors = null
        )
    }

    private fun validateSnippetContent(
        content: String,
        language: String,
        version: String
    ): ValidationResponse {
        return runnerClient.validateSnippet(content, language, version)
    }
}

class InvalidSnippetException(message: String) : RuntimeException(message)
