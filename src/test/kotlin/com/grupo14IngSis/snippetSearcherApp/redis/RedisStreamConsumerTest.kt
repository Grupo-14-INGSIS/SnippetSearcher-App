package com.grupo14IngSis.snippetSearcherApp.redis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.PendingMessage
import org.springframework.data.redis.connection.stream.PendingMessages
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate
import java.time.Duration

class RedisStreamConsumerTest {
    private val redis = mock(StringRedisTemplate::class.java)

    @Suppress("UNCHECKED_CAST")
    private val ops = mock(StreamOperations::class.java) as StreamOperations<String, String, String>

    private val settings =
        RedisStreamConsumer
            .Settings(minIdle = Duration.ofSeconds(60), maxDeliveries = 3, errorBackoff = Duration.ofMillis(1))

    /** Consumidor de prueba: registra lo que recibe y puede fallar a pedido. */
    private inner class RecordingConsumer(
        var fail: Boolean = false,
    ) : RedisStreamConsumer("stream", "group", redis, settings) {
        val received = mutableListOf<RecordId>()
        val deadLetters = mutableListOf<RecordId>()

        override fun onMessage(record: MapRecord<String, String, String>) {
            received += record.id
            if (fail) error("boom")
        }

        override fun onDeadLetter(record: MapRecord<String, String, String>) {
            deadLetters += record.id
        }
    }

    init {
        `when`(redis.opsForStream<String, String>()).thenReturn(ops)
    }

    private fun record(id: String): MapRecord<String, String, String> =
        MapRecord
            .create("stream", mapOf("k" to "v"))
            .withId(RecordId.of(id))

    private fun pending(
        id: String,
        idle: Duration,
        deliveries: Long,
    ) = PendingMessage(RecordId.of(id), Consumer.from("group", "other"), idle, deliveries)

    private fun stubPending(vararg messages: PendingMessage) {
        `when`(ops.pending(eq("stream"), eq("group"), any(Range::class.java) ?: Range.unbounded<String>(), anyLong()))
            .thenReturn(PendingMessages("group", messages.toList()))
    }

    private fun stubClaim(
        id: String,
        vararg result: MapRecord<String, String, String>,
    ) {
        `when`(ops.claim(eq("stream"), eq("group"), anyString(), eq(settings.minIdle), eq(RecordId.of(id)))).thenReturn(result.toList())
    }

    @Test
    fun `a processed message is acknowledged`() {
        val consumer = RecordingConsumer()

        consumer.process(record("1-0"))

        verify(ops).acknowledge("stream", "group", RecordId.of("1-0"))
    }

    @Test
    fun `a failing message is not acknowledged so it stays pending`() {
        val consumer = RecordingConsumer(fail = true)

        consumer.process(record("1-0"))

        assertEquals(listOf(RecordId.of("1-0")), consumer.received)
        verify(ops, never()).acknowledge("stream", "group", RecordId.of("1-0"))
    }

    @Test
    fun `idle pending messages are claimed and retried`() {
        val consumer = RecordingConsumer()
        stubPending(pending("1-0", Duration.ofSeconds(90), 1))
        stubClaim("1-0", record("1-0"))

        consumer.reclaimPending()

        assertEquals(listOf(RecordId.of("1-0")), consumer.received)
        verify(ops).acknowledge("stream", "group", RecordId.of("1-0"))
    }

    @Test
    fun `recently delivered messages are left to their consumer`() {
        val consumer = RecordingConsumer()
        stubPending(pending("1-0", Duration.ofSeconds(5), 1))

        consumer.reclaimPending()

        assertTrue(consumer.received.isEmpty())
    }

    @Test
    fun `a message that exhausted its deliveries goes to the dead letter handler and is acknowledged`() {
        val consumer = RecordingConsumer()
        stubPending(pending("1-0", Duration.ofSeconds(90), 3))
        stubClaim("1-0", record("1-0"))

        consumer.reclaimPending()

        assertTrue(consumer.received.isEmpty())
        assertEquals(listOf(RecordId.of("1-0")), consumer.deadLetters)
        verify(ops).acknowledge("stream", "group", RecordId.of("1-0"))
    }

    @Test
    fun `a message claimed by another replica first is skipped`() {
        val consumer = RecordingConsumer()
        stubPending(pending("1-0", Duration.ofSeconds(90), 1))
        stubClaim("1-0")

        consumer.reclaimPending()

        assertTrue(consumer.received.isEmpty())
    }

    @Test
    fun `reclaim tolerates redis being down`() {
        val consumer = RecordingConsumer()
        `when`(ops.pending(eq("stream"), eq("group"), any(Range::class.java) ?: Range.unbounded<String>(), anyLong()))
            .thenThrow(RuntimeException("connection refused"))

        consumer.reclaimPending()

        assertTrue(consumer.received.isEmpty())
    }

    @Test
    fun `a NOGROUP read error recreates the consumer group`() {
        val consumer = RecordingConsumer()

        consumer.onReadError(RuntimeException("NOGROUP No such key 'stream' or consumer group 'group'"))
        consumer.onReadError(RuntimeException("some other error"))

        verify(redis).execute(any(RedisCallback::class.java) ?: RedisCallback<Any?> { null })
    }
}
