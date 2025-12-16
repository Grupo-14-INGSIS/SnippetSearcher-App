package com.grupo14IngSis.snippetSearcherApp.service

import org.slf4j.LoggerFactory
import org.slf4j.MDC
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
        val requestId = MDC.get("requestId") ?: "unknown"
        snippets.forEach {
            val payload: Map<String, String> =
                mapOf(
                    "task" to task,
                    "userId" to userId,
                    "snippetId" to it,
                    "language" to language,
                )
            logger.info("[SNIPPET-APP] Request $requestId - Publishing task to stream '$streamKey' with payload: $payload")
            try {
                redisTemplate.opsForStream<String, String>().add(streamKey, payload)
                logger.debug("[SNIPPET-APP] Request $requestId - Redis STREAM ADD successful: $streamKey")
            } catch (ex: Exception) {
                logger.error("[SNIPPET-APP] Request $requestId - Redis error on STREAM ADD: $streamKey", ex)
            }
        }
    }
}
