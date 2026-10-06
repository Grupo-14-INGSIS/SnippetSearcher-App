package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetCreationRequest(
    val userId: String,
    val name: String,
    val language: String,
    val description: String? = "",
    val version: String? = "1.1",
)
