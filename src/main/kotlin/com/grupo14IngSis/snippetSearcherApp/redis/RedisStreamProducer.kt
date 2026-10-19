package com.grupo14IngSis.snippetSearcherApp.redis

import org.springframework.data.redis.connection.RedisStreamCommands.XAddOptions
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamRecords
import org.springframework.data.redis.core.StringRedisTemplate

/**
 * Productor genérico de un Redis Stream (mismo rol que `RedisStreamProducer` de
 * austral-ingsis/class-redis-streams).
 *
 * Cada `emit` es un XADD con MAXLEN aproximado: el stream guarda historial (lo que permite
 * reintentos y consumidores nuevos), pero recortado para que no crezca sin límite en memoria.
 */
abstract class RedisStreamProducer(
    protected val streamKey: String,
    protected val redis: StringRedisTemplate,
    private val maxLength: Long,
) {
    protected fun emit(fields: Map<String, String>): RecordId? {
        val record = StreamRecords.newRecord().ofMap(fields).withStreamKey(streamKey)
        return redis
            .opsForStream<String, String>()
            .add(record, XAddOptions.maxlen(maxLength).approximateTrimming(true))
    }
}
