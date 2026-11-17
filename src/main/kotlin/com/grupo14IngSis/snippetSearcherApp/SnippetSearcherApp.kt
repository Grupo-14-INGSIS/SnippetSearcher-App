package com.grupo14IngSis.snippetSearcherApp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

// Scheduling: recuperación de mensajes pendientes del stream y reconciliador de linteos
@SpringBootApplication
@EnableScheduling
class SnippetSearcherApp

fun main(args: Array<String>) {
    runApplication<SnippetSearcherApp>(*args)
}
