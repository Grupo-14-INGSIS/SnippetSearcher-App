package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetUpdateResponse
import com.grupo14IngSis.snippetSearcherApp.dto.ValidationResponse
import com.grupo14IngSis.snippetSearcherApp.model.Snippet
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import org.springframework.stereotype.Component
import org.springframework.core.ParameterizedTypeReference
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class RunnerClient(private val webClientBuilder: WebClient.Builder) {

    // WebClient configurado con la base URL del Runner
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
}