package com.grupo14IngSis.snippetSearcherApp.dto

data class TestResult(
    val testCaseId: String,
    val testCaseName: String,
    val status: TestStatus,
    val actualOutput: List<String>,
    val expectedOutput: List<String>,
    val errorMessage: String? = null,
    val executionTime: Long? = null, // en milisegundos
)
