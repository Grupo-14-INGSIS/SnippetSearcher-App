package com.grupo14IngSis.snippetSearcherApp.redis

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.slf4j.LoggerFactory
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.data.domain.Range
import org.springframework.data.redis.connection.stream.Consumer
import org.springframework.data.redis.connection.stream.MapRecord
import org.springframework.data.redis.connection.stream.ReadOffset
import org.springframework.data.redis.connection.stream.RecordId
import org.springframework.data.redis.connection.stream.StreamOffset
import org.springframework.data.redis.core.RedisCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.stream.StreamMessageListenerContainer
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamMessageListenerContainerOptions
import org.springframework.data.redis.stream.StreamMessageListenerContainer.StreamReadRequest
import org.springframework.scheduling.annotation.Scheduled
import java.net.InetAddress
import java.time.Duration
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Consumidor genérico de un Redis Stream dentro de un consumer group (mismo rol que
 * `RedisStreamConsumer` de austral-ingsis/class-redis-streams), con dos diferencias a propósito:
 *
 * 1. ACK manual: el mensaje se confirma (XACK) solo si [onMessage] terminó bien. Si tira una
 *    excepción queda en la Pending Entries List (PEL) del grupo y se reintenta. El ejemplo de la
 *    cátedra usa `receiveAutoAck`, que confirma al leer y pierde el mensaje si el proceso falla.
 * 2. Recuperación de pendientes: [reclaimPending] corre periódicamente, toma con XCLAIM los
 *    mensajes que llevan más de [Settings.minIdle] sin confirmarse (réplica que se cayó a mitad
 *    de camino, error transitorio) y los vuelve a procesar. Después de [Settings.maxDeliveries]
 *    intentos el mensaje va a [onDeadLetter] y se confirma, para no reintentarlo para siempre.
 *
 * El procesamiento es de a un mensaje por instancia (lock): marca el ritmo ("pacing") y escalar
 * es agregar réplicas, que el consumer group reparte solas.
 */
abstract class RedisStreamConsumer(
    protected val streamKey: String,
    protected val groupId: String,
    protected val redis: StringRedisTemplate,
    private val settings: Settings = Settings(),
) {
    data class Settings(
        val pollTimeout: Duration = Duration.ofSeconds(2),
        val minIdle: Duration = Duration.ofSeconds(60),
        val maxDeliveries: Long = 3,
        val reclaimBatch: Long = 50,
        val errorBackoff: Duration = Duration.ofSeconds(5),
    )

    private val logger = LoggerFactory.getLogger(javaClass)
    private val lock = ReentrantLock()
    private var container: StreamMessageListenerContainer<String, MapRecord<String, String, String>>? = null

    // En Swarm cada tarea es un contenedor con hostname propio: el nombre identifica a la réplica.
    protected val consumerName: String =
        System.getenv("HOSTNAME") ?: runCatching { InetAddress.getLocalHost().hostName }.getOrDefault("consumer")

    /** Procesa un mensaje. Si lanza una excepción el mensaje NO se confirma y se reintenta. */
    protected abstract fun onMessage(record: MapRecord<String, String, String>)

    /** Se llama cuando un mensaje agotó los reintentos; después se confirma y sale de la PEL. */
    protected abstract fun onDeadLetter(record: MapRecord<String, String, String>)

    @PostConstruct
    fun subscribe() {
        ensureGroup()
        val options =
            StreamMessageListenerContainerOptions
                .builder()
                .pollTimeout(settings.pollTimeout)
                // Hilo daemon: nunca impide que el proceso termine (el stop() de @PreDestroy lo corta igual)
                .executor(SimpleAsyncTaskExecutor("redis-stream-").apply { setDaemon(true) })
                .build()
        val listenerContainer = StreamMessageListenerContainer.create(redis.requiredConnectionFactory, options)
        val request =
            StreamReadRequest
                .builder(StreamOffset.create(streamKey, ReadOffset.lastConsumed()))
                .consumer(Consumer.from(groupId, consumerName))
                .autoAcknowledge(false)
                // Sin esto el container cancela la suscripción al primer error (p. ej. Redis caído)
                .cancelOnError { false }
                .errorHandler { e -> onReadError(e) }
                .build()
        listenerContainer.register(request) { record -> process(record) }
        listenerContainer.start()
        container = listenerContainer
        logger.info("Consumer '$consumerName' subscribed to stream '$streamKey' (group '$groupId')")
    }

    @PreDestroy
    fun shutdown() {
        container?.stop()
    }

    @Scheduled(
        fixedDelayString = "\${redis.streams.reclaim-interval-ms:30000}",
        initialDelayString = "\${redis.streams.reclaim-interval-ms:30000}",
    )
    fun reclaimPending() {
        val ops = redis.opsForStream<String, String>()
        val pending =
            try {
                ops.pending(streamKey, groupId, Range.unbounded<String>(), settings.reclaimBatch)
            } catch (e: Exception) {
                logger.debug("Could not read pending messages of '$streamKey': ${e.message}")
                return
            }
        pending
            .filter { it.elapsedTimeSinceLastDelivery >= settings.minIdle }
            .forEach { message ->
                // XCLAIM solo devuelve el mensaje si sigue ocioso: si otra réplica lo tomó primero, viene vacío
                val claimed = ops.claim(streamKey, groupId, consumerName, settings.minIdle, message.id)
                claimed.forEach { record ->
                    if (message.totalDeliveryCount >= settings.maxDeliveries) {
                        deadLetter(record)
                    } else {
                        logger.info("Retrying message ${record.id} (delivery ${message.totalDeliveryCount + 1})")
                        process(record)
                    }
                }
            }
    }

    internal fun process(record: MapRecord<String, String, String>) =
        lock.withLock {
            try {
                onMessage(record)
                acknowledge(record.id)
            } catch (e: Exception) {
                logger.warn("Message ${record.id} of '$streamKey' failed, it stays pending to be retried: ${e.message}")
            }
        }

    private fun deadLetter(record: MapRecord<String, String, String>) =
        lock.withLock {
            logger.error("Message ${record.id} of '$streamKey' exhausted ${settings.maxDeliveries} deliveries: ${record.value}")
            try {
                onDeadLetter(record)
            } catch (e: Exception) {
                logger.error("Dead letter handler failed for ${record.id}", e)
            }
            acknowledge(record.id)
        }

    private fun acknowledge(id: RecordId) {
        redis.opsForStream<String, String>().acknowledge(streamKey, groupId, id)
    }

    private fun ensureGroup() {
        try {
            // MKSTREAM: crea el stream si todavía no existe (si no, XGROUP CREATE falla y nunca se consume)
            redis.execute(
                RedisCallback { connection ->
                    connection.streamCommands().xGroupCreate(streamKey.toByteArray(), groupId, ReadOffset.from("0"), true)
                },
            )
            logger.info("Consumer group '$groupId' created on stream '$streamKey'")
        } catch (e: Exception) {
            if (e.fullMessage().contains("BUSYGROUP")) {
                logger.debug("Consumer group '$groupId' already exists on '$streamKey'")
            } else {
                logger.warn("Could not create consumer group '$groupId' on '$streamKey': ${e.message}")
            }
        }
    }

    internal fun onReadError(e: Throwable) {
        if (e.fullMessage().contains("NOGROUP")) {
            // Redis se reinició sin persistencia o borraron el stream: se vuelve a crear el grupo
            ensureGroup()
        } else {
            logger.warn("Error reading stream '$streamKey': ${e.message}")
        }
        // El error handler corre en el hilo de lectura: dormir evita un loop caliente con Redis caído
        Thread.sleep(settings.errorBackoff.toMillis())
    }

    private fun Throwable.fullMessage(): String = generateSequence(this) { it.cause }.joinToString(" | ") { it.message ?: "" }
}
