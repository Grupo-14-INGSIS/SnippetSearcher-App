package com.grupo14IngSis.snippetSearcherApp.service

import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service

@Service
class SnippetCacheService(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val logger = LoggerFactory.getLogger(SnippetCacheService::class.java)

    fun getFromCache(key: String): String? {
        val requestId = MDC.get("requestId") ?: "unknown"

        logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis GET: $key")

        return try {
            val result = redisTemplate.opsForValue().get(key)

            if (result != null) {
                logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis HIT: $key")
            } else {
                logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis MISS: $key")
            }

            result
        } catch (ex: Exception) {
            logger.error("[SNIPPET-RUNNER] Request $requestId - Redis error on GET: $key", ex)
            null
        }
    }

    fun saveToCache(
        key: String,
        value: String,
    ) {
        val requestId = MDC.get("requestId") ?: "unknown"

        logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis SET: $key")

        try {
            redisTemplate.opsForValue().set(key, value)
            logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis SET successful: $key")
        } catch (ex: Exception) {
            logger.error("[SNIPPET-RUNNER] Request $requestId - Redis error on SET: $key", ex)
        }
    }

    fun deleteFromCache(key: String) {
        val requestId = MDC.get("requestId") ?: "unknown"

        logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis DELETE: $key")

        try {
            redisTemplate.delete(key)
            logger.debug("[SNIPPET-RUNNER] Request $requestId - Redis DELETE successful: $key")
        } catch (ex: Exception) {
            logger.error("[SNIPPET-RUNNER] Request $requestId - Redis error on DELETE: $key", ex)
        }
    }
}
