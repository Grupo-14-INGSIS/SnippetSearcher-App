package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetRunRequest(
    val version: String,
    val environment: Map<String, String>,
    /** Inputs ya ingresados por el usuario (ejecución interactiva, ver Runner). */
    val inputs: List<String> = emptyList(),
)
