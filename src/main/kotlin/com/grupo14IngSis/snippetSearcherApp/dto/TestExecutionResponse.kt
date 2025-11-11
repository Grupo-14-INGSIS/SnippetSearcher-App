package com.grupo14IngSis.snippetSearcherApp.dto

data class TestExecutionResponse(
    val results: List<TestResult>,
    val summary: TestSummary,
)

data class TestSummary(
    val total: Int,
    val passed: Int,
    val failed: Int,
    val pending: Int,
    val executionTime: Long, // tiempo total en milisegundos
)
