package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AccessManagerClient(
    restTemplateBuilder: RestTemplateBuilder,
    @Value("\${app.accessmanager.url}") private val accessManagerUrl: String,
) {
    private val restTemplate: RestTemplate = restTemplateBuilder.build()

    fun getPermissionsForUser(userId: String): GetPermissionsForUserResponse {
        val url = "$accessManagerUrl/permissions?userId=$userId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            requestEntity,
            GetPermissionsForUserResponse::class.java,
        )
        return response.body ?: GetPermissionsForUserResponse(userId, emptyList(), emptyList())
    }
}