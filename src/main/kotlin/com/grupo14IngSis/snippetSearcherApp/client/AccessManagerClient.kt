package com.grupo14IngSis.snippetSearcherApp.client

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class AccessManagerClient(private val webClientBuilder: WebClient.Builder) {

    // URL ajustada para prueba local (Puerto 8081)
    private val webClient = webClientBuilder.baseUrl("http://localhost:8081").build()

    fun authorize(authHeader: String): String? {
        return webClient.get()
            .uri("/internal/auth/user-id")
            .header("Authorization", authHeader)
            .retrieve()
            // Si el AccessManager devuelve 4xx (401 Unauthorized), la llamada devuelve null
            .onStatus({ status -> status.is4xxClientError }) { Mono.empty() }
            .bodyToMono(String::class.java)
            .block()
    }
}