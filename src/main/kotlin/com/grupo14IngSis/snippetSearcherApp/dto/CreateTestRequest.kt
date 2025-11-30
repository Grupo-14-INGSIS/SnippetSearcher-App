package com.grupo14IngSis.snippetSearcherApp.dto

data class CreateTestRequest(
    val snippetId: String,
    val input: List<String>,
    val expected: String,
)
