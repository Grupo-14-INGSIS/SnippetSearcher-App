package com.grupo14IngSis.snippetSearcherApp.dto

data class LintingError(
    val message: String,
    val line: Int,
    val column: Int,
)
