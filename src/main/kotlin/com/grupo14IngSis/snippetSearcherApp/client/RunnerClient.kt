package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.ValidationResponse
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class RunnerClient(private val webClientBuilder: WebClient.Builder) {

    // WebClient deberá estar configurado con la base URL del Runner
    private val webClient = webClientBuilder.baseUrl("http://localhost:8082").build()

    fun processAndSaveSnippet(request: SnippetCreationRequest): SnippetCreationResponse {
        return webClient.post()
            .uri("/internal/snippets") // Endpoint que el Runner debe exponer
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                response.bodyToMono<ValidationResponse>()
                    .flatMap { error: ValidationResponse ->
                        Mono.error(InvalidSnippetException(error.message))
                    }
            }
            .bodyToMono<SnippetCreationResponse>()
            .block() ?: throw RuntimeException("Error desconocido en la comunicación con Runner")
    }
}