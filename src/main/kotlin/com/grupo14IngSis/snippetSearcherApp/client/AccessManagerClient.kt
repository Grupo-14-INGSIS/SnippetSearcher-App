package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForSnippetResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import com.grupo14IngSis.snippetSearcherApp.dto.PostPermissionRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AccessManagerClient(
    private val restTemplate: RestTemplate,
    @Value("\${app.accessmanager.url}") private val accessManagerUrl: String,
) {
    fun getPermission(userId: String, snippetId: String): GetPermissionResponse? {
        val url = "$accessManagerUrl/permissions?userId=$userId&snippetId=$snippetId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            requestEntity,
            GetPermissionResponse::class.java
        )
        return response.body
    }

    fun postPermission(userId: String, snippetId: String, role: String): GetPermissionResponse? {
        val url = "$accessManagerUrl/permissions"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity(PostPermissionRequest(userId, snippetId, role), headers)
        val response = restTemplate.exchange<GetPermissionResponse>(
            url,
            HttpMethod.POST,
            requestEntity,
            GetPermissionResponse::class.java
        )
        return response.body
    }

    fun deletePermission(userId: String, snippetId: String) {
        val url = "$accessManagerUrl/permissions?userId=$userId&snippetId=$snippetId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java
        )
    }

    fun getPermissionsForSnippet(snippetId: String): GetPermissionsForSnippetResponse? {
        val url = "$accessManagerUrl/permissions?snippetId=$snippetId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            requestEntity,
            GetPermissionsForSnippetResponse::class.java
        )
        return response.body
    }

    fun getPermissionsForUser(userId: String): GetPermissionsForUserResponse? {
        val url = "$accessManagerUrl/permissions?userId=$userId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            requestEntity,
            GetPermissionsForUserResponse::class.java
        )
        return response.body
    }

    fun deletePermissionForSnippet(snippetId: String){
        val url = "$accessManagerUrl/permissions?snippetId=$snippetId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java
        )
    }

    fun deletePermissionForUser(userId: String){
        val url = "$accessManagerUrl/permissions?userId=$userId"
        val headers = HttpHeaders()
        val requestEntity = HttpEntity<Void>(headers)
        restTemplate.exchange(
            url,
            HttpMethod.DELETE,
            requestEntity,
            Void::class.java
        )
    }
}