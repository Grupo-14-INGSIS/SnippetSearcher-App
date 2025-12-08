package com.grupo14IngSis.snippetSearcherApp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {
    @Value("\${spring.redis.host}")
    private lateinit var redisHost: String

    @Value("\${spring.redis.port}")
    private var redisPort: Int = 6379

    @Bean
    @Primary // Importante: esto asegura que Spring use ESTA factory
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        val factory = LettuceConnectionFactory(config)
        factory.afterPropertiesSet() // Importante: inicializa la factory
        return factory
    }

    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(connectionFactory)

        template.keySerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.valueSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        template.hashValueSerializer = Jackson2JsonRedisSerializer(Any::class.java)

        template.afterPropertiesSet()
        return template
    }
}
