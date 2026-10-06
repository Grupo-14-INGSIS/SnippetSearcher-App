package com.grupo14IngSis.snippetSearcherApp.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.grupo14IngSis.snippetSearcherApp.config.RestTemplateConfig
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionResponse
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate

@RestClientTest(AccessManagerClient::class)
@Import(RestTemplateConfig::class)
@TestPropertySource(properties = ["app.accessmanager.url=http://localhost"])
class AccessManagerClientTest {
    @Autowired
    private lateinit var client: AccessManagerClient

    @Autowired
    private lateinit var restTemplate: RestTemplate

    private lateinit var server: MockRestServiceServer

    private val objectMapper = ObjectMapper()

    @BeforeEach
    fun setUp() {
        server = MockRestServiceServer.createServer(restTemplate)
    }

    @Test
    fun `getPermissionsForUser should return permissions from access manager`() {
        val userId = "user123"
        val expectedResponse =
            GetPermissionsForUserResponse(
                userId = userId,
                owned = listOf("snippet1", "snippet2"),
                shared = listOf("snippet3"),
            )
        val responseJson = objectMapper.writeValueAsString(expectedResponse)

        server
            .expect(requestTo("http://localhost/permissions?userId=$userId"))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val actualResponse = client.getPermissionsForUser(userId)

        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `getPermissionsForUser should return empty lists when server responds with no content`() {
        val userId = "user456"

        server
            .expect(requestTo("http://localhost/permissions?userId=$userId"))
            .andRespond(withNoContent())

        val actualResponse = client.getPermissionsForUser(userId)

        val expectedResponse =
            GetPermissionsForUserResponse(
                userId = userId,
                owned = emptyList(),
                shared = emptyList(),
            )

        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `postPermission should send request to access manager`() {
        val userId = "user123"
        val snippetId = "snippet1"
        val role = "owner"
        val expectedResponse =
            GetPermissionResponse(
                userId = userId,
                snippetId = snippetId,
                role = role,
            )
        val responseJson = objectMapper.writeValueAsString(expectedResponse)

        server
            .expect(requestTo("http://localhost/permissions"))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val actualResponse = client.postPermission(userId, snippetId, role)

        assertEquals(expectedResponse, actualResponse)
    }
}
