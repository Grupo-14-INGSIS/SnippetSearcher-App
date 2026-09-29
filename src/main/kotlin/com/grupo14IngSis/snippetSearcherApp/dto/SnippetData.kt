package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetData(
    val snippetId: String,
    val name: String,
    val language: String,
    val compliance: String? = null,
    val status: String? = null,
    val description: String? = null,
    val version: String? = null,
)
