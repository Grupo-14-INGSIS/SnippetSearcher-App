package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetUpdateRequest(
    val task: String,
    val language: String,
    val rules: Map<String, Any>,
)
