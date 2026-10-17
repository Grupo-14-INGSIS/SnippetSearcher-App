package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForSnippetResponse
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StringRedisTemplate
import java.util.Optional

class SnippetJobResultConsumerTest {
    private val repository = mock(SnippetRepository::class.java)
    private val lintStatus = mock(LintStatusStore::class.java)
    private val producer = mock(SnippetJobProducer::class.java)
    private val accessManager = mock(AccessManagerClient::class.java)
    private val consumer =
        SnippetJobResultConsumer(
            "snippet-job-results",
            "app_group",
            mock(StringRedisTemplate::class.java),
            repository,
            lintStatus,
            producer,
            accessManager,
        )

    private val snippet = Snippet("s1", "name", "printscript", "s1", compliant = true)

    private fun result(
        task: String,
        outcome: String,
    ): MapRecord<String, String, String> =
        MapRecord.create("snippet-job-results", mapOf("task" to task, "snippetId" to "s1", "outcome" to outcome))

    @Test
    fun `lint result is stored in the table and the pending status is cleared`() {
        `when`(repository.findById("s1")).thenReturn(Optional.of(snippet))

        consumer.onMessage(result("lint", "not-compliant"))

        assertEquals(false, snippet.compliant)
        verify(repository).save(snippet)
        verify(lintStatus).clear("s1")
    }

    @Test
    fun `failed lint goes to redis and keeps the last stored result`() {
        `when`(repository.findById("s1")).thenReturn(Optional.of(snippet))

        consumer.onMessage(result("lint", "failed"))

        assertEquals(true, snippet.compliant)
        verify(lintStatus).markFailed("s1")
        verify(repository, never()).save(any())
    }

    @Test
    fun `formatted snippet is linted again because its content changed`() {
        `when`(repository.findById("s1")).thenReturn(Optional.of(snippet))
        `when`(accessManager.getPermissionsForSnippet("s1")).thenReturn(GetPermissionsForSnippetResponse("s1", "owner", emptyList()))

        consumer.onMessage(result("format", "formatted"))

        verify(producer).requestLint(snippet, "owner")
    }

    @Test
    fun `failed format is recorded and does not trigger a lint`() {
        `when`(repository.findById("s1")).thenReturn(Optional.of(snippet))

        consumer.onMessage(result("format", "failed"))

        assertFalse(snippet.formatterApplied)
        verifyNoInteractions(producer)
    }

    @Test
    fun `result of a deleted snippet only clears its status`() {
        `when`(repository.findById("s1")).thenReturn(Optional.empty())

        consumer.onMessage(result("lint", "compliant"))

        verify(lintStatus).clear("s1")
        verify(repository, never()).save(any())
    }

    @Test
    fun `malformed result is discarded`() {
        consumer.onMessage(MapRecord.create("snippet-job-results", mapOf("task" to "lint")))

        verifyNoInteractions(repository, lintStatus, producer)
    }
}
