package com.grupo14IngSis.snippetSearcherApp.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate
import java.time.Duration

@Configuration
class RestTemplateConfig {
    @Bean
    @Primary
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.requestFactory = HttpComponentsClientHttpRequestFactory()
        return restTemplate
    }

    /**
     * Para el chequeo de salud: con timeouts cortos, para que un Runner o AccessManager colgado se
     * informe como DOWN en vez de dejar colgado el propio /api/actuator/health.
     */
    @Bean("healthRestTemplate")
    fun healthRestTemplate(): RestTemplate {
        val factory = SimpleClientHttpRequestFactory()
        factory.setConnectTimeout(Duration.ofSeconds(2))
        factory.setReadTimeout(Duration.ofSeconds(3))
        return RestTemplate(factory)
    }
}
