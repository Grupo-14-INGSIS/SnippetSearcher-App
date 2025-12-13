package com.grupo14IngSis.snippetSearcherApp.dto.runnerclient

data class RunTestRequest(
    val snippetId: String,
    val testId: String,
    val version: String,
    val environment: Map<String, String>,
    val input: List<String>,
    val expected: List<String>,
)
