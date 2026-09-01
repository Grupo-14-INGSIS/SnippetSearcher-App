package com.grupo14IngSis.snippetSearcherApp.dto.runnerclient

data class RunTestRequest(
    val snippetId: String,
    val userId: String,
    val version: String,
    val environment: Map<String, String> = emptyMap(),
    val input: List<String> = emptyList(),
    val expected: List<String> = emptyList(),
)
