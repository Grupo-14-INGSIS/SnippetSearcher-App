package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.dto.*
import com.grupo14IngSis.snippetSearcherApp.service.SnippetTaskProducer
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/snippets")
class TaskController (
    private val snippetTaskProducer: SnippetTaskProducer,
    private val accessManagerClient: AccessManagerClient,
) {
    private val allowedTasks = setOf("formatting", "linting")

    @PostMapping("/test?message={message}")
    fun sendMessage(
        @PathVariable("message") message: String,
    ) {
        snippetTaskProducer.requestTask(message, "test")
        println("Sent $message")
    }

    // ===== START TASK =====
    @PostMapping("/{task}")
    fun startTask(
        @RequestBody taskRequest: TaskRequest,
        @PathVariable task: String
    ): ResponseEntity<String> {
        // Validate task
        if (task !in allowedTasks) {
            return ResponseEntity
                .badRequest()
                .body("Invalid task '$task'. Valid tasks: $allowedTasks")
        }
        // Get all snippets owned by user through accessmanager
        val userId = taskRequest.userId
        val snippets = accessManagerClient.getPermissionsForUser(userId).owned
        if (snippets.isEmpty()) return ResponseEntity.noContent().build()
        // Send all to runner through redis
        snippets.forEach { snippet ->
            snippetTaskProducer.requestTask(snippet, task)
        }
        return ResponseEntity.accepted().body("$task tasks queued: ${snippets.size}")
    }
}