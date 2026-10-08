package com.grupo14IngSis.snippetSearcherApp.client

import com.grupo14IngSis.snippetSearcherApp.config.RestTemplateConfig
import com.grupo14IngSis.snippetSearcherApp.dto.ExecutionEventType
import com.grupo14IngSis.snippetSearcherApp.dto.TestResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo
import org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent
import org.springframework.test.web.client.response.MockRestResponseCreators.withServerError
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import org.springframework.web.client.RestTemplate

@RestClientTest(RunnerClient::class)
@Import(RestTemplateConfig::class)
@TestPropertySource(properties = ["runner.service.url=http://localhost"])
class RunnerClientTest {
    @Autowired
    private lateinit var client: RunnerClient

    @Autowired
    private lateinit var restTemplate: RestTemplate

    private lateinit var server: MockRestServiceServer

    private val base = "http://localhost/api/v1"

    @BeforeEach
    fun setUp() {
        server = MockRestServiceServer.createServer(restTemplate)
    }

    @Test
    fun `runSnippet should post execution request and return status`() {
        server
            .expect(requestTo("$base/snippets/s1/executions"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""{"userId":"u1","version":"1.1","environment":{},"inputs":["a"]}"""))
            .andRespond(withSuccess("""{"status":"COMPLETED","message":["hello"]}""", MediaType.APPLICATION_JSON))

        val response = client.runSnippet("s1", "u1", "1.1", emptyMap(), listOf("a"))

        assertEquals(ExecutionEventType.COMPLETED, response.status)
        assertEquals(listOf("hello"), response.message)
        server.verify()
    }

    @Test
    fun `runSnippet should return error when runner returns no body`() {
        server
            .expect(requestTo("$base/snippets/s1/executions"))
            .andRespond(withNoContent())

        val response = client.runSnippet("s1", "u1", "1.1", emptyMap())

        assertEquals(ExecutionEventType.ERROR, response.status)
    }

    @Test
    fun `sendInput should post to input endpoint`() {
        server
            .expect(requestTo("$base/snippets/s1/executions/input"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(content().json("""{"userId":"u1","input":"42"}"""))
            .andRespond(withNoContent())

        client.sendInput("s1", "u1", "42")

        server.verify()
    }

    @Test
    fun `cancelExecution should delete execution`() {
        server
            .expect(requestTo("$base/snippets/s1/executions"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withNoContent())

        client.cancelExecution("s1", "u1")

        server.verify()
    }

    @Test
    fun `getExecutionStatus should include userId and return status`() {
        server
            .expect(requestTo("$base/snippets/s1/executions/status?userId=u1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"status":"WAITING","message":["line"]}""", MediaType.APPLICATION_JSON))

        val response = client.getExecutionStatus("s1", "u1")

        assertEquals(ExecutionEventType.WAITING, response.status)
        assertEquals(listOf("line"), response.message)
    }

    @Test
    fun `getExecutionStatus without userId should return error on empty body`() {
        server
            .expect(requestTo("$base/snippets/s1/executions/status"))
            .andRespond(withNoContent())

        val response = client.getExecutionStatus("s1")

        assertEquals(ExecutionEventType.ERROR, response.status)
    }

    @Test
    fun `runTest should post test request and return result`() {
        server
            .expect(requestTo("$base/testing"))
            .andExpect(method(HttpMethod.POST))
            .andRespond(
                withSuccess("""{"actual":["1"],"result":"SUCCESS","message":"ok"}""", MediaType.APPLICATION_JSON),
            )

        val response = client.runTest("s1", "u1", "1.1", emptyMap(), listOf("in"), listOf("1"))

        assertEquals(TestResult.SUCCESS, response.result)
        assertEquals(listOf("1"), response.actual)
    }

    @Test
    fun `runTest should return error when runner returns no body`() {
        server
            .expect(requestTo("$base/testing"))
            .andRespond(withNoContent())

        val response = client.runTest("s1", "u1", "1.1", emptyMap(), emptyList(), emptyList())

        assertEquals(TestResult.ERROR, response.result)
    }

    @Test
    fun `getRules should return rules map`() {
        server
            .expect(requestTo("$base/users/u1/linting/rules/printscript"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess("""{"rule1":true,"rule2":3,"rule3":null}""", MediaType.APPLICATION_JSON))

        val rules = client.getRules("u1", "linting", "printscript")

        assertEquals(true, rules["rule1"])
        assertEquals(3, rules["rule2"])
        assertTrue(!rules.containsKey("rule3"))
    }

    @Test
    fun `patchRules should send PATCH with rules`() {
        server
            .expect(requestTo("$base/users/u1/formatting/rules/printscript"))
            .andExpect(method(HttpMethod.PATCH))
            .andExpect(content().json("""{"rule1":false}"""))
            .andRespond(withNoContent())

        client.patchRules("u1", "formatting", "printscript", mapOf("rule1" to false))

        server.verify()
    }

    @Test
    fun `registerUser and createUser should PUT user`() {
        server
            .expect(requestTo("$base/users/u1"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess())
        server
            .expect(requestTo("$base/users/u2"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess())

        client.registerUser("u1")
        client.createUser("u2")

        server.verify()
    }

    @Test
    fun `deleteUser should DELETE user`() {
        server
            .expect(requestTo("$base/users/u1"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withNoContent())

        client.deleteUser("u1")

        server.verify()
    }

    @Test
    fun `deleteSnippet should DELETE snippet`() {
        server
            .expect(requestTo("$base/snippets/s1"))
            .andExpect(method(HttpMethod.DELETE))
            .andRespond(withNoContent())

        client.deleteSnippet("snippets", "s1")

        server.verify()
    }

    @Test
    fun `callTask should return processed snippet`() {
        server
            .expect(requestTo("$base/snippets/s1/tasks/format"))
            .andExpect(method(HttpMethod.PUT))
            .andRespond(withSuccess("formatted", MediaType.TEXT_PLAIN))

        val result = client.callTask("s1", "format")

        assertEquals("formatted", result)
    }

    @Test
    fun `callTask should return error message when body is empty`() {
        server
            .expect(requestTo("$base/snippets/s1/tasks/lint"))
            .andRespond(withNoContent())

        val result = client.callTask("s1", "lint")

        assertEquals("Error while lint snippet", result)
    }

    @Test
    fun `getSnippetData should return data`() {
        server
            .expect(requestTo("$base/snippets/s1"))
            .andRespond(
                withSuccess(
                    """{"snippetId":"s1","name":"n","language":"printscript","content":"println(1);"}""",
                    MediaType.APPLICATION_JSON,
                ),
            )

        val data = client.getSnippetData("s1")

        assertEquals("s1", data?.snippetId)
        assertEquals("println(1);", data?.content)
    }

    @Test
    fun `getSnippetData should return null on error`() {
        server
            .expect(requestTo("$base/snippets/s1"))
            .andRespond(withServerError())

        assertNull(client.getSnippetData("s1"))
    }

    @Test
    fun `getSnippetContent should return content`() {
        server
            .expect(requestTo("$base/snippets/s1"))
            .andRespond(withSuccess("""{"snippetId":"s1","content":"println(1);"}""", MediaType.APPLICATION_JSON))

        assertEquals("println(1);", client.getSnippetContent("s1"))
    }

    @Test
    fun `getSnippetContent should return null on error`() {
        server
            .expect(requestTo("$base/snippets/s1"))
            .andRespond(withServerError())

        assertNull(client.getSnippetContent("s1"))
    }

    @Test
    fun `toStringAnyMap should drop null values`() {
        val result = client.toStringAnyMap(mapOf("a" to 1, "b" to null, 3 to "c"))

        assertEquals(mapOf("a" to 1, "3" to "c"), result)
    }
}
