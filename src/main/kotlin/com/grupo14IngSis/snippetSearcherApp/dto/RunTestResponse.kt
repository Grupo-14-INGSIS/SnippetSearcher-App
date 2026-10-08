package com.grupo14IngSis.snippetSearcherApp.dto

data class RunTestResponse(
    val actual: List<String>,
    val result: TestResult,
    val message: String,
)

enum class TestResult {
    SUCCESS,
    FAILED,
    ERROR,
}
