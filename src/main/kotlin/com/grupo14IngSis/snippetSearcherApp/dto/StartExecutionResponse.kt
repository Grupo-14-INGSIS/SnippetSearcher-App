package com.grupo14IngSis.snippetSearcherApp.dto

data class StartExecutionResponse(
    val status: ExecutionEventType,
    val message: List<String>,
)
