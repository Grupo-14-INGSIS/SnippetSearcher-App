package com.grupo14IngSis.snippetSearcherApp.service
/*
import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

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
}

class InvalidSnippetException(message: String) : RuntimeException(message)


 */