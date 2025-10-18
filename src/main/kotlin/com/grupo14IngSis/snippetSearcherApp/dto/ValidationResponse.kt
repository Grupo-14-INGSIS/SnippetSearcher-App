package com.grupo14IngSis.snippetSearcherApp.dto

// DTO para manejar errores detallados de validación (usado por el Runner y atrapado por el App)
data class ValidationResponse(
    val message: String,
    val rule: String?,
    val line: Int?,
    val column: Int?
)