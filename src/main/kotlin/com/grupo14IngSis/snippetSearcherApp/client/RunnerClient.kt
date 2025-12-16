package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherApp.dto.InputSendRequest
import com.grupo14IngSis.snippetSearcherApp.dto.RunTestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.StartExecutionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TestResult
import com.grupo14IngSis.snippetSearcherApp.dto.runnerclient.RunTestRequest
import com.grupo14IngSis.snippetSearcherApp.dto.runnerclient.SnippetExecutionRunerCancel
import com.grupo14IngSis.snippetSearcherApp.dto.runnerclient.SnippetExecutionRunnerRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class RunnerClient(
    private val restTemplate: RestTemplate,
    @Value("\${runner.service.url}/api/v1") private val runnerUrl: String,
) {
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
    ): StartExecutionResponse {
        val url = "$runnerUrl/snippets/$snippetId/run"
        val headers = HttpHeaders()
        val requestEntity =
            HttpEntity<SnippetExecutionRunnerRequest>(
                SnippetExecutionRunnerRequest(userId, version, environment),
                headers,
            )
        val response =
            restTemplate.exchange(
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
        val url = "$runnerUrl/snippets/$snippetId/run/input"
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
        val url = "$runnerUrl/snippets/$snippetId/run/"
        val requestEntity =
            HttpEntity<SnippetExecutionRunerCancel>(
                SnippetExecutionRunerCancel(userId),
                HttpHeaders(),
            )
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java,
        )
    }

    fun getExecutionStatus(snippetId: String): StartExecutionResponse {
        val url = "$runnerUrl/snippets/$snippetId/run/status"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response =
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                StartExecutionResponse::class.java,
            ).body ?: StartExecutionResponse(ExecutionEventType.ERROR, listOf("Could not fetch status"))
        return response
    }

// ##### TESTS #####

    fun runTest(
        snippetId: String,
        testId: String,
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
                    testId,
                    version,
                    environment,
                    input,
                    expected,
                ),
                HttpHeaders(),
            )
        val response =
            restTemplate.exchange(
                url,
                HttpMethod.DELETE,
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

    fun deleteSnippet(
        container: String,
        snippetId: String,
    ) {
        val url = "$runnerUrl/snippet/$container/$snippetId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java,
        )
    }
}
