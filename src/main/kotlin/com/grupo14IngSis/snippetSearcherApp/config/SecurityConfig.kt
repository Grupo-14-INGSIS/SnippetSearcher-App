package com.grupo14IngSis.snippetSearcherApp.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Habilita @PreAuthorize
class SecurityConfig(
    // Orígenes que pueden llamar a la API desde el navegador (lista separada por comas). En las VMs la UI y
    // la API comparten dominio y CORS no interviene; hace falta para la UI local (localhost:5173 -> 19081).
    @Value("\${app.cors.allowed-origins:$DEFAULT_ALLOWED_ORIGINS}") private val allowedOrigins: List<String>,
) {
    companion object {
        const val DEFAULT_ALLOWED_ORIGINS =
            "https://snippet26dev.duckdns.org,https://snippet26prod.duckdns.org,http://localhost:5173,http://127.0.0.1:5173"
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .authorizeHttpRequests { authorize ->
                // El resultado del linteo/formateo ya no entra por HTTP: llega por Redis Streams
                authorize
                    .requestMatchers(HttpMethod.PUT, "/api/v1/snippets/*")
                    .permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/snippets/*")
                    .permitAll()
                    .requestMatchers("/api/v1/**")
                    .authenticated()
                    .anyRequest()
                    .permitAll()
            }.oauth2ResourceServer { oauth2 ->
                oauth2.jwt {}
            }.csrf { csrf -> csrf.disable() }
        return http.build()
    }

    /**
     * Antes: allowedOriginPatterns("*") + allowCredentials(true), que hace que Spring devuelva como permitido
     * el Origin de cualquier sitio. Ahora solo los orígenes conocidos y sin credenciales: la autenticación es
     * con `Authorization: Bearer` (el navegador no lo manda solo), no con cookies.
     */
    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = allowedOrigins.map { it.trim().trimEnd('/') }.filter { it.isNotEmpty() }
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        // Los headers que manda la UI (api.ts / runnerService.ts)
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "Accept", "X-Request-Id")
        configuration.exposedHeaders = listOf("Content-Disposition") // nombre del archivo en la descarga (US #13)
        configuration.allowCredentials = false
        configuration.maxAge = 3600 // el navegador cachea el preflight una hora
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
