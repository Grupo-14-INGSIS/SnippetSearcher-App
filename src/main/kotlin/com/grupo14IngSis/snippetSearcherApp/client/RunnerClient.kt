package com.grupo14IngSis.snippetSearcherApp.client

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
    private val restTemplate: RestTemplate = RestTemplate(),
) {
    @Value("\${runner.service.url}")
    private lateinit var runnerServiceUrl: String

    /**
     * Ejecuta un snippet con los inputs proporcionados y captura los outputs.
     * Los inputs se evalúan en orden y los outputs corresponden a las llamadas a println.
     *
     * @param content El código del snippet a ejecutar
     * @param inputs Lista de inputs que se proporcionarán al snippet en orden
     * @return Lista de outputs capturados de println en orden
     * @throws RunnerExecutionException si hay un error en la ejecución
     */
    fun executeSnippet(
        content: String,
        inputs: List<String>,
    ): List<String> {
        try {
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
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

    /**
     * Valida la sintaxis de un snippet sin ejecutarlo
     */
    fun validateSnippet(content: String): ValidationResult {
        try {
            val headers =
                HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
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
