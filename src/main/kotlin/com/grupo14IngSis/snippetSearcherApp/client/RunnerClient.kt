package com.grupo14IngSis.snippetSearcherApp.client

import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.RestTemplate

@Component
class RunnerClient(
    val restTemplate: RestTemplate = RestTemplate(),
) {
    @Value("\${runner.service.url}")
    lateinit var runnerServiceUrl: String

    fun executeSnippet(
        content: String,
        inputs: List<String>,
    ): List<String> {
        try {
            val requestId = MDC.get("request_id")
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                    set("X-Request-ID", requestId)
                }

            val request =
                ExecuteSnippetRequest(
                    code = content,
                    inputs = inputs,
                )

            val entity = HttpEntity(request, headers)
            val url = "$runnerServiceUrl/execute"

            val response =
                restTemplate.postForEntity(
                    url,
                    entity,
                    ExecuteSnippetResponse::class.java,
                )

            return response.body?.outputs ?: emptyList()
        } catch (e: HttpClientErrorException) {
            throw RunnerExecutionException(
                "Error ejecutando snippet: ${e.responseBodyAsString}",
                e,
            )
        } catch (e: HttpServerErrorException) {
            throw RunnerExecutionException(
                "Error del servidor runner: ${e.responseBodyAsString}",
                e,
            )
        } catch (e: Exception) {
            throw RunnerExecutionException(
                "Error inesperado ejecutando snippet: ${e.message}",
                e,
            )
        }
    }

    fun validateSnippet(content: String): ValidationResult {
        try {
            val requestId = MDC.get("request_id")
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                    set("X-Request-ID", requestId)
                }

            val request = ValidateSnippetRequest(code = content)
            val entity = HttpEntity(request, headers)
            val url = "$runnerServiceUrl/validate"

            val response =
                restTemplate.postForEntity(
                    url,
                    entity,
                    ValidationResponse::class.java,
                )

            return ValidationResult(
                isValid = response.body?.isValid ?: false,
                errors = response.body?.errors ?: emptyList(),
            )
        } catch (e: Exception) {
            return ValidationResult(
                isValid = false,
                errors = listOf("Error validating snippet: ${e.message}"),
            )
        }
    }
}

// DTOs para la comunicación con el servicio Runner
data class ExecuteSnippetRequest(
    val code: String,
    val inputs: List<String>,
)

data class ExecuteSnippetResponse(
    val outputs: List<String>,
    val errors: List<String> = emptyList(),
)

data class ValidateSnippetRequest(
    val code: String,
)

data class ValidationResponse(
    val isValid: Boolean,
    val errors: List<String> = emptyList(),
)

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
)

class RunnerExecutionException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)
