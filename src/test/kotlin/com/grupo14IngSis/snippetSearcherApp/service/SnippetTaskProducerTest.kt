package com.grupo14IngSis.snippetSearcherApp.service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StreamOperations

class SnippetTaskProducerTest {
    @Suppress("UNCHECKED_CAST")
    private val redisTemplate = mock(RedisTemplate::class.java) as RedisTemplate<String, String>

    @Suppress("UNCHECKED_CAST")
    private val streamOps = mock(StreamOperations::class.java) as StreamOperations<String, String, String>

    private val producer = SnippetTaskProducer(redisTemplate, "jobs")

    @Test
    fun `publish adds one message per snippet`() {
        `when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOps)

        producer.publish("u1", listOf("s1", "s2"), "printscript", "linting")

        verify(streamOps, times(2)).add(eq("jobs"), anyMap<String, String>())
    }

    @Test
    fun `publish with no snippets does nothing`() {
        `when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOps)

        producer.publish("u1", emptyList(), "printscript", "formatting")

        verify(streamOps, never()).add(anyString(), anyMap<String, String>())
    }

    @Test
    fun `publish swallows redis errors`() {
        `when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOps)
        `when`(streamOps.add(anyString(), anyMap<String, String>())).thenThrow(RuntimeException("redis down"))

        assertDoesNotThrow {
            producer.publish("u1", listOf("s1"), "printscript", "linting")
        }
    }
}
