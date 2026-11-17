package com.grupo14IngSis.snippetSearcherApp.dto

data class CreateTestRequest(
    val input: List<String>,
    val expected: List<String>,
    val version: String,
    val environment: Map<String, String>,
    // Nombre que el usuario le pone al test en la UI. Opcional: sin nombre, la UI muestra "Test #N"
    val name: String? = null,
)
