package com.grupo14IngSis.snippetSearcherApp.observability

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.connection.stream.PendingMessagesSummary
import org.springframework.data.redis.core.StreamOperations
import org.springframework.data.redis.core.StringRedisTemplate

class StreamSaturationReporterTest {
    private val redis = mock(StringRedisTemplate::class.java)

    @Suppress("UNCHECKED_CAST")
    private val streams = mock(StreamOperations::class.java) as StreamOperations<String, String, String>
    private val events = mock(BusinessEvents::class.java)
    private val reporter = StreamSaturationReporter(redis, events, "jobs", "runner_group", "results", "app_group")

    @Test
    fun `reports length and pending messages of both streams`() {
        `when`(redis.opsForStream<String, String>()).thenReturn(streams)
        `when`(streams.size("jobs")).thenReturn(7L)
        `when`(streams.size("results")).thenReturn(2L)
        val jobsPending = mock(PendingMessagesSummary::class.java)
        `when`(jobsPending.totalPendingMessages).thenReturn(3L)
        `when`(streams.pending("jobs", "runner_group")).thenReturn(jobsPending)
        `when`(streams.pending("results", "app_group")).thenReturn(null)

        reporter.report()

        verify(events).streamStatus("jobs", "runner_group", 7L, 3L)
        verify(events).streamStatus("results", "app_group", 2L, 0L)
    }

    @Test
    fun `a Redis error skips the measurement without failing`() {
        `when`(redis.opsForStream<String, String>()).thenThrow(IllegalStateException("redis down"))

        assertDoesNotThrow { reporter.report() }

        verify(events, never()).streamStatus("jobs", "runner_group", 0L, 0L)
    }

    @Test
    fun `business events do nothing without the New Relic agent`() {
        val realEvents = BusinessEvents()
        assertDoesNotThrow {
            realEvents.snippetsListed("u1", 3, 2)
            realEvents.streamStatus("jobs", "runner_group", 1L, 0L)
        }
    }
}
