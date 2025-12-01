package com.grupo14IngSis.snippetSearcherApp.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class SnippetTaskProducer(
    private val redisTemplate: RedisTemplate<String, String>,
    @Value("\${redis.stream.key}") private val streamKey: String,
) {
    private val logger = LoggerFactory.getLogger(SnippetTaskProducer::class.java)

    fun publish(payload: Map<String, String>): RecordId? {
        logger.info("Publishing task to stream '$streamKey' with payload: $payload")
        return redisTemplate.opsForStream<String, String>().add(streamKey, payload)
    }
}