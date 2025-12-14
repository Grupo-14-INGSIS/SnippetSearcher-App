package com.grupo14IngSis.snippetSearcherApp.dto

data class ExecutionEvent(
    val status: ExecutionEventType,
    val message: String? = null,
)

enum class ExecutionEventType {
    COMPLETED,
    OUTPUT,
    WAITING,
    ERROR,
}
