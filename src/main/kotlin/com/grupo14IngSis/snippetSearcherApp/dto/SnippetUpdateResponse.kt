package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetUpdateResponse(
    val id: String,
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    val content: String,
    val isValid: Boolean,
    val validationErrors: List<String>? = null,
)
