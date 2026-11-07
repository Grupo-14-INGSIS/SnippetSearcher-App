package com.grupo14IngSis.snippetSearcherApp.health

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.health.contributor.Status
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withException
import org.springframework.test.web.client.response.MockRestResponseCreators.withStatus
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate
import java.io.IOException

class DependencyHealthIndicatorsTest {
    private lateinit var restTemplate: RestTemplate
    private lateinit var server: MockRestServiceServer

    @BeforeEach
    fun setUp() {
        restTemplate = RestTemplate()
        server = MockRestServiceServer.bindTo(restTemplate).build()
    }

    @Test
    fun `runner is UP when its health endpoint answers 200`() {
        server
            .expect(requestTo("http://runner:8080/actuator/health"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"status":"UP"}""", MediaType.APPLICATION_JSON))

        val health = RunnerHealthIndicator(restTemplate, "http://runner:8080").health()

        assertEquals(Status.UP, health.status)
        server.verify()
    }

    @Test
    fun `runner is DOWN when its health endpoint answers 503`() {
        server
            .expect(requestTo("http://runner:8080/actuator/health"))
            .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE))

        val health = RunnerHealthIndicator(restTemplate, "http://runner:8080").health()

        assertEquals(Status.DOWN, health.status)
    }

    @Test
    fun `access manager is UP when its health endpoint answers 200`() {
        server
            .expect(requestTo("http://access:8080/actuator/health"))
            .andRespond(withSuccess("""{"status":"UP"}""", MediaType.APPLICATION_JSON))

        val health = AccessManagerHealthIndicator(restTemplate, "http://access:8080").health()

        assertEquals(Status.UP, health.status)
    }

    @Test
    fun `access manager is DOWN when the connection fails`() {
        server
            .expect(requestTo("http://access:8080/actuator/health"))
            .andRespond(withException(IOException("connection refused")))

        val health = AccessManagerHealthIndicator(restTemplate, "http://access:8080").health()

        assertEquals(Status.DOWN, health.status)
    }

    @Test
    fun `dependency is DOWN with its status when it answers a non 2xx without error`() {
        server
            .expect(requestTo("http://other/actuator/health"))
            .andRespond(withStatus(HttpStatus.MOVED_PERMANENTLY))

        val health = dependencyHealth(restTemplate, "http://other/actuator/health")

        assertEquals(Status.DOWN, health.status)
        assertEquals(301, health.details["status"])
    }
}
