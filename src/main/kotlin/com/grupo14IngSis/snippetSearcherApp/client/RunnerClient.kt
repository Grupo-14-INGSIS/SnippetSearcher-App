package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import com.grupo14IngSis.snippetSearcherApp.dto.ValidationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.ValidationResponse
import com.grupo14IngSis.snippetSearcherApp.service.InvalidSnippetException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

@Component
class RunnerClient(private val webClientBuilder: WebClient.Builder) {

    // WebClient configurado con la base URL del Runner
    private val webClient = webClientBuilder.baseUrl("http://snippet-searcher-runner").build()

    fun validateCode(language: String, code: String): ValidationResponse {
        val validationRequest = ValidationRequest(code, language, "1.0.0") // Assuming a default version
        return webClient.post()
            .uri("/validate")
            .bodyValue(validationRequest)
            .retrieve()
            .bodyToMono<ValidationResponse>()
            .block() ?: throw RuntimeException("Error desconocido en la comunicación con Runner")
    }

    fun validateSnippet(content: String, language: String, version: String): ValidationResponse {
        val validationRequest = ValidationRequest(content, language, version)
        return webClient.post()
            .uri("/validate")
            .bodyValue(validationRequest)
            .retrieve()
            .bodyToMono<ValidationResponse>()
            .block() ?: throw RuntimeException("Error desconocido en la comunicación con Runner")
    }

    fun processAndSaveSnippet(request: SnippetCreationRequest): SnippetCreationResponse {
        return webClient.post()
            .uri("/internal/snippets") // Endpoint que el Runner debe exponer
            .bodyValue(request)
            .retrieve()
            .onStatus({ status -> status.is4xxClientError }) { response ->
                response.bodyToMono<ValidationResponse>()
                    .flatMap { error: ValidationResponse -> // Tipado explícito para mayor claridad
                        // El 'error' es de tipo ValidationResponse, usamos su 'message'
                        Mono.error(InvalidSnippetException(error.message))
                    }
            }
            .bodyToMono<SnippetCreationResponse>()
            .block() ?: throw RuntimeException("Error desconocido en la comunicación con Runner")
    }
}