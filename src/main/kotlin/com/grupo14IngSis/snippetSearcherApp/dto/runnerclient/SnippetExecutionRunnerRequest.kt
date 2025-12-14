package com.grupo14IngSis.snippetSearcherApp.dto.runnerclient

data class SnippetExecutionRunnerRequest(
    val userId: String,
    val version: String,
    val environment: Map<String, String>,
)
