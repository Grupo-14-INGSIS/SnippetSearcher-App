package com.grupo14IngSis.snippetSearcherApp.service
/*
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.StreamOperations

class SnippetTaskProducerTest {
    private val redisTemplate: RedisTemplate<String, String> =
        mock(RedisTemplate::class.java) as RedisTemplate<String, String>
    private val streamOperations: StreamOperations<String, String, String> =
        mock(StreamOperations::class.java) as StreamOperations<String, String, String>

    @Test
    fun `should send task to redis stream`() {
        val snippetId = "sid"
        val task = "linting"

        // Stub del método opsForStream
        Mockito.`when`(redisTemplate.opsForStream<String, String>()).thenReturn(streamOperations)

        val producer = SnippetTaskProducer(redisTemplate)
        producer.streamKey = "snippet-tasks"

        producer.requestTask(snippetId, task)

        // Capturamos el MapRecord enviado
        val captor: ArgumentCaptor<MapRecord<String, String, String>> =
            ArgumentCaptor.forClass(MapRecord::class.java as Class<MapRecord<String, String, String>>)

        verify(streamOperations).add(captor.capture())
    }
}
*/