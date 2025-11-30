package com.grupo14IngSis.snippetSearcherApp.dto

data class LintingError(
    val rule: String,
    val message: String,
    val line: Int,
    val column: Int,
    val severity: String = "ERROR",
    // ERROR, WARNING, INFO
)
