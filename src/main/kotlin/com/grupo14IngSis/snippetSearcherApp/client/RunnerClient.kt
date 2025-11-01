package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.*
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import org.springframework.stereotype.Component
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class RunnerClient(private val webClientBuilder: WebClient.Builder) {

    private val webClient = webClientBuilder.baseUrl("http://localhost:8082").build()

    fun processAndSaveSnippet(request: SnippetCreationRequest): SnippetCreationResponse {
        return webClient.post()
            .uri("/internal/snippets")
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                response.bodyToMono<ValidationResponse>()
                    .flatMap { error: ValidationResponse ->
                        Mono.error(InvalidSnippetException(error.message, "To be implemented", 0, 0))
                    }
            }
            .bodyToMono<SnippetCreationResponse>()
            .block() ?: throw RuntimeException("Error desconocido en la comunicación con Runner")
    }

    fun updateSnippet(snippetId: Long, userId: String, request: SnippetUpdateRequest): SnippetUpdateResponse {
        return webClient.put()
            .uri("/internal/snippets/$snippetId")
            .header("X-User-Id", userId)
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                response.bodyToMono<ValidationResponse>()
                    .flatMap { error: ValidationResponse ->
                        Mono.error(InvalidSnippetException(
                            error.message,
                            error.rule ?: "Unknown",
                            error.line ?: 0,
                            error.column ?: 0
                        ))
                    }
            }
            .bodyToMono<SnippetUpdateResponse>()
            .block() ?: throw RuntimeException("Error desconocido en la comunicación con Runner")
    }

    fun getSnippet(snippetId: Long, userId: String): Snippet {
        return webClient.get()
            .uri("/internal/snippets/$snippetId")
            .header("X-User-Id", userId)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                Mono.error(RuntimeException("Snippet no encontrado o sin permisos"))
            }
            .bodyToMono<Snippet>()
            .block() ?: throw RuntimeException("Snippet no encontrado")
    }

    fun getAllSnippets(userId: String): List<Snippet> {
        return webClient.get()
            .uri("/internal/snippets")
            .header("X-User-Id", userId)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                Mono.error(RuntimeException("Error al obtener snippets"))
            }
            .bodyToMono(object : ParameterizedTypeReference<List<Snippet>>() {})
            .block() ?: emptyList()
    }

    // ========== NUEVOS MÉTODOS PARA USER STORY #6 ==========

    fun getSnippetDetail(snippetId: Long, userId: String): SnippetDetailResponse {
        return webClient.get()
            .uri("/internal/snippets/$snippetId/detail")
            .header("X-User-Id", userId)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                Mono.error(RuntimeException("Snippet no encontrado o sin permisos"))
            }
            .bodyToMono<SnippetDetailResponse>()
            .block() ?: throw RuntimeException("Error al obtener detalles del snippet")
    }

    fun executeTests(snippetId: Long, userId: String, request: TestExecutionRequest): TestExecutionResponse {
        return webClient.post()
            .uri("/internal/snippets/$snippetId/test")
            .header("X-User-Id", userId)
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                Mono.error(RuntimeException("Error al ejecutar tests"))
            }
            .bodyToMono<TestExecutionResponse>()
            .block() ?: throw RuntimeException("Error al ejecutar tests del snippet")
    }

    fun getLintingErrors(snippetId: Long, userId: String): List<LintingError> {
        return webClient.get()
            .uri("/internal/snippets/$snippetId/linting-errors")
            .header("X-User-Id", userId)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                Mono.error(RuntimeException("Error al obtener errores de linting"))
            }
            .bodyToMono(object : ParameterizedTypeReference<List<LintingError>>() {})
            .block() ?: emptyList()
    }
}