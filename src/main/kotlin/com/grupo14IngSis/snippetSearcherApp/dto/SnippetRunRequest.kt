package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetRunRequest(
    val version: String,
    val environment: Map<String, String>,
)
