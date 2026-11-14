package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetData(
    val snippetId: String,
    val name: String,
    val language: String,
    val compliance: String? = null,
    val status: String? = null,
    val description: String? = null,
    val version: String? = null,
    val content: String? = null,
    // "owner" o "shared": la UI deja editar solo al dueño
    val permission: String? = null,
)
