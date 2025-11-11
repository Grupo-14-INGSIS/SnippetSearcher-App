package com.grupo14IngSis.snippetSearcherApp.dto

data class LintingConfig(
    val userId: String,
    val rules: List<LintingRule>,
)
