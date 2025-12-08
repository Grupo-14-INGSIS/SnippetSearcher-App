package com.grupo14IngSis.snippetSearcherApp.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class SnippetTaskProducer(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${redis.stream.key}") private val streamKey: String,
) {
    private val logger = LoggerFactory.getLogger(SnippetTaskProducer::class.java)

    fun publish(
        userId: String,
        snippets: List<String>,
        language: String,
        task: String,
    ) {
        snippets.forEach {
            val payload: Map<String, String> =
                mapOf(
                    "task" to task,
                    "userId" to userId,
                    "snippetId" to it,
                    "language" to language,
                )
            logger.info("Publishing task to stream '$streamKey' with payload: $payload")
            redisTemplate.opsForStream<String, String>().add(streamKey, payload)
        }
    }
}
