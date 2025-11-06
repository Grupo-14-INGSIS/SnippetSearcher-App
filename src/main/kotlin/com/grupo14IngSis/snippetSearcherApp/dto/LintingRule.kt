package com.grupo14IngSis.snippetSearcherApp.dto

data class LintingRule(
    val name: String,
    val isActive: Boolean,
    val description: String? = null
)