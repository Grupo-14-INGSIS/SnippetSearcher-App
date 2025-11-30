package com.grupo14IngSis.snippetSearcherApp.client

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess

@RestClientTest(RunnerClient::class)
class RunnerClientTest {

    @Autowired
    private lateinit var client: RunnerClient

    @Autowired
    private lateinit var server: MockRestServiceServer

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `executeSnippet should return outputs from runner`() {
        val content = "print('hello')"
        val inputs = listOf("input1")
        val expectedResponse = ExecuteSnippetResponse(outputs = listOf("hello"))
        val responseJson = objectMapper.writeValueAsString(expectedResponse)

        server.expect(requestTo("/execute"))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val actualResponse = client.executeSnippet(content, inputs)

        assertEquals(expectedResponse.outputs, actualResponse)
    }

    @Test
    fun `validateSnippet should return validation result from runner`() {
        val content = "print('hello')"
        val expectedResponse = ValidationResponse(isValid = true)
        val responseJson = objectMapper.writeValueAsString(expectedResponse)

        server.expect(requestTo("/validate"))
            .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON))

        val actualResponse = client.validateSnippet(content)

        assertEquals(expectedResponse.isValid, actualResponse.isValid)
        assertEquals(expectedResponse.errors, actualResponse.errors)
    }
}