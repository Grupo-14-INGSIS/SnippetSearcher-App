package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetDetailResponse(
    val id: String,
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    val content: String,
    val testCases: List<TestCase>,
    val lintingErrors: List<LintingError>,
    val isValid: Boolean,
)
