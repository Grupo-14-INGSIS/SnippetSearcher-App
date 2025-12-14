package com.grupo14IngSis.snippetSearcherApp.dto

data class ErrorResponse(
    val status: Int,
    val message: String,
    val details: String,
)
