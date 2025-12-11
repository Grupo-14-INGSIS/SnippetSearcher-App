package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.ExecutionEvent
import com.grupo14IngSis.snippetSearcherApp.dto.ExecutionEventType
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

    fun runSnippet(snippetId: String, version: String?): ExecutionEvent {
        var url = "$runnerUrl/snippets/$snippetId/execution"
        if (version != null) { url = "$url?version=$version" }
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response =
            restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                ExecutionEvent::class.java
            )
        return response.body!!
    }

    fun getRules(
        userId: String,
        task: String,
        language: String,
    ): Map<*, *>? {
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
        return response.body
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
