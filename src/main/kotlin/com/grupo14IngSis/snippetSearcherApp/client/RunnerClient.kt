package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherApp.dto.InputSendRequest
import com.grupo14IngSis.snippetSearcherApp.dto.RunTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetData
import com.grupo14IngSis.snippetSearcherApp.dto.StartExecutionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TestResult
import com.grupo14IngSis.snippetSearcherApp.dto.runnerclient.RunTestRequest
import com.grupo14IngSis.snippetSearcherApp.dto.runnerclient.SnippetExecutionRunnerRequest
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class RunnerClient(
    private val restTemplate: RestTemplate,
    @Value("\${runner.service.url}/api/v1") private val runnerUrl: String,
) {
    private fun createHeaders(): HttpHeaders {
        val headers = HttpHeaders()
        val requestId = MDC.get("requestId")
        if (!requestId.isNullOrBlank()) {
            headers.set("X-Request-Id", requestId)
        }
        return headers
    }

    // ##### UTILS #####

    fun toStringAnyMap(input: Map<*, *>): Map<String, Any> {
        val pairs =
            input.entries.associate { (k, v) ->
                k.toString() to v
            }
        val output = mutableMapOf<String, Any>()
        for (key in pairs.keys) {
            if (pairs[key] != null) {
                output[key] = pairs[key]!!
            }
        }
        return output
    }

    // ##### EXECUTION #####

    fun runSnippet(
        snippetId: String,
        userId: String,
        version: String,
        environment: Map<String, String>,
        inputs: List<String> = emptyList(),
    ): StartExecutionResponse {
        val url = "$runnerUrl/snippets/$snippetId/executions"
        val headers = HttpHeaders()
        val requestEntity =
            HttpEntity<SnippetExecutionRunnerRequest>(
                SnippetExecutionRunnerRequest(userId, version, environment, inputs),
                headers,
            )
        val response =
            restTemplate
                .exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    StartExecutionResponse::class.java,
                ).body ?: StartExecutionResponse(ExecutionEventType.ERROR, listOf("Could not fetch response"))
        return StartExecutionResponse(response.status, response.message)
    }

    fun sendInput(
        snippetId: String,
        userId: String,
        input: String,
    ) {
        val url = "$runnerUrl/snippets/$snippetId/executions/input"
        val headers = HttpHeaders()
        val requestEntity =
            HttpEntity<InputSendRequest>(
                InputSendRequest(userId, input),
                headers,
            )
        restTemplate.exchange(
            url,
            HttpMethod.POST,
            requestEntity,
            Void::class.java,
        )
    }

    fun cancelExecution(
        snippetId: String,
        userId: String,
    ) {
        // Sin body: el usuario viaja como query param, igual que en getExecutionStatus
        restTemplate.exchange(
            "$runnerUrl/snippets/$snippetId/executions?userId={userId}",
            HttpMethod.DELETE,
            HttpEntity<Void>(HttpHeaders()),
            Void::class.java,
            mapOf("userId" to userId),
        )
    }

    /** Estado y output hasta ahora de la ejecución (`kind` = run) o de la corrida de un test (`kind` = test). */
    fun getExecutionStatus(
        snippetId: String,
        userId: String? = null,
        kind: String = "run",
    ): StartExecutionResponse {
        val url =
            if (userId != null) {
                "$runnerUrl/snippets/$snippetId/executions/status?userId={userId}&kind={kind}"
            } else {
                "$runnerUrl/snippets/$snippetId/executions/status?kind={kind}"
            }
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response =
            restTemplate
                .exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    StartExecutionResponse::class.java,
                    mapOf("userId" to (userId ?: ""), "kind" to kind),
                ).body ?: StartExecutionResponse(ExecutionEventType.ERROR, listOf("Could not fetch status"))
        return response
    }

    /**
     * US #13: código para descargar. `variant` = original (como lo escribió el usuario) o formatted
     * (formateado en el momento con las reglas de [userId], sin guardar).
     */
    fun downloadSnippet(
        snippetId: String,
        variant: String,
        userId: String,
        language: String,
        version: String,
    ): String =
        restTemplate
            .exchange(
                "$runnerUrl/snippets/$snippetId/download?variant={variant}&userId={userId}&language={language}&version={version}",
                HttpMethod.GET,
                HttpEntity<Void>(HttpHeaders()),
                String::class.java,
                mapOf("variant" to variant, "userId" to userId, "language" to language, "version" to version),
            ).body ?: ""

// ##### TESTS #####

    fun runTest(
        snippetId: String,
        userId: String,
        version: String,
        environment: Map<String, String>,
        input: List<String>,
        expected: List<String>,
    ): RunTestResponse {
        val url = "$runnerUrl/testing"
        val requestEntity =
            HttpEntity<RunTestRequest>(
                RunTestRequest(
                    snippetId,
                    userId,
                    version,
                    environment,
                    input,
                    expected,
                ),
                HttpHeaders(),
            )
        val response =
            restTemplate
                .exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    RunTestResponse::class.java,
                ).body ?: RunTestResponse(
                emptyList(),
                TestResult.ERROR,
                "Error while receiving test",
            )
        return response
    }

// ##### RULES #####

    fun getRules(
        userId: String,
        task: String,
        language: String,
    ): Map<String, Any> {
        val url = "$runnerUrl/users/$userId/$task/rules/$language"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                Map::class.java,
            )
        val rules = toStringAnyMap(response.body as Map<*, *>)
        return rules
    }

    fun patchRules(
        userId: String,
        task: String,
        language: String,
        rules: Map<String, Any>,
    ) {
        val url = "$runnerUrl/users/$userId/$task/rules/$language"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity(rules, headers)
        restTemplate.exchange(
            url,
            HttpMethod.PATCH,
            requestEntity,
            Void::class.java,
        )
    }

// ##### USER #####

    fun registerUser(userId: String) {
        val url = "$runnerUrl/users/$userId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.PUT,
            requestEntity,
            Void::class.java,
        )
    }

    fun createUser(userId: String) {
        val url = "$runnerUrl/users/$userId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.PUT,
            requestEntity,
            Void::class.java,
        )
    }

    fun deleteUser(userId: String) {
        val url = "$runnerUrl/users/$userId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java,
        )
    }

// ##### SNIPPETS #####

    fun deleteSnippet(snippetId: String) {
        val url = "$runnerUrl/snippets/$snippetId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java,
        )
    }

    /**
     * Aplica una tarea (format/lint) en el Runner con las reglas de [userId] para ese lenguaje y versión.
     * [content] (solo lint): código a analizar en lugar del guardado, por ejemplo lo que hay en el editor.
     */
    fun callTask(
        snippetId: String,
        task: String,
        userId: String,
        language: String,
        version: String,
        content: String? = null,
    ): String {
        val url = "$runnerUrl/snippets/$snippetId/tasks/$task?userId={userId}&language={language}&version={version}"
        val headers = HttpHeaders()
        if (content != null) headers.contentType = MediaType.TEXT_PLAIN
        val requestEntity = HttpEntity(content, headers)
        val response =
            restTemplate
                .exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String::class.java,
                    mapOf("userId" to userId, "language" to language, "version" to version),
                ).body ?: return "Error while $task snippet"
        return response
    }

    fun getSnippetData(snippetId: String): SnippetData? =
        try {
            val url = "$runnerUrl/snippets/$snippetId"
            val headers = createHeaders()
            val requestEntity = HttpEntity<Void>(headers)
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    requestEntity,
                    SnippetData::class.java,
                )
            response.body
        } catch (_: Exception) {
            null
        }

    fun getSnippetContent(snippetId: String): String? =
        try {
            val url = "$runnerUrl/snippets/$snippetId"
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Void>(createHeaders()),
                    Map::class.java,
                )
            response.body?.get("content") as? String
        } catch (_: Exception) {
            null
        }
}
