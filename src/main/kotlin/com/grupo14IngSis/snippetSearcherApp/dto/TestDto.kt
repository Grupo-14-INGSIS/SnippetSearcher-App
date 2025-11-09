package com.grupo14IngSis.snippetSearcherApp.dto

data class CreateTestRequest(
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
)

data class UpdateTestRequest(
    val name: String?,
    val inputs: List<String>?,
    val expectedOutputs: List<String>?,
)

data class TestResponse(
    val id: String,
    val snippetId: String,
    val name: String,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
    val createdAt: String,
    val updatedAt: String,
)

data class TestResultResponse(
    val testId: String,
    val passed: Boolean,
    val actualOutputs: List<String>,
    val expectedOutputs: List<String>,
    val error: String? = null,
    val isValid: Boolean, // true si passed, false si es "inválido"
)
