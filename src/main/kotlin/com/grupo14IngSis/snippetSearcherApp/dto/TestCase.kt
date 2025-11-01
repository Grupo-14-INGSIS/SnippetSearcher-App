package com.grupo14IngSis.snippetSearcherApp.dto

data class TestCase(
    val id: String,
    val name: String,
    val input: List<String>,
    val expectedOutput: List<String>,
    val status: TestStatus = TestStatus.PENDING
)

enum class TestStatus {
    PENDING,
    PASSED,
    FAILED
}