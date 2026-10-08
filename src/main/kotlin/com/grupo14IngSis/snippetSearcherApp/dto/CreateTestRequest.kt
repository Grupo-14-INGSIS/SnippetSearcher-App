package com.grupo14IngSis.snippetSearcherApp.dto

data class CreateTestRequest(
    val input: List<String>,
    val expected: List<String>,
    val version: String,
    val environment: Map<String, String>,
)
