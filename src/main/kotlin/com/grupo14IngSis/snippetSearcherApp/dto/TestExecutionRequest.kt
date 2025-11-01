package com.grupo14IngSis.snippetSearcherApp.dto

data class TestExecutionRequest(
    val testCaseIds: List<String>? = null // null = ejecutar todos los tests
)