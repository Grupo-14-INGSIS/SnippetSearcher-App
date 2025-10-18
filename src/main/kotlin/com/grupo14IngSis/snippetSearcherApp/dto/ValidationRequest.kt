package com.grupo14IngSis.snippetSearcherApp.dto

data class ValidationRequest(
    val content: String,
    val language: String,
    val version: String
)
