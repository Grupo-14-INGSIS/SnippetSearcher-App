package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetStatusUpdateRequest(
    val userId: String? = null,
    val task: String,
    val status: Boolean,
    val compliance: String? = null,
)
