package com.grupo14IngSis.snippetSearcherApp.model

import java.time.LocalDateTime

data class Test(
    val id: String,
    val snippetId: String,
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class TestResult(
    val testId: String,
    val passed: Boolean,
    val actualOutputs: List<String>,
    val expectedOutputs: List<String>,
    val error: String? = null
)