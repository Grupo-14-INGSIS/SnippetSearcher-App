package com.grupo14IngSis.snippetSearcherApp.model

// El modelo del App NO debe tener anotaciones JPA
data class Snippet(
    // El App solo necesita campos que manejará o pasará a otros servicios
    val id: Long? = null,
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    val code: String
)