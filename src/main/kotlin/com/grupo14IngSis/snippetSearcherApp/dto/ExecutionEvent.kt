package com.grupo14IngSis.snippetSearcherApp.dto

data class ExecutionEvent(
    val executionId: String,
    val status: ExecutionEventType,
    val message: String? = null,
)

enum class ExecutionEventType {
    COMPLETED,
    OUTPUT,
    WAITING,
    ERROR,
}
