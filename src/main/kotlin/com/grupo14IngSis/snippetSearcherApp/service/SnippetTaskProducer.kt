package com.grupo14IngSis.snippetSearcherApp.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.data.redis.connection.stream.MapRecord

@Service
class SnippetTaskProducer(
 private val redisTemplate: RedisTemplate<String, String>
) {
    @Value("\${redis.stream.key}")
    private lateinit var streamKey: String

    fun requestTask(snippetId: String, task: String) {
        val message = mapOf(
            "task" to task,
            "snippetId" to snippetId,
        )
        val record = MapRecord.create(streamKey, message)
        redisTemplate.opsForStream<String, String>().add(record)
    }
}