package com.grupo14IngSis.snippetSearcherApp.health

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

/**
 * Consulta el /actuator/health de otro servicio por la red interna. Responde DOWN si el servicio no
 * contesta a tiempo, responde con error o informa DOWN (Spring responde 503 en ese caso).
 */
fun dependencyHealth(
    restTemplate: RestTemplate,
    url: String,
): Health =
    try {
        val response = restTemplate.getForEntity(url, String::class.java)
        if (response.statusCode.is2xxSuccessful) {
            Health.up().build()
        } else {
            Health.down().withDetail("status", response.statusCode.value()).build()
        }
    } catch (e: RestClientException) {
        Health.down(e).build()
    }

/** Aparece como "runner" dentro de /api/actuator/health */
@Component
class RunnerHealthIndicator(
    @Qualifier("healthRestTemplate") private val restTemplate: RestTemplate,
    @Value("\${runner.service.url}") private val runnerUrl: String,
) : HealthIndicator {
    override fun health(): Health = dependencyHealth(restTemplate, "$runnerUrl/actuator/health")
}

/** Aparece como "accessManager" dentro de /api/actuator/health */
@Component
class AccessManagerHealthIndicator(
    @Qualifier("healthRestTemplate") private val restTemplate: RestTemplate,
    @Value("\${app.accessmanager.url}") private val accessManagerUrl: String,
) : HealthIndicator {
    override fun health(): Health = dependencyHealth(restTemplate, "$accessManagerUrl/actuator/health")
}
