package com.grupo14IngSis.snippetSearcherApp.client

import com.fasterxml.jackson.databind.ObjectMapper
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForUserResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(AccessManagerClient::class)
class AccessManagerClientTest {

    @Autowired
    private lateinit var client: AccessManagerClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `getPermissionsForUser should return permissions from access manager`() {
        val userId = "user123"
        val expectedResponse = GetPermissionsForUserResponse(
            userId = userId,
            owned = listOf("snippet1", "snippet2"),
            shared = listOf("snippet3")
        )
        val responseJson = objectMapper.writeValueAsString(expectedResponse)

        server.expect(requestTo("/permissions?userId=$userId"))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val actualResponse = client.getPermissionsForUser(userId)

        assertEquals(expectedResponse, actualResponse)
    }
}