package com.grupo14IngSis.snippetSearcherApp.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Profile("dev")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfigDev {
    @PostConstruct
    fun init() {
        println("🟢🟢🟢 SecurityConfigDev IS ACTIVE 🟢🟢🟢")
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        println("🔵 Building DEV SecurityFilterChain...")
        http
            .authorizeHttpRequests { authorize ->
                authorize.anyRequest().permitAll()
            }
            .csrf { csrf -> csrf.disable() }
            .oauth2ResourceServer { oauth2 -> oauth2.disable() }
        println("✅ DEV SecurityFilterChain built")
        return http.build()
    }
}
