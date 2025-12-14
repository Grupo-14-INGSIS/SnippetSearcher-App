package com.grupo14IngSis.snippetSearcherApp.config

import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskDecorator
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor

/**
 * Decorador de tareas que propaga el contexto MDC del hilo de la petición
 * a los hilos que ejecutan las tareas asíncronas.
 */
class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        // Captura el contexto del hilo actual (el de la petición web)
        val contextMap = MDC.getCopyOfContextMap()
        return Runnable {
            try {
                // Establece el contexto capturado en el hilo de la tarea asíncrona
                MDC.setContextMap(contextMap)
                runnable.run()
            } finally {
                // Limpia el contexto del hilo de la tarea para evitar memory leaks
                MDC.clear()
            }
        }
    }
}

/**
 * Configuración para habilitar y personalizar la ejecución de tareas asíncronas (@Async).
 *
 * Esta configuración define un pool de hilos personalizado y le adjunta
 * un TaskDecorator para asegurar que el contexto de logging (MDC), incluyendo el requestId,
 * se propague a los hilos de las tareas asíncronas.
 */
@Configuration
@EnableAsync
class AsyncConfig {
    @Bean("asyncTaskExecutor")
    fun asyncTaskExecutor(): Executor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 5
        executor.maxPoolSize = 10
        executor.setQueueCapacity(25)
        executor.setThreadNamePrefix("Async-")
        // adjunto decorador de MDC
        executor.setTaskDecorator(MdcTaskDecorator())
        executor.initialize()
        return executor
    }
}
