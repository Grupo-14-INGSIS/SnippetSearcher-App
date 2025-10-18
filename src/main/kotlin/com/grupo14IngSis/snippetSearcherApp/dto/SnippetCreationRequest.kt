package com.grupo14IngSis.snippetSearcherApp.dto

// DTO que se enviará desde 'App' a 'Runner'
data class SnippetCreationRequest(
    val userId: String,
    val name: String,
    val description: String,
    val language: String,
    val code: String
)