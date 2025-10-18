package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetUpdateRequest(
    val name: String?,
    val description: String?,
    val language: String?,
    val version: String?,
    val content: String?
)