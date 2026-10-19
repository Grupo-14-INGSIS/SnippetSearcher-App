package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.inOrder
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class SnippetJobProducerTest {
    private val redis = mock(StringRedisTemplate::class.java)

    @Suppress("UNCHECKED_CAST")
    private val streamOps = mock(StreamOperations::class.java) as StreamOperations<String, String, String>
    private val lintStatus = mock(LintStatusStore::class.java)
    private val producer = SnippetJobProducer("snippet-jobs", 1000, redis, lintStatus)

    private val snippet = Snippet("s1", "name", "printscript", "s1", version = "1.1")

    init {
        `when`(redis.opsForStream<String, String>()).thenReturn(streamOps)
    }

    // Spring Data Redis 4 marca los parámetros @NonNull: el matcher se registra y se devuelve un valor no nulo
    private fun anyRecord(): MapRecord<String, String, String> {
        any(MapRecord::class.java)
        return MapRecord.create("any", emptyMap<String, String>())
    }

    @Suppress("UNCHECKED_CAST")
    private fun captureRecord(): Pair<MapRecord<String, String, String>, XAddOptions> {
        val record = ArgumentCaptor.forClass(MapRecord::class.java) as ArgumentCaptor<MapRecord<String, String, String>>
        val options = ArgumentCaptor.forClass(XAddOptions::class.java)
        verify(streamOps).add(record.capture(), options.capture())
        return record.value to options.value
    }

    @Test
    fun `requestLint marks pending before publishing the job`() {
        producer.requestLint(snippet, "owner")

        val order = inOrder(lintStatus, streamOps)
        order.verify(lintStatus).markPending("s1")
        order.verify(streamOps).add(anyRecord(), any(XAddOptions::class.java))
    }

    @Test
    fun `requestLint publishes the message schema with version and owner`() {
        producer.requestLint(snippet, "owner")

        val (record, options) = captureRecord()
        assertEquals("snippet-jobs", record.stream)
        assertEquals(
            mapOf("task" to "lint", "snippetId" to "s1", "userId" to "owner", "language" to "printscript", "version" to "1.1"),
            record.value,
        )
        // El stream se recorta: no crece sin límite
        assertEquals(1000L, options.maxlen)
        assertTrue(options.isApproximateTrimming)
    }

    @Test
    fun `requestFormat publishes a format job without touching the lint status`() {
        producer.requestFormat(snippet, "owner")

        val (record, _) = captureRecord()
        assertEquals("format", record.value["task"])
        verify(lintStatus, never()).markPending("s1")
    }

    @Test
    fun `requestLintIfIdle does not publish when the snippet already has a status`() {
        `when`(lintStatus.markPendingIfAbsent("s1")).thenReturn(false)

        assertFalse(producer.requestLintIfIdle(snippet, "owner"))

        verify(streamOps, never()).add(anyRecord(), any(XAddOptions::class.java))
    }

    @Test
    fun `requestLintIfIdle publishes when the snippet has no status`() {
        `when`(lintStatus.markPendingIfAbsent("s1")).thenReturn(true)

        assertTrue(producer.requestLintIfIdle(snippet, "owner"))

        captureRecord()
    }

    @Test
    fun `a snippet without owner is marked failed instead of queued`() {
        // Si se publicara, el Runner descartaría el mensaje y el snippet quedaría "pending" para siempre
        producer.requestLint(snippet, "")

        verify(lintStatus).markFailed("s1")
        verify(lintStatus, never()).markPending("s1")
        verify(streamOps, never()).add(anyRecord(), any(XAddOptions::class.java))
    }

    @Test
    fun `requestLintIfIdle does not queue a snippet without owner`() {
        `when`(lintStatus.markPendingIfAbsent("s1")).thenReturn(true)

        assertFalse(producer.requestLintIfIdle(snippet, " "))

        verify(lintStatus).markFailed("s1")
        verify(streamOps, never()).add(anyRecord(), any(XAddOptions::class.java))
    }

    @Test
    fun `requestFormat does not queue a snippet without owner`() {
        producer.requestFormat(snippet, "")

        verify(streamOps, never()).add(anyRecord(), any(XAddOptions::class.java))
    }

    @Test
    fun `a failed publish marks the lint as failed and is reported to the caller`() {
        `when`(streamOps.add(anyRecord(), any(XAddOptions::class.java))).thenThrow(RuntimeException("redis down"))

        assertThrows<JobPublishException> { producer.requestLint(snippet, "owner") }

        verify(lintStatus).markFailed("s1")
    }
}
